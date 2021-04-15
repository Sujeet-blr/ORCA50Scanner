package in.mobiux.android.orca50scanner.activity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.module.interaction.ModuleConnector;
import com.module.interaction.RXTXListener;
import com.nativec.tools.ModuleManager;
import com.rfid.RFIDReaderHelper;
import com.rfid.config.CMD;
import com.rfid.rxobserver.RXObserver;
import com.rfid.rxobserver.ReaderSetting;
import com.rfid.rxobserver.bean.RXInventoryTag;

import in.mobiux.android.orca50scanner.R;
import in.mobiux.android.orca50scanner.api.model.Inventory;
import in.mobiux.android.orca50scanner.util.DeviceConnector;

import static in.mobiux.android.orca50scanner.util.DeviceConnector.BOUD_RATE;

public class InventoryDetailActivity extends BaseActivity {

    public static final String TAG = InventoryDetailActivity.class.getName();

    private SeekBar seekBar;
    ModuleConnector connector;
    RFIDReaderHelper rfidReaderHelper;
    private Inventory inventory;

    private TextView tvEPC, tvRSSI, tvBack, tvClose, tvRSSIValue;
    private boolean observerRegistrationStatus = false;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_detail);

        handler = new Handler(getMainLooper());

        seekBar = findViewById(R.id.seekBar);
        tvEPC = findViewById(R.id.tvEPC);
        tvRSSI = findViewById(R.id.tvRSSI);
        tvBack = findViewById(R.id.tvBack);
        tvClose = findViewById(R.id.tvClose);
        tvRSSIValue = findViewById(R.id.tvRSSIValue);

        connector = RFIDActivity.connector;
        inventory = (Inventory) getIntent().getSerializableExtra("tag");

        if (inventory == null) {
            Toast.makeText(this, "Inventory not selected", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            tvEPC.setText("" + inventory.getEpc());
        }

        if (!connector.isConnected()) {
            connectRFID();
        } else {
            try {
                rfidReaderHelper = RFIDReaderHelper.getDefaultHelper();
                rfidReaderHelper.setTrigger(true);
                ModuleManager.newInstance().setUHFStatus(true);
                rfidReaderHelper.registerObserver(rxObserver);
                observerRegistrationStatus = true;
                rfidReaderHelper.realTimeInventory(ReaderSetting.newInstance().btReadId, (byte) 0x01);
                logger.i(TAG, "Connected && On && observerRegistered :" + observerRegistrationStatus);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

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

        tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if ((!observerRegistrationStatus) && (rfidReaderHelper != null)) {
            rfidReaderHelper.registerObserver(rxObserver);
            observerRegistrationStatus = true;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (observerRegistrationStatus) {
            rfidReaderHelper.unRegisterObserver(rxObserver);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (rfidReaderHelper != null) {
            rfidReaderHelper.unRegisterObservers();
        }
    }

    private void connectRFID() {
        try {
            if (connector.connectCom(DeviceConnector.PORT, BOUD_RATE)) {
                logger.i(TAG, "CONNECTION SUCCESS");

                try {
                    ModuleManager.newInstance().setUHFStatus(true);
                    rfidReaderHelper = RFIDReaderHelper.getDefaultHelper();
                    rfidReaderHelper.registerObserver(rxObserver);
                    observerRegistrationStatus = true;
//                    rfidReaderHelper.setRXTXListener(rxtxListener);
                    rfidReaderHelper.setTrigger(true);

                    rfidReaderHelper.realTimeInventory(ReaderSetting.newInstance().btReadId, (byte) 0x01);

                } catch (Exception e) {
                    logger.i(TAG, "Exception " + e.getLocalizedMessage());
                    e.printStackTrace();
                }
            } else {
                logger.i(TAG, "CONNECTION FAILED");
            }
        } catch (Exception e) {
            logger.e(TAG, "" + e.getLocalizedMessage());
        }
    }

    RXObserver rxObserver = new RXObserver() {
        @Override
        protected void refreshSetting(ReaderSetting readerSetting) {
            logger.i(TAG, "Setting Refresh ");
        }

        @Override
        protected void onExeCMDStatus(byte cmd, byte status) {
            logger.i(TAG, "Command Executed " + cmd + "\tstatus " + status);
        }

        @Override
        protected void onInventoryTag(RXInventoryTag tag) {
            logger.i(TAG, "Tag Scanned " + tag.strEPC);

            if ((tag.strEPC.replace(" ","")).equals(inventory.getFormattedEPC())) {


                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        inventory.setRssi(tag.strRSSI);
                        tvEPC.setText(tag.strEPC);
                        tvRSSI.setText(tag.strRSSI);
                        tvRSSIValue.setText(tag.strRSSI);
                        try {
                            seekBar.setProgress(Integer.valueOf(tag.strRSSI));
                        } catch (Exception e) {
                            e.printStackTrace();
                            logger.e(TAG, "" + e.getLocalizedMessage());
                        }
                    }
                });
            }
        }

        @Override
        protected void onInventoryTagEnd(RXInventoryTag.RXInventoryTagEnd tagEnd) {
            logger.i(TAG, "onInventoryTgEnd");
            rfidReaderHelper.realTimeInventory(ReaderSetting.newInstance().btReadId, (byte) 0x01);
        }
    };

    RXTXListener rxtxListener = new RXTXListener() {
        @Override
        public void reciveData(byte[] bytes) {

        }

        @Override
        public void sendData(byte[] bytes) {

        }

        @Override
        public void onLostConnect() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    connectRFID();
                }
            });
        }
    };
}