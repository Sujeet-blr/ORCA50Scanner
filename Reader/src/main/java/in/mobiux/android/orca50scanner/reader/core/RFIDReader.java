package in.mobiux.android.orca50scanner.reader.core;

import android.content.Context;
import android.os.Handler;

import com.module.interaction.ModuleConnector;
import com.module.interaction.RXTXListener;
import com.nativec.tools.ModuleManager;
import com.rfid.RFIDReaderHelper;
import com.rfid.ReaderConnector;
import com.rfid.rxobserver.RXObserver;
import com.rfid.rxobserver.ReaderSetting;
import com.rfid.rxobserver.bean.RXInventoryTag;

import in.mobiux.android.orca50scanner.common.utils.App;
import in.mobiux.android.orca50scanner.common.utils.AppBuildConfig;
import in.mobiux.android.orca50scanner.common.utils.AppLogger;
import in.mobiux.android.orca50scanner.common.utils.SessionManager;
import in.mobiux.android.orca50scanner.reader.model.Inventory;
import in.mobiux.android.orca50scanner.reader.simulator.AppSimulator;

public class RFIDReader implements Reader {

    public static final String TAG = RFIDReader.class.getCanonicalName();
    private final Context context;
    private final SessionManager session;
    private final AppLogger logger;
    private final Handler mHandler;

    private App app;
    public static String PORT = "dev/ttyS4";
    public static int BAUD_RATE = 115200;

    public ModuleConnector connector = new ReaderConnector();
    private ReaderSetting readerSetting = ReaderSetting.newInstance();
    public RFIDReaderHelper rfidReaderHelper;
    public RFIDReaderListener listener;
    private boolean connectionStatus = false;
    private boolean observerRegistrationStatus = false;
    private boolean scanningStatus = false;

    private final RXTXListener rxtxListener = new RXTXListener() {
        @Override
        public void reciveData(byte[] bytes) {
            logger.i(TAG, "receiveData " + bytes);
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
                        connectionStatus = false;
                    }
                });
            }
        }
    };

    private final RXObserver rxObserver = new RXObserver() {

        @Override
        protected void refreshSetting(ReaderSetting readerSetting) {
            logger.i(TAG, "Setting Refresh ");

            session.setValue("rssi", String.valueOf(rfidReaderHelper.getOutputPower(readerSetting.btReadId)));
        }

        @Override
        protected void onExeCMDStatus(byte cmd, byte status) {
            logger.i(TAG, "Command Executed " + cmd + "\tstatus " + status);

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
            logger.i(TAG, "onInventoryTag : crc-" + tag.strCRC + "# rssi-" + tag.strRSSI + "# freq-" + tag.strFreq + "#pc-" + tag.strPC + "#btnID-" + tag.btAntId);

            Inventory inventory = new Inventory();
            inventory.setEpc(tag.strEPC);
            inventory.setRssi(tag.strRSSI);

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
            logger.i(TAG, "Read Rate " + tagReadingSpeed);

//            app.playBeep();

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

    public RFIDReader(Context context) {
        this.context = context;
        app = (App) context;
        logger = AppLogger.getInstance(context);
        session = SessionManager.getInstance(context);
        mHandler = new Handler(context.getMainLooper());
    }

    @Override
    public void connect(ReaderType type) {
        initConnection();
    }

    @Override
    public boolean isConnected() {
        return connectionStatus;
    }

    private void initConnection() {

        if (AppBuildConfig.DEBUG) {

            if (listener != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onConnection(true);
                    }
                });
            }
            return;
        }

        logger.i(TAG, "Connecting to RFID");

        try {
            if (connector.connectCom(PORT, BAUD_RATE)) {
                logger.i(TAG, "CONNECTION SUCCESS");

                connectionStatus = true;

                try {

                    rfidReaderHelper = RFIDReaderHelper.getDefaultHelper();
                    if (!observerRegistrationStatus) {
                        rfidReaderHelper.registerObserver(rxObserver);
                        rfidReaderHelper.setRXTXListener(rxtxListener);
                        observerRegistrationStatus = true;
                    }

                    readerSetting = ReaderSetting.newInstance();
                    ModuleManager.newInstance().setUHFStatus(true);
//                    readerType = DeviceConnector.ReaderType.RFID;

                    int beeperResult = -1;
                    beeperResult = rfidReaderHelper.setBeeperMode(ReaderSetting.newInstance().btReadId, (byte) 2);
                    logger.i(TAG, "beeper result value " + beeperResult);

                    ReaderSetting.newInstance().btBeeperMode = ((byte) 2);

                    logger.i(TAG, "beeper result " + beeperResult);

                    rfidReaderHelper.setTrigger(true);
//                    setOutputPower("5");
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

        connectionStatus = connector.isConnected();
    }

    public int selectAccessEpcMatch(byte selectedTagLength, byte[] btAryEpc) {
        return rfidReaderHelper.setAccessEpcMatch(ReaderSetting.newInstance().btReadId, selectedTagLength, btAryEpc);
    }

    public int writeToTag(byte[] btAryPassWord, byte btMemBank, byte btWordAdd, byte btWordCnt, byte[] btAryData) {
        return rfidReaderHelper.writeTag(ReaderSetting.newInstance().btReadId, btAryPassWord, btMemBank, btWordAdd, btWordCnt, btAryData);
    }

    public void releaseResources() {

        logger.i(TAG, "Releasing resources");

        try {
            if (connector != null && !connector.isConnected()) {
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.e(TAG, "" + e.getLocalizedMessage());
        } finally {
            if (connector != null) {
                ModuleManager.newInstance().setUHFStatus(false);
                ModuleManager.newInstance().release();
            }
        }

        if (observerRegistrationStatus) {
            rfidReaderHelper.unRegisterObserver(rxObserver);
            rfidReaderHelper.unRegisterObservers();
        }

        if (rfidReaderHelper != null) {
            rfidReaderHelper.signOut();
        }
    }

    public void setOnRFIDReaderListener(RFIDReaderListener listener) {
        this.listener = listener;

        if (AppBuildConfig.DEBUG && AppSimulator.simulator != null) {
            AppSimulator.simulator.activateRFIDSimulation(listener);
        }
    }
}
