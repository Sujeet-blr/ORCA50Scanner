package in.mobiux.android.orca50scanner.sorfidtobarcode.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import in.mobiux.android.orca50scanner.reader.core.RFIDReader;
import in.mobiux.android.orca50scanner.reader.core.RFIDReaderListener;
import in.mobiux.android.orca50scanner.reader.core.Reader;
import in.mobiux.android.orca50scanner.reader.model.Inventory;
import in.mobiux.android.orca50scanner.reader.model.OperationTag;
import in.mobiux.android.orca50scanner.sorfidtobarcode.R;

public class RenameRfidTagsActivity extends BaseActivity {


    private RFIDReader rfidReader;
    private RFIDReaderListener rfidReaderListener;
    private Inventory inventory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rename_rfid_tags);

        rfidReader = new RFIDReader(getApplicationContext());
        rfidReader.connect(Reader.ReaderType.RFID);

        registerRfidListener();
    }

    private void registerRfidListener() {

        rfidReaderListener = new RFIDReaderListener() {
            @Override
            public void onScanningStatus(boolean status) {

            }

            @Override
            public void onInventoryTag(Inventory inventory) {
                
            }

            @Override
            public void onOperationTag(OperationTag operationTag) {

            }

            @Override
            public void onInventoryTagEnd(Inventory.InventoryTagEnd tagEnd) {

            }

            @Override
            public void onConnection(boolean status) {

            }
        };

        rfidReader.setOnRFIDReaderListener(rfidReaderListener);
    }
}