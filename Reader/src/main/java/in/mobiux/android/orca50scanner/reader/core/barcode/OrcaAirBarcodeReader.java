package in.mobiux.android.orca50scanner.reader.core.barcode;

import android.content.Context;
import android.util.Log;

import com.nativec.tools.ModuleManager;
import com.nativec.tools.SerialPort;
import com.nativec.tools.SerialPortFinder;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import in.mobiux.android.orca50scanner.reader.R;
import in.mobiux.android.orca50scanner.reader.core.Reader;
import in.mobiux.android.orca50scanner.reader.core.barcode.base.ConstantFlag;
import in.mobiux.android.orca50scanner.reader.core.barcode.base.ReaderBase;
import in.mobiux.android.orca50scanner.reader.core.barcode.helper.ReaderHelper;
import in.mobiux.android.orca50scanner.reader.core.barcode.helper.ScannerSetting;
import in.mobiux.android.orca50scanner.reader.core.barcode.helper.TDCodeTagBuffer;
import in.mobiux.android.orca50scanner.reader.core.barcode.tools.Beeper;
import in.mobiux.android.orca50scanner.reader.core.barcode.tools.PreferenceUtil;

public class OrcaAirBarcodeReader implements Reader {

    private static final String TAG = "OrcaAirBarcodeReader";

    private static final int BAUD_RATE = 9600;
    private static final String PORT = "dev/ttyS1";
    private ReaderHelper mReaderHelper;
    private ReaderBase mReader;

    private SerialPort mSerialPort = null;
    private List<String> mPortList = new ArrayList<String>();

    private SerialPortFinder mSerialPortFinder;
    private int mPosPort = -1;

    String[] entries = null;
    String[] entryValues = null;

    //the buffer of 2D code and bar code data;
    public static TDCodeTagBuffer mTagBuffer;

    private Context context;

    public OrcaAirBarcodeReader(Context context) {
        this.context = context;

        mSerialPortFinder = new SerialPortFinder();
        entries = mSerialPortFinder.getAllDevices();
        entryValues = mSerialPortFinder.getAllDevicesPath();
    }

    void initConnection() {

        try {
            if (ModuleManager.newInstance().getUHFStatus()) {
                ModuleManager.newInstance().setUHFStatus(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "initConnection: Please exit UHFDemo first!");
            return;
        }

        try {
            mSerialPort = new SerialPort(new File(PORT), BAUD_RATE, 0);

            if (!ModuleManager.newInstance().setScanStatus(true)) {
                throw new RuntimeException("Scan power on failure,may you open in other" +
                        "Process and do not exit it");
            }

            try {
                mReaderHelper = ReaderHelper.getDefaultHelper();
                mReaderHelper.setReader(mSerialPort.getInputStream(), mSerialPort.getOutputStream());
                mReader = mReaderHelper.getReader();

                mTagBuffer = mReaderHelper.getCurOperateTagBinDCodeBuffer();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    try {
                        if (!PreferenceUtil.getBoolean(ConstantFlag.IS_FIRST_OPEN, false)) {
                            Thread.currentThread().sleep(3000);
                            ScannerSetting.newInstance().defaultSettings();
                            PreferenceUtil.commitBoolean(ConstantFlag.IS_FIRST_OPEN, true);
                        }
                        Thread.currentThread().sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }


                    Log.i(TAG, "=== BARCODE === Connection Established Successfully");
                }
            }).start();

            //finish();
        } catch (SecurityException e) {
            Log.e(TAG, "initConnection: " + R.string.error_security);
        } catch (IOException e) {
            Log.e(TAG, "initConnection: " + R.string.error_unknown);
        } catch (InvalidParameterException e) {
            Log.e(TAG, "initConnection: " + R.string.error_configuration);
        } catch (Exception e) {
            Log.d("where the exception!", "is here" + e.getLocalizedMessage());
            /*catch exception test */
        }
    }

    public void releaseResources() {

        if (mReader != null)
            mReader.signOut();
        if (mSerialPort != null)
            mSerialPort.close();
        mSerialPort = null;
        Beeper.release();
    }

    void resumeConnection() {
        if (mReader != null) {
            if (!mReader.IsAlive())
                mReader.StartWait();
        }
    }

    void pauseConnection() {
        ModuleManager.newInstance().setScanStatus(false);
    }

    public void clearBuffer(){
        mTagBuffer.clearBuffer();
    }

    @Override
    public void connect(ReaderType type) {
        initConnection();
    }

    @Override
    public boolean isConnected() {
        return false;
    }
}
