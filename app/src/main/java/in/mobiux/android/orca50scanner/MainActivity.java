package in.mobiux.android.orca50scanner;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

import in.mobiux.android.orca50scanner.activity.BaseActivity;
import in.mobiux.android.orca50scanner.adapter.InventoryAdapter;
import in.mobiux.android.orca50scanner.api.model.Inventory;
import in.mobiux.android.orca50scanner.database.AppDatabase;
import in.mobiux.android.orca50scanner.util.RFIDReaderListener;
import in.mobiux.android.orca50scanner.viewmodel.InventoryViewModel;

public class MainActivity extends BaseActivity {

    private static String TAG = MainActivity.class.getName();

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


    //    Build connector
    public static ModuleConnector connector = new ReaderConnector();
    RFIDReaderHelper rfidReaderHelper;
//    ReaderHelper rfidReaderHelper;

    InventoryViewModel viewModel;

    //    move to repository
    RXObserver rxObserver = new RXObserver() {
        @Override
        protected void onInventoryTag(RXInventoryTag tag) {
//            super.onInventoryTag(tag);
            logger.i(TAG, "onInventoryTag method called ");

//                            Toast.makeText(getApplicationContext(), "Tag Scanned : " + tag.strEPC, Toast.LENGTH_SHORT).show();

            logger.i(TAG, "reader count : " + String.valueOf(tag.mReadCount));
            logger.i(TAG, "epc : " + String.valueOf(tag.strEPC));
            logger.i(TAG, "command : " + String.valueOf(tag.cmd));

            Inventory inventory = new Inventory();
            inventory.setEpc(tag.strEPC);
            inventory.setRssi(tag.strRSSI);
            viewModel.insert(inventory);
        }

        @Override
        protected void onExeCMDStatus(byte cmd, byte status) {
//            super.onExeCMDStatus(cmd, status);
            logger.i(TAG, "onExeCMDSTATUS method called " + String.valueOf(cmd));
        }

        @Override
        protected void onOperationTag(RXOperationTag tag) {
//            super.onOperationTag(tag);
            logger.i(TAG, "onOperationTag method called");
//                            Toast.makeText(getApplicationContext(), "OnOperationTag" + tag.strEPC, Toast.LENGTH_SHORT).show();
//            logger.i(TAG, "" + String.valueOf(tag.strData));
            logger.i(TAG, "" + String.valueOf(tag.strEPC));
            logger.i(TAG, "" + String.valueOf(tag.cmd));
        }
    };

    RXTXListener rxtxListener = new RXTXListener() {
        @Override
        public void reciveData(byte[] bytes) {
            logger.i(TAG, "data received");
        }

        @Override
        public void sendData(byte[] bytes) {
            logger.i(TAG, "data sent");
        }

        @Override
        public void onLostConnect() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(app, "Connection Lost", Toast.LENGTH_SHORT).show();
                }
            });
            logger.i(TAG, "Connection has lost");
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        checkPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);

        viewModel = new ViewModelProvider(this).get(InventoryViewModel.class);


        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                logger.i(TAG, "clicked on Inventory Button");
                if (connector.isConnected()) {
                    Toast.makeText(app, "Already Connected", Toast.LENGTH_SHORT).show();
                    logger.i(TAG, "already connected");
                    ModuleManager.newInstance().setScanStatus(true);
                } else {
                    startRFID();
                }
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logger.i(TAG, "clicked on Stop Button==========");
                stopRFID();
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
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    exportToCSV(inventoryList);
                } else {
                    checkPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);
                }
            }
        });

        btnExportLogs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    logger.createAndExportLogs(MainActivity.this);
                } else {
                    checkPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);
                }
            }
        });


        viewModel.getAllInventory().observe(this, new Observer<List<Inventory>>() {
            @Override
            public void onChanged(List<Inventory> inventories) {
                inventoryList = inventories;
                adapter = new InventoryAdapter(MainActivity.this, inventories);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);

                layoutManager.smoothScrollToPosition(recyclerView, null, adapter.getItemCount());

                tvInventoryCount.setText("" + adapter.getItemCount());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRFID();

        if (rfidReaderHelper != null) {
            rfidReaderHelper.unRegisterObservers();
            rfidReaderHelper.signOut();
        }
    }


    void startRFID() {
        logger.i(TAG, "STARTING RFID Reader");
        initRFID();
    }

    void initRFID() {

        logger.i(TAG, "initializing RFID Reader");
        try {
            //Power on the UHF,must set the UHF can work.
//            boolean status = ModuleManager.newInstance().setUHFStatus(true);
//            logger.i(TAG, "UHF Status " + status);
            try {
                boolean uhfStatus = ModuleManager.newInstance().getUHFStatus();
                logger.i(TAG, "UHF Status " + uhfStatus);
                boolean scanStatus = ModuleManager.newInstance().getScanStatus();
                logger.i(TAG, "Scan Status " + uhfStatus);
            } catch (Exception e) {
                e.printStackTrace();
                logger.i(TAG, "Exception " + e.getLocalizedMessage());
            }

            if (connector == null) {
                connector = new ReaderConnector();
                logger.i(TAG, "created connector object");
            }


            logger.i(TAG, "Connecting to port : dev/ttyS4 and Boudrate at 115200");
            boolean connectStatus = connector.connectCom("dev/ttyS4", 115200);
            logger.i(TAG, "Connection Status : " + connectStatus);
            if (connectStatus) {
                try {
                    logger.i(TAG, "== RFID Reader : CONNECTED SUCCESSFULLY");
                    boolean uhfStatus = ModuleManager.newInstance().setUHFStatus(true);
                    logger.i(TAG, "UHF Status " + uhfStatus);
//                    ModuleManager.newInstance().setScanStatus(false);
                    rfidReaderHelper = RFIDReaderHelper.getDefaultHelper();

                    logger.i(TAG, "RFIDReaderHelper initialized success");
                    if (rfidReaderHelper == null) {
                        logger.i(TAG, "RFIDReaderHelper is null");
                    }
                    rfidReaderHelper.realTimeInventory((byte) 0xFF, (byte) 0x01);
                    rfidReaderHelper.realTimeInventory(ReaderSetting.newInstance().btReadId, (byte) 0x01);
                    rfidReaderHelper.setBeeperMode(ReaderSetting.newInstance().btReadId,CMD.SET_BEEPER_MODE);
//                    rfidReaderHelper.registerObserver(rxObserver);
                    rfidReaderHelper.registerObserver(new RXObserver() {
                        @Override
                        protected void onInventoryTag(RXInventoryTag tag) {
//                            super.onInventoryTag(tag);
                            logger.i(TAG, "onInventoryTag " + tag.strEPC);

                            Inventory inventory = new Inventory();
                            inventory.setEpc(tag.strEPC);
                            inventory.setRssi(tag.strRSSI);
                            viewModel.insert(inventory);
                            Toast.makeText(app, "Scanned Success " + tag.strEPC, Toast.LENGTH_SHORT).show();
                        }
                    });

                    logger.i(TAG, "RXObserver registered success");

                    if (connector.isConnected()) {
                        Toast.makeText(app, "Connected Successfully", Toast.LENGTH_SHORT).show();
                    }

//                    rfidReaderHelper.setRXTXListener(rxtxListener);
                    rfidReaderHelper.setRXTXListener(new RXTXListener() {
                        @Override
                        public void reciveData(byte[] bytes) {
                            logger.i(TAG, "reciveData");
                        }

                        @Override
                        public void sendData(byte[] bytes) {
                            logger.i(TAG, "sendData");
                        }

                        @Override
                        public void onLostConnect() {
                            logger.i(TAG, "onLostConnect");
                        }
                    });

                    logger.i(TAG, "rxtListener registered");

                } catch (Exception e) {
                    logger.i(TAG, "== RFID Reader : CONNECTION FAILED with exception " + e.getLocalizedMessage());
                    e.printStackTrace();
                }
            } else {

                logger.i(TAG, "== RFID Reader : CONNECTION FAILED " + connector.isConnected());
                snackbar.setAction("Ok", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startRFID();
                        snackbar.dismiss();
                    }
                });

                Toast.makeText(app, "Device Connection Failed", Toast.LENGTH_SHORT).show();
                generateDummyData();
                snackbar.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.i(TAG, "" + e.getLocalizedMessage());
        }
    }

    void stopRFID() {
        logger.i(TAG, "STOPPING RFID Reader");
        if (connector != null) {
            if (connector.isConnected())
                connector.disConnect();
            logger.i(TAG, "Connection Status " + connector.isConnected());
        }

//        boolean UHFStatus = ModuleManager.newInstance().setUHFStatus(false);
//
//        logger.i(TAG, "UHF Status " + UHFStatus);

//        boolean moduleManagerReleaseStatus = ModuleManager.newInstance().release();
//        logger.i(TAG, "Module Manager Release Status " + moduleManagerReleaseStatus);
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