package in.mobiux.android.orca50scanner.sampleapp;


import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.TextView;

import in.mobiux.android.orca50scanner.reader.activity.RFIDReaderBaseActivity;
import in.mobiux.android.orca50scanner.reader.model.Inventory;

public class RFIDActivity extends RFIDReaderBaseActivity {

    private TextView tvMessage;
    private Button btnStart;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rfidactivity);

        tvMessage = findViewById(R.id.tvMessage);
        btnStart = findViewById(R.id.btnStart);

        btnStart.setOnClickListener(view -> {
            startScan();
        });
    }

    @Override
    public void onInventoryTag(Inventory inventory) {
        super.onInventoryTag(inventory);
        tvMessage.setText("" + inventory.getFormattedEPC());
    }

    @Override
    public void onInventoryTagEnd(Inventory.InventoryTagEnd tagEnd) {
        super.onInventoryTagEnd(tagEnd);
        logger.i(TAG, "inventory end " + tagEnd.mTotalRead + "\t" + tagEnd.mReadRate);
    }
}