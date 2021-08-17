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
    private static AppSimulator simulator;
    private Handler handler;

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

    private void activateSimulator() {

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                if (app.listener != null && app.session.hasCredentials()) {

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            app.listener.onConnection(true);
                            app.listener.onScanningStatus(true);
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
                            inventory.setEpc("AA000003");
                            int rssi = new Random().nextInt(99);
                            inventory.setRssi("" + rssi);

                            app.logger.i(TAG, "Asset generated " + inventory.getFormattedEPC());

                            Inventory inventory1 = new Inventory();
                            inventory1.setEpc("AA000004");
                            rssi = new Random().nextInt(99);
                            inventory1.setRssi("" + rssi);


                            Inventory inventory2 = new Inventory();
                            inventory2.setEpc("123456789123456789123456");
                            rssi = new Random().nextInt(99);
                            inventory2.setRssi("" + rssi);


                            Inventory inventory3 = new Inventory();
                            inventory3.setEpc("AA000005");
                            rssi = new Random().nextInt(99);
                            inventory3.setRssi("" + rssi);


                            app.listener.onInventoryTag(inventory);
                            app.listener.onInventoryTag(inventory1);
                            app.listener.onInventoryTag(inventory2);
                            app.listener.onInventoryTag(inventory3);

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
                                app.listener.onScanningStatus(false);
                                app.listener.onInventoryTagEnd(tagEnd);
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 30000, 3000);

    }
}
