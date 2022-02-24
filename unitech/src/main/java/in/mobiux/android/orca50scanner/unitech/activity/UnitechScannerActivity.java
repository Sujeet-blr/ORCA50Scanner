package in.mobiux.android.orca50scanner.unitech.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.device.ScanManager;
import android.device.scanner.configuration.PropertyID;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import in.mobiux.android.orca50scanner.unitech.R;

public class UnitechScannerActivity extends AppCompatActivity {

    private static final String TAG = "UnitechScannerActivity";

    private Button btnExecute;
    private ScanManager mScanManager;

    private final static String SCAN_ACTION = ScanManager.ACTION_DECODE;//default action

    private BroadcastReceiver mScanReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
//            soundpool.play(soundid, 1, 1, 0, 0, 1);
//            mVibrator.vibrate(100);

            byte[] barcode = intent.getByteArrayExtra(ScanManager.DECODE_DATA_TAG);
            int barcodelen = intent.getIntExtra(ScanManager.BARCODE_LENGTH_TAG, 0);
//            byte temp = intent.getByteExtra(ScanManager.BARCODE_TYPE_TAG, (byte) 0);
            String barcodeStr = new String(barcode, 0, barcodelen);
            Log.i(TAG, barcodeStr+"\n");
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unitech_scanner);

        btnExecute = findViewById(R.id.btnExecute);
        initScan();


        btnExecute.setOnClickListener(view->{
            mScanManager.startDecode();
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        int[] idbuf = new int[]{PropertyID.WEDGE_INTENT_ACTION_NAME, PropertyID.WEDGE_INTENT_DATA_STRING_TAG};
        String[] value_buf = mScanManager.getParameterString(idbuf);
        if(value_buf != null && value_buf[0] != null && !value_buf[0].equals("")) {
            filter.addAction(value_buf[0]);
        } else {
            filter.addAction(SCAN_ACTION);
        }

        registerReceiver(mScanReceiver,filter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(mScanReceiver);
    }

    private void initScan() {
        mScanManager = new ScanManager();
        mScanManager.openScanner();

        mScanManager.switchOutputMode( 0);
//        soundpool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 100); // MODE_RINGTONE
//        soundid = soundpool.load("/etc/Scan_new.ogg", 1);
    }
}