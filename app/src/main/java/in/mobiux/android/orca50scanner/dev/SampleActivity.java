package in.mobiux.android.orca50scanner.dev;

import in.mobiux.android.orca50scanner.R;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;

import in.mobiux.android.orca50scanner.activity.BaseActivity;
import in.mobiux.android.orca50scanner.api.ApiClient;
import in.mobiux.android.orca50scanner.util.AppUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SampleActivity extends BaseActivity {

    private Button btnSubmit, btnShare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);

        btnSubmit = findViewById(R.id.btnSubmit);
        btnShare = findViewById(R.id.btnShare);

        checkPermission(SampleActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);

        logger.i(TAG, "Sample Activity Created...!");

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File logFile = logger.getLogFile(app);
                if (logFile != null) {
                    sendLogsToServer(logger.getLogFile(app));
                } else {
                    showToast("logs not found");
                }
            }
        });

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logger.createAndExportLogs(SampleActivity.this);
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
                Log.i(TAG, "" + t.getLocalizedMessage());
                showToast("something went wrong");
            }
        });
    }
}