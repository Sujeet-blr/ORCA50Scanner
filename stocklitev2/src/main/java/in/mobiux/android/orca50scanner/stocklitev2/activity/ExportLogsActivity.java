package in.mobiux.android.orca50scanner.stocklitev2.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import in.mobiux.android.commonlibs.activity.AppActivity;

import in.mobiux.android.orca50scanner.stocklitev2.R;
import in.mobiux.android.orca50scanner.stocklitev2.utils.AppLogger;
import in.mobiux.android.orca50scanner.stocklitev2.utils.Constraints;

public class ExportLogsActivity extends BaseActivity {

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

                checkPermission(ExportLogsActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, Constraints.STORAGE_PERMISSION_CODE);
                logger.createAndExportLogs(ExportLogsActivity.this);
            }
        });
    }


}