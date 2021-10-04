package in.mobiux.android.orca50scanner.common.activity;

import android.Manifest;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import in.mobiux.android.orca50scanner.common.R;
import in.mobiux.android.orca50scanner.common.utils.AppLogger;

public class ExportLogsActivity extends AppActivity {

    private Button btnExportLogs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_logs);

        AppLogger logger = AppLogger.getInstance(getApplicationContext());

        btnExportLogs = findViewById(R.id.btnExportLogs);

        btnExportLogs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                checkPermission(ExportLogsActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);
                logger.createAndExportLogs(ExportLogsActivity.this);
            }
        });
    }
}