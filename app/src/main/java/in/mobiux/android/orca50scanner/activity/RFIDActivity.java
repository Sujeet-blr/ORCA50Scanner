package in.mobiux.android.orca50scanner.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

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


    //    Build connector
    public static ModuleConnector connector = new ReaderConnector();
    RFIDReaderHelper rfidReaderHelper;
//    ReaderHelper rfidReaderHelper;

    InventoryViewModel viewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle("RFID SCANNER APP");

        app = (MyApplication) getApplicationContext();
        db = app.db;

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

        try {
            if (connector.connectCom("dev/ttyS4", 115200)) {
                logger.i(TAG, "CONNECTION SUCCESS");
                Toast.makeText(app, "Connected Success", Toast.LENGTH_SHORT).show();

                try {
                    ModuleManager.newInstance().setUHFStatus(true);
                    rfidReaderHelper = RFIDReaderHelper.getDefaultHelper();

                    rfidReaderHelper.registerObserver(new RXObserver() {
                        @Override
                        protected void onInventoryTag(RXInventoryTag tag) {
//                            super.onInventoryTag(tag);
                            logger.i(TAG, "Tag Scanned " + tag.strEPC);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(app, "Tag Scanned " + tag.strEPC, Toast.LENGTH_SHORT).show();
                                }
                            });

                            Inventory inventory = new Inventory();
                            inventory.setEpc(tag.strEPC);
                            inventory.setRssi(tag.strRSSI);
                            viewModel.insert(inventory);
                        }

                        @Override
                        protected void onInventoryTagEnd(RXInventoryTag.RXInventoryTagEnd tagEnd) {
//                            super.onInventoryTagEnd(tagEnd);
                            logger.i(TAG, "onInventoryTgEnd");
                            if (scanningStatus) {
                                rfidReaderHelper.realTimeInventory(ReaderSetting.newInstance().btReadId, CMD.REAL_TIME_INVENTORY);
                            }
                        }
                    });

                    rfidReaderHelper.setRXTXListener(new RXTXListener() {
                        @Override
                        public void reciveData(byte[] bytes) {
                            logger.i(TAG, "recieved data");
                        }

                        @Override
                        public void sendData(byte[] bytes) {
                            logger.i(TAG, "sent data");
                        }

                        @Override
                        public void onLostConnect() {
                            logger.i(TAG, "CONNECTION LOST");

                            logger.i(TAG, "ReCONNECTING");
                            connector.connectCom("dev/ttyS4", 115200);
                            logger.i(TAG, "Connection Status : " + connector.isConnected());
                        }
                    });

                } catch (Exception e) {
                    logger.i(TAG, "Exception " + e.getLocalizedMessage());
                    e.printStackTrace();
                }
            } else {
                logger.i(TAG, "CONNECTION FAILED");
                Toast.makeText(app, "NOT Connected", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(app, "Connection Failed", Toast.LENGTH_SHORT).show();
        }


        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                logger.i(TAG, "clicked on Inventory Button");
                if (connector.isConnected()) {
                    scanningStatus = true;
                    rfidReaderHelper.realTimeInventory(ReaderSetting.newInstance().btReadId, (byte) 0x01);
//                    rfidReaderHelper.realTimeInventory(ReaderSetting.newInstance().btReadId, CMD.REAL_TIME_INVENTORY);
                } else {
                    generateDummyData();
                }


            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logger.i(TAG, "clicked on Stop Button==========");
                scanningStatus = false;

                stopDummyData();
            }
        });

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logger.i(TAG, "clicked on Refresh Button");
                viewModel.refresh();

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

        viewModel.getAllInventory().observe(this, new Observer<List<Inventory>>() {
            @Override
            public void onChanged(List<Inventory> inventories) {
                inventoryList = inventories;
                adapter = new InventoryAdapter(RFIDActivity.this, inventories);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);

                layoutManager.smoothScrollToPosition(recyclerView, null, adapter.getItemCount());

                tvInventoryCount.setText("" + adapter.getItemCount());
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mVirtualKeyListenerBroadcastReceiver = new VirtualKeyListenerBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        this.registerReceiver(mVirtualKeyListenerBroadcastReceiver, intentFilter);
        if (mSwitchFlag) {
            ModuleManager.newInstance().setUHFStatus(true);
        }
    }

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

    @Override
    protected void onDestroy() {
        super.onDestroy();

        this.unregisterReceiver(mVirtualKeyListenerBroadcastReceiver);
        stopDummyData();
    }

    private void generateDummyData() {
        Toast.makeText(app, "Started Dummy Data", Toast.LENGTH_SHORT).show();
        stopDummyData();
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                Inventory inventory = new Inventory();
                inventory.setEpc("" + UUID.randomUUID().toString());
                viewModel.insert(inventory);
            }
        };
        timer.schedule(timerTask, 2000, 10000);
    }

    private void stopDummyData() {
        if (timer != null)
            timer.cancel();
        if (timerTask != null)
            timerTask.cancel();
    }
}