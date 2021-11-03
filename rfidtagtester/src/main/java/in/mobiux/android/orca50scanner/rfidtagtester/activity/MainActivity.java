package in.mobiux.android.orca50scanner.rfidtagtester.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import in.mobiux.android.orca50scanner.common.utils.pdf.PdfUtils;
import in.mobiux.android.orca50scanner.reader.activity.RFIDReaderBaseActivity;
import in.mobiux.android.orca50scanner.reader.activity.SettingsActivity;
import in.mobiux.android.orca50scanner.reader.core.RFIDReader;
import in.mobiux.android.orca50scanner.reader.core.RFIDReaderListener;
import in.mobiux.android.orca50scanner.reader.core.Reader;
import in.mobiux.android.orca50scanner.reader.model.Inventory;
import in.mobiux.android.orca50scanner.reader.model.OperationTag;
import in.mobiux.android.orca50scanner.rfidtagtester.R;

public class MainActivity extends RFIDReaderBaseActivity {

    private ImageView ivIndicator, ivSettings;
    private TextView tvCount, tvStatus;
    private Button btnCreate, btnReset;

    private HashMap<String, Inventory> tags = new HashMap<>();
    private long timestamp = System.currentTimeMillis();
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivIndicator = findViewById(R.id.ivIndicator);
        tvCount = findViewById(R.id.tvCount);
        tvStatus = findViewById(R.id.tvStatus);
        btnCreate = findViewById(R.id.btnCreate);
        btnReset = findViewById(R.id.btnReset);
        ivSettings = findViewById(R.id.ivSettings);

        startTimer();

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logger.i(TAG, "Create clicked");

                checkPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);
                exportToPdf();
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logger.i(TAG, "Reset Clicked");
                tags.clear();
                tvCount.setText("Count : " + tags.size());

//                checkPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);
//                logger.createAndExportLogs(MainActivity.this);
            }
        });

        ivSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SettingsActivity.launchActivity(getApplicationContext());
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        timer.cancel();
    }

    private void startTimer() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ivIndicator.setVisibility(View.GONE);
                        tvStatus.setText("");
                    }
                });
            }
        }, 0, 2000);
    }

    @Override
    public void onScanningStatus(boolean status) {
        super.onScanningStatus(status);

        logger.i(TAG, "Scanning Status " + status);
        if (status) {
            tvStatus.setText("Scanning");
        } else {
            tvStatus.setText("");
        }
    }

    @Override
    public void onInventoryTag(Inventory inventory) {
        super.onInventoryTag(inventory);
        tags.put(inventory.getFormattedEPC(), inventory);
        tvCount.setText("Count : " + tags.size());
        ivIndicator.setVisibility(View.VISIBLE);
        timestamp = System.currentTimeMillis();

    }

    @Override
    public void onOperationTag(OperationTag operationTag) {
        super.onOperationTag(operationTag);
    }

    @Override
    public void onInventoryTagEnd(Inventory.InventoryTagEnd tagEnd) {
        super.onInventoryTagEnd(tagEnd);
    }

    @Override
    public void onConnection(boolean status) {
        super.onConnection(status);

        logger.i(TAG, "Connection Status " + status);
        if (status) {
            showToast(R.string.connected);
        } else {
            showToast(R.string.connection_lost);
        }
    }

    private void exportToPdf() {
        checkPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);

        PdfUtils pdfUtils = new PdfUtils(MainActivity.this);

        String title = "Sensing Object Rfid Reader";

        String[] columns = {"Rfid", "Rssi"};
        PdfUtils.PdfTable table = new PdfUtils.PdfTable(columns);
        for (Inventory i : tags.values()) {
            table.addCell(i.getEpc());
            table.addCell(i.getRssi());
            if (i.isScanStatus()) {
                table.addCell("Scanned");
            } else {
                table.addCell("n/a");
            }
        }

        pdfUtils.createPdfFile(PdfUtils.getPdfPath(MainActivity.this), table, title);
    }
}