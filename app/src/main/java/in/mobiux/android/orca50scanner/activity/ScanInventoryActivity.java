package in.mobiux.android.orca50scanner.activity;

import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.mobiux.android.orca50scanner.R;
import in.mobiux.android.orca50scanner.adapter.InventoryAdapter;
import in.mobiux.android.orca50scanner.common.utils.pdf.PdfUtils;
import in.mobiux.android.orca50scanner.reader.core.RFIDReader;
import in.mobiux.android.orca50scanner.reader.core.RFIDReaderListener;
import in.mobiux.android.orca50scanner.reader.core.Reader;
import in.mobiux.android.orca50scanner.reader.model.Inventory;
import in.mobiux.android.orca50scanner.reader.model.OperationTag;

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

        rfidReader = new RFIDReader(getApplicationContext());
        rfidReader.connect(Reader.ReaderType.RFID);

        registerRFIDListener();

        adapter = new InventoryAdapter(ScanInventoryActivity.this, scannedInventories);
        recyclerView.setAdapter(adapter);
        tvCount.setText(adapter.getItemCount() + " Pcs");

    }

    private void registerRFIDListener() {
        rfidReaderListener = new RFIDReaderListener() {
            @Override
            public void onInventoryTag(Inventory inventory) {
                logger.i(TAG, "scanned inventory " + inventory.getEpc());

                Inventory matchingAsset = inventoriesMap.get(inventory.getFormattedEPC());

                if (matchingAsset == null) {
                    matchingAsset = inventory;
                    inventoriesMap.put(matchingAsset.getEpc(), matchingAsset);
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

                checkPermission(ScanInventoryActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);
                logger.createAndExportLogs(ScanInventoryActivity.this);
                break;
            case R.id.btnPrint:
                logger.i(TAG, "print");

                checkPermission(ScanInventoryActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);

                PdfUtils pdfUtils = new PdfUtils(ScanInventoryActivity.this);

                String title = "Sensing Object Rfid Reader";

                String[] columns = {"Rfid", "Rssi"};
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

    private void arrangeScannedList() {

        HashMap<String, Inventory> map = new HashMap<>();
        for (Inventory i : scannedInventories) {
            map.put(i.getFormattedEPC(), i);
        }

        scannedInventories.clear();
        for (Inventory i : map.values()) {
            if (i.isScanStatus()) {
                scannedInventories.add(0, i);
            } else {
                scannedInventories.add(i);
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        rfidReader.releaseResources();
        app.onTerminate();
    }
}