package in.mobiux.android.orca50scanner.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.rfid.rxobserver.bean.RXInventoryTag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import in.mobiux.android.orca50scanner.R;
import in.mobiux.android.orca50scanner.api.model.AssetHistory;
import in.mobiux.android.orca50scanner.api.model.DepartmentResponse;
import in.mobiux.android.orca50scanner.api.model.Inventory;
import in.mobiux.android.orca50scanner.api.model.Laboratory;
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
    private TextView tvLab, tvLabName, tvSerialNumber, tvName, tvEPC, tvRSSI, txtIndicator;
    private Button btnSave;
    private CardView cardAssetInfo;

    private LaboratoryViewModel laboratoryViewModel;
    private Inventory asset;
    private Inventory selectedAsset;
    private InventoryViewModel inventoryViewModel;
    private HashMap<String, Inventory> assets = new HashMap<>();
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
        txtIndicator = findViewById(R.id.txtIndicator);
        btnSave = findViewById(R.id.btnSave);
        cardAssetInfo = findViewById(R.id.cardAssetInfo);
        cardAssetInfo.setVisibility(View.GONE);
        txtIndicator.setTag(false);
        txtIndicator.setText(getResources().getString(R.string.start_scan));

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
                for (Inventory inventory : list) {
                    assets.put(inventory.getFormattedEPC(), inventory);
                }
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

                    AssetHistory history = new AssetHistory();
                    history.setEpc(selectedAsset.getFormattedEPC());
                    history.setDepartment(selectedLab.getId());
                    inventoryViewModel.insertAssetHistory(history);

                    showSuccessDialog("Asset " + selectedAsset.getName() + "\nmoved to " + selectedLab.getName() + " successfully");

                } else {
                    logger.e(TAG, "Asset not selected");
                    showToast(getResources().getString(R.string.asset_not_selected));
                }
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int position) {
                if (scannedTagsList.size() > 0) {
                    selectedAsset = scannedTagsList.get(position);
                    cardAssetInfo.setVisibility(View.VISIBLE);
                }
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

        Inventory matchingAsset = assets.get(inventory.getFormattedEPC());

        if (matchingAsset != null) {
            scannedTags.put(inventory.getFormattedEPC(), inventory);
            logger.i(TAG, "matching found " + matchingAsset.getFormattedEPC());
            matchingAsset.setRssi(inventory.getRssi());
            scannedTags.put(matchingAsset.getFormattedEPC(), matchingAsset);
            if (selectedAsset != null) {
                selectedAsset = scannedTags.get(selectedAsset.getFormattedEPC());
            }

            arrangeScannedInventory();
        }
    }

    @Override
    public void onScanningStatus(boolean status) {

        txtIndicator.setTag(status);

        if (status) {
            txtIndicator.setText(getResources().getString(R.string.scanning));
            radioGroup.setEnabled(false);
        } else {
            txtIndicator.setText(R.string.start_scan);
        }
    }


    @Override
    public void onInventoryTagEnd(RXInventoryTag.RXInventoryTagEnd tagEnd) {
//        arrangeScannedInventory();
        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                radioGroup.setEnabled(true);
            }
        }, 500);
    }

    @Override
    public void onConnection(boolean status) {

    }

    private void updateUI(Inventory selectedAsset) {
        if (selectedAsset != null) {
            tvName.setText("" + selectedAsset.getName());
            tvEPC.setText("" + selectedAsset.getEpc());
            tvRSSI.setText("" + selectedAsset.getRssi() + getResources().getString(R.string.rssi_unit));

            tvLabName.setText("" + selectedAsset.getLaboratoryName());
            tvSerialNumber.setText("" + selectedAsset.getInventoryId());
        }
    }

    private void showSuccessDialog(String message) {

        AlertDialog.Builder builder = new MaterialAlertDialogBuilder(this);
        builder.setMessage(message);

        builder.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
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
                logger.i(TAG, "removing " + i);
            }
        }

        logger.i(TAG, "scannedTagsSize " + scannedTagsList.size());
        updateScannedListViews(scannedTagsList);
        return scannedTagsList;
    }

    private void updateScannedListViews(List<Inventory> scannedTagsList) {
        radioGroup.removeAllViews();
        logger.i(TAG, "update scanned list view " + scannedTagsList.size());
        for (int i = 0; i < 3 && i < scannedTagsList.size(); i++) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setId(i);
            radioButton.setText("" + scannedTagsList.get(i).getName());
            radioGroup.addView(radioButton, i);
        }
        radioGroup.invalidate();
        selectedAsset = null;
        updateUI(selectedAsset);
    }
}