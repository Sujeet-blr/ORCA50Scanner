package in.mobiux.android.orca50scanner.sgul.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.mobiux.android.orca50scanner.sgul.R;
import in.mobiux.android.orca50scanner.sgul.adapter.InventoryAdapter;
import in.mobiux.android.orca50scanner.sgul.adapter.InventoryAlertAdapter;
import in.mobiux.android.orca50scanner.sgul.api.model.AssetHistory;
import in.mobiux.android.orca50scanner.sgul.api.model.DepartmentResponse;
import in.mobiux.android.orca50scanner.sgul.api.model.Inventory;
import in.mobiux.android.orca50scanner.sgul.util.AppUtils;
import in.mobiux.android.orca50scanner.sgul.util.pdf.PdfUtils;
import in.mobiux.android.orca50scanner.sgul.viewmodel.InventoryViewModel;

public class ScanInventoryActivity extends BaseActivity implements View.OnClickListener {

    private Button btnSave, btnClear, btnPrint;
    private TextView tvCount, txtIndicator;
    private RecyclerView recyclerView;
    private DepartmentResponse.Child laboratory;
    private List<Inventory> inventoryList = new ArrayList<>();
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
        btnPrint.setVisibility(View.GONE);
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


        adapter = new InventoryAdapter(ScanInventoryActivity.this, inventoryList);
        recyclerView.setAdapter(adapter);
        tvCount.setText(adapter.getItemCount() + " Pcs");

        viewModel = new ViewModelProvider(this).get(InventoryViewModel.class);
        viewModel.getAllInventory().observe(this, new Observer<List<Inventory>>() {
            @Override
            public void onChanged(List<Inventory> list) {
                for (Inventory inventory : list) {
                    logger.i(TAG, "lab id " + inventory.getLabId());
//                    inventories.put(inventory.getEpc(), inventory);
                    inventories.put(inventory.getFormattedEPC(), inventory);

                    Inventory matching = AppUtils.getMatchingInventory(inventory.getFormattedEPC(), inventoryList);

                    if (laboratory.getId() == inventory.getLabId() && matching == null) {
                        inventoryList.add(inventory);
                    }
                }

                arrangeScannedList();
                tvCount.setText(adapter.getItemCount() + " Pcs");
                adapter.notifyDataSetChanged();
                logger.i(TAG, "list fetched" + inventories.size());
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btnClear:
                logger.i(TAG, "Clear");
                inventoryList.clear();
                adapter.notifyDataSetChanged();
                tvCount.setText(adapter.getItemCount() + " Pcs");
                break;
            case R.id.btnSave:
                logger.i(TAG, "Save");

                List<Inventory> extraAssets = new ArrayList<>();
                for (Inventory inv : inventoryList) {
                    if (inv.getLabId() != laboratory.getId()) {
                        inv.setChecked(false);
                        extraAssets.add(inv);
                    }
                }

                if (extraAssets.size() > 0) {
                    showAlertDialogWithDifference(extraAssets);
                } else {
                    saveAndExit();
                }

                break;
            case R.id.btnPrint:
                logger.i(TAG, "print");

                checkPermission(ScanInventoryActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);

                PdfUtils pdfUtils = new PdfUtils(ScanInventoryActivity.this);

                String title = "Department : " + laboratory.getName();
                pdfUtils.createPdfFile(PdfUtils.getPdfPath(ScanInventoryActivity.this), inventoryList, title);

                break;
        }
    }

    @Override
    public void onInventoryTag(in.mobiux.android.orca50scanner.reader.model.Inventory inventory) {

        Inventory matchingAsset = inventories.get(inventory.getFormattedEPC());
        if (matchingAsset != null) {
            logger.i(TAG, "Matching found");
            matchingAsset.setRssi(inventory.getRssi());

            Inventory m = AppUtils.getMatchingInventory(inventory.getEpc(), inventoryList);
            if (m != null) {
                logger.i(TAG, "existing in Scanned list " + m.getEpc());
                m.setRssi(inventory.getRssi());
                m.setScanStatus(true);
            } else {
                matchingAsset.setScanStatus(true);
                inventoryList.add(matchingAsset);
                logger.i(TAG, "added to scanned list " + inventory.getEpc());
            }

        } else {
            logger.i(TAG, "Scanned tag is not found in database " + inventory.getEpc());
        }

        arrangeScannedList();

        adapter.notifyDataSetChanged();
        tvCount.setText(adapter.getItemCount() + " PCS");
    }

    private void arrangeScannedList() {

        HashMap<String, Inventory> map = new HashMap<>();
        for (Inventory i : inventoryList) {
            map.put(i.getFormattedEPC(), i);
        }

        inventoryList.clear();
        for (Inventory i : map.values()) {
            if (i.isScanStatus()) {
                inventoryList.add(0, i);
            } else {
                inventoryList.add(i);
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
    public void onInventoryTagEnd(in.mobiux.android.orca50scanner.reader.model.Inventory.InventoryTagEnd tagEnd) {
//        super.onInventoryTagEnd(tagEnd);
        logger.i(TAG, "Tag Count " + tagEnd.mTagCount);
    }

    private void showAlertDialogWithDifference(List<Inventory> scannedList) {

        AlertDialog.Builder builder = new AlertDialog.Builder(ScanInventoryActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_list, null);
        builder.setView(dialogView);
        builder.setTitle("Choose if these assets belongs to " + laboratory.getName() + " ?");
        builder.setMessage(" ");


        CheckBox checkboxAll = dialogView.findViewById(R.id.checkboxAll);
        RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        InventoryAlertAdapter alertAdapter = new InventoryAlertAdapter(getApplicationContext(), scannedList);
        recyclerView.setAdapter(alertAdapter);
        alertAdapter.setOnItemClickListener(new InventoryAlertAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, Inventory a) {

            }
        });

        checkboxAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                alertAdapter.notifyDataSetChanged();
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                for (Inventory in : scannedList) {
                    if (in.isChecked()) {
                        in.setLabId(laboratory.getId());
                        in.setLaboratoryName(laboratory.getName());
                    }
                }

                saveAndExit();
                dialogInterface.dismiss();

            }
        });


//        builder.setNegativeButton("no", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                dialogInterface.dismiss();
//            }
//        });


        builder.show();
    }

    private void saveAndExit() {
        progressDialog = new ProgressDialog(ScanInventoryActivity.this);
        progressDialog.setMessage(getResources().getString(R.string.saving));
        progressDialog.setIndeterminate(true);
        progressDialog.show();

        for (Inventory inventory : inventoryList) {

            if (inventory.isChecked()) {
                inventory.setLabId(laboratory.getId());
                inventory.setLaboratoryName(laboratory.getName());
                inventory.setChecked(false);
                if (inventory.isScanStatus()) {
                    inventory.setSyncRequired(true);
                }

                viewModel.update(inventory);

                if (inventory.isScanStatus()) {
                    AssetHistory history = new AssetHistory();
                    history.setEpc(inventory.getFormattedEPC());
                    history.setDepartment(laboratory.getId());
                    viewModel.insertAssetHistory(history);
                }
            }
        }

        progressDialog.dismiss();
        finish();
    }
}