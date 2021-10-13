package in.mobiux.android.orca50scanner.sorfidtobarcode.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.zebra.sdl.BarcodeReaderBaseActivity;
import com.zebra.util.BarcodeListener;

import in.mobiux.android.orca50scanner.sorfidtobarcode.R;

public class OrcaBarcodeActivity extends BarcodeReaderBaseActivity {

    private TextView tvMessage, tvBarcode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_orca_barcode);
        tvMessage = findViewById(R.id.tvMessage);
        tvBarcode = findViewById(R.id.tvBarcode);

        initListener();
    }

    private void initListener() {
        setOnBarcodeListener(new BarcodeListener() {
            @Override
            public void onBarcodeScan(String barcode) {
                tvBarcode.setText(barcode);
                tvMessage.setText("Barcode Scanned \n" + barcode);
            }

            @Override
            public void onScanStatus(boolean status) {
                if (status)
                    tvMessage.setText("Scanning");
            }

            @Override
            public void onConnection(boolean status) {
                if (status) {
                    tvMessage.setText("Device Connected \n Pull trigger to Scan");
                } else {
                    tvMessage.setText("Device is Not Connected");
                }
            }

            @Override
            public void message(int type, String msg) {
                tvMessage.setText("Message : " + msg);
            }
        });
    }
}