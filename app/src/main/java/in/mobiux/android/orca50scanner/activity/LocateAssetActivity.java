package in.mobiux.android.orca50scanner.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.module.interaction.ModuleConnector;
import com.nativec.tools.ModuleManager;
import com.rfid.RFIDReaderHelper;
import com.rfid.rxobserver.RXObserver;
import com.rfid.rxobserver.ReaderSetting;
import com.rfid.rxobserver.bean.RXInventoryTag;

import java.util.ArrayList;
import java.util.List;

import in.mobiux.android.orca50scanner.R;
import in.mobiux.android.orca50scanner.api.model.Inventory;
import in.mobiux.android.orca50scanner.util.DeviceConnector;
import in.mobiux.android.orca50scanner.util.RFIDReaderListener;
import in.mobiux.android.orca50scanner.viewmodel.InventoryViewModel;

import static in.mobiux.android.orca50scanner.util.DeviceConnector.BOUD_RATE;

public class LocateAssetActivity extends BaseActivity implements RFIDReaderListener {

    private Spinner spinner;
    private TextView tvRSSIValue;
    private Button btnStart;
    private ArrayAdapter<Inventory> arrayAdapter;
    private List<Inventory> inventories = new ArrayList<>();
    private InventoryViewModel viewModel;
    private Inventory inventory = new Inventory();
    private Inventory selectedAsset = new Inventory();
    boolean buttonStatus = false;

    private SeekBar seekBar;
    ModuleConnector connector;
    RFIDReaderHelper rfidReaderHelper;
    private boolean observerRegistrationStatus = false;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locate_asset);

        setTitle(getResources().getString(R.string.label_locate_asset));

        spinner = findViewById(R.id.spinner);
        tvRSSIValue = findViewById(R.id.tvRSSIValue);
        seekBar = findViewById(R.id.seekBar);
        btnStart = findViewById(R.id.btnStart);
        btnStart.setTag(false);

        handler = new Handler(getMainLooper());
//        connector = RFIDActivity.connector;
        connector = app.connector;

        if (!connector.isConnected()) {
            app.connectRFID();
            ModuleManager.newInstance().setUHFStatus(true);
        } else {
            ModuleManager.newInstance().setUHFStatus(true);
            logger.i(TAG, "connected");
        }

        viewModel = new ViewModelProvider(this).get(InventoryViewModel.class);

        viewModel.getAllInventory().observe(this, new Observer<List<Inventory>>() {
            @Override
            public void onChanged(List<Inventory> list) {
                inventories = list;
                arrayAdapter = new ArrayAdapter<Inventory>(LocateAssetActivity.this, android.R.layout.simple_spinner_item, inventories);
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(arrayAdapter);
                arrayAdapter.notifyDataSetChanged();
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                inventory = inventories.get(position);
                logger.i(TAG, "" + inventory.getEpc() + "\t" + inventory.getName());
                seekBar.setProgress(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                logger.i(TAG, "Start Clicked : " + btnStart.getText().toString());
                btnStart.setTag(!(boolean) btnStart.getTag());
                logger.i(TAG, "Button Status " + (boolean) btnStart.getTag());

                if ((boolean) btnStart.getTag()) {
                    if (app.connector.isConnected()) {
                        ModuleManager.newInstance().setUHFStatus(true);
                        btnStart.setText(getResources().getString(R.string.stop_scan));
                        app.scanningStatus = true;
                        app.triggerEnable = false;
                        app.rfidReaderHelper.realTimeInventory(ReaderSetting.newInstance().btReadId, (byte) 0x01);
                        logger.i(TAG, "realtimeinventorycommand sent");
//                        app.startScanning(TAG);

                    } else {
                        app.reconnectRFID();
                        btnStart.setTag(false);
                    }
                } else {
                    app.triggerEnable = true;
                    app.rfidReaderHelper.setTrigger(true);
                    app.stopScanning();
//                    app.scanningStatus = false;
//                    ModuleManager.newInstance().setUHFStatus(false);
                    btnStart.setText(getResources().getString(R.string.start_scan));
                    logger.i(TAG, "stopping scan");
                }

                logger.i(TAG, "Connection Status : " + connector.isConnected());
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
    }

    @Override
    protected void onStart() {
        super.onStart();

        app.setOnRFIDListener(this);

        try {
//            ModuleManager.newInstance().setScanStatus(true);
            ModuleManager.newInstance().setUHFStatus(true);
        } catch (Exception e) {
            logger.e(TAG, "" + e.getLocalizedMessage());
        }
    }

    @Override
    public void onInventoryTag(Inventory tag) {
        boolean found = false;

        logger.i(TAG, "Scanned " + tag.getEpc());

        selectedAsset = inventories.get(spinner.getSelectedItemPosition());
        inventory = selectedAsset;

//        tvRSSIValue.setText(tag.getRssi()+"%");

        logger.i(TAG, inventory.getEpc() + " ### " + tag.getEpc());

        if (inventory.getFormattedEPC().equals(tag.getFormattedEPC())) {
            logger.i(TAG, "Matching");
            inventory.setRssi(tag.strRSSI);
            tvRSSIValue.setText(tag.strRSSI + "%");
            try {
                logger.i(TAG, "rssi is " + tag.getRssi());
                int rssi = 0;
                rssi = Integer.parseInt(tag.getRssi());
                seekBar.setProgress(rssi);
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
        try {
            seekBar.setProgress(Integer.parseInt(inventory.getRssi()));
        } catch (Exception e) {
            logger.e(TAG, "" + e.getLocalizedMessage());
        }
    }

    @Override
    public void onScanningStatus(boolean status) {
        if (status) {
            btnStart.setText(getResources().getString(R.string.stop_scan));
            btnStart.setTag(status);
        } else {
            btnStart.setText(getResources().getString(R.string.start_scan));
            btnStart.setTag(status);
        }
    }

    @Override
    public void onConnection(boolean status) {
        if (status) {
            Toast.makeText(app, "Connected", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(app, "Connection Lost", Toast.LENGTH_SHORT).show();
        }
    }
}