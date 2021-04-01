package in.mobiux.android.orca50scanner.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import in.mobiux.android.orca50scanner.BuildConfig;
import in.mobiux.android.orca50scanner.MyApplication;
import in.mobiux.android.orca50scanner.R;
import in.mobiux.android.orca50scanner.adapter.InventoryAdapter;
import in.mobiux.android.orca50scanner.api.model.Inventory;
import in.mobiux.android.orca50scanner.database.AppDatabase;
import in.mobiux.android.orca50scanner.viewmodel.InventoryViewModel;

public class RFIDActivity extends BaseActivity {

    private static String TAG = RFIDActivity.class.getName();

    protected boolean mSwitchFlag = false;
    private VirtualKeyListenerBroadcastReceiver mVirtualKeyListenerBroadcastReceiver;

    private View parentLayout;
    private Snackbar snackbar;
    Button btnStart, btnStop, btnRefresh, btnExport, btnExportLogs;
    TextView tvInventoryCount, tvSpeed;

    private List<Inventory> inventoryList = new ArrayList<>();

    private AppDatabase db;
    private MyApplication app;

    private RecyclerView recyclerView;
    InventoryAdapter adapter;

    Timer timer = new Timer();
    TimerTask timerTask;
    boolean scanningStatus = false;
    boolean observerRegistrationStatus = false;

    HashMap<String, Integer> map = new HashMap<>();


    //    Build connector
    public static ModuleConnector connector = new ReaderConnector();
    RFIDReaderHelper rfidReaderHelper;

    InventoryViewModel viewModel;
    private String PORT = "dev/ttyS4";
    private int BOUD_RATE = 115200;

    private Handler handler;

    private void connectRFID() {
        try {
            if (connector.connectCom(PORT, BOUD_RATE)) {
                logger.i(TAG, "CONNECTION SUCCESS");

                try {

                    rfidReaderHelper = RFIDReaderHelper.getDefaultHelper();
                    if (!observerRegistrationStatus) {
                        rfidReaderHelper.registerObserver(rxObserver);
                        observerRegistrationStatus = true;
                    }
                    ModuleManager.newInstance().setUHFStatus(true);

                    rfidReaderHelper.setBeeperMode(ReaderSetting.newInstance().btReadId, (byte) 0x02);
                    ReaderSetting.newInstance().btBeeperMode = (byte) 0x02;
                    rfidReaderHelper.setTrigger(true);

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
    }

    RXObserver rxObserver = new RXObserver() {
        @Override
        protected void refreshSetting(ReaderSetting readerSetting) {
            logger.i(TAG, "Setting Refresh ");
        }

        @Override
        protected void onExeCMDStatus(byte cmd, byte status) {
            logger.i(TAG, "Command Executed " + cmd + "\tstatus " + status);

            if (cmd == CMD.REAL_TIME_INVENTORY)
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        btnStart.setText("Scanning");
                    }
                });
        }

        @Override
        protected void onInventoryTag(RXInventoryTag tag) {
//            super.onInventoryTag(tag);
            logger.i(TAG, "Tag Scanned " + tag.strEPC);
            logger.i(TAG, "scanner " + tag.strCRC + "#" + tag.strRSSI + "#" + tag.strFreq + "#" + tag.strPC + "#" + tag.btAntId);
            Inventory inventory = new Inventory();
            inventory.setEpc(tag.strEPC);
            inventory.setRssi("" + tag.strRSSI);

            logger.i(TAG, "sending to viewModel");
            viewModel.insert(inventory);
        }

        @Override
        protected void onInventoryTagEnd(RXInventoryTag.RXInventoryTagEnd tagEnd) {
            logger.i(TAG, "Inventory tag read end " + tagEnd.mTotalRead);
            logger.i(TAG, "onInventoryTgEnd");

            int tagReadingSpeed = tagEnd.mReadRate;

            handler.post(new Runnable() {
                @Override
                public void run() {
                    tvInventoryCount.setText("" + tagEnd.mTagCount);
                    tvSpeed.setText("" + tagReadingSpeed);

                    if (scanningStatus) {
                        btnStart.setText("Scanning");
                        rfidReaderHelper.realTimeInventory(ReaderSetting.newInstance().btReadId, (byte) 0x01);
                    } else {
                        btnStart.setText("Inventory");
                    }
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        app = (MyApplication) getApplicationContext();
        db = app.db;

        handler = new Handler(getMainLooper());

        parentLayout = findViewById(android.R.id.content);
        recyclerView = findViewById(R.id.recyclerView);
        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
        btnRefresh = findViewById(R.id.btnRefresh);
        btnExport = findViewById(R.id.btnExport);
        tvInventoryCount = findViewById(R.id.tvInventoryCount);
        tvSpeed = findViewById(R.id.tvSpeed);
        btnExportLogs = findViewById(R.id.btnExportLogs);

        snackbar = Snackbar.make(parentLayout, "RFID-Device is not connected or PORT is not open, Please Check & Try Again", Snackbar.LENGTH_INDEFINITE);
        checkPermission(RFIDActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(RFIDActivity.this);

        viewModel = new ViewModelProvider(this).get(InventoryViewModel.class);

        adapter = new InventoryAdapter(RFIDActivity.this, inventoryList);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);


        viewModel.getAllInventory().observe(this, new Observer<List<Inventory>>() {
            @Override
            public void onChanged(List<Inventory> inventories) {

                logger.i(TAG, "onChanged ; listsize : " + inventories.size());

                adapter.setValues(inventories);
                layoutManager.smoothScrollToPosition(recyclerView, null, adapter.getItemCount());
                tvInventoryCount.setText("" + adapter.getItemCount());
            }
        });

        adapter.setOnItemClickListener(new InventoryAdapter.InventoryClickListener() {
            @Override
            public void onClick(Inventory inventory) {
                Intent intent = new Intent(RFIDActivity.this, InventoryDetailActivity.class);
                intent.putExtra("tag", inventory);
                scanningStatus = false;
                ModuleManager.newInstance().setUHFStatus(true);
                startActivity(intent);
            }
        });


        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                btnStart.setTag(true);

                logger.i(TAG, "clicked on Inventory Button");
                if (connector.isConnected()) {
                    try {
//                        rfidReaderHelper.setTrigger(true);
//                        if (!ModuleManager.newInstance().getUHFStatus())
//                            ModuleManager.newInstance().setUHFStatus(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    scanningStatus = true;
                    btnStart.setText("Scanning");
                    rfidReaderHelper.realTimeInventory(ReaderSetting.newInstance().btReadId, (byte) 0x01);
                } else {
                    btnStart.setText("Inventory");
                    rfidReaderHelper.startWith();
                }
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logger.i(TAG, "clicked on Stop Button==========");
                scanningStatus = false;
//                ModuleManager.newInstance().setUHFStatus(false);
                btnStart.setText("Inventory");
                btnStart.setTag(false);
            }
        });

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logger.i(TAG, "clicked on Refresh Button");
                viewModel.refresh();
                map.clear();
                inventoryList.clear();
                if (!connector.isConnected()) {
                    rfidReaderHelper.startWith();
                    ModuleManager.newInstance().setUHFStatus(true);
                }

                scanningStatus = false;
                btnStart.setTag(false);
//                rfidReaderHelper.setTrigger(true);
                btnStart.setText("Inventory");
            }
        });


        btnExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logger.i(TAG, "clicked on Data Export Button");
                if (ContextCompat.checkSelfPermission(RFIDActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    exportToCSV(inventoryList);
                } else {
                    checkPermission(RFIDActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);
                }
            }
        });

        btnExportLogs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(RFIDActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    logger.createAndExportLogs(RFIDActivity.this);
                } else {
                    checkPermission(RFIDActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);
                }
            }
        });

        connectRFID();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!connector.isConnected() && rfidReaderHelper != null) {
            rfidReaderHelper.startWith();
        }

        mVirtualKeyListenerBroadcastReceiver = new VirtualKeyListenerBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        this.registerReceiver(mVirtualKeyListenerBroadcastReceiver, intentFilter);
        if (mSwitchFlag) {
            ModuleManager.newInstance().setUHFStatus(true);
        }

        if (!observerRegistrationStatus) {
            rfidReaderHelper.registerObserver(rxObserver);
            ModuleManager.newInstance().setUHFStatus(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (observerRegistrationStatus) {
            rfidReaderHelper.unRegisterObserver(rxObserver);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        this.unregisterReceiver(mVirtualKeyListenerBroadcastReceiver);

//        removing all RXObserver to monitor the data
        rfidReaderHelper.unRegisterObservers();

//        stopping corresponding threads, and turn off the I/O resources accordingly.
//        OR : releasing rxtlistener which is running in seperate thread
        rfidReaderHelper.signOut();

//        releasing the power-off controller on the Reader.
        ModuleManager.newInstance().release();
        connector.disConnect();
    }

//    RXTXListener rxtxListener = new RXTXListener() {
//        @Override
//        public void reciveData(byte[] bytes) {
//            logger.i(TAG, "Data recieved");
//        }
//
//        @Override
//        public void sendData(byte[] bytes) {
//            logger.i(TAG, "Data Sent");
//        }
//
//        @Override
//        public void onLostConnect() {
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//
//                    logger.i(TAG, "CONNECTION LOST");
//                    logger.i(TAG, "Re-CONNECTING");
//
//                    btnStart.setText("Start");
//
//                    try {
////                        connectRFID();
//                        logger.i(TAG, "Connection Status : " + connector.isConnected());
//                        Toast.makeText(app, "Connection lost", Toast.LENGTH_SHORT).show();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        logger.e(TAG, "" + e.getLocalizedMessage());
//                    }
//                }
//            });
//        }
//    };

    private class VirtualKeyListenerBroadcastReceiver extends BroadcastReceiver {
        private final String SYSTEM_REASON = "reason";
        private final String SYSTEM_HOME_KEY = "homekey";
        private final String SYSTEM_RECENT_APPS = "recentapps";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String systemReason = intent.getStringExtra(SYSTEM_REASON);
                if (systemReason != null) {
                    if (systemReason.equals(SYSTEM_HOME_KEY)) {
                        System.out.println("Press HOME key");
                    } else if (systemReason.equals(SYSTEM_RECENT_APPS)) {
                        System.out.println("Press RECENT_APPS key");
                        ModuleManager.newInstance().setUHFStatus(false);
                        mSwitchFlag = true;
                    }
                }
            }
        }
    }
}