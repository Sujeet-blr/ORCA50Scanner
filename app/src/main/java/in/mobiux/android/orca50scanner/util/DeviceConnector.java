package in.mobiux.android.orca50scanner.util;

import android.content.Context;

import com.module.interaction.ModuleConnector;
import com.module.interaction.RXTXListener;
import com.nativec.tools.ModuleManager;
import com.rfid.RFIDReaderHelper;
import com.rfid.ReaderConnector;
import com.rfid.rxobserver.RXObserver;
import com.rfid.rxobserver.ReaderSetting;
import com.rfid.rxobserver.bean.RXInventoryTag;

import java.util.logging.Handler;

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

    DeviceListener listener;
    private static Handler handler;

    public static DeviceConnector getInstance(Context context) {
        if (deviceConnector == null)
            deviceConnector = new DeviceConnector(context);
        return deviceConnector;
    }

    public DeviceConnector(Context context) {
        this.context = context;
    }

    private boolean connectDevice() {
        if (!connector.isConnected()) {
            ModuleManager.newInstance().setUHFStatus(true);
            try {
                rfidReaderHelper = RFIDReaderHelper.getDefaultHelper();
            } catch (Exception e) {
                e.printStackTrace();
            }
            boolean status = connector.connectCom(PORT, BOUD_RATE);
            return status;
        }
        return true;
    }

    private void addObserver(RXObserver rxObserver) {
        if (connectDevice() && rfidReaderHelper != null) {
            rfidReaderHelper.registerObserver(rxObserver);
        }
    }

    private void addListener(RXTXListener rxtxListener) {
        if (!connectDevice()) {
            return;
        }
        if (rfidReaderHelper != null) {
            rfidReaderHelper.setRXTXListener(rxtxListener);
        }
    }


    public void setOnEventListener(DeviceListener listener) {
        this.listener = listener;
    }

    public abstract class DeviceListener extends RXObserver {

        @Override
        protected void refreshSetting(ReaderSetting readerSetting) {
            if (listener != null) {
                listener.refreshSetting(readerSetting);
            }
        }

        @Override
        protected void onExeCMDStatus(byte cmd, byte status) {
            if (listener != null) {
                listener.onExeCMDStatus(cmd, status);
            }
        }

        @Override
        protected void onInventoryTag(RXInventoryTag tag) {
            if (listener != null) {
                listener.onInventoryTag(tag);
            }
        }

        @Override
        protected void onInventoryTagEnd(RXInventoryTag.RXInventoryTagEnd tagEnd) {
            if (listener != null) {
                listener.onInventoryTagEnd(tagEnd);
            }
        }
    }

    RXTXListener rxtxListener = new RXTXListener() {
        @Override
        public void reciveData(byte[] bytes) {

        }

        @Override
        public void sendData(byte[] bytes) {

        }

        @Override
        public void onLostConnect() {

        }
    };
}
