package in.mobiux.android.orca50scanner.reader.core;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import com.module.interaction.ModuleConnector;
import com.module.interaction.RXTXListener;
import com.nativec.tools.ModuleManager;
import com.scanner1d.ODScannerConnector;
import com.scanner1d.ODScannerHelper;
import com.scanner1d.bean.ScannerSetting;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import in.mobiux.android.orca50scanner.common.utils.AppBuildConfig;
import in.mobiux.android.orca50scanner.common.utils.AppLogger;
import in.mobiux.android.orca50scanner.common.utils.SessionManager;
import in.mobiux.android.orca50scanner.reader.model.Barcode;
import in.mobiux.android.orca50scanner.reader.simulator.AppSimulator;


public class BarcodeReader implements Reader {

    public static final String TAG = BarcodeReader.class.getCanonicalName();
    private Context context;
    private SessionManager session;
    private AppLogger logger;

    private static final int BAUD_RATE = 9600;
    private static final String PORT = "dev/ttyS1";

    private ModuleConnector connector = new ODScannerConnector();
    private ODScannerHelper odScannerHelper;

    private Handler handler;
    private List<String> receivedData = new ArrayList<>();
    private String prStr;
    private String txtString = "";
    private BarcodeReaderListener listener;
    private boolean connectionStatus = false;

    RXTXListener rxtxListener = new RXTXListener() {
        @Override
        public void reciveData(byte[] bytes) {
            logger.i(TAG, "receiving data ");
            String s = new String(bytes, StandardCharsets.UTF_8);
            txtString += s;
            logger.i(TAG, "Scanned string is " + s);
            processReceivedData(s);

            if (listener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onScanningStatus(true);
                        Toast.makeText(context, "scanning recivedData", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        @Override
        public void sendData(byte[] bytes) {
            logger.i(TAG, "sending data " + new String(bytes, StandardCharsets.UTF_8));

            if (listener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onScanningStatus(true);
                        Toast.makeText(context, "Scanning SendData", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        @Override
        public void onLostConnect() {
            connectionStatus = false;
            logger.e(TAG, "Scanner connection Lost");

            if (listener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onConnection(connectionStatus);
                        Toast.makeText(context, "Connection Status " + connectionStatus, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    };

    //  todo -  this should work , But it's not working
    Observer observer = new Observer() {
        @Override
        public void update(Observable observable, Object arg) {

            Toast.makeText(context, "recived data is Observer", Toast.LENGTH_SHORT).show();

            logger.i(TAG, "observer data received");
            if (arg instanceof String) {
                String result = (String) arg;
                Barcode barcode = new Barcode();
                barcode.setName(result);

                if (listener != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onScanSuccess(barcode);
                            listener.onScanningStatus(false);
                        }
                    });
                }

                logger.i(TAG, "scanned barcode is " + result);
            } else if (arg instanceof ScannerSetting) {
                logger.i(TAG, " instance of scanSetting");

                if (listener != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onScanFailed(arg);
                            listener.onScanningStatus(false);
                        }
                    });
                }
            } else {
                logger.i(TAG, "update data type " + arg.getClass().getCanonicalName());

                if (listener != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onScanFailed(arg);
                            listener.onScanningStatus(false);
                        }
                    });
                }
            }
        }
    };

    public BarcodeReader(Context context) {
        this.context = context;
        logger = AppLogger.getInstance(context);
        session = SessionManager.getInstance(context);
        handler = new Handler(context.getMainLooper());
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

        handler = new Handler(context.getMainLooper());

        if (AppBuildConfig.isDEBUG()) {

            connectionStatus = true;
            if (listener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onConnection(connectionStatus);
                    }
                });
            }
            return;
        }

        ModuleManager.newInstance().setUHFStatus(false);
        ModuleManager.newInstance().setScanStatus(true);

        connector.connectCom(PORT, BAUD_RATE);

        if (connector.isConnected()) {
            connectionStatus = true;
            logger.i(TAG, "ODScanner is connected");
            odScannerHelper = ODScannerHelper.getDefaultHelper();

            //Power off the UHF,the UHF will not work.
            ModuleManager.newInstance().setUHFStatus(false);
            //Power on the 1D Scanner,must set the UHF can work.
            ModuleManager.newInstance().setScanStatus(true);
            //Must set the flag that the UHF is running,as it will effect 1D scanner when UHF is running.
            //so you should set like this when the UHF no Running.
            odScannerHelper.setRunFlag(false);
            odScannerHelper.setScanEnable();
            odScannerHelper.setLedOn();
            odScannerHelper.startWith();

//            app.readerType = ReaderType.BARCODE;

            odScannerHelper.registerObserver(observer);
            odScannerHelper.setRXTXListener(rxtxListener);
        }

        connectionStatus = connector.isConnected();

        if (listener != null) {
            listener.onConnection(connectionStatus);
        }
    }

    private void processReceivedData(String s) {
        logger.i(TAG, "processReceivedData " + s);
        if (s != null) {
            s = s.trim();
        }

        if (s != null && !s.isEmpty()) {
            receivedData.add(s);
            logger.i(TAG, "adding to string list " + receivedData.toString());
        } else {
            logger.i(TAG, "previous string is " + prStr);
            if (prStr != null && !prStr.isEmpty()) {
                String result = "";

                for (String code : receivedData) {
                    result += code.trim();
                }
                logger.i(TAG, "barcode is " + result);
                Barcode barcode = new Barcode();
                barcode.setName(result);

                if (listener != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onScanSuccess(barcode);
                            listener.onScanningStatus(false);
//                            app.playBeep();
                        }
                    });
                }
            }

            receivedData.clear();
        }

        prStr = s;
    }

    public void switchToBarcode() {

        ModuleManager.newInstance().setUHFStatus(false);
        ModuleManager.newInstance().setScanStatus(true);
        odScannerHelper.setRunFlag(true);
    }

    public void releaseResources() {
        if (AppBuildConfig.isDEBUG()) {
            return;
        }

        ModuleManager.newInstance().setScanStatus(false);
        ModuleManager.newInstance().setUHFStatus(true);
//        odScannerHelper.unRegisterObserver(observer);
//        if(odScannerHelper != null) {
        odScannerHelper.unRegisterObservers();
        odScannerHelper.setRunFlag(true);
        connector.disConnect();
        odScannerHelper.signOut();
//        }

        logger.i(TAG, "Resources released");
    }

    public void setOnBarcodeReaderListener(BarcodeReaderListener barcodeReaderListener) {
        this.listener = barcodeReaderListener;

        if (AppBuildConfig.isDEBUG() && AppSimulator.simulator != null) {
            AppSimulator.simulator.activateODSSimulation(listener);
        }
        logger.i(TAG, "Barcode listener registered");

    }
}
