package in.mobiux.android.orca50scanner.sensingobjectbarcode.activity;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.mobiux.android.orca50scanner.common.utils.AppUtils;
import in.mobiux.android.orca50scanner.common.utils.pdf.PdfUtils;
import in.mobiux.android.orca50scanner.reader.core.BarcodeReaderListener;
import in.mobiux.android.orca50scanner.reader.core.Reader;
import in.mobiux.android.orca50scanner.reader.model.Barcode;
import in.mobiux.android.orca50scanner.sensingobjectbarcode.R;
import in.mobiux.android.orca50scanner.sensingobjectbarcode.adapter.BarcodeAdapter;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private Button btnSave, btnClear, btnPrint;
    private TextView tvCount, txtIndicator;
    private RecyclerView recyclerView;

    private List<Barcode> barcodes = new ArrayList<>();
    private Map<String, Barcode> map = new HashMap<>();
    private BarcodeAdapter adapter;

    private BarcodeReaderListener barcodeReaderListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
//        txtIndicator.setTag(startButtonStatus);

//        barcodeReader = new BarcodeReader(getApplicationContext());
//        barcodeReader = new BarcodeReaderOrca(getApplicationContext());
//        barcodeReader.connect(Reader.ReaderType.BARCODE);

        registerBarcodeListener();

        adapter = new BarcodeAdapter(MainActivity.this, barcodes);
        recyclerView.setAdapter(adapter);
        tvCount.setText(adapter.getItemCount() + " Pcs");

    }

    private void registerBarcodeListener() {

        barcodeReaderListener = new BarcodeReaderListener() {
            @Override
            public void onConnection(boolean status) {
                logger.i(TAG, "connection status " + status);
                if (status) {
                    showToast("Connected");
                } else {
                    showToast("Disconnected");
                }
            }

            @Override
            public void onScanSuccess(Barcode barcode) {
                logger.i(TAG, "scanned success " + barcode.getName());
                barcodes.add(barcode);
                map.put(barcode.getName(), barcode);

                adapter.notifyDataSetChanged();
                app.playBeep();
            }

            @Override
            public void onScanFailed(Object o) {
                logger.e(TAG, "Scanned Failed");
            }

            @Override
            public void onScanningStatus(boolean status) {
                if (status) {
                    txtIndicator.setVisibility(View.VISIBLE);
                } else {
                    txtIndicator.setVisibility(View.GONE);
                }

                logger.i(TAG, "Scanning Status " + status);
            }
        };

//        barcodeReader.setOnBarcodeReaderListener(barcodeReaderListener);
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

                checkPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);
                logger.createAndExportLogs(MainActivity.this);
                break;
            case R.id.btnPrint:
                logger.i(TAG, "print");

                checkPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);

                PdfUtils pdfUtils = new PdfUtils(MainActivity.this);

                String title = "Sensing Object Barcode Reader";

                String[] columns = {"Barcode", "Time"};
                PdfUtils.PdfTable table = new PdfUtils.PdfTable(columns);
                for (Barcode i : barcodes) {
                    table.cell(i.getName());
                    table.cell(AppUtils.getFormattedTimestamp());
                }

                pdfUtils.createPdfFile(PdfUtils.getPdfPath(MainActivity.this), table, title);

                break;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

//        barcodeReader.releaseResources();
        app.onTerminate();
    }
}