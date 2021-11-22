package in.mobiux.android.orca50scanner.reader.core;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import com.module.interaction.ModuleConnector;
import com.module.interaction.RXTXListener;
import com.nativec.tools.ModuleManager;
import com.rfid.RFIDReaderHelper;
import com.rfid.ReaderConnector;
import com.rfid.config.CMD;
import com.rfid.rxobserver.RXObserver;
import com.rfid.rxobserver.ReaderSetting;
import com.rfid.rxobserver.bean.RXInventoryTag;
import com.rfid.rxobserver.bean.RXOperationTag;
import com.util.Converter;
import com.util.StringTool;

import java.nio.ByteBuffer;
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

    ReaderSetting mReaderSetting = ReaderSetting.newInstance();

    private List<RFIDReaderListener> listeners = new ArrayList<>();

    private final RXTXListener rxtxListener = new RXTXListener() {
        @Override
        public void reciveData(byte[] bytes) {
//            logger.i(TAG, "receiveData " + Arrays.toString(bytes));
            if (listener != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
//                        listener.onScanningStatus(true);
                    }
                });
            }
        }

        @Override
        public void sendData(byte[] bytes) {
            logger.i(TAG, "send Data " + Arrays.toString(bytes));
            if (listener != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
//                        listener.onScanningStatus(true);
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

            int rssiValue = byteArrayToInt(readerSetting.btAryOutputPower);
            session.setInt(session.KEY_RF_OUTPUT_POWER, rssiValue);
            mReaderSetting = readerSetting;
        }

        @Override
        protected void onExeCMDStatus(byte cmd, byte status) {
            logger.i(TAG, "Command Executed " + cmd + "\tstatus " + status);

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    scanningStatus = true;
                    if (cmd == CMD.SET_ACCESS_EPC_MATCH || cmd == CMD.GET_OUTPUT_POWER || cmd == CMD.WRITE_TAG) {
                        scanningStatus = false;
                    }

                    for (RFIDReaderListener l : listeners) {
                        l.onScanningStatus(scanningStatus);
                    }
                }
            });
        }

        @Override
        protected void onInventoryTag(RXInventoryTag tag) {
            logger.i(TAG, "onInventoryTag :epc " + tag.strEPC + "\t # crc-" + tag.strCRC + "# rssi-" + tag.strRSSI + "# freq-" + tag.strFreq + "#pc-" + tag.strPC + "#btnID-" + tag.btAntId);

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
            scanningStatus = false;

            if (tagEnd.mTotalRead > 0) {
                beep();
            }

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (RFIDReaderListener l : listeners) {
                        l.onInventoryTagEnd(new Inventory.InventoryTagEnd(tagEnd));
                        l.onScanningStatus(scanningStatus);
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

        logger.i(TAG, "Connecting to RFID");

        try {

            if (connector.isConnected()) {
                connectionStatus = connector.isConnected();
            } else {
                connectionStatus = connector.connectCom(PORT, BAUD_RATE);
            }


            if (isConnected()) {
                logger.i(TAG, "RFID READER CONNECTION SUCCESS");

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
                    beeperResult = rfidReaderHelper.setBeeperMode(mReaderSetting.btReadId, (byte) 2);
                    logger.i(TAG, "beeper result value " + beeperResult);

                    mReaderSetting.newInstance().btBeeperMode = ((byte) 2);

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

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (rfidReaderHelper != null && isConnected()) {
                    getRFOutputPower();
                }
            }
        }, 500);

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

//        if (app.isDebugBuild())
//            return 0;

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

//            logger.i(TAG, "data " + new String(btAryData, StandardCharsets.UTF_8));
            logger.i(TAG, "data " + btAryData);
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

//        if (app.isDebugBuild())
//            return 0;

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

    public void deactivateRfidReader() {
        logger.i(TAG, "Activating Rfid Reader");
        if (connector != null && connector.isConnected()) {
            ModuleManager.newInstance().setUHFStatus(false);
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

    public int getRFOutputPower() {
        int value = 0;
        if (INSTANCE != null && isConnected()) {
            int status = rfidReaderHelper.getOutputPower(mReaderSetting.btReadId);
        } else {
            logger.e(TAG, "RFID Reader is not connected");
            return 1;
        }
        return value;
    }

    private int byteArrayToInt(byte[] bytes) {
        int rssiValue = 0;
        String str = Arrays.toString(bytes);
        str = str.replace("[", "");
        str = str.replace("]", "");
        rssiValue = Integer.parseInt(str);

        return rssiValue;
    }

    public void startScan() {
        rfidReaderHelper.realTimeInventory(mReaderSetting.btReadId, (byte) 0x01);
    }

    public int reset() {
        int status = -1;
        if (isConnected()) {
            status = rfidReaderHelper.reset(mReaderSetting.btReadId);
        }

        logger.i(TAG, "Resetting reader status (success = 0 ; failed = -1) : " + status);
        return status;
    }

    public void getStatus() {

        int c = 119;
        byte[] cmd = intToBytes(c);

        rfidReaderHelper.sendCommand(cmd);
    }

    private byte[] intToBytes(final int i) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt(i);
        return bb.array();
    }

    public void setStatus(int value) {

//        byte btDetectorStatus = 0x00;
//        try {
//            btDetectorStatus = (byte) value;
//        } catch (Exception e) {
//            logger.e(TAG, "Invaild number! ");
//            return;
//        }
    }
}
