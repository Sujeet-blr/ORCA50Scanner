package in.mobiux.android.orca50scanner.sorfidtobarcode.activity;


import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.zebra.model.Barcode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import in.mobiux.android.orca50scanner.common.utils.AppUtils;
import in.mobiux.android.orca50scanner.reader.core.RFIDReader;
import in.mobiux.android.orca50scanner.reader.core.RFIDReaderListener;
import in.mobiux.android.orca50scanner.reader.core.Reader;
import in.mobiux.android.orca50scanner.reader.model.Inventory;
import in.mobiux.android.orca50scanner.reader.model.OperationTag;
import in.mobiux.android.orca50scanner.sorfidtobarcode.R;

public class RenameRfidTagsActivity extends BaseActivity {


    private TextView txtIndicator, tvBarcode, tvBarcodeHEX, textToolbarTitle;
    private Spinner spnrRfids;
    private Button btnAssign, btnConfirmSelect, btnSaveExit, btnNextBarcode;
    private LinearLayout lltWrite, lltWriteSuccess;
    private ImageView ivRFIDStatus, ivRFIDSWritetatus, ivHome;

    private List<Inventory> inventories = new ArrayList<>();
    private ArrayAdapter<Inventory> inventoryAdapter;
    private HashMap<String, Inventory> inventoryMap = new HashMap<>();
    private Barcode barcode;
    private Inventory selectedInventory = null;

    private RFIDReader rfidReader;
    private RFIDReaderListener rfidReaderListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rename_rfid_tags);

        tvBarcodeHEX = findViewById(R.id.tvBarcodeHEX);
        spnrRfids = findViewById(R.id.spnrRfids);
        btnConfirmSelect = findViewById(R.id.btnConfirmSelect);
        btnAssign = findViewById(R.id.btnAssign);
        btnSaveExit = findViewById(R.id.btnSaveExit);
        btnNextBarcode = findViewById(R.id.btnNextBarcode);
        txtIndicator = findViewById(R.id.txtIndicator);
//        edtBarcode = findViewById(R.id.edtBarcode);
        tvBarcode = findViewById(R.id.tvBarcode);
        ivRFIDStatus = findViewById(R.id.ivRFIDStatus);
        ivRFIDSWritetatus = findViewById(R.id.ivRFIDSWritetatus);
        lltWrite = findViewById(R.id.lltWrite);
        lltWriteSuccess = findViewById(R.id.lltWriteSuccess);

        btnConfirmSelect.setVisibility(View.GONE);
        ivRFIDStatus.setVisibility(View.GONE);
        ivRFIDSWritetatus.setVisibility(View.GONE);
        lltWrite.setVisibility(View.GONE);
        lltWriteSuccess.setVisibility(View.GONE);

        txtIndicator.setText("");

        setTitle("READ BARCODE & WRITE TO RFID TAG");
        setHomeButtonEnable(true);

        barcode = (Barcode) getIntent().getSerializableExtra("barcode");

        if (barcode == null) {
            Intent intent = new Intent();
            intent.putExtra("rfid", selectedInventory);
            setResult(RESULT_OK, intent);
            finish();

            showToast("Invalid barcode");
        } else {
            logger.i(TAG, "barcode is " + barcode.getName());
            barcode.setHex(AppUtils.generateHexEPC(barcode.getName()));
            tvBarcode.setText(barcode.getName());
        }

        rfidReader = new RFIDReader(getApplicationContext());
        rfidReader.connect(Reader.ReaderType.RFID);
        registerRfidListener();

        inventoryAdapter = new ArrayAdapter<Inventory>(RenameRfidTagsActivity.this, android.R.layout.simple_spinner_item, inventories);
        inventoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnrRfids.setAdapter(inventoryAdapter);
        spnrRfids.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                selectedInventory = inventoryAdapter.getItem(position);
                logger.i(TAG, "selectedItem " + selectedInventory.getEpc());

                btnConfirmSelect.setVisibility(View.VISIBLE);
                ivRFIDStatus.setVisibility(View.VISIBLE);
                lltWrite.setVisibility(View.GONE);
                lltWriteSuccess.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                logger.i(TAG, "spnrRfids Nothing selected");
            }
        });

        btnConfirmSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int selectStatus = rfidReader.selectAccessEpcMatch(selectedInventory.getEpc());
                logger.i(TAG, "Select Status " + selectStatus);

                if (selectStatus == 0) {
                    logger.i(TAG, "rfid tag selected");

                    btnConfirmSelect.setVisibility(View.GONE);
                    lltWrite.setVisibility(View.VISIBLE);

                    tvBarcodeHEX.setText(barcode.getHex());
                    btnAssign.setVisibility(View.VISIBLE);
                } else {
                    logger.e(TAG, "rfid tag not selected");
                }
            }
        });

        btnAssign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                in.mobiux.android.orca50scanner.reader.model.Barcode b = new in.mobiux.android.orca50scanner.reader.model.Barcode();
                b.setName(barcode.getName());
                int writeStatus = rfidReader.writeToTag(b, selectedInventory);
                logger.i(TAG, "write status " + writeStatus);

                if (writeStatus == 0) {
                    logger.i(TAG, "write is success");
                } else {
                    logger.e(TAG, "write is failed");
                }
            }
        });


        btnSaveExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("rfid", selectedInventory);
                setResult(201, intent);
                finish();
            }
        });

        btnNextBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("rfid", selectedInventory);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    private void registerRfidListener() {

        rfidReaderListener = new RFIDReaderListener() {
            @Override
            public void onScanningStatus(boolean status) {
                if (status) {
                    txtIndicator.setText(getResources().getString(R.string.scanning));
                } else {
                    txtIndicator.setText("");
                }
            }

            @Override
            public void onInventoryTag(Inventory inventory) {
                logger.i(TAG, "scanned tag is " + inventory.getEpc());
                inventory.setName(inventory.getFormattedEPC());
                inventoryMap.put(inventory.getFormattedEPC(), inventory);
                logger.i(TAG, "mapsize " + inventoryMap.size());
            }

            @Override
            public void onOperationTag(OperationTag operationTag) {
                logger.i(TAG, "onOperationTag " + operationTag.strEPC);

                Inventory operatedTag = new Inventory();
                operatedTag.setEpc(operationTag.strEPC);

                if (operationTag.strEPC.equalsIgnoreCase(selectedInventory.getEpc())) {
                    operatedTag = inventoryMap.remove(operatedTag.getFormattedEPC());
                    operatedTag.setEpc(barcode.getHex());
                    inventories.remove(selectedInventory);
                    inventoryAdapter.remove(selectedInventory);
//                    selectedInventory.setEpc(barcode.getHex());
                    inventoryAdapter.notifyDataSetChanged();
                    showDialoge("", "RFID tag is renamed successfully");
                    logger.i(TAG, "operation success");

                    btnAssign.setVisibility(View.GONE);
//            edtBarcode.setText("");
                    lltWriteSuccess.setVisibility(View.VISIBLE);

                    app.playBeep();
                } else {
                    btnAssign.setVisibility(View.VISIBLE);
                    logger.e(TAG, "operation not done");
                    lltWriteSuccess.setVisibility(View.GONE);
                }
            }

            @Override
            public void onInventoryTagEnd(Inventory.InventoryTagEnd tagEnd) {
                logger.i(TAG, "onInventoryTagEnd");

                logger.i(TAG, "map size " + inventoryMap.size());
                inventoryAdapter.clear();
                for (Inventory inventory : inventoryMap.values()) {
                    inventoryAdapter.insert(inventory, inventoryAdapter.getCount());
                }
                logger.i(TAG, "map size " + inventoryMap.size());

                inventoryAdapter.notifyDataSetChanged();

                if (inventoryAdapter.getCount() > 0) {
                    ivRFIDStatus.setVisibility(View.VISIBLE);
                    btnConfirmSelect.setVisibility(View.VISIBLE);
                } else {
                    ivRFIDStatus.setVisibility(View.GONE);
                }

                lltWrite.setVisibility(View.GONE);
                lltWriteSuccess.setVisibility(View.GONE);
            }

            @Override
            public void onConnection(boolean status) {
                logger.i(TAG, "RFID connection status " + status);
                if (status)
                    showToast("Connected");
                else
                    showToast("Connection Lost");
            }
        };

        rfidReader.setOnRFIDReaderListener(rfidReaderListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        rfidReader.releaseResources();
        rfidReader.unregisterListener(rfidReaderListener);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_F4) {
            rfidReader.startScan();
        }
        return super.onKeyDown(keyCode, event);
    }

    protected void setTitle(String title) {
        textToolbarTitle = findViewById(R.id.textToolbarTitle);
        textToolbarTitle.setText(title);
    }

    protected void setHomeButtonEnable(boolean enable) {
        ivHome = findViewById(R.id.ivHome);
        if (enable) {
            ivHome.setVisibility(View.VISIBLE);
        } else {
            ivHome.setVisibility(View.GONE);
        }

        ivHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}