package in.mobiux.android.orca50scanner.reader.simulator;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.rfid.rxobserver.bean.RXInventoryTag;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import in.mobiux.android.orca50scanner.reader.core.BarcodeReaderListener;
import in.mobiux.android.orca50scanner.reader.core.RFIDReaderListener;
import in.mobiux.android.orca50scanner.reader.model.Inventory;

public class AppSimulator {

    private static final String TAG = AppSimulator.class.getCanonicalName();
    private Context context;
    public static AppSimulator simulator;
    private Handler handler;

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
    }

    public void activateODSSimulation(BarcodeReaderListener listener) {
        this.barcodeReaderListener = listener;
    }

    public void activateRFIDSimulation(RFIDReaderListener listener) {
        this.rfidReaderListener = listener;
    }

    private void activateSimulator() {

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {


                Log.i(TAG, "tick");

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
                        Thread.sleep(2000);
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
                                rfidReaderListener.onInventoryTagEnd(tagEnd);
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }, 3000, 5000);

    }
}
