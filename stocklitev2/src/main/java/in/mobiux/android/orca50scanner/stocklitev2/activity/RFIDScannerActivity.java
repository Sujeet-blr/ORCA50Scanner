package in.mobiux.android.orca50scanner.stocklitev2.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import in.mobiux.android.orca50scanner.common.utils.AppUtils;
import in.mobiux.android.orca50scanner.common.utils.CSVUtils;
import in.mobiux.android.orca50scanner.common.utils.SessionManager;
import in.mobiux.android.orca50scanner.reader.activity.RFIDReaderBaseActivity;
import in.mobiux.android.orca50scanner.reader.model.Inventory;
import in.mobiux.android.orca50scanner.stocklitev2.R;
import in.mobiux.android.orca50scanner.stocklitev2.adapter.InventoryAdapter;
import in.mobiux.android.orca50scanner.stocklitev2.db.AppDatabaseRepo;
import in.mobiux.android.orca50scanner.stocklitev2.db.model.RFIDTag;
import in.mobiux.android.orca50scanner.stocklitev2.model.Stock;
import in.mobiux.android.orca50scanner.stocklitev2.utils.MyApplication;
import in.mobiux.android.orca50scanner.stocklitev2.utils.RFIDUtils;

import static in.mobiux.android.orca50scanner.stocklitev2.utils.RFIDUtils.MatchingRule.MR3;

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
    private FloatingActionButton fabRSSI;
    private int rfOutputPower = 0;

    private AppDatabaseRepo dbRepo;
    private HashMap<String, RFIDTag> dbSample = new HashMap<>();
    private RFIDUtils rfidUtils;

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
        fabRSSI = findViewById(R.id.fabRSSI);

        myApp = (MyApplication) getApplicationContext();
        dbRepo = new AppDatabaseRepo(app);
        rfidUtils = RFIDUtils.getInstance(app);

        dbRepo.getRFIDTagsList().observe(this, new Observer<List<RFIDTag>>() {
            @Override
            public void onChanged(List<RFIDTag> tags) {
                dbSample.clear();
                for (RFIDTag tag : tags) {
                    dbSample.put(AppUtils.getFormattedEPC(tag.getEpc()), tag);
                }
            }
        });

        barcode = getIntent().getStringExtra("barcode");
        if (TextUtils.isEmpty(barcode)) {
            Toast.makeText(this, "Barcode not found error", Toast.LENGTH_SHORT).show();
            finish();
        }

        stock.setBarcode(barcode);
        adapter = new InventoryAdapter(RFIDScannerActivity.this, tagList);
        recyclerView.setAdapter(adapter);

        fabRSSI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPowerOutputSetting();
            }
        });

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

        Inventory matchingInventory = tags.get(inventory.getFormattedEPC());

//        match found
        if (matchingInventory != null) {
            matchingInventory.setRssi(inventory.getRssi());
            matchingInventory.setQuantity(matchingInventory.getQuantity() + 1);
        } else {
            inventory.setQuantity(inventory.getQuantity() + 1);
            matchingInventory = inventory;
        }

        tags.put(matchingInventory.getFormattedEPC(), matchingInventory);
        checkForMatchingCriteria(matchingInventory);

        tvHeader.setText("RFID Tags - " + tagList.size() + " items");
    }

    @Override
    public void onInventoryTagEnd(Inventory.InventoryTagEnd tagEnd) {
        super.onInventoryTagEnd(tagEnd);

        adapter.notifyDataSetChanged();
//        recyclerView.scrollToPosition(adapter.getItemCount() - 1);
        stock.setRfidTags(tagList);
    }

    private void checkForMatchingCriteria(Inventory i) {

        RFIDUtils.MatchingRule matchingRule = rfidUtils.getMatchingRule();
        i.setName(i.getEpc());

        if (matchingRule == RFIDUtils.MatchingRule.MR1) {

            i.setMatchingWithSample(true);

        } else if (matchingRule == RFIDUtils.MatchingRule.MR2) {

            RFIDTag matchingTag = dbSample.get(i.getFormattedEPC());

            if (matchingTag != null) {
                i.setMatchingWithSample(true);
                i.setName(matchingTag.getName());
            } else {
                i.setMatchingWithSample(false);
            }

        } else if (matchingRule == MR3) {

            Set<String> acronyms = rfidUtils.getAcronyms();

            if (acronyms.isEmpty()) {
                i.setMatchingWithSample(false);
            } else {
                for (String key : acronyms) {
                    String str = i.getFormattedEPC();
                    if (str.startsWith(key)) {
                        i.setMatchingWithSample(true);
                        break;
                    }
                }
            }

        } else {
            i.setMatchingWithSample(true);
        }

        if (i.isMatchingWithSample() && !tagList.contains(i)) {
            tagList.add(i);
        } else {
            if (rfidUtils.getNonMatchingRule() == RFIDUtils.NonMatchingRule.NMR1 && !tagList.contains(i)) {
                tagList.add(i);
            }
        }
    }

    private void openPowerOutputSetting() {

        rfOutputPower = session.getInt(session.KEY_RF_OUTPUT_POWER, rfOutputPower);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = this.getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_power_output_setting, null);
        builder.setView(layout);

        int MIN = 3;
        TextView tvRSSI = layout.findViewById(R.id.tvRSSI);
        SeekBar seekBar = layout.findViewById(R.id.seekBar);
        Button btnClose = layout.findViewById(R.id.btnClose);
        tvRSSI.setText(rfOutputPower + "");
        seekBar.setProgress(rfOutputPower);
        AlertDialog alertDialog = builder.create();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                if (progress < MIN) {
                    progress = MIN;
                    seekBar.setProgress(MIN);
                }

                rfOutputPower = progress;

                setRFOutputPower(rfOutputPower);
                tvRSSI.setText(progress + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        btnClose.setOnClickListener(view -> {
            alertDialog.dismiss();
        });


        alertDialog.show();
        seekBar.setProgress(rfOutputPower);

    }
}