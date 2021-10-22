package in.mobiux.android.orca50scanner.rfidtagtester.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import in.mobiux.android.orca50scanner.reader.activity.RFIDReaderBaseActivity;
import in.mobiux.android.orca50scanner.reader.activity.SettingsActivity;
import in.mobiux.android.orca50scanner.reader.model.Inventory;
import in.mobiux.android.orca50scanner.reader.model.OperationTag;
import in.mobiux.android.orca50scanner.rfidtagtester.R;

public class RFIDScannerActivity extends RFIDReaderBaseActivity {

    private TextView tvStatus, tvMessage, tvCount;
    private ImageView ivIndicator, ivSettings;
    private Button btnBarcode, btnReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rfidscanner);

        tvStatus = findViewById(R.id.tvStatus);
        tvMessage = findViewById(R.id.tvMessage);
        tvCount = findViewById(R.id.tvCount);
        ivIndicator = findViewById(R.id.ivIndicator);
        ivSettings = findViewById(R.id.ivSettings);
        btnBarcode = findViewById(R.id.btnBarcode);
        btnReset = findViewById(R.id.btnReset);
        ivIndicator.setVisibility(View.GONE);
        tvStatus.setText("");

        ivSettings.setOnClickListener(view -> {
            SettingsActivity.launchActivity(getApplicationContext());
        });

        btnBarcode.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), BarcodeScannerActivity.class);
            startActivity(intent);
            finish();
        });

        btnReset.setOnClickListener(view -> {
            tags.clear();
            tvCount.setText("Count : " + tags.size());
        });
    }

    @Override
    public void onScanningStatus(boolean status) {
        super.onScanningStatus(status);

        if (status) {
            tvStatus.setText("Scanning");
        } else {
            tvStatus.setText("");
        }
    }

    @Override
    public void onInventoryTag(Inventory inventory) {
        super.onInventoryTag(inventory);
        ivIndicator.setVisibility(View.VISIBLE);
        tvMessage.setText("" + inventory.getEpc());

    }

    @Override
    public void onOperationTag(OperationTag operationTag) {
        super.onOperationTag(operationTag);
    }

    @Override
    public void onInventoryTagEnd(Inventory.InventoryTagEnd tagEnd) {
        super.onInventoryTagEnd(tagEnd);
        ivIndicator.setVisibility(View.GONE);
        tvCount.setText("Count : " + tags.size());
    }

    @Override
    public void onConnection(boolean status) {
        super.onConnection(status);

        if (!status)
            tvStatus.setText("Not Connected");
    }
}