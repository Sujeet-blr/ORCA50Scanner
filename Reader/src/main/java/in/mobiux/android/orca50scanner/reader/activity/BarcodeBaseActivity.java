package in.mobiux.android.orca50scanner.reader.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.mobiux.android.orca50scanner.reader.core.RFIDReader;
import in.mobiux.android.orca50scanner.reader.core.RFIDReaderListener;
import in.mobiux.android.orca50scanner.reader.core.Reader;
import in.mobiux.android.orca50scanner.reader.core.barcode.OrcaAirBarcodeReader;
import in.mobiux.android.orca50scanner.reader.core.barcode.base.ReaderBase;
import in.mobiux.android.orca50scanner.reader.core.barcode.helper.ReaderHelper;
import in.mobiux.android.orca50scanner.reader.core.barcode.helper.TDCodeTagBuffer;
import in.mobiux.android.orca50scanner.reader.core.barcode.tools.Beeper;
import in.mobiux.android.orca50scanner.reader.core.barcode.tools.CalculateSpeed;
import in.mobiux.android.orca50scanner.reader.core.barcode.tools.PreferenceUtil;
import in.mobiux.android.orca50scanner.reader.model.Barcode;
import in.mobiux.android.orca50scanner.reader.model.Inventory;
import in.mobiux.android.orca50scanner.reader.model.OperationTag;

public class BarcodeBaseActivity extends BaseActivity {

    private static final String TAG = "BarcodeBaseActivity";
    private LocalBroadcastManager lbm;

    private OrcaAirBarcodeReader orcaAirBarcodeReader;
    private ReaderHelper mReaderHelper;
    private ReaderBase mReader;


//    ======== This implementation will work with "Orca50 Air" only, will not work with "Orca50" =======

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            ReaderHelper.setContext(getApplicationContext());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        PreferenceUtil.init(getApplicationContext());
        Beeper.init(this);

        orcaAirBarcodeReader = new OrcaAirBarcodeReader(getApplicationContext());

        initializeReceiver();
    }

    void initializeReceiver() {
        lbm = LocalBroadcastManager.getInstance(getApplicationContext());
        IntentFilter itent = new IntentFilter();
        itent.addAction(ReaderHelper.BROADCAST_REFRESH_BAR_CODE);
        lbm.registerReceiver(mRecv, itent);
    }

    private final BroadcastReceiver mRecv = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Beeper.beeper();

            String barcode = "008800";
//            barcode = String.valueOf(mTagBuffer.getmRawData());
            List<TDCodeTagBuffer.BinDCodeTagMap> mVals = orcaAirBarcodeReader.mTagBuffer.getIsTagList();
            barcode = OrcaAirBarcodeReader.mTagBuffer.getmRawData().toString();
            if (mVals != null) {
                barcode = mVals.get(0).mBarCodeValue;
            }

            onBarcodeScan(barcode);
        }
    };

    void clearBarcodeBuffer() {
        lbm.sendBroadcast(new Intent(ReaderHelper.BROADCAST_REFRESH_BAR_CODE));
    }

    @Override
    protected void onStart() {
        super.onStart();
        orcaAirBarcodeReader.connect(Reader.ReaderType.BARCODE);
        initializeReceiver();

        try {
            mReaderHelper = ReaderHelper.getDefaultHelper();
            mReader = mReaderHelper.getReader();
        } catch (Exception e) {
            return;
        }
    }

    @Override
    protected void onResume() {
        if (mReader != null) {
            if (!mReader.IsAlive())
                mReader.StartWait();
        }
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();

        orcaAirBarcodeReader.releaseResources();

        if (lbm != null)
            lbm.unregisterReceiver(mRecv);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_F4) {
            CalculateSpeed.mStartTime = System.currentTimeMillis();

            Log.i(TAG, "onKeyDown: " + orcaAirBarcodeReader.isConnected());
            Log.i(TAG, "onKeyDown: " + orcaAirBarcodeReader.mTagBuffer.getIsTagList().size());

            showToast("Barcode buffer size " + orcaAirBarcodeReader.mTagBuffer.getIsTagList().size());

            if (OrcaAirBarcodeReader.mTagBuffer.getIsTagList().size() > 0) {
                String b = OrcaAirBarcodeReader.mTagBuffer.getIsTagList().get(0).mBarCodeValue;
                showToast("Barcode is " + OrcaAirBarcodeReader.mTagBuffer.getIsTagList().get(0));
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    public void onBarcodeScan(String barcode) {
        Log.i(TAG, "onBarcodeScan: " + barcode);
//        orcaAirBarcodeReader.clearBuffer();
    }
}
