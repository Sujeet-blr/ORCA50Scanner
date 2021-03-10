package in.mobiux.android.orca50scanner;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.module.interaction.ModuleConnector;
import com.nativec.tools.ModuleManager;
import com.rfid.RFIDReaderHelper;
import com.rfid.ReaderConnector;
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
import in.mobiux.android.orca50scanner.viewmodel.InventoryViewModel;

public class MainActivity extends BaseActivity {

    private static String TAG = MainActivity.class.getName();

    private View parentLayout;
    private Snackbar snackbar;
    Button btnStart, btnStop, btnRefresh, btnExport;
    TextView tvInventoryCount, tvSpeed;

    private List<Inventory> inventoryList = new ArrayList<>();

    private AppDatabase db;
    private MyApplication app;

    private RecyclerView recyclerView;
    InventoryAdapter adapter;
    RecyclerView.LayoutManager layoutManager;

    Timer timer = new Timer();
    TimerTask timerTask;


    //    Build connecter
    ModuleConnector readerConnector = new ReaderConnector();
//    ModuleConnector scannerConnector = new ODScannerConnector();

    RFIDReaderHelper rfidReaderHelper;
//    ODScannerHelper odScannerHelper;

    InventoryViewModel viewModel;

    //    move to repository
    RXObserver rxObserver = new RXObserver() {
        @Override
        protected void onInventoryTag(RXInventoryTag tag) {
            super.onInventoryTag(tag);

            Toast.makeText(getApplicationContext(), "Tag Scanned : " + tag.strEPC, Toast.LENGTH_SHORT).show();
            Log.d(TAG, "" + tag.mReadCount);
            Log.d(TAG, "" + tag.strEPC);
            Log.d(TAG, "" + tag.cmd);

            Inventory inventory = new Inventory();
            inventory.setEpc(tag.strEPC);
            inventory.setRssi(tag.strRSSI);
            viewModel.insert(inventory);
        }

        @Override
        protected void onExeCMDStatus(byte cmd, byte status) {
            super.onExeCMDStatus(cmd, status);
        }

        @Override
        protected void refreshSetting(ReaderSetting readerSetting) {
            super.refreshSetting(readerSetting);
            Toast.makeText(getApplicationContext(), "Setting Refresh", Toast.LENGTH_SHORT).show();

        }

        @Override
        protected void onOperationTag(RXOperationTag tag) {
            super.onOperationTag(tag);

            Toast.makeText(getApplicationContext(), "OnOperationTag" + tag.strEPC, Toast.LENGTH_SHORT).show();
            Log.d(TAG, "" + tag.strData);
            Log.d(TAG, "" + tag.strEPC);
            Log.d(TAG, "" + tag.cmd);
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

        snackbar = Snackbar.make(parentLayout, "RFID-Device is not connected or PORT is not open, Please Check & Try Again", Snackbar.LENGTH_INDEFINITE);
        checkPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);

        viewModel = new ViewModelProvider(this).get(InventoryViewModel.class);


        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRFID();
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopRFID();
            }
        });

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewModel.refresh();
            }
        });


        btnExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    exportToCSV(inventoryList);
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


    void startRFID() {
        initRFID();
    }

    void stopRFID() {
        if (readerConnector != null) {
            readerConnector.disConnect();
        }

        ModuleManager.newInstance().setScanStatus(false);
        ModuleManager.newInstance().setUHFStatus(false);

        ModuleManager.newInstance().release();
        stopDummyData();
    }

    void initRFID() {

        //Power on the UHF,must set the UHF can work.
        ModuleManager.newInstance().setUHFStatus(true);
        //Must set the flag that the UHF is running,as it will effect 1D scanner when UHF is running.
//        mScanner.setRunFlag(true);
        //Power off the 1D Scanner,the 1D scanner will not work.
        ModuleManager.newInstance().setScanStatus(false);

        if (readerConnector.connectCom("dev/ttyS4", 115200)) {
            try {
                rfidReaderHelper = RFIDReaderHelper.getDefaultHelper();
                rfidReaderHelper.registerObserver(rxObserver);

                Toast.makeText(app, "Connected Successfully", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {

            snackbar.setAction("Ok", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startRFID();
                    snackbar.dismiss();
                }
            });

            Toast.makeText(app, "Device Connection Failed", Toast.LENGTH_SHORT).show();
            startDummyData();
            snackbar.show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopRFID();
    }

    private void startDummyData() {
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
        timer.schedule(timerTask, 2000, 5000);
    }

    private void stopDummyData() {
        if (timer != null)
            timer.cancel();
        if (timerTask != null)
            timerTask.cancel();
    }
}