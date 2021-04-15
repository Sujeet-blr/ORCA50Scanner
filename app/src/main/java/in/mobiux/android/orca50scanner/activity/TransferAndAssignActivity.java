package in.mobiux.android.orca50scanner.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.nativec.tools.ModuleManager;
import com.rfid.rxobserver.ReaderSetting;
import com.rfid.rxobserver.bean.RXInventoryTag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
    private RadioGroup radioGroup;
    private TextView tvLab, tvLabName, tvSerialNumber, tvName, tvEPC, tvRSSI;
    private Button btnStart, btnSave;

    private LaboratoryViewModel laboratoryViewModel;
    private Inventory asset;
    private Inventory selectedAsset;
    private InventoryViewModel inventoryViewModel;
    private List<Inventory> inventories = new ArrayList<>();
    private List<Inventory> scannedTagsList = new ArrayList<>();
    private Map<String, Inventory> scannedTags = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_and_assign);

        setTitle(getResources().getString(R.string.label_transfer_assign));

        radioGroup = findViewById(R.id.radioGroup);
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
//                        ModuleManager.newInstance().setScanStatus(true);
                        btnStart.setText(getResources().getString(R.string.stop_scan));
//                        app.rfidReaderHelper.realTimeInventory(ReaderSetting.newInstance().btReadId, (byte) 0x01);
                        app.startScanning(TAG);
                    } else {
                        app.reconnectRFID();
                        btnStart.setTag(false);
                    }
                } else {
//                    app.scanningStatus = false;
//                    btnStart.setTag(false);
                    btnStart.setText(getResources().getString(R.string.start_scan));
                    app.stopScanning();
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

                    showSuccessDialog("Asset " + selectedAsset.getName() + "\nmoved to " + selectedLab.getName() + " successfully");

                } else {
                    logger.e(TAG, "Asset not selected");
                    Toast.makeText(app, "Asset not selected", Toast.LENGTH_SHORT).show();
                }
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int position) {
                if (scannedTagsList.size() > 0)
                    selectedAsset = scannedTagsList.get(position);
                updateUI(selectedAsset);
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
//        if (asset == null) {
//            asset = inventory;
//        }

        scannedTags.put(inventory.getFormattedEPC(), inventory);

        Inventory matchingAsset = AppUtils.getMatchingInventory(inventory.getFormattedEPC(), inventories);
        if (matchingAsset != null) {
            matchingAsset.setRssi(inventory.getRssi());
            scannedTags.put(matchingAsset.getFormattedEPC(), matchingAsset);
            if (selectedAsset != null) {
                selectedAsset = scannedTags.get(selectedAsset.getFormattedEPC());
            }
        }


//        if (Integer.parseInt(inventory.getRssi()) > Integer.parseInt(asset.getRssi())) {
//            asset = inventory;
//            selectedAsset = AppUtils.getMatchingInventory(asset.getEpc(), inventories);
//            selectedAsset.setRssi(inventory.getRssi());
//        }

//        scannedTagsList.add(inventory);

        updateUI(selectedAsset);
    }

    @Override
    public void onScanningStatus(boolean status) {

        btnStart.setTag(status);

        if (status) {
            btnStart.setText(getResources().getString(R.string.start_scan));
        } else {
            btnStart.setText(getResources().getString(R.string.stop_scan));
        }
    }


    @Override
    public void onInventoryTagEnd(RXInventoryTag.RXInventoryTagEnd tagEnd) {
//        asset = null;

        arrangeScannedInventory();
    }

    @Override
    public void onConnection(boolean status) {

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

    private void showSuccessDialog(String message) {

        AlertDialog.Builder builder = new MaterialAlertDialogBuilder(this);
        builder.setMessage(message);

        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                finish();
            }
        });

        builder.show();
    }

    private List<Inventory> arrangeScannedInventory() {

        scannedTagsList.clear();
        scannedTagsList.addAll(scannedTags.values());

        Collections.sort(scannedTagsList, new Comparator<Inventory>() {
            @Override
            public int compare(Inventory inventory, Inventory t1) {
                return (Integer.parseInt(inventory.getRssi()) - Integer.parseInt(t1.getRssi()));
//                return 0;
            }
        });

        Collections.reverse(scannedTagsList);

        if (scannedTagsList.size() > 3) {
            for (int i = 3; i < scannedTagsList.size(); i++) {
                scannedTagsList.remove(i);
            }
        }

        updateScannedListViews();
        return scannedTagsList;
    }

    private void updateScannedListViews() {
        radioGroup.removeAllViews();
        selectedAsset = null;
        for (int i = 0; i < scannedTagsList.size(); i++) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setId(i);
            radioButton.setText("" + scannedTagsList.get(i).getName());
            radioGroup.addView(radioButton);
        }
        radioGroup.invalidate();
    }
}