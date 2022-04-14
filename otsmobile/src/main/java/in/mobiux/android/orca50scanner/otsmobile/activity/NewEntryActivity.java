package in.mobiux.android.orca50scanner.otsmobile.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.Observer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import in.mobiux.android.orca50scanner.otsmobile.R;
import in.mobiux.android.orca50scanner.otsmobile.api.ApiClient;
import in.mobiux.android.orca50scanner.otsmobile.api.BaseModel;
import in.mobiux.android.orca50scanner.otsmobile.api.model.ProcessPoint;
import in.mobiux.android.orca50scanner.otsmobile.api.model.ScanItem;
import in.mobiux.android.orca50scanner.otsmobile.api.model.UserDetails;
import in.mobiux.android.orca50scanner.otsmobile.database.ScanItemRepo;
import in.mobiux.android.orca50scanner.otsmobile.utils.TokenManger;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewEntryActivity extends BaseActivity {

    private static final String TAG = "NewEntryActivity";

    private EditText edtOrderNumber, edtField2, edtItemNumber;
    private Button btnSubmit;
    private TextView tvUserName;
    private UserDetails userDetails;
    private ProcessPoint processPoint;
    private List<ScanItem> list = new ArrayList<>();
    private ScanItemRepo scanItemRepo;
    private TokenManger tokenManger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_entry);

        getSupportActionBar().setTitle("Manual Entry");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userDetails = (UserDetails) getIntent().getSerializableExtra("user");
        processPoint = (ProcessPoint) getIntent().getSerializableExtra("processPoint");

        edtOrderNumber = findViewById(R.id.edtOrderNumber);
        edtField2 = findViewById(R.id.edtField2);
        edtItemNumber = findViewById(R.id.edtItemNumber);
        btnSubmit = findViewById(R.id.btnSubmit);
        tvUserName = findViewById(R.id.tvUserName);

        tvUserName.setText(userDetails.getFirstName() + " " + userDetails.getLastName());

        scanItemRepo = new ScanItemRepo(getApplicationContext());
        tokenManger = TokenManger.getInstance(getApplicationContext());

        scanItemRepo.getAllScanItemList().observe(this, new Observer<List<ScanItem>>() {
            @Override
            public void onChanged(List<ScanItem> items) {
                list = items;
                Log.i(TAG, "onChanged: " + list.size());
            }
        });


        btnSubmit.setOnClickListener(view -> {

            String itemNumber = edtItemNumber.getText().toString();
            ScanItem item = new ScanItem();
            item.setProcessPointId(processPoint.getId());
            item.setScanType("manual");
            item.setOrderItemId(itemNumber);

            if (itemNumber.length() > 0 && item.isValidItem()) {

                scanItemRepo.insert(item);
                list.add(item);
                sendScanItems(list);


            } else {
                edtItemNumber.setError("Enter valid Item Number");
            }
        });
    }

    private void sendScanItems(List<ScanItem> list) {

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
                        Toast.makeText(NewEntryActivity.this, "Sent Successfully", Toast.LENGTH_SHORT).show();

                        for (ScanItem item : list) {
                            item.setUploaded(true);
                            scanItemRepo.delete(item);
                        }

                        finish();
                    } else {
                        Toast.makeText(NewEntryActivity.this, "" + response.body().getBody().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    Toast.makeText(NewEntryActivity.this, "", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "onResponse: " + response.toString());
                }

            }

            @Override
            public void onFailure(Call<BaseModel> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });

    }
}