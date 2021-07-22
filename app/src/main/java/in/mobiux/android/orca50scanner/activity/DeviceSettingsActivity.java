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
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import in.mobiux.android.orca50scanner.BuildConfig;
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

public class DeviceSettingsActivity extends BaseActivity {

    private CardView cardRSSI, cardBuzzer, cardLogs, cardLogout;
    private TextView tvAppVersion;

    private List<Laboratory> laboratories = new ArrayList<>();
    private List<Inventory> inventoryList = new ArrayList<>();
    private List<Inventory> inventories = new ArrayList<>();
    private List<AssetHistory> histories = new ArrayList<>();

    private InventoryViewModel viewModel;
    private ServerClient serverClient;
    private int requestCode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_settings);

        setTitle("Device Settings");
        cardRSSI = findViewById(R.id.cardRSSI);
        cardBuzzer = findViewById(R.id.cardBuzzer);
        cardLogs = findViewById(R.id.cardLogs);
        cardLogout = findViewById(R.id.cardLogout);
        tvAppVersion = findViewById(R.id.tvAppVersion);

        cardBuzzer.setVisibility(View.GONE);

        tvAppVersion.setText("version " + BuildConfig.VERSION_NAME);

        serverClient = ServerClient.getInstance(getApplicationContext());
        serverClient.setOnSyncListener(DeviceSettingsActivity.this, new DataSyncListener() {
            @Override
            public void onSyncSuccess() {
                logger.i(TAG, "sync success");
                showToast("Sync Success");
                progressDialog.dismiss();

                if (requestCode == 401) {
//                    app.clearStackOnSignOut();
                    finish();
                    processLogs();
                    session.logout();

                    Intent intent = new Intent(app, LoginActivity.class);
                    startActivity(intent);

                    requestCode = 0;
                }
            }

            @Override
            public void onSyncFailed() {
                logger.i(TAG, "sync failed");
                showToast("Sync Failed");
                progressDialog.dismiss();
            }
        });


        progressDialog = new ProgressDialog(DeviceSettingsActivity.this);
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

        tvAppVersion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showToast("Version Code " + BuildConfig.VERSION_CODE);
            }
        });

        cardRSSI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(app, RFOutputPowerSettingActivity.class);
                startActivity(intent);
            }
        });

        cardBuzzer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(app, BuzzerSettingActivity.class);
                startActivity(intent);
            }
        });

        cardLogs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(DeviceSettingsActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//                    logger.createAndExportLogs(DeviceSettingsActivity.this);

                    Intent intent = new Intent(app, SystemLogsManagementActivity.class);
                    startActivity(intent);

                } else {
                    checkPermission(DeviceSettingsActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);
                }
            }
        });

        cardLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                sync(inventories);

                logger.i(TAG, "Syncing with Server");
                progressDialog.setMessage("Syncing with Server");
                progressDialog.setIndeterminate(true);
                progressDialog.show();

                requestCode = 401;

                serverClient.sync(DeviceSettingsActivity.this);

            }
        });

        Presenter.INSTANCE.setOnServerSyncListener(new Presenter.OnServerSyncListener() {
            @Override
            public void onSync(boolean status, List<Inventory> list) {
                if (status) {
                    progressDialog.dismiss();
                    app.clearAllActivity();
                    finish();
                    processLogs();
                    session.logout();

                    Intent intent = new Intent(app, LoginActivity.class);
                    startActivity(intent);
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
                progressDialog.dismiss();
            }
        });
    }

    private void sendLogsToServer(File logFile) {

        ApiClient.getApiService().uploadLogs(session.token(), AppUtils.convertFileToRequestBody(logFile)).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    logger.clearLogs();
                    logger.i(TAG, "logs uploaded succes & clear");
                } else {
                    logger.i(TAG, "logs upload failed");
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                logger.i(TAG, "Something went wrong on logs upload " + t.getLocalizedMessage());
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
                logger.e(TAG, "logs not found");
            }
        } else if (synSetting.equals("1")) {
//            clear from device only
            logger.clearLogs();
        }
    }
}