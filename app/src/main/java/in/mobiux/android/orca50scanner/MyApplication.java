package in.mobiux.android.orca50scanner;

import android.app.Activity;
import android.app.Application;
import android.media.MediaPlayer;
import android.os.Handler;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;
import androidx.room.Room;

import com.module.interaction.ModuleConnector;
import com.module.interaction.RXTXListener;
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

import in.mobiux.android.orca50scanner.activity.BaseActivity;
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

    public List<BaseActivity> activities = new ArrayList<>();

    private MediaPlayer mediaPlayer;


    @Override
    public void onCreate() {
        super.onCreate();

        logger = AppLogger.getInstance(getApplicationContext());
        logger.i(TAG, "= App Started =\n");
        mHandler = new Handler(getMainLooper());
        Presenter.init(getApplicationContext());
        session = SessionManager.getInstance(getApplicationContext());

        mediaPlayer = MediaPlayer.create(this, R.raw.beeper);

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

    private RXTXListener rxtxListener = new RXTXListener() {
        @Override
        public void reciveData(byte[] bytes) {
            logger.i(TAG, "reciveData " + bytes);
            scanningEndPoint = System.currentTimeMillis() + scanningInterval;
            if (listener != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onScanningStatus(true);
                    }
                });
            }
        }

        @Override
        public void sendData(byte[] bytes) {
            logger.i(TAG, "send Data " + bytes);
            if (listener != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onScanningStatus(true);
                    }
                });
            }
        }

        @Override
        public void onLostConnect() {
            logger.i(TAG, "onLostConnect");
            if (listener != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onConnection(false);
                    }
                });
            }
        }
    };

    private RXObserver rxObserver = new RXObserver() {

        @Override
        protected void refreshSetting(ReaderSetting readerSetting) {
            logger.i(TAG, "Setting Refresh ");

            session.setValue("rssi", String.valueOf(rfidReaderHelper.getOutputPower(readerSetting.btReadId)));
        }

        @Override
        protected void onExeCMDStatus(byte cmd, byte status) {
            logger.i(TAG, "Command Executed " + cmd + "\tstatus " + status);

            scanningEndPoint = System.currentTimeMillis() + scanningInterval;

            if (cmd == CMD.REAL_TIME_INVENTORY) {
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
                            scanningStatus = true;
                            listener.onScanningStatus(scanningStatus);
                        }
                    }
                });
            }
        }

        @Override
        protected void onInventoryTag(RXInventoryTag tag) {
            logger.i(TAG, "onInventoryTag : crc-" + tag.strCRC + "# rssi-" + tag.strRSSI + "# freq-" + tag.strFreq + "#pc-" + tag.strPC + "#btnID-" + tag.btAntId);

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
            logger.i(TAG, "onInventoryTagEnd " + tagEnd.mTotalRead);

            int tagReadingSpeed = tagEnd.mReadRate;

            scanningEndPoint = System.currentTimeMillis() + scanningInterval;

            mediaPlayer.start();

            if (listener != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onInventoryTagEnd(tagEnd);
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
                        rfidReaderHelper.setRXTXListener(rxtxListener);
                        observerRegistrationStatus = true;
                    }

                    readerSetting = ReaderSetting.newInstance();
                    ModuleManager.newInstance().setUHFStatus(true);

                    int beeperResult = -1;
                    beeperResult = rfidReaderHelper.setBeeperMode(ReaderSetting.newInstance().btReadId, (byte) 2);
                    logger.i(TAG, "beeper result value " + beeperResult);
//                    Toast.makeText(this, "beeper value " + beeperResult, Toast.LENGTH_SHORT).show();

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
            rfidReaderHelper.setRXTXListener(rxtxListener);

            rfidReaderHelper.setTrigger(true);

            int beeperResult = -1;
            beeperResult = rfidReaderHelper.setBeeperMode(ReaderSetting.newInstance().btReadId, (byte) 2);

            logger.i(TAG, "beeper result rec " + beeperResult);
//            Toast.makeText(this, "beeper result rec " + beeperResult, Toast.LENGTH_SHORT).show();
            ReaderSetting.newInstance().btBeeperMode = ((byte) 2);

            logger.i(TAG, "beeper result recc " + beeperResult);
            session.setValue("rssi", String.valueOf(rfidReaderHelper.getOutputPower(readerSetting.btReadId)));


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
            rfidReaderHelper.unRegisterObservers();
        }

        if (rfidReaderHelper != null) {
            rfidReaderHelper.signOut();
        }

        for (BaseActivity activity : activities) {
            activity.finish();
        }

        timer.cancel();
    }

    public void addActivity(BaseActivity activity) {
        activities.add(activity);
    }

    private void initTimer() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

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
                            }
                        });
                    }
                }
            }
        }, 1000, 1000);
    }
}
