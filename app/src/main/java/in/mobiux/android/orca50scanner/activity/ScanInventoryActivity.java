package in.mobiux.android.orca50scanner.activity;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.nativec.tools.ModuleManager;
import com.rfid.rxobserver.bean.RXInventoryTag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.mobiux.android.orca50scanner.BuildConfig;
import in.mobiux.android.orca50scanner.R;
import in.mobiux.android.orca50scanner.adapter.InventoryAdapter;
import in.mobiux.android.orca50scanner.api.model.AssetHistory;
import in.mobiux.android.orca50scanner.api.model.DepartmentResponse;
import in.mobiux.android.orca50scanner.api.model.Inventory;
import in.mobiux.android.orca50scanner.util.AppUtils;
import in.mobiux.android.orca50scanner.util.Common;
import in.mobiux.android.orca50scanner.util.PdfUtils;
import in.mobiux.android.orca50scanner.util.RFIDReaderListener;
import in.mobiux.android.orca50scanner.viewmodel.InventoryViewModel;

public class ScanInventoryActivity extends BaseActivity implements View.OnClickListener, RFIDReaderListener {

    private Button btnSave, btnClear, btnPrint;
    private TextView tvCount, txtIndicator;
    private RecyclerView recyclerView;
    private DepartmentResponse.Child laboratory;
    private List<Inventory> scannedInventories = new ArrayList<>();
    private Map<String, Inventory> inventories = new HashMap<>();
    private InventoryAdapter adapter;
    boolean startButtonStatus = false;

    private InventoryViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_inventory);

        setTitle("");

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
        btnClear.setOnClickListener(this);
        btnPrint.setOnClickListener(this);
//        btnPrint.setVisibility(View.GONE);
        txtIndicator.setTag(startButtonStatus);

        laboratory = (DepartmentResponse.Child) getIntent().getSerializableExtra("laboratory");
        if (laboratory != null) {
            setTitle(getResources().getString(R.string.label_you_are_in) + laboratory.getName());
            logger.i(TAG, "lab selected " + laboratory.getName() + "\t" + laboratory.getId());
        } else {
            logger.e(TAG, "Lab not selected");
            showToast(getResources().getString(R.string.lab_not_selected));
            finish();
        }

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

        viewModel = new ViewModelProvider(this).get(InventoryViewModel.class);
        viewModel.getAllInventory().observe(this, new Observer<List<Inventory>>() {
            @Override
            public void onChanged(List<Inventory> list) {
                for (Inventory inventory : list) {
                    logger.i(TAG, "lab id " + inventory.getLabId());
                    inventories.put(inventory.getEpc(), inventory);

                    if (laboratory.getId() == inventory.getLabId()) {
                        scannedInventories.add(inventory);
                    }
                }


//                scannedInventories.addAll(inventories.values());
                tvCount.setText(adapter.getItemCount() + " Pcs");
                adapter.notifyDataSetChanged();
                logger.i(TAG, "list fetched" + inventories.size());
            }
        });
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
                adapter.notifyDataSetChanged();
                tvCount.setText(adapter.getItemCount() + " Pcs");
                break;
            case R.id.btnSave:
                logger.i(TAG, "Save");

                progressDialog = new ProgressDialog(ScanInventoryActivity.this);
                progressDialog.setMessage(getResources().getString(R.string.saving));
                progressDialog.setIndeterminate(true);
                progressDialog.show();

                for (Inventory inventory : scannedInventories) {
                    inventory.setLabId(laboratory.getId());
                    inventory.setLaboratoryName(laboratory.getName());
                    inventory.setSyncRequired(true);
                    viewModel.update(inventory);

                    AssetHistory history = new AssetHistory();
                    history.setEpc(inventory.getFormattedEPC());
                    history.setDepartment(laboratory.getId());
                    viewModel.insertAssetHistory(history);
                }

                progressDialog.dismiss();
                finish();
                break;
            case R.id.btnPrint:
                logger.i(TAG, "print");

                checkPermission(ScanInventoryActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);

                PdfUtils pdfUtils = new PdfUtils(ScanInventoryActivity.this);
                pdfUtils.createPdfFile(PdfUtils.getPdfPath(ScanInventoryActivity.this));

                break;
        }
    }

    @Override
    public void onInventoryTag(Inventory inventory) {

        Inventory matchingAsset = inventories.get(inventory.getFormattedEPC());
        if (matchingAsset != null) {
            logger.i(TAG, "Matching found");
            matchingAsset.setRssi(inventory.getRssi());

            Inventory m = AppUtils.getMatchingInventory(inventory.getEpc(), scannedInventories);
            if (m != null) {
                logger.i(TAG, "existing in Scanned list " + m.getEpc());
                m.setRssi(inventory.getRssi());
                m.setScanStatus(true);
            } else {
                matchingAsset.setScanStatus(true);
                scannedInventories.add(matchingAsset);
                logger.i(TAG, "added to scanned list " + inventory.getEpc());
            }

        } else {
            logger.i(TAG, "Scanned tag is not found in database " + inventory.getEpc());
        }

        HashMap<String, Inventory> m = new HashMap<>();
        for (Inventory i : scannedInventories) {
            m.put(i.getFormattedEPC(), i);
        }

        scannedInventories.clear();
        for (Inventory i : m.values()) {
            if (i.isScanStatus()) {
                scannedInventories.add(0, i);
            } else {
                scannedInventories.add(i);
            }
        }

        adapter.notifyDataSetChanged();
        tvCount.setText(adapter.getItemCount() + " PCS");
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
    }

    @Override
    public void onConnection(boolean status) {
        if (!status)
            app.reconnectRFID();
    }
}