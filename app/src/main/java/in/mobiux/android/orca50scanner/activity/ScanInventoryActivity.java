package in.mobiux.android.orca50scanner.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.nativec.tools.ModuleManager;
import com.rfid.rxobserver.ReaderSetting;
import com.rfid.rxobserver.bean.RXInventoryTag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import in.mobiux.android.orca50scanner.R;
import in.mobiux.android.orca50scanner.adapter.InventoryAdapter;
import in.mobiux.android.orca50scanner.api.model.AssetHistory;
import in.mobiux.android.orca50scanner.api.model.DepartmentResponse;
import in.mobiux.android.orca50scanner.api.model.Inventory;
import in.mobiux.android.orca50scanner.api.model.Laboratory;
import in.mobiux.android.orca50scanner.util.AppUtils;
import in.mobiux.android.orca50scanner.util.RFIDReaderListener;
import in.mobiux.android.orca50scanner.viewmodel.InventoryViewModel;

public class ScanInventoryActivity extends BaseActivity implements View.OnClickListener, RFIDReaderListener {

    private Button btnSave, btnClear;
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
        recyclerView = findViewById(R.id.recyclerView);
        tvCount.setText("");
        txtIndicator.setTag(false);
        txtIndicator.setText("");

        btnSave.setOnClickListener(this);
        btnClear.setOnClickListener(this);
        txtIndicator.setTag(startButtonStatus);

        laboratory = (DepartmentResponse.Child) getIntent().getSerializableExtra("laboratory");
        if (laboratory != null) {
            setTitle("You are in " + laboratory.getName());
            logger.i(TAG, "lab selected " + laboratory.getName());
        } else {
            Toast.makeText(app, "Lab not selected", Toast.LENGTH_SHORT).show();
            finish();
        }

        if (app.connector.isConnected()) {
            logger.i(TAG, "Connected");
            ModuleManager.newInstance().setUHFStatus(true);
        } else {
            app.connectRFID();
        }

        viewModel = new ViewModelProvider(this).get(InventoryViewModel.class);
        viewModel.getAllInventory().observe(this, new Observer<List<Inventory>>() {
            @Override
            public void onChanged(List<Inventory> list) {
                for (Inventory inventory : list) {
                    inventories.put(inventory.getEpc(), inventory);
                }

                logger.i(TAG, "list fetched" + inventories.size());
            }
        });

        adapter = new InventoryAdapter(ScanInventoryActivity.this, scannedInventories);
        recyclerView.setAdapter(adapter);
        tvCount.setText(adapter.getItemCount() + " PCS");
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
                tvCount.setText(adapter.getItemCount() + " PCS");
                break;
            case R.id.btnSave:
                logger.i(TAG, "Save");

                progressDialog = new ProgressDialog(ScanInventoryActivity.this);
                progressDialog.setMessage("Saving");
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
        }
    }

    @Override
    public void onInventoryTag(Inventory inventory) {

        Inventory matchingAsset = inventories.get(inventory.getFormattedEPC());
        if (matchingAsset != null) {
            matchingAsset.setRssi(inventory.getRssi());

            Inventory m = AppUtils.getMatchingInventory(inventory.getEpc(), scannedInventories);
            if (m != null) {
                logger.i(TAG, "existing in Scanned list " + m.getEpc());
                m.setRssi(inventory.getRssi());
            } else {
                scannedInventories.add(matchingAsset);
                logger.i(TAG, "added to scanned list " + inventory.getEpc());
            }

        } else {
            logger.i(TAG, "Scanned tag is not found in database " + inventory.getEpc());
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
            txtIndicator.setText("");
            txtIndicator.setTag(false);
        }
    }

    @Override
    public void onInventoryTagEnd(RXInventoryTag.RXInventoryTagEnd tagEnd) {
        logger.i(TAG, "" + tagEnd.mTagCount);
    }

    @Override
    public void onConnection(boolean status) {
        if (!status)
            app.reconnectRFID();
    }
}