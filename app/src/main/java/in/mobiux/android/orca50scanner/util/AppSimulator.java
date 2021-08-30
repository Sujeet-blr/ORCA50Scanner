package in.mobiux.android.orca50scanner.util;

import android.content.Context;
import android.os.Handler;

import com.rfid.rxobserver.bean.RXInventoryTag;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import in.mobiux.android.orca50scanner.BuildConfig;
import in.mobiux.android.orca50scanner.MyApplication;
import in.mobiux.android.orca50scanner.api.model.Inventory;

public class AppSimulator {

    private static final String TAG = AppSimulator.class.getCanonicalName();
    MyApplication app;
    public static AppSimulator simulator;
    private Handler handler;

    private RFIDReaderListener rfidReaderListener;

    public static void initSimulator(Context context) {

        if (!BuildConfig.DEBUG) {
            return;
        }

        if (simulator == null) {
            simulator = new AppSimulator(context);
        }

        simulator.activateSimulator();
    }

    private AppSimulator(Context context) {
        app = (MyApplication) context.getApplicationContext();
        handler = new Handler(context.getMainLooper());
    }

    public void activateRFIDSimulation(RFIDReaderListener listener) {
        this.rfidReaderListener = listener;
    }

    private void activateSimulator() {

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {


                app.logger.i(TAG, "tick");

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
                            inventory.setEpc("AA00000"+new Random().nextInt(9));
                            int rssi = new Random().nextInt(99);
                            inventory.setRssi("" + rssi);

                            app.logger.i(TAG, "Asset generated " + inventory.getFormattedEPC());

//                            Inventory inventory1 = new Inventory();
////                            inventory.setEpc("AA000004");
//                            inventory1.setEpc("AA000004");
//                            rssi = new Random().nextInt(99);
//                            inventory1.setRssi("" + rssi);
//
//
//                            Inventory inventory2 = new Inventory();
////                            inventory.setEpc("AA000004");
//                            inventory2.setEpc("123456789123456789123456");
//                            rssi = new Random().nextInt(99);
//                            inventory2.setRssi("" + rssi);
//
//
//                            Inventory inventory3 = new Inventory();
////                            inventory.setEpc("AA000004");
//                            inventory3.setEpc("AA000005"+new Random().nextInt(99));
//                            rssi = new Random().nextInt(99);
//                            inventory3.setRssi("" + rssi);
//

                            rfidReaderListener.onInventoryTag(inventory);
//                            rfidReaderListener.onInventoryTag(inventory1);
//                            rfidReaderListener.onInventoryTag(inventory2);
//                            rfidReaderListener.onInventoryTag(inventory3);
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
