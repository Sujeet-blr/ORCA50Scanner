package in.mobiux.android.orca50scanner.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import in.mobiux.android.orca50scanner.R;
import in.mobiux.android.orca50scanner.api.ApiClient;
import in.mobiux.android.orca50scanner.api.Presenter;
import in.mobiux.android.orca50scanner.api.model.AssetHistory;
import in.mobiux.android.orca50scanner.api.model.Inventory;
import in.mobiux.android.orca50scanner.api.model.Laboratory;
import in.mobiux.android.orca50scanner.core.DataSyncListener;
import in.mobiux.android.orca50scanner.core.ServerClient;
import in.mobiux.android.orca50scanner.util.AppUtils;
import in.mobiux.android.orca50scanner.viewmodel.InventoryViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DataSyncSettingActivity extends BaseActivity {

    private CardView cardSync, cardSettings, cardLicense, cardAbout;

    private List<Inventory> inventories = new ArrayList<>();
    private List<AssetHistory> histories = new ArrayList<>();

    private InventoryViewModel viewModel;
    private ServerClient serverClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_sync_setting);


        setTitle(getResources().getString(R.string.label_data_sync));

        cardSync = findViewById(R.id.cardSync);
        cardSettings = findViewById(R.id.cardSettings);
        cardLicense = findViewById(R.id.cardLicense);
        cardAbout = findViewById(R.id.cardAbout);

        viewModel = new ViewModelProvider(this).get(InventoryViewModel.class);

        viewModel.getAllInventory().observe(this, new Observer<List<Inventory>>() {
            @Override
            public void onChanged(List<Inventory> list) {
                inventories = list;
            }
        });

        viewModel.getHistories().observe(this, new Observer<List<AssetHistory>>() {
            @Override
            public void onChanged(List<AssetHistory> assetHistories) {
                logger.i(TAG, "history size " + assetHistories.size());


                histories.clear();
                histories.addAll(assetHistories);
            }
        });

        cardSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                sync(inventories);

                logger.i(TAG, "Syncing with Server");
                progressDialog = new ProgressDialog(DataSyncSettingActivity.this);
                progressDialog.setMessage(getResources().getString(R.string.syncing_with_server));
                progressDialog.setIndeterminate(true);
                progressDialog.show();

                doSync();
            }
        });

        cardSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(app, DeviceSettingsActivity.class);
                startActivity(intent);
            }
        });

        cardLicense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(app, LicenseActivity.class);
                startActivity(intent);
            }
        });

        cardAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(app, AboutUsActivity.class);
                startActivity(intent);
            }
        });
    }


    private void doSync() {

        serverClient = ServerClient.getInstance(getApplicationContext());
        serverClient.setOnSyncListener(DataSyncSettingActivity.this, new DataSyncListener() {
            @Override
            public void onSyncSuccess() {
                showToast(getResources().getString(R.string.sync_success));
                progressDialog.dismiss();
            }

            @Override
            public void onSyncFailed() {
                showToast(getResources().getString(R.string.sync_failed));
                progressDialog.dismiss();
            }
        });

        serverClient.sync(DataSyncSettingActivity.this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!session.hasCredentials()) {
            finish();
        }
    }
}