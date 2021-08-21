package in.mobiux.android.orca50scanner.activity;

import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.nativec.tools.ModuleManager;
import com.rfid.rxobserver.bean.RXInventoryTag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.mobiux.android.orca50scanner.BuildConfig;
import in.mobiux.android.orca50scanner.R;
import in.mobiux.android.orca50scanner.adapter.InventoryAdapter;
import in.mobiux.android.orca50scanner.api.model.AssetHistory;
import in.mobiux.android.orca50scanner.api.model.DepartmentResponse;
import in.mobiux.android.orca50scanner.api.model.Inventory;
import in.mobiux.android.orca50scanner.util.RFIDReaderListener;
import in.mobiux.android.orca50scanner.util.pdf.PdfUtils;
import in.mobiux.android.orca50scanner.viewmodel.InventoryViewModel;

public class ScanInventoryActivity extends BaseActivity implements View.OnClickListener, RFIDReaderListener {

    private Button btnSave, btnClear, btnPrint;
    private TextView tvCount, txtIndicator;
    private RecyclerView recyclerView;
    private DepartmentResponse.Child laboratory;
    private List<Inventory> scannedInventories = new ArrayList<>();
    private Map<String, Inventory> inventoriesMap = new HashMap<>();
    private InventoryAdapter adapter;
    boolean startButtonStatus = false;

    private InventoryViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_inventory);

        setTitle("Sensing Object");
        setHomeButtonEnable(false);

        tvCount = findViewById(R.id.tvCount);
        txtIndicator = findViewById(R.id.txtIndicator);
        btnSave = findViewById(R.id.btnSave);
        btnClear = findViewById(R.id.btnClear);
        btnPrint = findViewById(R.id.btnPrint);
        recyclerView = findViewById(R.id.recyclerView);
        tvCount.setText("");
        txtIndicator.setTag(false);
        txtIndicator.setText("");

        btnSave.setOnClickListener(this);
//        btnSave.setVisibility(View.GONE);
        btnClear.setOnClickListener(this);
        btnPrint.setOnClickListener(this);
        txtIndicator.setTag(startButtonStatus);


        if (BuildConfig.DEBUG) {

        } else {
            if (app.connector.isConnected()) {
                logger.i(TAG, "Connected");
                ModuleManager.newInstance().setUHFStatus(true);
            } else {
                app.connectRFID();
            }
        }

        adapter = new InventoryAdapter(ScanInventoryActivity.this, scannedInventories);
        recyclerView.setAdapter(adapter);
        tvCount.setText(adapter.getItemCount() + " Pcs");

    }

    @Override
    protected void onStart() {
        super.onStart();
        app.setOnRFIDListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btnClear:
                logger.i(TAG, "Clear");
                scannedInventories.clear();
                inventoriesMap.clear();
                adapter.notifyDataSetChanged();
                tvCount.setText(adapter.getItemCount() + " Pcs");
                break;
            case R.id.btnSave:
                logger.i(TAG, "Save");

                logger.createAndExportLogs(ScanInventoryActivity.this);
                break;
            case R.id.btnPrint:
                logger.i(TAG, "print");

                checkPermission(ScanInventoryActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);

                PdfUtils pdfUtils = new PdfUtils(ScanInventoryActivity.this);

                String title = "Sensing Object Rfid Reader";
                pdfUtils.createPdfFile(PdfUtils.getPdfPath(ScanInventoryActivity.this), scannedInventories, title);

                break;
        }
    }

    @Override
    public void onInventoryTag(Inventory inventory) {

        Inventory matchingAsset = inventoriesMap.get(inventory.getFormattedEPC());

        if (matchingAsset == null) {
            matchingAsset = inventory;
            inventoriesMap.put(matchingAsset.getEpc(), matchingAsset);
            scannedInventories.add(matchingAsset);
        }

        matchingAsset.setRssi(inventory.getRssi());
        matchingAsset.setScanStatus(true);

//        arrangeScannedList();
    }

    private void arrangeScannedList() {

        HashMap<String, Inventory> map = new HashMap<>();
        for (Inventory i : scannedInventories) {
            map.put(i.getFormattedEPC(), i);
        }

        scannedInventories.clear();
        for (Inventory i : map.values()) {
            if (i.isScanStatus()) {
                scannedInventories.add(0, i);
            } else {
                scannedInventories.add(i);
            }
        }
    }

    @Override
    public void onScanningStatus(boolean status) {
        if (status) {
            txtIndicator.setText(getResources().getString(R.string.scanning));
            txtIndicator.setTag(true);
        } else {
            txtIndicator.setText(getResources().getString(R.string.start_scan));
            txtIndicator.setTag(false);
        }
    }

    @Override
    public void onInventoryTagEnd(RXInventoryTag.RXInventoryTagEnd tagEnd) {
        logger.i(TAG, "Tag count " + tagEnd.mTagCount);

        tvCount.setText(adapter.getItemCount() + " PCS");
        adapter.notifyDataSetChanged();
        app.playBeep();
    }

    @Override
    public void onConnection(boolean status) {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        app.onTerminate();
    }
}