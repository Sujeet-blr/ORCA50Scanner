package in.mobiux.android.orca50scanner.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.module.interaction.ModuleConnector;
import com.nativec.tools.ModuleManager;
import com.rfid.rxobserver.bean.RXInventoryTag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import in.mobiux.android.orca50scanner.BuildConfig;
import in.mobiux.android.orca50scanner.R;
import in.mobiux.android.orca50scanner.api.model.DepartmentResponse;
import in.mobiux.android.orca50scanner.api.model.Inventory;
import in.mobiux.android.orca50scanner.api.model.Laboratory;
import in.mobiux.android.orca50scanner.util.RFIDReaderListener;
import in.mobiux.android.orca50scanner.viewmodel.InventoryViewModel;
import in.mobiux.android.orca50scanner.viewmodel.LaboratoryViewModel;

public class LocateAssetActivity extends BaseActivity implements RFIDReaderListener {

    private Spinner spinner;
    private TextView tvRSSIValue, txtIndicator;
    private ArrayAdapter<Inventory> arrayAdapter;
    private List<Inventory> inventories = new ArrayList<>();
    private List<Inventory> assetsList = new ArrayList<>();
    private InventoryViewModel viewModel;
    private Inventory inventory = new Inventory();
    private Inventory selectedAsset = new Inventory();
    boolean buttonStatus = false;

    private Timer timer = null;

    private SeekBar seekBar;
    ModuleConnector connector;

    private Spinner spinnerLevel, spinnerLab;
    private ArrayAdapter<DepartmentResponse> levelAdapter;
    private ArrayAdapter<DepartmentResponse.Child> labAdapter;
    private LaboratoryViewModel laboratoryViewModel;
    private List<DepartmentResponse> responses = new ArrayList<>();
    private DepartmentResponse selectedLevel;
    private DepartmentResponse.Child selectedLab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locate_asset);

        setTitle(getResources().getString(R.string.label_locate_asset));

        spinner = findViewById(R.id.spinner);
        tvRSSIValue = findViewById(R.id.tvRSSIValue);
        seekBar = findViewById(R.id.seekBar);
        spinnerLevel = findViewById(R.id.spinnerLevel);
        spinnerLab = findViewById(R.id.spinnerLab);
        txtIndicator = findViewById(R.id.txtIndicator);
        txtIndicator.setTag(false);
        txtIndicator.setText("");

        connector = app.connector;

        Inventory asset = (Inventory) getIntent().getSerializableExtra("asset");
        laboratoryViewModel = new ViewModelProvider(this).get(LaboratoryViewModel.class);
        viewModel = new ViewModelProvider(this).get(InventoryViewModel.class);

        if (asset != null) {
            inventory = asset;
        }

        if (BuildConfig.DEBUG) {

        } else {
            if (!connector.isConnected()) {
                app.connectRFID();
                ModuleManager.newInstance().setUHFStatus(true);
            } else {
                ModuleManager.newInstance().setUHFStatus(true);
                logger.i(TAG, "connected");
            }
        }

        viewModel.getAllInventory().observe(this, new Observer<List<Inventory>>() {
            @Override
            public void onChanged(List<Inventory> list) {
                assetsList = list;
                inventories.clear();
                inventories.addAll(list);
                arrayAdapter = new ArrayAdapter<Inventory>(LocateAssetActivity.this, android.R.layout.simple_spinner_item, inventories);
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(arrayAdapter);
                arrayAdapter.notifyDataSetChanged();

//                if (asset != null) {
//                    for (Inventory i : inventories) {
//                        if (i.getFormattedEPC().equals(asset.getFormattedEPC())) {
//                            inventory = i;
//                            spinner.setSelection(inventories.indexOf(inventory));
//                        }
//                    }
//                }
            }
        });

        laboratoryViewModel.getAllInventory().observe(this, new Observer<List<Laboratory>>() {
            @Override
            public void onChanged(List<Laboratory> list) {
                responses.clear();
                HashMap<Integer, DepartmentResponse> levels = new HashMap<Integer, DepartmentResponse>();
                HashSet<String> hashSet = new HashSet<>();


                for (Laboratory laboratory : list) {
//                    hashSet.add("" + laboratory.getLevelId());
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

                levelAdapter = new ArrayAdapter<DepartmentResponse>(LocateAssetActivity.this, android.R.layout.simple_spinner_item, responses);
                levelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerLevel.setAdapter(levelAdapter);

                String sessionLevel = session.getValue(TAG + "level");
                if (!sessionLevel.isEmpty()) {
                    for (DepartmentResponse l : responses) {
                        if (String.valueOf(l.getId()).equals(sessionLevel)) {
                            spinnerLevel.setSelection(responses.indexOf(l));
                        }
                    }
                }
            }
        });

        spinnerLevel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if (responses.size() > 0) {
                    selectedLevel = responses.get(position);

                    session.setValue(TAG + "level", "" + selectedLevel.getId());

                    List<DepartmentResponse.Child> labList = responses.get(position).getChild();

                    labAdapter = new ArrayAdapter<DepartmentResponse.Child>(LocateAssetActivity.this, android.R.layout.simple_spinner_item, labList);
                    labAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerLab.setAdapter(labAdapter);

                    String sessionLabId = session.getValue(TAG + "lab");
                    if (!sessionLabId.isEmpty()) {
                        for (DepartmentResponse.Child child : labList) {
                            if (String.valueOf(child.getId()).equals(sessionLabId)) {
                                spinnerLab.setSelection(labList.indexOf(child));
                            }
                        }
                    }
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

                    session.setValue(TAG + "lab", "" + selectedLab.getId());

//                    todo

                    inventories.clear();
                    for (Inventory i : assetsList) {
                        if (i.getLabId() == selectedLab.getId()) {
                            inventories.add(i);
                        }
                    }

                    arrayAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                inventory = inventories.get(position);
                logger.i(TAG, "" + inventory.getEpc() + "\t" + inventory.getName());
                seekBar.setProgress(0);
                inventory.setRssi("0");
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                inventory = null;
                seekBar.setProgress(0);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                tvRSSIValue.setText(progress + " %");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        seekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        app.setOnRFIDListener(this);

        try {
            if (!BuildConfig.DEBUG)
                ModuleManager.newInstance().setUHFStatus(true);
        } catch (Exception e) {
            logger.e(TAG, "" + e.getLocalizedMessage());
        }
    }

    @Override
    public void onInventoryTag(Inventory tag) {
        boolean found = false;

        logger.i(TAG, "Scanned " + tag.getEpc());

        if (inventories.isEmpty() || arrayAdapter.getCount() < 1) {
            return;
        }

        selectedAsset = inventories.get(spinner.getSelectedItemPosition());
        inventory = selectedAsset;

        logger.i(TAG, inventory.getEpc() + " ### " + tag.getEpc());

        if (inventory.getFormattedEPC().equals(tag.getFormattedEPC())) {
            logger.i(TAG, "Matching");
            inventory.setRssi(tag.strRSSI);
//            tvRSSIValue.setText(tag.strRSSI + "%");
            try {
                logger.i(TAG, "rssi is " + tag.getRssi());
                int rssi = 0;
                rssi = Integer.parseInt(tag.getRssi());
                seekBar.setProgress(rssi);


                if (timer != null) {
                    timer.cancel();
                    timer = null;
                    logger.i(TAG, "setting timer to null");
                }

            } catch (Exception e) {
                e.printStackTrace();
                logger.e(TAG, "" + e.getLocalizedMessage());
            }
        } else {
            logger.i(TAG, "Not Matching");
        }
    }

    @Override
    public void onInventoryTagEnd(RXInventoryTag.RXInventoryTagEnd tagEnd) {
        logger.i(TAG, "tagEnd " + tagEnd.mTagCount);

//        when tag is away , value should reset
        if (tagEnd.mTagCount == 0) {
            inventory.setRssi("0");
            logger.i(TAG, "tag end " + tagEnd.mTagCount);
            if (timer == null) {
                logger.i(TAG, "initializing timer object");
                timer = new Timer();
                logger.i(TAG, "created new timer");
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        logger.i(TAG, "inside timerTask");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                logger.i(TAG, "updating UI");
                                inventory.setRssi("0");
                                seekBar.setProgress(Integer.parseInt(inventory.getRssi()));
                                logger.i(TAG, "updating UI " + inventory.getRssi() + " progress " + seekBar.getProgress());
                            }
                        });
                    }
                }, 300);
            } else {
                logger.i(TAG, "  current timer " + timer.toString());
            }
        }
    }

    @Override
    public void onScanningStatus(boolean status) {
        if (status) {
            txtIndicator.setText(getResources().getString(R.string.scanning));
        } else {
            txtIndicator.setText("Start Scan");
        }
    }

    @Override
    public void onConnection(boolean status) {
        if (status) {
            showToast("Connected");
        } else {
            showToast("Connection Lost");
        }
    }
}