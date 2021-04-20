package in.mobiux.android.orca50scanner;

import android.app.Application;
import android.os.Handler;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;
import androidx.room.Room;

import com.module.interaction.ModuleConnector;
import com.nativec.tools.ModuleManager;
import com.rfid.RFIDReaderHelper;
import com.rfid.ReaderConnector;
import com.rfid.config.CMD;
import com.rfid.rxobserver.RXObserver;
import com.rfid.rxobserver.ReaderSetting;
import com.rfid.rxobserver.bean.RXInventoryTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import in.mobiux.android.orca50scanner.api.ApiClient;
import in.mobiux.android.orca50scanner.api.Presenter;
import in.mobiux.android.orca50scanner.api.model.AssetResponse;
import in.mobiux.android.orca50scanner.api.model.Inventory;
import in.mobiux.android.orca50scanner.database.AppDatabase;
import in.mobiux.android.orca50scanner.database.InventoryDatabase;
import in.mobiux.android.orca50scanner.database.LaboratoryDatabase;
import in.mobiux.android.orca50scanner.util.AppLogger;
import in.mobiux.android.orca50scanner.util.DeviceConnector;
import in.mobiux.android.orca50scanner.util.RFIDReaderListener;
import in.mobiux.android.orca50scanner.util.SessionManager;
import in.mobiux.android.orca50scanner.viewmodel.LaboratoryViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by SUJEET KUMAR on 08-Mar-21.
 */
public class MyApplication extends Application {

    public AppDatabase db;
    public AppLogger logger;
    private String TAG = MyApplication.class.getName();

    public ModuleConnector connector = new ReaderConnector();
    private ReaderSetting readerSetting = ReaderSetting.newInstance();
    public RFIDReaderHelper rfidReaderHelper;
    private RFIDReaderListener listener;
    boolean connectionStatus = false;
    private Handler mHandler;
    private boolean observerRegistrationStatus = false;
    public boolean scanningStatus = false;
    public SessionManager session;

    public InventoryDatabase inventoryDatabase;
    public LaboratoryDatabase laboratoryDatabase;
    public byte beeperMode = 1;
    Timer timer = new Timer();
    long scanningEndPoint = System.currentTimeMillis();
    long scanningInterval = 500;


    @Override
    public void onCreate() {
        super.onCreate();

        logger = AppLogger.getInstance(getApplicationContext());
        logger.i(TAG, "============App Started....==========\n");
        mHandler = new Handler(getMainLooper());
        Presenter.init(getApplicationContext());
        session = SessionManager.getInstance(getApplicationContext());


        inventoryDatabase = InventoryDatabase.getInstance(getApplicationContext());
        laboratoryDatabase = LaboratoryDatabase.getInstance(getApplicationContext());

        try {
//            beeperMode = Byte.parseByte(session.getValue("beeperMode"));
            logger.i(TAG, "" + beeperMode);
        } catch (Exception e) {
            logger.e(TAG, "" + e.getLocalizedMessage());
        }

        initTimer();
        scanningEndPoint = scanningEndPoint + scanningInterval;
    }

    private RXObserver rxObserver = new RXObserver() {


        @Override
        protected void refreshSetting(ReaderSetting readerSetting) {
            logger.i(TAG, "Setting Refresh ");
        }

        @Override
        protected void onExeCMDStatus(byte cmd, byte status) {
            logger.i(TAG, "Command Executed " + cmd + "\tstatus " + status);

            if (cmd == CMD.REAL_TIME_INVENTORY) {
                scanningEndPoint = System.currentTimeMillis() + scanningInterval;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            scanningStatus = true;
                            listener.onScanningStatus(scanningStatus);
                        }
                    }
                });
            } else {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            scanningStatus = false;
                            listener.onScanningStatus(scanningStatus);
                        }
                    }
                });
            }
        }

        @Override
        protected void onInventoryTag(RXInventoryTag tag) {
            logger.i(TAG, "Tag scanner : crc-" + tag.strCRC + "# rssi-" + tag.strRSSI + "# freq-" + tag.strFreq + "#pc-" + tag.strPC + "#btnID-" + tag.btAntId);

            Inventory inventory = new Inventory();
            inventory.setEpc(tag.strEPC);
            inventory.setRssi(tag.strRSSI);

            scanningEndPoint = System.currentTimeMillis() + scanningInterval;

            if (listener != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onScanningStatus(true);
                        listener.onInventoryTag(inventory);
                    }
                });
            }
        }

        @Override
        protected void onInventoryTagEnd(RXInventoryTag.RXInventoryTagEnd tagEnd) {
            logger.i(TAG, "Inventory tag read end " + tagEnd.mTotalRead);

            int tagReadingSpeed = tagEnd.mReadRate;

            scanningEndPoint = System.currentTimeMillis() + scanningInterval;

            if (listener != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        listener.onInventoryTagEnd(tagEnd);
//                        listener.onScanningStatus(scanningStatus);
//                        listener.onScanningStatus(false);
                    }
                });
            }

            if (scanningStatus) {
//                rfidReaderHelper.realTimeInventory(ReaderSetting.newInstance().btReadId, (byte) 0x01);
            }
        }
    };


    public void connectRFID() {

        try {
            if (connector.connectCom(DeviceConnector.PORT, DeviceConnector.BOUD_RATE)) {
                logger.i(TAG, "CONNECTION SUCCESS");

                try {


                    rfidReaderHelper = RFIDReaderHelper.getDefaultHelper();
                    if (!observerRegistrationStatus) {
                        rfidReaderHelper.registerObserver(rxObserver);
                        observerRegistrationStatus = true;
                    }

                    readerSetting = ReaderSetting.newInstance();
//                    ModuleManager.newInstance().setScanStatus(true);

                    ModuleManager.newInstance().setUHFStatus(true);

                    int beeperResult = -1;
                    beeperResult = rfidReaderHelper.setBeeperMode(ReaderSetting.newInstance().btReadId, (byte) 2);
                    logger.i(TAG, "beeper result value " + beeperResult);
                    Toast.makeText(this, "beeper value " + beeperResult, Toast.LENGTH_SHORT).show();

                    ReaderSetting.newInstance().btBeeperMode = ((byte) 2);

                    logger.i(TAG, "beeper resultttt " + beeperResult);

                    rfidReaderHelper.setTrigger(true);

                    session.setValue("rssi", String.valueOf(rfidReaderHelper.getOutputPower(readerSetting.btReadId)));

                } catch (Exception e) {
                    logger.i(TAG, "Exception " + e.getLocalizedMessage());
                    e.printStackTrace();
                }
            } else {
                logger.e(TAG, "CONNECTION FAILED");
            }
        } catch (Exception e) {
            logger.e(TAG, "" + e.getLocalizedMessage());
        }

        if (listener != null) {
            listener.onConnection(connector.isConnected());
        }
    }

    public void reconnectRFID() {
        logger.i(TAG, "reconnecting rfid");
        if (connector.isConnected() && rfidReaderHelper != null) {
            rfidReaderHelper.startWith();

            ModuleManager.newInstance().setUHFStatus(true);
            try {
                rfidReaderHelper = RFIDReaderHelper.getDefaultHelper();
            } catch (Exception e) {
                logger.e(TAG, "" + e.getLocalizedMessage());
                e.printStackTrace();
            }


            rfidReaderHelper.registerObserver(rxObserver);
            observerRegistrationStatus = true;

            rfidReaderHelper.setTrigger(true);

            int beeperResult = -1;
            beeperResult = rfidReaderHelper.setBeeperMode(ReaderSetting.newInstance().btReadId, (byte) 2);

            logger.i(TAG, "beeper result rec " + beeperResult);
            Toast.makeText(this, "beeper result rec " + beeperResult, Toast.LENGTH_SHORT).show();
            ReaderSetting.newInstance().btBeeperMode = ((byte) 2);

            logger.i(TAG, "beeper result recc " + beeperResult);


            if (listener != null) {
                listener.onConnection(connector.isConnected());
            }
        }

        if (!observerRegistrationStatus && rfidReaderHelper != null) {
            rfidReaderHelper.registerObserver(rxObserver);
            ModuleManager.newInstance().setUHFStatus(true);
        }
    }

    public void setOnRFIDListener(RFIDReaderListener listener) {
        this.listener = listener;
    }

    public void startScanning(String sourceTAG) {

        ModuleManager.newInstance().setUHFStatus(true);
        scanningStatus = true;
        rfidReaderHelper.realTimeInventory(ReaderSetting.newInstance().btReadId, (byte) 0x01);

        logger.i(TAG, sourceTAG + " # start Scan command sent");
    }

    public void stopScanning() {
        scanningStatus = false;
        logger.i(TAG, "stop scan command sent");
    }

    public void setOutputPower(String outputPower) {

        byte btOutputPower = 0x00;
        try {
            btOutputPower = (byte) Integer.parseInt(outputPower.toString());
        } catch (Exception e) {
            logger.e(TAG, "" + e.getLocalizedMessage());
        }

        if (rfidReaderHelper != null) {
            rfidReaderHelper.setOutputPower(readerSetting.btReadId, btOutputPower);
            readerSetting.btAryOutputPower = new byte[]{btOutputPower};
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        ModuleManager.newInstance().setUHFStatus(false);
        ModuleManager.newInstance().release();

        if (observerRegistrationStatus) {
            rfidReaderHelper.unRegisterObserver(rxObserver);
        }

        if (rfidReaderHelper != null) {
            rfidReaderHelper.signOut();
        }

        timer.cancel();
    }

    private void initTimer() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                logger.i(TAG, "" + System.currentTimeMillis());

                if ((System.currentTimeMillis() % 2) == 0) {
                    if (!connector.isConnected()) {
                        reconnectRFID();
                    }
                }

                if (System.currentTimeMillis() > scanningEndPoint) {
                    if (listener != null) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onScanningStatus(false);
                                logger.i(TAG, "Scanning trigger off");
                            }
                        });
                    }
                }
            }
        }, 1000, 1000);
    }
}
