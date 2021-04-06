package in.mobiux.android.orca50scanner;

import android.app.Application;
import android.os.Handler;

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
    }

    private RXObserver rxObserver = new RXObserver() {
        @Override
        protected void refreshSetting(ReaderSetting readerSetting) {
            logger.i(TAG, "Setting Refresh ");
        }

        @Override
        protected void onExeCMDStatus(byte cmd, byte status) {
            logger.i(TAG, "Command Executed " + cmd + "\tstatus " + status);

            if (cmd == CMD.REAL_TIME_INVENTORY)
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            scanningStatus = true;
                            listener.onScanningStatus(scanningStatus);
                        }
                    }
                });
        }

        @Override
        protected void onInventoryTag(RXInventoryTag tag) {
            logger.i(TAG, "Tag scanner : crc-" + tag.strCRC + "# rssi-" + tag.strRSSI + "# freq-" + tag.strFreq + "#pc-" + tag.strPC + "#btnID-" + tag.btAntId);

            Inventory inventory = new Inventory();
            inventory.setEpc(tag.strEPC);
            inventory.setRssi(tag.strRSSI);

            if (listener != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onInventoryTag(inventory);
                    }
                });
            }
        }

        @Override
        protected void onInventoryTagEnd(RXInventoryTag.RXInventoryTagEnd tagEnd) {
            logger.i(TAG, "Inventory tag read end " + tagEnd.mTotalRead);

            int tagReadingSpeed = tagEnd.mReadRate;

            if (listener != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onInventoryTagEnd(tagEnd);
//                        tvInventoryCount.setText("" + tagEnd.mTagCount);
//                        tvSpeed.setText("" + tagReadingSpeed);

//                        TODO
//                        if (scanningStatus) {
////                            btnStart.setText("Scanning");
////                            rfidReaderHelper.realTimeInventory(ReaderSetting.newInstance().btReadId, (byte) 0x01);
//                            listener.onScanningStatus(scanningStatus);
//                        } else {
////                            btnStart.setText("Inventory");
//                            listener.onScanningStatus(scanningStatus);
//                        }
                    }
                });
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

                    ModuleManager.newInstance().setUHFStatus(true);
                    byte beeperMode = 1;

                    rfidReaderHelper.setBeeperMode(readerSetting.btReadId, beeperMode);
                    readerSetting.btBeeperMode = beeperMode;

                    rfidReaderHelper.setTrigger(true);

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
        if (connector.isConnected() && rfidReaderHelper != null) {
            rfidReaderHelper.startWith();

            if (listener != null) {
                listener.onScanningStatus(connector.isConnected());
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
    }
}
