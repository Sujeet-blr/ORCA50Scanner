package in.mobiux.android.orca50scanner.sologistics.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import in.mobiux.android.orca50scanner.common.utils.AppBuildConfig;
import in.mobiux.android.orca50scanner.common.utils.AppUtils;
import in.mobiux.android.orca50scanner.common.utils.CSVUtils;
import in.mobiux.android.orca50scanner.reader.activity.RFIDReaderBaseActivity;
import in.mobiux.android.orca50scanner.reader.model.Inventory;
import in.mobiux.android.orca50scanner.sologistics.R;
import in.mobiux.android.orca50scanner.sologistics.adapter.InventoryAdapter;
import in.mobiux.android.orca50scanner.sologistics.model.Stock;
import in.mobiux.android.orca50scanner.sologistics.utils.MyApplication;

public class RFIDScannerActivity extends RFIDReaderBaseActivity {

    private HashMap<String, Inventory> tags = new HashMap<>();
    private List<Inventory> tagList = new ArrayList<>();
    private RecyclerView recyclerView;
    private InventoryAdapter adapter;
    private String barcode = "";
    private Button btnRepeat, btnReScan, btnExportData;
    private Stock stock = new Stock();
    private MyApplication myApp;
    private TextView tvHeader;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rfidscanner);

        getSupportActionBar().setTitle("Scan RFID Tags");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.recyclerView);
        btnRepeat = findViewById(R.id.btnRepeat);
        btnReScan = findViewById(R.id.btnReScan);
        btnExportData = findViewById(R.id.btnExportData);
        tvHeader = findViewById(R.id.tvHeader);

        myApp = (MyApplication) getApplicationContext();

        barcode = getIntent().getStringExtra("barcode");
        if (TextUtils.isEmpty(barcode)) {
            Toast.makeText(this, "Barcode not found error", Toast.LENGTH_SHORT).show();
            finish();
        }

        stock.setBarcode(barcode);
        adapter = new InventoryAdapter(RFIDScannerActivity.this, tagList);
        recyclerView.setAdapter(adapter);

        btnRepeat.setOnClickListener(view -> {

            if (tagList.isEmpty()) {
                showToast("No Data");
                return;
            }

            myApp.addStock(stock);
            Intent intent = new Intent();
            setResult(RESULT_FIRST_USER);
            finish();
        });

        btnReScan.setOnClickListener(view -> {
            tagList.clear();
            tags.clear();
            stock.getRfidTags().clear();
            adapter.notifyDataSetChanged();
            tvHeader.setText("RFID Tags - " + tagList.size() + " items");
        });

        btnExportData.setOnClickListener(view -> {
            checkPermission(RFIDScannerActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);

            if (tagList.isEmpty()) {
                showToast("No Data");
                return;
            }

            myApp.addStock(stock);
            CSVUtils csvUtils = new CSVUtils(getApplicationContext());
            List<String> columns = new ArrayList<>();
            StringBuilder data;
            StringBuilder header = new StringBuilder(("Barcode \t, RFID Tags \t, RSSI (dBm)"));
            columns.add(header.toString());

            for (Stock stock : myApp.getStocks()) {

                for (Inventory inventory : stock.getRfidTags()) {
                    data = new StringBuilder(("\n" + stock.getBarcode() + "\t," + inventory.getEpc()) + "\t," + inventory.getRssi());
                    columns.add(data.toString());
                }
            }

            csvUtils.writeToColumn(columns);

            csvUtils.createAndExportLogs(RFIDScannerActivity.this);
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onInventoryTag(Inventory inventory) {
        super.onInventoryTag(inventory);

        if (!tags.containsKey(inventory.getFormattedEPC())) {
            tagList.add(0, inventory);
        }

        tags.put(inventory.getFormattedEPC(), inventory);
        tvHeader.setText("RFID Tags - " + tagList.size() + " items");
    }

    @Override
    public void onInventoryTagEnd(Inventory.InventoryTagEnd tagEnd) {
        super.onInventoryTagEnd(tagEnd);

        adapter.notifyDataSetChanged();
//        recyclerView.scrollToPosition(adapter.getItemCount() - 1);
        stock.setRfidTags(tagList);
    }
}