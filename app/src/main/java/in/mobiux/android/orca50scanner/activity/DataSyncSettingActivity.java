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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import in.mobiux.android.orca50scanner.R;
import in.mobiux.android.orca50scanner.api.ApiClient;
import in.mobiux.android.orca50scanner.api.Presenter;
import in.mobiux.android.orca50scanner.api.model.Inventory;
import in.mobiux.android.orca50scanner.api.model.Laboratory;
import in.mobiux.android.orca50scanner.viewmodel.InventoryViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DataSyncSettingActivity extends BaseActivity {

    private CardView cardSync, cardSettings, cardLicense, cardAbout;
//    private ProgressDialog progressDialog;

    private List<Laboratory> laboratories = new ArrayList<>();
    private List<Inventory> inventoryList = new ArrayList<>();
    private List<Inventory> inventories = new ArrayList<>();

    private InventoryViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_sync_setting);


        setTitle(getResources().getString(R.string.label_data_sync));

        cardSync = findViewById(R.id.cardSync);
        cardSettings = findViewById(R.id.cardSettings);
        cardLicense = findViewById(R.id.cardLicense);
        cardAbout = findViewById(R.id.cardAbout);

        progressDialog = new ProgressDialog(DataSyncSettingActivity.this);
        viewModel = new ViewModelProvider(this).get(InventoryViewModel.class);

        viewModel.getAllInventory().observe(this, new Observer<List<Inventory>>() {
            @Override
            public void onChanged(List<Inventory> list) {
                inventories = list;
            }
        });

        cardSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sync(inventories);
            }
        });

        cardSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(app, "Not Implemented", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(app, DeviceSettingsActivity.class);
                startActivity(intent);
            }
        });

        cardLicense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(app, "Not Implemented", Toast.LENGTH_SHORT).show();
            }
        });

        cardAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(DataSyncSettingActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    logger.createAndExportLogs(DataSyncSettingActivity.this);
                } else {
                    checkPermission(DataSyncSettingActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);
                }
            }
        });

        Presenter.INSTANCE.setOnServerSyncListener(new Presenter.OnServerSyncListener() {
            @Override
            public void onSync(boolean status, List<Inventory> list) {
                if (status) {
                    progressDialog.dismiss();
                }
            }
        });
    }


    private void sync(List<Inventory> list) {

        progressDialog.setMessage("Syncing with Server");
        progressDialog.setIndeterminate(true);
        progressDialog.show();

        inventoryList = new ArrayList<>();
        Set<String> uniquesLabs = new HashSet<>();

        for (Inventory inventory : list) {
            if (inventory.isSyncRequired()) {
                inventoryList.add(inventory);
                uniquesLabs.add("" + inventory.getLabId());
            }
        }

        for (String labId : uniquesLabs) {
            Laboratory laboratory = new Laboratory();
            laboratories.add(laboratory);
            laboratory.setDepartment(Integer.parseInt(labId));

            for (Inventory inventory : inventoryList) {
                if (labId.equals("" + inventory.getLabId())) {
                    laboratory.getAssets().add(inventory.getEpc());
                }
            }
        }

        if (laboratories.size() > 0) {
            updateAsset(laboratories.get(0));
//            progressDialog.setMessage("Syncing with Server");
//            progressDialog.setIndeterminate(true);
//            progressDialog.show();
        } else {
            Presenter.INSTANCE.pullLatestData();
        }
    }


    private void updateAsset(Laboratory laboratory) {

        ApiClient.getApiService().updateAssets(session.rawToken(), laboratory).enqueue(new Callback<Laboratory>() {
            @Override
            public void onResponse(Call<Laboratory> call, Response<Laboratory> response) {
                if (response.isSuccessful()) {

                    laboratories.remove(laboratory);
                    if (laboratories.size() > 0) {
                        updateAsset(laboratories.get(0));
                    } else {
                        Presenter.INSTANCE.pullLatestData();
                        progressDialog.dismiss();
                    }
                }
            }

            @Override
            public void onFailure(Call<Laboratory> call, Throwable t) {

            }
        });
    }
}