package in.mobiux.android.orca50scanner.util;

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

import in.mobiux.android.orca50scanner.api.model.Inventory;

/**
 * Created by SUJEET KUMAR on 16-Mar-21.
 */

public class DeviceConnector {

    private Context context;
    private static DeviceConnector deviceConnector;
    private ModuleConnector connector = new ReaderConnector();
    private RFIDReaderHelper rfidReaderHelper;

    public static String PORT = "dev/ttyS4";
    public static int BOUD_RATE = 115200;

    private RFIDReaderListener listener;
    private Handler mHandler;
    public byte beeperMode = 1;
    boolean connectionStatus = false;

    public static DeviceConnector getInstance(Context context) {
        if (deviceConnector == null)
            deviceConnector = new DeviceConnector(context);
        return deviceConnector;
    }

    public DeviceConnector(Context context) {
        this.context = context;
        mHandler = new Handler(context.getMainLooper());
    }

    private boolean connectDevice() {
        if (!connector.isConnected()) {
            ModuleManager.newInstance().setUHFStatus(true);
            try {
                rfidReaderHelper = RFIDReaderHelper.getDefaultHelper();
                rfidReaderHelper.setTrigger(true);
                rfidReaderHelper.setBeeperMode(ReaderSetting.newInstance().btReadId, beeperMode);
                ReaderSetting.newInstance().btBeeperMode = beeperMode;
                rfidReaderHelper.registerObserver(rxObserver);
            } catch (Exception e) {
                e.printStackTrace();
            }
            connectionStatus = connector.connectCom(PORT, BOUD_RATE);
        } else {
            connectionStatus = true;
        }

        if (listener != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onConnection(connectionStatus);
                }
            });
        }
        return connectionStatus;
    }

    public boolean isConnected() {
        return connector.isConnected();
    }

    private static DeviceConnector getClient(Context context) {
        return getInstance(context);
    }

    private void addListener(RXTXListener rxtxListener) {
        if (!connectDevice()) {
            return;
        }
        if (rfidReaderHelper != null) {
            rfidReaderHelper.setRXTXListener(rxtxListener);
        }
    }

    public void setOnEventListener(RFIDReaderListener listener) {
        this.listener = listener;
    }

    private RXObserver rxObserver = new RXObserver() {
        @Override
        protected void refreshSetting(ReaderSetting readerSetting) {
            if (listener != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }
        }

        @Override
        protected void onExeCMDStatus(byte cmd, byte status) {
            if (listener != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }
        }

        @Override
        protected void onInventoryTag(RXInventoryTag tag) {

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

    RXTXListener rxtxListener = new RXTXListener() {
        @Override
        public void reciveData(byte[] bytes) {

        }

        @Override
        public void sendData(byte[] bytes) {

        }

        @Override
        public void onLostConnect() {
            connectionStatus = false;
            if (listener != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onConnection(connectionStatus);
                    }
                });
            }
        }
    };
}
