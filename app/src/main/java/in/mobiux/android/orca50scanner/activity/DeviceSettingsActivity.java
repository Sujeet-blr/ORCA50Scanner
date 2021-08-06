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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
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
import in.mobiux.android.orca50scanner.util.AppConfig;
import in.mobiux.android.orca50scanner.util.AppUtils;
import in.mobiux.android.orca50scanner.util.LanguageUtils;
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
    private Spinner spnrLanguage;
    private List<LanguageUtils.Language> languages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_settings);

        setTitle(getResources().getString(R.string.label_device_settings));
        cardRSSI = findViewById(R.id.cardRSSI);
        cardBuzzer = findViewById(R.id.cardBuzzer);
        cardLogs = findViewById(R.id.cardLogs);
        cardLogout = findViewById(R.id.cardLogout);
        spnrLanguage = findViewById(R.id.spnrLanguage);
        tvAppVersion = findViewById(R.id.tvAppVersion);

        cardBuzzer.setVisibility(View.GONE);

        if (AppConfig.MULTI_LANGUAGE) {
            spnrLanguage.setVisibility(View.VISIBLE);
        } else {
            spnrLanguage.setVisibility(View.GONE);
        }

        languages.addAll(Arrays.asList(LanguageUtils.Language.values()));
        ArrayAdapter<LanguageUtils.Language> arrayAdapter = new ArrayAdapter<LanguageUtils.Language>(DeviceSettingsActivity.this, android.R.layout.simple_spinner_item, languages);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnrLanguage.setAdapter(arrayAdapter);

        LanguageUtils.Language selectedLanguage = session.getLanguage();

        int position = languages.indexOf(selectedLanguage);
        spnrLanguage.setSelection(position);

        spnrLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                LanguageUtils.Language language = (LanguageUtils.Language) spnrLanguage.getSelectedItem();

                logger.i(TAG, "selected language is " + language);

                if (!session.getLanguage().equals(language)) {
                    languageUtils.switchLanguage(DeviceSettingsActivity.this, language);
                    recreate();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

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
                progressDialog = new ProgressDialog(DeviceSettingsActivity.this);
                progressDialog.setMessage(getResources().getString(R.string.syncing_with_server));
                progressDialog.setIndeterminate(true);
                progressDialog.show();

                requestCode = 401;

                doSync();
            }
        });
    }

    private void doSync() {

        serverClient = ServerClient.getInstance(getApplicationContext());
        serverClient.setOnSyncListener(DeviceSettingsActivity.this, new DataSyncListener() {
            @Override
            public void onSyncSuccess() {
                progressDialog.dismiss();
                logger.i(TAG, "sync success");
                showToast(getResources().getString(R.string.sync_success));

                if (requestCode == 401) {
//                    app.clearStackOnSignOut();
                    finish();
//                    processLogs();
                    session.logout();

                    Intent intent = new Intent(app, LoginActivity.class);
                    startActivity(intent);

                    requestCode = 0;
                }
            }

            @Override
            public void onSyncFailed() {
                progressDialog.dismiss();
                logger.i(TAG, "sync failed");
                showToast(getResources().getString(R.string.sync_failed));
            }
        });

        serverClient.sync(DeviceSettingsActivity.this);
    }
}