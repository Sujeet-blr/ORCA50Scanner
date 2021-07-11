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
import in.mobiux.android.orca50scanner.util.AppUtils;
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
    private List<AssetHistory> histories = new ArrayList<>();

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

        logger.i(TAG, "Syncing with Server");
        progressDialog.setMessage("Syncing with Server");
        progressDialog.setIndeterminate(true);
        progressDialog.show();

        inventoryList = new ArrayList<>();
        HashMap<String, Laboratory> historyLabs = new HashMap<>();

        for (AssetHistory history : histories) {
            logger.i(TAG, "" + history.getEpc() + "    " + history.getDepartment() + "  " + history.getUpdateTimeIntervalInSeconds());

            String departmentId = String.valueOf(history.getDepartment());

            Laboratory laboratory = historyLabs.get(departmentId);
            if (laboratory == null) {
                laboratory = new Laboratory();
                laboratory.setDepartment(Integer.parseInt(departmentId));
                historyLabs.put(departmentId, laboratory);
            }

            history.setTime(history.getUpdateTimeIntervalInSeconds());
            laboratory.getAssets().add(history);
        }

        laboratories.addAll(historyLabs.values());
        for (Laboratory l : laboratories) {
            logger.i(TAG, "lab " + l.getDepartment() + " assets " + l.getAssets().size());
        }

        if (laboratories.size() > 0) {
            updateAsset(laboratories.get(0));
        } else {
            Presenter.INSTANCE.pullLatestData();
            processLogs();
        }

    }


    private void updateAsset(Laboratory laboratory) {

        for (AssetHistory history : laboratory.getAssets()) {
            logger.i(TAG, "payload " + history.getEpc() + " dept " + history.getTime());
        }

        ApiClient.getApiService().updateAssets(session.rawToken(), laboratory).enqueue(new Callback<Laboratory>() {
            @Override
            public void onResponse(Call<Laboratory> call, Response<Laboratory> response) {
                if (response.isSuccessful()) {
                    laboratories.remove(laboratory);
                    for (AssetHistory history : laboratory.getAssets()) {
                        viewModel.deleteHistory(history);
                    }

                    if (laboratories.size() > 0) {
                        updateAsset(laboratories.get(0));
                    } else {
                        viewModel.clearHistory();
                        Presenter.INSTANCE.pullLatestData();
                        progressDialog.dismiss();
                        processLogs();
                    }
                } else {
                    logger.e(TAG, "" + response.message());
                    progressDialog.dismiss();
                }

            }

            @Override
            public void onFailure(Call<Laboratory> call, Throwable t) {
                logger.e(TAG, "" + t.getLocalizedMessage());
            }
        });
    }

    private void sendLogsToServer(File logFile) {

        ApiClient.getApiService().uploadLogs(session.token(), AppUtils.convertFileToRequestBody(logFile)).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    logger.clearLogs();
                    showToast("Clear logs");
                } else {
                    showToast("logs upload failed");
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                showToast("something went wrong");
            }
        });
    }

    //    process the logs according to logs settingsA
    private void processLogs() {

        String synSetting = session.getValue(SystemLogsManagementActivity.KEY_RADIO);

        if (synSetting.isEmpty() || synSetting.equals("0")) {
//            send to server then clear device
            File logFile = logger.getLogFile(app);
            if (logFile != null) {
                sendLogsToServer(logger.getLogFile(app));
            } else {
                showToast("logs not found");
            }
        } else if (synSetting.equals("1")) {
//            clear from device only
            logger.clearLogs();
        }
    }
}