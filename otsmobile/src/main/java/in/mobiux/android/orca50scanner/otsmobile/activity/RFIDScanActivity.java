package in.mobiux.android.orca50scanner.otsmobile.activity;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.mobiux.android.orca50scanner.otsmobile.R;
import in.mobiux.android.orca50scanner.otsmobile.api.ApiClient;
import in.mobiux.android.orca50scanner.otsmobile.api.BaseModel;
import in.mobiux.android.orca50scanner.otsmobile.api.model.ProcessPoint;
import in.mobiux.android.orca50scanner.otsmobile.api.model.ScanItem;
import in.mobiux.android.orca50scanner.otsmobile.api.model.UserDetails;
import in.mobiux.android.orca50scanner.otsmobile.database.ScanItemRepo;
import in.mobiux.android.orca50scanner.otsmobile.utils.TokenManger;
import in.mobiux.android.orca50scanner.reader.activity.RFIDReaderBaseActivity;
import in.mobiux.android.orca50scanner.reader.model.Inventory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RFIDScanActivity extends RFIDReaderBaseActivity {

    private static final String TAG = "RFIDScanActivity";

    private Button btnRFID, btnManual, btnBarcode;
    private TextView tvMessage, tvUserName, tvUploadMessage;
    private ScanItemRepo scanItemRepo;
    private ProcessPoint processPoint;
    private UserDetails userDetails;
    private List<ScanItem> items = new ArrayList<>();
    private TokenManger tokenManger;
    private List<Inventory> validTags = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rfidscan);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Scan Entry");

        btnRFID = findViewById(R.id.btnRFID);
        btnManual = findViewById(R.id.btnManual);
        tvMessage = findViewById(R.id.tvMessage);
        tvUserName = findViewById(R.id.tvUserName);
        btnBarcode = findViewById(R.id.btnBarcode);
        tvUploadMessage = findViewById(R.id.tvUploadMessage);

        tvUploadMessage.setVisibility(View.GONE);
        tvMessage.setText("");
        tvMessage.setEnabled(false);

        userDetails = (UserDetails) getIntent().getSerializableExtra("user");
        processPoint = (ProcessPoint) getIntent().getSerializableExtra("processPoint");
        scanItemRepo = new ScanItemRepo(getApplicationContext());
        tokenManger = TokenManger.getInstance(getApplicationContext());

        tvUserName.setText(userDetails.getFirstName() + " " + userDetails.getLastName());

        btnBarcode.setOnClickListener(view -> {
            finish();
            Intent intent = new Intent(app, MainActivity.class);
            intent.putExtra("user", userDetails);
            intent.putExtra("processPoint", processPoint);
            startActivity(intent);
        });

        btnManual.setOnClickListener(view -> {
            tvMessage.setText("");
            tvUploadMessage.setVisibility(View.GONE);
            Intent intent = new Intent(this, NewEntryActivity.class);
            intent.putExtra("user", userDetails);
            intent.putExtra("processPoint", processPoint);
            startActivity(intent);
        });

        scanItemRepo.getAllScanItemList().observe(this, new Observer<List<ScanItem>>() {
            @Override
            public void onChanged(List<ScanItem> scanItems) {
                items = scanItems;
                Log.i(TAG, "onChanged: " + scanItems.size());
                for (ScanItem item : scanItems) {
                    Log.i(TAG, "item id " + item.getId() + "barcode: " + item.getOrderItemId() + " uploaded " + item.isUploaded());
                }
            }
        });
    }

    @Override
    public void onInventoryTag(Inventory inventory) {

        ScanItem item = new ScanItem();
        item.setProcessPointId(processPoint.getId());
        item.setScanType("rfid");
        item.setOrderItemId(inventory.getFormattedEPC());

        if (item.isValidItem()) {
            validTags.add(inventory);
            tvMessage.setEnabled(true);
            tvMessage.setText("Scan Success\n" + inventory.getFormattedEPC());
            scanItemRepo.insert(item);
            items.add(item);
        } else {
            tvMessage.setText("Not a valid item");
            tvUploadMessage.setVisibility(View.GONE);
        }
    }

    @Override
    public void onInventoryTagEnd(Inventory.InventoryTagEnd tagEnd) {
        Log.i(TAG, "onInventoryTagEnd: " + tagEnd.mTagCount);

        if (!validTags.isEmpty())
            sendScanItems(items);
    }

    private void sendScanItems(List<ScanItem> list) {

        if (list.isEmpty()) {
            return;
        }

        JsonObject payload = new JsonObject();
        JsonArray orderItems = new JsonArray();
        for (ScanItem item : list) {
            JsonObject object = new JsonObject();
            object.addProperty("orderItemId", item.getOrderItemId());
            object.addProperty("processPointId", item.getProcessPointId());
            object.addProperty("scanTime", item.getScanTime());
            orderItems.add(object);
        }

        payload.add("orderItems", orderItems);

        ApiClient.getApiService().uploadScanItem(tokenManger.getFullAuthorizationToken(), payload).enqueue(new Callback<BaseModel>() {
            @Override
            public void onResponse(Call<BaseModel> call, Response<BaseModel> response) {
                if (response.isSuccessful()) {
                    if (response.body().isSuccess()) {

                        for (ScanItem item : list) {
                            item.setUploaded(true);
                            scanItemRepo.delete(item);
                        }

                        tvUploadMessage.setVisibility(View.VISIBLE);

                    } else {
                        tvUploadMessage.setVisibility(View.GONE);
                        Toast.makeText(RFIDScanActivity.this, "" + response.body().getBody().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    Toast.makeText(RFIDScanActivity.this, "", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "onResponse: " + response.toString());
                    tvUploadMessage.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<BaseModel> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t.getLocalizedMessage());
                tvUploadMessage.setVisibility(View.GONE);
            }
        });

    }
}