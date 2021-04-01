package in.mobiux.android.orca50scanner.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.nativec.tools.ModuleManager;
import com.rfid.rxobserver.ReaderSetting;
import com.rfid.rxobserver.bean.RXInventoryTag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import in.mobiux.android.orca50scanner.R;
import in.mobiux.android.orca50scanner.api.model.DepartmentResponse;
import in.mobiux.android.orca50scanner.api.model.Inventory;
import in.mobiux.android.orca50scanner.api.model.Laboratory;
import in.mobiux.android.orca50scanner.database.InventoryRepository;
import in.mobiux.android.orca50scanner.util.AppUtils;
import in.mobiux.android.orca50scanner.util.RFIDReaderListener;
import in.mobiux.android.orca50scanner.viewmodel.InventoryViewModel;
import in.mobiux.android.orca50scanner.viewmodel.LaboratoryViewModel;

public class TransferAndAssignActivity extends BaseActivity implements RFIDReaderListener {

    private Spinner spinnerLevel, spinnerLab;
    private ArrayAdapter<DepartmentResponse> levelAdapter;
    private ArrayAdapter<DepartmentResponse.Child> labAdapter;

    private List<DepartmentResponse> responses = new ArrayList<>();
    private DepartmentResponse selectedLevel;
    private DepartmentResponse.Child selectedLab;
    private TextView tvLab, tvLabName, tvSerialNumber, tvName, tvEPC, tvRSSI;
    private Button btnStart, btnSave;

    private LaboratoryViewModel laboratoryViewModel;
    private Inventory asset;
    private Inventory selectedAsset;
    private InventoryViewModel inventoryViewModel;
    private List<Inventory> inventories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_and_assign);

//        getSupportActionBar().setTitle("ASSET INVENTORY");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        spinnerLevel = findViewById(R.id.spinnerLevel);
        spinnerLab = findViewById(R.id.spinnerLab);
        tvLab = findViewById(R.id.tvLab);
        tvLabName = findViewById(R.id.tvLabName);
        tvSerialNumber = findViewById(R.id.tvSerialNumber);
        tvName = findViewById(R.id.tvName);
        tvEPC = findViewById(R.id.tvEPC);
        tvRSSI = findViewById(R.id.tvRSSI);
        btnStart = findViewById(R.id.btnStart);
        btnSave = findViewById(R.id.btnSave);
        btnStart.setTag(false);

        tvLab.setText("");
        tvSerialNumber.setText("");
        tvLabName.setText("");
        tvEPC.setText("");
        tvName.setText("");
        tvRSSI.setText("");

        laboratoryViewModel = new ViewModelProvider(this).get(LaboratoryViewModel.class);
        inventoryViewModel = new ViewModelProvider(this).get(InventoryViewModel.class);

        if (!app.connector.isConnected()) {
            app.connectRFID();
        }

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnStart.setTag(!(boolean) btnStart.getTag());

                if ((boolean) btnStart.getTag()) {
                    if (app.connector.isConnected()) {
                        ModuleManager.newInstance().setScanStatus(true);
                        btnStart.setTag(true);
                        btnStart.setText("Stop");
                        app.scanningStatus = true;
                        app.rfidReaderHelper.realTimeInventory(ReaderSetting.newInstance().btReadId, (byte) 0x01);
                    } else {
                        app.reconnectRFID();
                        btnStart.setTag(false);
                    }
                } else {
                    app.scanningStatus = false;
                    btnStart.setTag(false);
                    btnStart.setText("Scan Asset");
                }
            }
        });

        laboratoryViewModel.getAllInventory().observe(this, new Observer<List<Laboratory>>() {
            @Override
            public void onChanged(List<Laboratory> list) {
                responses.clear();
                HashMap<Integer, DepartmentResponse> levels = new HashMap<Integer, DepartmentResponse>();
                HashSet<String> hashSet = new HashSet<>();

                for (Laboratory laboratory : list) {
                    DepartmentResponse d = new DepartmentResponse();
                    d.setId(laboratory.getLevelId());
                    d.setName(laboratory.getLevelName());
                    d.setChild(new ArrayList<>());
                    if (levels.get(laboratory.getLevelId()) == null) {
                        levels.put(laboratory.getLevelId(), d);
                        responses.add(d);
                    }

                    DepartmentResponse.Child child = new DepartmentResponse.Child();
                    child.setId(laboratory.getLabId());
                    child.setName(laboratory.getLabName());
                    levels.get(laboratory.getLevelId()).getChild().add(child);
                }

                levelAdapter = new ArrayAdapter<DepartmentResponse>(TransferAndAssignActivity.this, android.R.layout.simple_spinner_item, responses);
                levelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerLevel.setAdapter(levelAdapter);
            }
        });

        inventoryViewModel.getAllInventory().observe(this, new Observer<List<Inventory>>() {
            @Override
            public void onChanged(List<Inventory> list) {
                inventories = list;
                logger.i(TAG, "list update");

                updateUI(selectedAsset);
            }
        });

        spinnerLevel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if (responses.size() > 0) {
                    selectedLevel = responses.get(position);
                    labAdapter = new ArrayAdapter<DepartmentResponse.Child>(TransferAndAssignActivity.this, android.R.layout.simple_spinner_item, responses.get(position).getChild());
                    labAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerLab.setAdapter(labAdapter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spinnerLab.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if (selectedLevel != null && selectedLevel.getChild().size() > 0) {
                    selectedLab = selectedLevel.getChild().get(position);

                    tvLab.setText("" + selectedLab.getName());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedAsset != null) {
                    selectedAsset.setLabId(selectedLab.getId());
                    selectedAsset.setLaboratoryName(selectedLab.getName());
                    selectedAsset.setLocationAssigned(true);
                    selectedAsset.setSyncRequired(true);

                    inventoryViewModel.update(selectedAsset);
                    logger.i(TAG, "Asset data saved ");

                } else {
                    logger.e(TAG, "Asset not found");
                    Toast.makeText(app, "Asset not found", Toast.LENGTH_SHORT).show();
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
        if (asset == null) {
            asset = inventory;
        }

        if (Integer.parseInt(inventory.getRssi()) > Integer.parseInt(asset.getRssi())) {
            asset = inventory;
            selectedAsset = AppUtils.getMatchingInventory(asset.getEpc(), inventories);
        }

        updateUI(selectedAsset);
    }

    private void updateUI(Inventory selectedAsset) {
        if (selectedAsset != null) {
            tvName.setText("" + selectedAsset.getName());
            tvEPC.setText("" + selectedAsset.getEpc());
            tvRSSI.setText("" + selectedAsset.getRssi());

            tvLabName.setText("" + selectedAsset.getLaboratoryName());
            tvSerialNumber.setText("" + selectedAsset.getInventoryId());
        }
    }

    @Override
    public void onScanningStatus(boolean status) {

        btnStart.setTag(status);

        if (status) {
            btnStart.setText("Scan Asset");
        } else {
            btnStart.setText("Stop");
        }
    }

    @Override
    public void onInventoryTagEnd(RXInventoryTag.RXInventoryTagEnd tagEnd) {
        asset = null;
    }

    @Override
    public void onConnection(boolean status) {

    }
}