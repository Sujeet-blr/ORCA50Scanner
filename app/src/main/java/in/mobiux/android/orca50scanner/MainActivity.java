package in.mobiux.android.orca50scanner;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
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
import in.mobiux.android.orca50scanner.activity.InventoryDetailActivity;
import in.mobiux.android.orca50scanner.activity.RFIDActivity;
import in.mobiux.android.orca50scanner.adapter.InventoryAdapter;
import in.mobiux.android.orca50scanner.api.model.Inventory;
import in.mobiux.android.orca50scanner.database.AppDatabase;
import in.mobiux.android.orca50scanner.util.RFIDReaderListener;
import in.mobiux.android.orca50scanner.viewmodel.InventoryViewModel;

public class MainActivity extends BaseActivity implements RFIDReaderListener {

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

    InventoryViewModel viewModel;
    private RecyclerView.LayoutManager layoutManager;


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
        layoutManager = new LinearLayoutManager(MainActivity.this);

        viewModel = new ViewModelProvider(this).get(InventoryViewModel.class);

//        connecting to RFID Device
        app.connectRFID();

        adapter = new InventoryAdapter(MainActivity.this, inventoryList);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new InventoryAdapter.InventoryClickListener() {
            @Override
            public void onClick(Inventory inventory) {
                Intent intent = new Intent(MainActivity.this, InventoryDetailActivity.class);
                intent.putExtra("tag", inventory);
                app.scanningStatus = false;
                ModuleManager.newInstance().setUHFStatus(true);
                startActivity(intent);
            }
        });

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                app.scanningStatus = true;
                logger.i(TAG, "clicked on Inventory Button");
                if (app.connector.isConnected()) {
                    logger.i(TAG, "Already connected");
                    ModuleManager.newInstance().setScanStatus(true);
                    btnStart.setText("Scanning");
                    app.rfidReaderHelper.realTimeInventory(ReaderSetting.newInstance().btReadId, (byte) 0x01);
                } else {
                    app.reconnectRFID();
                }
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logger.i(TAG, "clicked on Stop Button==========");
                app.scanningStatus = false;
            }
        });

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logger.i(TAG, "clicked on Refresh Button");
//                viewModel.refresh();
                app.scanningStatus = false;
                if (!app.connector.isConnected()) {
                    app.reconnectRFID();
                }

                inventoryList.clear();
                adapter.notifyDataSetChanged();
                tvInventoryCount.setText("" + adapter.getItemCount());
                tvSpeed.setText("0");
                btnStart.setEnabled(true);
                btnStart.setText("Inventory");
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
    }

    @Override
    protected void onStart() {
        super.onStart();

        app.setOnRFIDListener(this);
    }

    @Override
    public void onInventoryTag(Inventory inventory) {
        boolean found = false;
        for (Inventory inv : inventoryList) {
            if (inv.getEpc().equals(inventory.getEpc())) {
                found = true;
            }
        }
        if (!found) {
            inventoryList.add(inventory);
            adapter.notifyDataSetChanged();
        }

        layoutManager.smoothScrollToPosition(recyclerView, null, adapter.getItemCount());
        tvInventoryCount.setText("" + adapter.getItemCount());
    }

    @Override
    public void onInventoryTagEnd(RXInventoryTag.RXInventoryTagEnd tagEnd) {
        tvSpeed.setText("" + tagEnd.mReadRate);
    }

    @Override
    public void onScanningStatus(boolean status) {
        if (status) {
            btnStart.setText("Scanning");
            btnStart.setEnabled(false);
        } else {
            btnStart.setText("Inventory");
            btnStart.setEnabled(true);
        }
    }

    @Override
    public void onConnection(boolean status) {

    }
}