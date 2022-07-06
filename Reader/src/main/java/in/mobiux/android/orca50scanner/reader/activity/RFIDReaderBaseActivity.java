package in.mobiux.android.orca50scanner.reader.activity;

import android.os.Bundle;
import android.view.KeyEvent;

import androidx.annotation.Nullable;

import com.rfid.rxobserver.ReaderSetting;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import in.mobiux.android.orca50scanner.reader.core.RFIDReader;
import in.mobiux.android.orca50scanner.reader.core.RFIDReaderListener;
import in.mobiux.android.orca50scanner.reader.core.Reader;
import in.mobiux.android.orca50scanner.reader.model.Inventory;
import in.mobiux.android.orca50scanner.reader.model.OperationTag;

public class RFIDReaderBaseActivity extends BaseActivity implements RFIDReaderListener {

    private RFIDReader rfidReader;
    protected Map<String, Inventory> tags = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        logger.i(TAG, "onResume");

        rfidReader = new RFIDReader(getApplicationContext());
        rfidReader.connect(Reader.ReaderType.RFID);
        rfidReader.setOnRFIDReaderListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        logger.i(TAG, "onPause");

        rfidReader.releaseResources();
        rfidReader.unregisterListener(this);
    }


    @Override
    public void onScanningStatus(boolean status) {
//        logger.i(TAG, "Scanning Status " + status);
    }

    @Override
    public void onInventoryTag(Inventory inventory) {
        logger.i(TAG, "onInventoryTag " + inventory.getEpc());
        tags.put(inventory.getFormattedEPC(), inventory);
    }

    @Override
    public void onOperationTag(OperationTag operationTag) {
        logger.i(TAG, "onOperationTag " + operationTag.strEPC);
    }

    @Override
    public void onInventoryTagEnd(Inventory.InventoryTagEnd tagEnd) {
        logger.i(TAG, "Scan End " + tagEnd.mTagCount);
    }

    @Override
    public void onConnection(boolean status) {
        logger.i(TAG, "Connection Status " + status);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_F4) {
            startScan();
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_F4) {
            startScan();
        }
        return super.onKeyLongPress(keyCode, event);
    }

    public void startScan() {
        rfidReader.startScan();
    }


    private Timer timer = new Timer();
    private final long DELAY = 1000;

    public void setRFOutputPower(int rssi) {

        timer.cancel();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                rfidReader.setRFOutputPower(rssi);
            }
        }, DELAY);
    }
}
