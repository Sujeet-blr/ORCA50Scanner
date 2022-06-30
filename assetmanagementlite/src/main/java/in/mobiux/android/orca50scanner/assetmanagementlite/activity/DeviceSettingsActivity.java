package in.mobiux.android.orca50scanner.assetmanagementlite.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

import in.mobiux.android.orca50scanner.assetmanagementlite.BuildConfig;
import in.mobiux.android.orca50scanner.assetmanagementlite.R;
import in.mobiux.android.orca50scanner.assetmanagementlite.api.model.AssetHistory;
import in.mobiux.android.orca50scanner.assetmanagementlite.api.model.Inventory;
import in.mobiux.android.orca50scanner.assetmanagementlite.api.model.Laboratory;
import in.mobiux.android.orca50scanner.assetmanagementlite.util.AppConfig;
import in.mobiux.android.orca50scanner.assetmanagementlite.util.LanguageUtils;
import in.mobiux.android.orca50scanner.assetmanagementlite.util.SessionManager;
import in.mobiux.android.orca50scanner.assetmanagementlite.viewmodel.InventoryViewModel;

public class DeviceSettingsActivity extends BaseActivity {

    private CardView cardRSSI, cardBuzzer, cardLogs, cardLogout;
    private TextView tvAppVersion;

    private List<Laboratory> laboratories = new ArrayList<>();
    private List<Inventory> inventoryList = new ArrayList<>();
    private List<Inventory> inventories = new ArrayList<>();
    private List<AssetHistory> histories = new ArrayList<>();

    private InventoryViewModel viewModel;
    private int requestCode = 0;
    private Spinner spnrLanguage;
    private List<LanguageUtils.Language> languages = new ArrayList<>();
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_settings);

        setTitle(getResources().getString(R.string.settings));
        cardRSSI = findViewById(R.id.cardRSSI);
        cardBuzzer = findViewById(R.id.cardBuzzer);
        cardLogs = findViewById(R.id.cardLogs);
        cardLogout = findViewById(R.id.cardLogout);
        spnrLanguage = findViewById(R.id.spnrLanguage);
        tvAppVersion = findViewById(R.id.tvAppVersion);

        cardBuzzer.setVisibility(View.GONE);
        cardLogs.setVisibility(View.GONE);
        cardRSSI.setVisibility(View.GONE);
        session = SessionManager.getInstance(this);


        tvAppVersion.setText("version " + BuildConfig.VERSION_NAME);

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

//        cardRSSI.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(app, RFOutputPowerSettingActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        cardBuzzer.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(app, BuzzerSettingActivity.class);
//                startActivity(intent);
//            }
//        });

//        cardLogs.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (ContextCompat.checkSelfPermission(DeviceSettingsActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
////                    logger.createAndExportLogs(DeviceSettingsActivity.this);
//
//                    Intent intent = new Intent(app, SystemLogsManagementActivity.class);
//                    startActivity(intent);
//
//                } else {
//                    checkPermission(DeviceSettingsActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);
//                }
//            }
//        });

        cardLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                sync(inventories);

                session.logout();

                finish();
            }
        });
    }
}