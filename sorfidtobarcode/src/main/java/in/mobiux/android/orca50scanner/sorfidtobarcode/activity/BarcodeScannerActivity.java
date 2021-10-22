package in.mobiux.android.orca50scanner.sorfidtobarcode.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.Serializable;

import in.mobiux.android.orca50scanner.reader.activity.SettingsActivity;
import in.mobiux.android.orca50scanner.reader.core.BarcodeReader;
import in.mobiux.android.orca50scanner.reader.core.BarcodeReaderListener;
import in.mobiux.android.orca50scanner.reader.core.Reader;
import in.mobiux.android.orca50scanner.reader.model.Barcode;
import in.mobiux.android.orca50scanner.sorfidtobarcode.R;

// This is NOT WORKING with new Orca Device
//this is previous code , which was working with old Orca device.
public class BarcodeScannerActivity extends BaseActivity {

    private BarcodeReader barcodeReader;
    private BarcodeReaderListener barcodeReaderListener;
    private Barcode barcode;

    private TextView tvMessage;
    private Button btnConfirmBarcode, btnExportLogs;
    private ImageView ivBarcodeStatus;

    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scanner);

        tvMessage = findViewById(R.id.tvMessage);
        ivBarcodeStatus = findViewById(R.id.ivBarcodeStatus);
        btnConfirmBarcode = findViewById(R.id.btnConfirmBarcode);
        btnExportLogs = findViewById(R.id.btnExportLogs);

        ivBarcodeStatus.setVisibility(View.GONE);
        btnConfirmBarcode.setVisibility(View.GONE);

        setTitle("READ BARCODE & WRITE TO RFID TAG");


        barcodeReader = new BarcodeReader(getApplicationContext());
        barcodeReader.connect(Reader.ReaderType.BARCODE);

        registerBarcodeListener();

        alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Scanner Connection Lost !");
        alertDialog.setMessage("please click on Connect to establish the connection");
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Connect", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                barcodeReader.connect(Reader.ReaderType.BARCODE);
            }
        });

        btnConfirmBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(app, RenameRfidTagsActivity.class);
                intent.putExtra("barcode", (Serializable) barcode);
                startActivityForResult(intent, 101);
            }
        });

        btnExportLogs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingsActivity.launchActivity(getApplicationContext());
            }
        });
    }

    private void registerBarcodeListener() {

        barcodeReaderListener = new BarcodeReaderListener() {
            @Override
            public void onConnection(boolean status) {
                if (status) {
                    tvMessage.setText("Connected");

                    if (alertDialog.isShowing()) {
                        alertDialog.dismiss();
                    }
                } else {
                    alertDialog.show();
                    tvMessage.setText("Not Connected");
                    logger.e(TAG, "Scanner is not connected");
                }
            }

            @Override
            public void onScanSuccess(Barcode b) {

                logger.i(TAG, "Scan Success");
                barcode = b;
                tvMessage.setText(barcode.getName());

                ivBarcodeStatus.setVisibility(View.VISIBLE);
                btnConfirmBarcode.setVisibility(View.VISIBLE);

                app.playBeep();
            }

            @Override
            public void onScanFailed(Object o) {
                logger.e(TAG, "Scan Failed");
            }

            @Override
            public void onScanningStatus(boolean status) {
                logger.i(TAG, "Scanning Status " + status);
            }
        };

        barcodeReader.setOnBarcodeReaderListener(barcodeReaderListener);
    }
}