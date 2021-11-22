package in.mobiux.android.orca50scanner.sensingobjectrfidreader.activity;

import android.Manifest;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.mobiux.android.orca50scanner.common.utils.pdf.PdfUtils;
import in.mobiux.android.orca50scanner.reader.activity.SettingsActivity;
import in.mobiux.android.orca50scanner.reader.core.RFIDReader;
import in.mobiux.android.orca50scanner.reader.core.RFIDReaderListener;
import in.mobiux.android.orca50scanner.reader.core.Reader;
import in.mobiux.android.orca50scanner.reader.model.Inventory;
import in.mobiux.android.orca50scanner.reader.model.OperationTag;
import in.mobiux.android.orca50scanner.sensingobjectrfidreader.R;
import in.mobiux.android.orca50scanner.sensingobjectrfidreader.adapter.InventoryAdapter;

public class ScanInventoryActivity extends BaseActivity implements View.OnClickListener {

    private Button btnSave, btnClear, btnPrint;
    private TextView tvCount, txtIndicator;
    private RecyclerView recyclerView;
    private List<Inventory> scannedInventories = new ArrayList<Inventory>();
    private Map<String, Inventory> inventoriesMap = new HashMap<>();
    private InventoryAdapter adapter;
    boolean startButtonStatus = false;

    private RFIDReader rfidReader;
    private RFIDReaderListener rfidReaderListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_inventory);

        setTitle("Sensing Object");
        setHomeButtonEnable(false);

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
//        btnSave.setVisibility(View.GONE);
        btnClear.setOnClickListener(this);
        btnPrint.setOnClickListener(this);
        txtIndicator.setTag(startButtonStatus);

        adapter = new InventoryAdapter(ScanInventoryActivity.this, scannedInventories);
        recyclerView.setAdapter(adapter);
        tvCount.setText(adapter.getItemCount() + " Pcs");

    }

    @Override
    protected void onStart() {
        super.onStart();

        rfidReader = new RFIDReader(getApplicationContext());
        rfidReader.connect(Reader.ReaderType.RFID);

        registerRFIDListener();
    }

    @Override
    protected void onStop() {
        super.onStop();

        rfidReader.releaseResources();
        rfidReader.unregisterListener(rfidReaderListener);
    }

    private void registerRFIDListener() {
        rfidReaderListener = new RFIDReaderListener() {
            @Override
            public void onInventoryTag(Inventory inventory) {
                logger.i(TAG, "scanned inventory " + inventory.getEpc());

                Inventory matchingAsset = inventoriesMap.get(inventory.getFormattedEPC());

                if (matchingAsset == null) {
                    matchingAsset = inventory;
                    inventoriesMap.put(matchingAsset.getFormattedEPC(), matchingAsset);
                    scannedInventories.add(matchingAsset);
                }

                matchingAsset.setRssi(inventory.getRssi());
                matchingAsset.setScanStatus(true);

            }

            @Override
            public void onOperationTag(OperationTag operationTag) {

            }

            @Override
            public void onScanningStatus(boolean status) {
                logger.i(TAG, "Scanning status " + status);

                if (status) {
                    txtIndicator.setText(getResources().getString(R.string.scanning));
                    txtIndicator.setTag(true);
                } else {
                    txtIndicator.setText(getResources().getString(R.string.start_scan));
                    txtIndicator.setTag(false);
                }
            }

            @Override
            public void onInventoryTagEnd(Inventory.InventoryTagEnd tagEnd) {
                logger.i(TAG, "Tag count " + tagEnd.mTagCount);

                tvCount.setText(adapter.getItemCount() + " PCS");
                adapter.notifyDataSetChanged();
                app.playBeep();

            }

            @Override
            public void onConnection(boolean status) {
                logger.i(TAG, "Connection status " + status);
            }
        };

        rfidReader.setOnRFIDReaderListener(rfidReaderListener);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btnClear:
                logger.i(TAG, "Clear");
                scannedInventories.clear();
                inventoriesMap.clear();
                adapter.notifyDataSetChanged();
                tvCount.setText(adapter.getItemCount() + " Pcs");
                break;
            case R.id.btnSave:
                logger.i(TAG, "Save");

                SettingsActivity.launchActivity(getApplicationContext());
                break;
            case R.id.btnPrint:
                logger.i(TAG, "print");

                checkPermission(ScanInventoryActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);

                PdfUtils pdfUtils = new PdfUtils(ScanInventoryActivity.this);

                String title = "Sensing Object Rfid Reader";

                String[] columns = {"Rfid", "Rssi", "Status"};
                PdfUtils.PdfTable table = new PdfUtils.PdfTable(columns);
                for (Inventory i : scannedInventories) {
                    table.addCell(i.getEpc());
                    table.addCell(i.getRssi());
                    if (i.isScanStatus()) {
                        table.addCell("Scanned");
                    } else {
                        table.addCell("n/a");
                    }
                }

                pdfUtils.createPdfFile(PdfUtils.getPdfPath(ScanInventoryActivity.this), table, title);

                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_F4) {
            rfidReader.startScan();
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        app.onTerminate();
    }
}