package in.mobiux.android.orca50scanner.sensingobjectbarcode.activity;

import android.Manifest;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.zebra.sdl.BarcodeReaderBaseActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.mobiux.android.orca50scanner.common.utils.AppLogger;
import in.mobiux.android.orca50scanner.common.utils.AppUtils;
import in.mobiux.android.orca50scanner.common.utils.pdf.PdfUtils;
import in.mobiux.android.orca50scanner.reader.activity.SettingsActivity;
import in.mobiux.android.orca50scanner.reader.core.BarcodeReaderListener;
import in.mobiux.android.orca50scanner.reader.model.Barcode;
import in.mobiux.android.orca50scanner.sensingobjectbarcode.R;
import in.mobiux.android.orca50scanner.sensingobjectbarcode.adapter.BarcodeAdapter;

import static in.mobiux.android.orca50scanner.sensingobjectbarcode.activity.BaseActivity.STORAGE_PERMISSION_CODE;

public class BarcodeScannerActivity extends BarcodeReaderBaseActivity implements View.OnClickListener {

    private Button btnSave, btnClear, btnPrint;
    private TextView tvCount, txtIndicator;
    private RecyclerView recyclerView;

    private List<Barcode> barcodes = new ArrayList<>();
    private Map<String, Barcode> map = new HashMap<>();
    private BarcodeAdapter adapter;
    private AppLogger logger;

    public static final String TAG = BarcodeScannerActivity.class.getCanonicalName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("Sensing Object");

        tvCount = findViewById(R.id.tvCount);
        txtIndicator = findViewById(R.id.txtIndicator);
        btnSave = findViewById(R.id.btnSave);
        btnClear = findViewById(R.id.btnClear);
        btnPrint = findViewById(R.id.btnPrint);
        recyclerView = findViewById(R.id.recyclerView);
        tvCount.setText("");
        txtIndicator.setTag(false);
        txtIndicator.setText("");

        logger = AppLogger.getInstance(getApplicationContext());
        btnSave.setOnClickListener(this);
        btnClear.setOnClickListener(this);
        btnPrint.setOnClickListener(this);

        adapter = new BarcodeAdapter(BarcodeScannerActivity.this, barcodes);
        recyclerView.setAdapter(adapter);
        tvCount.setText(adapter.getItemCount() + " Pcs");

    }

    @Override
    public void onBarcodeScan(String barcode) {
        super.onBarcodeScan(barcode);

        Barcode bar = new Barcode();
        bar.setName(barcode);

        logger.i(TAG, "scanned success " + bar.getName());
        barcodes.add(bar);
        map.put(bar.getName(), bar);

        adapter.notifyDataSetChanged();

        tvCount.setText(adapter.getItemCount() + " Pcs");
    }

    @Override
    public void onConnection(boolean status) {
        super.onConnection(status);

        logger.i(TAG, "connection status " + status);
        if (status) {
//            showToast("Connected");
        } else {
//            showToast("Disconnected");
        }
    }

    @Override
    public void onScanStatus(boolean status) {
        super.onScanStatus(status);

        if (status) {
            txtIndicator.setVisibility(View.VISIBLE);
        } else {
            txtIndicator.setVisibility(View.GONE);
        }

        logger.i(TAG, "Scanning Status " + status);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btnClear:
                logger.i(TAG, "Clear");
                barcodes.clear();
                map.clear();
                adapter.notifyDataSetChanged();
                tvCount.setText(adapter.getItemCount() + " Pcs");
                break;
            case R.id.btnSave:
                logger.i(TAG, "Save");

                checkPermission(BarcodeScannerActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);
//                logger.createAndExportLogs(BarcodeScannerActivity.this);
                SettingsActivity.launchActivity(getApplicationContext());
                break;
            case R.id.btnPrint:
                logger.i(TAG, "print");

                checkPermission(BarcodeScannerActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);

                PdfUtils pdfUtils = new PdfUtils(BarcodeScannerActivity.this);

                String title = "Sensing Object Barcode Reader";

                String[] columns = {"Barcode", "Time"};
                PdfUtils.PdfTable table = new PdfUtils.PdfTable(columns);
                for (Barcode i : barcodes) {
                    table.cell(i.getName());
                    table.cell(AppUtils.getFormattedTimestamp());
                }

                pdfUtils.createPdfFile(PdfUtils.getPdfPath(BarcodeScannerActivity.this), table, title);

                break;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}