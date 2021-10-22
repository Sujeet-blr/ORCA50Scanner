package in.mobiux.android.orca50scanner.rfidtagtester.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.zebra.sdl.BarcodeReaderBaseActivity;

import in.mobiux.android.orca50scanner.reader.activity.SettingsActivity;
import in.mobiux.android.orca50scanner.rfidtagtester.R;

public class BarcodeScannerActivity extends BarcodeReaderBaseActivity {

    private TextView tvStatus, tvMessage, tvCount;
    private ImageView ivIndicator, ivSettings;
    private Button btnRFID, btnReset;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scanner);

        tvStatus = findViewById(R.id.tvStatus);
        tvMessage = findViewById(R.id.tvMessage);
        tvCount = findViewById(R.id.tvCount);
        ivIndicator = findViewById(R.id.ivIndicator);
        ivSettings = findViewById(R.id.ivSettings);
        btnRFID = findViewById(R.id.btnRFID);
        btnReset = findViewById(R.id.btnReset);
        ivIndicator.setVisibility(View.GONE);
        tvStatus.setText("");

        ivSettings.setOnClickListener(view -> {
            SettingsActivity.launchActivity(getApplicationContext());
        });

        btnRFID.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), RFIDScannerActivity.class);
            startActivity(intent);
            finish();
        });

        btnReset.setOnClickListener(view -> {
            barcodes.clear();
            tvCount.setText("Count : " + barcodes.size());
        });
    }

    @Override
    public void onBarcodeScan(String barcode) {
        super.onBarcodeScan(barcode);

        ivIndicator.setVisibility(View.VISIBLE);
        tvMessage.setText("" + barcode);
    }

    @Override
    public void onScanStatus(boolean status) {
        super.onScanStatus(status);

        if (status) {
            tvStatus.setText("Scanning");
        } else {
            tvStatus.setText("");
        }
    }

    @Override
    public void onConnection(boolean status) {
        super.onConnection(status);

        if (!status)
            tvStatus.setText("Not Connected");
    }

    @Override
    public void message(int type, String msg) {
        super.message(type, msg);

        tvMessage.setText(msg);
    }
}