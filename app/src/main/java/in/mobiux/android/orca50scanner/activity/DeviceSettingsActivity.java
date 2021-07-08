package in.mobiux.android.orca50scanner.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import in.mobiux.android.orca50scanner.BuildConfig;
import in.mobiux.android.orca50scanner.R;

public class DeviceSettingsActivity extends BaseActivity {

    private CardView cardRSSI, cardBuzzer, cardLogs;
    private TextView tvAppVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_settings);

        setTitle("Device Settings");
        cardRSSI = findViewById(R.id.cardRSSI);
        cardBuzzer = findViewById(R.id.cardBuzzer);
        cardLogs = findViewById(R.id.cardLogs);
        tvAppVersion = findViewById(R.id.tvAppVersion);

        cardBuzzer.setVisibility(View.GONE);

        tvAppVersion.setText("version " + BuildConfig.VERSION_NAME);
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
    }
}