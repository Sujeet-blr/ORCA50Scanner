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
import com.rfid.rxobserver.bean.RXOperationTag;
import com.util.StringTool;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import in.mobiux.android.orca50scanner.common.utils.App;
import in.mobiux.android.orca50scanner.common.utils.AppBuildConfig;
import in.mobiux.android.orca50scanner.common.utils.AppLogger;
import in.mobiux.android.orca50scanner.common.utils.AppUtils;
import in.mobiux.android.orca50scanner.common.utils.SessionManager;
import in.mobiux.android.orca50scanner.reader.model.Barcode;
import in.mobiux.android.orca50scanner.reader.model.Inventory;
import in.mobiux.android.orca50scanner.reader.model.OperationTag;
import in.mobiux.android.orca50scanner.reader.simulator.AppSimulator;
import in.mobiux.android.orca50scanner.reader.utils.BeeperHelper;

public class RFIDReader implements Reader {

    public static final String TAG = RFIDReader.class.getCanonicalName();
    private Context context;
    private final SessionManager session;
    private final AppLogger logger;
    private final Handler mHandler;
    private static RFIDReader INSTANCE = null;

    private App app;
    public static String PORT = "dev/ttyS4";
    public static int BAUD_RATE = 115200;

    public ModuleConnector connector = new ReaderConnector();
    public RFIDReaderHelper rfidReaderHelper;
    public RFIDReaderListener listener;
    private boolean connectionStatus = false;
    private boolean observerRegistrationStatus = false;
    private boolean scanningStatus = false;

    private List<RFIDReaderListener> listeners = new ArrayList<>();

    private final RXTXListener rxtxListener = new RXTXListener() {
        @Override
        public void reciveData(byte[] bytes) {
//            logger.i(TAG, "receiveData " + Arrays.toString(bytes));
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
//            logger.i(TAG, "send Data " + Arrays.toString(bytes));
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
            logger.i(TAG, "Setting Refresh output power is : " + Arrays.toString(readerSetting.btAryOutputPower));

//            int rssiValue = AppUtils.byteArrayToInt(readerSetting.btAryOutputPower);
        }

        @Override
        protected void onExeCMDStatus(byte cmd, byte status) {
            logger.i(TAG, "Command Executed " + cmd + "\tstatus " + status);

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    scanningStatus = true;
                    for (RFIDReaderListener l : listeners) {
                        l.onScanningStatus(scanningStatus);
                    }
                }
            });
        }

        @Override
        protected void onInventoryTag(RXInventoryTag tag) {
            logger.i(TAG, "Scanned epc " + tag.strEPC);
            logger.i(TAG, "onInventoryTag : crc-" + tag.strCRC + "# rssi-" + tag.strRSSI + "# freq-" + tag.strFreq + "#pc-" + tag.strPC + "#btnID-" + tag.btAntId);

            Inventory inventory = new Inventory();
            inventory.setEpc(tag.strEPC);
            inventory.setRssi(tag.strRSSI);

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (RFIDReaderListener l : listeners) {
                        l.onScanningStatus(true);
                        l.onInventoryTag(inventory);
                    }
                }
            });
        }

        @Override
        protected void onInventoryTagEnd(RXInventoryTag.RXInventoryTagEnd tagEnd) {
            logger.i(TAG, "onInventoryTagEnd " + tagEnd.mTotalRead);

            int tagReadingSpeed = tagEnd.mReadRate;
            logger.i(TAG, "Read Rate " + tagReadingSpeed);

            beep();

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (RFIDReaderListener l : listeners) {
                        l.onInventoryTagEnd(new Inventory.InventoryTagEnd(tagEnd));
                    }
                }
            });
        }

        @Override
        protected void onOperationTag(RXOperationTag tag) {
            logger.i(TAG, "onOperationTag " + tag.strEPC);
            beep();

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    OperationTag operationTag = new OperationTag(tag);
                    for (RFIDReaderListener lis : listeners) {
                        lis.onOperationTag(operationTag);
                    }
                }
            });
        }
    };


    public RFIDReader(Context context) {
        this.context = context;
        app = (App) context;
        logger = AppLogger.getInstance(context);
        session = SessionManager.getInstance(context);
        mHandler = new Handler(context.getMainLooper());
        BeeperHelper.init(context);

        INSTANCE = this;
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

        if (AppBuildConfig.isDEBUG()) {

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

            if (connector.isConnected()) {
                connectionStatus = connector.isConnected();
            } else {
                connectionStatus = connector.connectCom(PORT, BAUD_RATE);
            }


            if (isConnected()) {
                logger.i(TAG, "CONNECTION SUCCESS");

                connectionStatus = true;

                try {

                    rfidReaderHelper = RFIDReaderHelper.getDefaultHelper();
                    if (!observerRegistrationStatus) {
                        rfidReaderHelper.registerObserver(rxObserver);
                        rfidReaderHelper.setRXTXListener(rxtxListener);
                        observerRegistrationStatus = true;
                    }

                    ModuleManager.newInstance().setUHFStatus(true);
//                    readerType = DeviceConnector.ReaderType.RFID;

                    int beeperResult = -1;
                    beeperResult = rfidReaderHelper.setBeeperMode(ReaderSetting.newInstance().btReadId, (byte) 2);
                    logger.i(TAG, "beeper result value " + beeperResult);

                    ReaderSetting.newInstance().btBeeperMode = ((byte) 2);

                    logger.i(TAG, "beeper result " + beeperResult);

                    rfidReaderHelper.setTrigger(true);
                } catch (Exception e) {
                    logger.i(TAG, "Exception - " + e.getLocalizedMessage());
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

    public int selectAccessEpcMatch(String tagEPC) {

        //        setting the selected rfid tag
        String[] tagResult = StringTool.stringToStringArray(tagEPC.replace(" ", "").toUpperCase(), 2);
        logger.i(TAG, "selected epc " + tagEPC);
        logger.i(TAG, "selected tag stringArray " + Arrays.toString(tagResult));
        byte[] btAryEpc = StringTool.stringToByteArray(tagEPC);
        btAryEpc = StringTool.stringArrayToByteArray(tagResult, tagResult.length);
        byte selectedTagLength = (byte) (btAryEpc.length & 0xFF);
        logger.i(TAG, "selected tag length " + selectedTagLength);
        logger.i(TAG, "selected tag byteArray " + Arrays.toString(btAryEpc));

        if (app.isDebugBuild())
            return 0;

        int selectStatus = rfidReaderHelper.setAccessEpcMatch(ReaderSetting.newInstance().btReadId, selectedTagLength, btAryEpc);

        if (selectStatus == 0) {
            logger.i(TAG, "rfid tag selected");
        } else {
            logger.e(TAG, "rfid tag not selected");
        }

        return selectStatus;
    }

//    public int writeToTag(byte[] btAryPassWord, byte btMemBank, byte btWordAdd, byte btWordCnt, byte[] btAryData) {
//
//        int writeStatus = rfidReaderHelper.writeTag(ReaderSetting.newInstance().btReadId, btAryPassWord, btMemBank, btWordAdd, btWordCnt, btAryData);
//        if (writeStatus == 0) {
//            logger.i(TAG, "write is success");
//
//        } else {
//            logger.e(TAG, "write is failed");
//        }
//
//        return writeStatus;
//    }

    public int writeToTag(Barcode barcode, Inventory selectedInventory) {

        logger.i(TAG, "initializing writeToTag " + barcode.getName() + " hex " + barcode.getHex() + " to epc " + selectedInventory.getEpc());

        int writeStatus = 1; // 0 = success , 1 = failed

        if (barcode == null || selectedInventory == null) {
            logger.e(TAG, "Barcode or Selected Epc is null");
            return writeStatus;
        }

        byte btMemBank = 0x00;
        byte btWordAdd = 0x00;
        byte btWordCnt = 0x00;
        byte[] btAryPassWord = null;

        btMemBank = 0x01;

        try {
            btWordAdd = (byte) Integer.parseInt("02");
        } catch (Exception e) {
            logger.e(TAG, "Invalid word add " + e.getLocalizedMessage());
//            showToast("Invalid word add " + e.getLocalizedMessage());
            return writeStatus;
        }

        try {
            String[] reslut = StringTool.stringToStringArray("00000000", 2);
            btAryPassWord = StringTool.stringArrayToByteArray(reslut, 4);
        } catch (Exception e) {
            logger.e(TAG, "Invalid password length " + btAryPassWord);
            return writeStatus;
        }

        byte[] btAryData = null;
        String[] result = null;

        try {
            result = StringTool.stringToStringArray(barcode.getHex().toUpperCase(), 2);
            logger.i(TAG, "barcode stringArray" + Arrays.toString(result));
            btAryData = StringTool.stringArrayToByteArray(result, result.length);

            logger.i(TAG, "data " + new String(btAryData, StandardCharsets.UTF_8));
            btWordCnt = (byte) ((result.length / 2 + result.length % 2) & 0xFF);

        } catch (Exception e) {
            logger.e(TAG, "barcode Data error " + btAryData);
            return writeStatus;
        }

        if (btAryData == null || btAryData.length <= 0) {
            logger.e(TAG, "Invalid data error");
            return writeStatus;
        }

        if (btAryPassWord == null || btAryPassWord.length < 4) {
            logger.e(TAG, "Password data error");
            return writeStatus;
        }

        logger.i(TAG, "pwd " + btAryPassWord + " membank " + btMemBank + " wordadd " + btWordAdd + " wordcount " + btWordCnt + " data array " + Arrays.toString(btAryData));

        if (app.isDebugBuild())
            return 0;

        writeStatus = rfidReaderHelper.writeTag(ReaderSetting.newInstance().btReadId, btAryPassWord, btMemBank, btWordAdd, btWordCnt, btAryData);
        if (writeStatus == 0) {
            logger.i(TAG, "write is success");

        } else {
            logger.e(TAG, "write is failed");
        }

        return writeStatus;
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
            if (connector != null && connector.isConnected()) {
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

        listeners.clear();
        BeeperHelper.release();
    }

    public void activateRfidReader() {
        logger.i(TAG, "Activating Rfid Reader");
        if (connector != null && connector.isConnected()) {
            ModuleManager.newInstance().setUHFStatus(true);
        }
    }

    public void setOnRFIDReaderListener(RFIDReaderListener listener) {
        this.listener = listener;

        listeners.remove(listener);
        listeners.add(listener);

        if (AppBuildConfig.isDEBUG() && AppSimulator.simulator != null) {
            AppSimulator.simulator.activateRFIDSimulation(listener);
        }
    }

    public void unregisterListener(RFIDReaderListener l) {
        listeners.remove(l);
    }


    private void beep() {
        if (session.getBooleanValue(session.KEY_BEEP)) {
            logger.i(TAG, "playing beep");
            BeeperHelper.beep(BeeperHelper.SOUND_FILE_TYPE_NORMAL);
        } else {
            logger.i(TAG, "beeper is disabled");
        }
    }

    public void enableBeep() {
        session.setBooleanValue(session.KEY_BEEP, true);
    }

    public void disableBeep() {
        session.setBooleanValue(session.KEY_BEEP, false);
    }


    //    methods for RF Output Power
    public int setRFOutputPower(int value) {
        logger.i(TAG, "Setting RSSI value " + value);
        if (INSTANCE != null && isConnected()) {
            byte val = (byte) value;
            int status = rfidReaderHelper.setOutputPower(ReaderSetting.newInstance().btReadId, val);
            if (status == 0) {
                beep();
                logger.i(TAG, "rf output power set success " + value);
                session.setInt(session.KEY_RF_OUTPUT_POWER, value);
            } else {
                logger.i(TAG, "rf output power set Failed " + status);
            }
            return status;
        } else {
            logger.e(TAG, "RFID Reader is not connected");
            return 1;
        }
    }
}
