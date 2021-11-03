package in.mobiux.android.orca50scanner.reader.simulator;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.rfid.rxobserver.bean.RXInventoryTag;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import in.mobiux.android.orca50scanner.common.utils.AppLogger;
import in.mobiux.android.orca50scanner.reader.core.BarcodeReaderListener;
import in.mobiux.android.orca50scanner.reader.core.RFIDReaderListener;
import in.mobiux.android.orca50scanner.reader.model.Barcode;
import in.mobiux.android.orca50scanner.reader.model.Inventory;

public class AppSimulator {

    private static final String TAG = AppSimulator.class.getCanonicalName();
    private Context context;
    public static AppSimulator simulator;
    private Handler handler;
    private AppLogger logger;

    private RFIDReaderListener rfidReaderListener;
    private BarcodeReaderListener barcodeReaderListener;

    public static void initSimulator(Context context) {

        if (simulator == null) {
            simulator = new AppSimulator(context);
        }

        simulator.activateSimulator();
    }

    private AppSimulator(Context context) {
        this.context = context.getApplicationContext();
        handler = new Handler(context.getMainLooper());
        logger = AppLogger.getInstance(context);

        logger.i(TAG, "AppSimulator is Created");
    }

    public void activateODSSimulation(BarcodeReaderListener listener) {
        this.barcodeReaderListener = listener;
        logger.i(TAG, "Barcode simulator activated");
    }

    public void activateRFIDSimulation(RFIDReaderListener listener) {
        this.rfidReaderListener = listener;
        logger.i(TAG, "RFID Simulator activated");
    }

    private void activateSimulator() {

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {


//                Log.i(TAG, "tick");

//                For RFID tags
//                if app.listener is register in any class then it will generate rfid tags
                if (rfidReaderListener != null) {

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            rfidReaderListener.onConnection(true);
                            rfidReaderListener.onScanningStatus(true);
                        }
                    });
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {


                            Inventory inventory = new Inventory();
//                            inventory.setEpc("AA000004");
                            inventory.setEpc("AA00000" + new Random().nextInt(9));
                            int rssi = new Random().nextInt(99);
                            inventory.setRssi("" + rssi);

                            Log.i(TAG, "Asset generated " + inventory.getFormattedEPC());


                            rfidReaderListener.onInventoryTag(inventory);
                        }
                    });


                    try {
                        Thread.sleep(500);

                        RXInventoryTag.RXInventoryTagEnd tagEnd = new RXInventoryTag.RXInventoryTagEnd();
                        tagEnd.mReadRate = 33;
                        tagEnd.mTotalRead = 44;
                        tagEnd.mTagCount = 55;

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                rfidReaderListener.onScanningStatus(false);
                                rfidReaderListener.onInventoryTagEnd(new Inventory.InventoryTagEnd(tagEnd));
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }


                if (barcodeReaderListener != null) {
                    Barcode barcode = new Barcode();
                    barcode.setName("123456" + new Random().nextInt(999));
                    logger.i(TAG, "barcode generated");

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            barcodeReaderListener.onScanningStatus(true);
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            barcodeReaderListener.onScanSuccess(barcode);
                            barcodeReaderListener.onScanningStatus(false);
                        }
                    });
                }

            }
        }, 3000, 5000);

    }
}
