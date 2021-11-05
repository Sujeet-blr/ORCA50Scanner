package in.mobiux.android.orca50scanner.sologistics.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.zebra.sdl.BarcodeReaderBaseActivity;

import org.jetbrains.annotations.NotNull;

import in.mobiux.android.orca50scanner.reader.activity.SettingsActivity;
import in.mobiux.android.orca50scanner.sologistics.R;

public class BarcodeScannerActivity extends BarcodeReaderBaseActivity {

    private TextView tvBarcode, tvMessage;
    private Button btnOK;
    private String barcode = "";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scanner);

        tvBarcode = findViewById(R.id.tvBarcode);
        tvMessage = findViewById(R.id.tvMessage);
        btnOK = findViewById(R.id.btnOK);
        tvBarcode.setText("");
        btnOK.setVisibility(View.GONE);

        getSupportActionBar().setTitle("Scan QR Code");

        btnOK.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), RFIDScannerActivity.class);
            intent.putExtra("barcode", barcode);
            startActivityForResult(intent, 201);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.barcode_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull @NotNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            SettingsActivity.launchActivity(getApplicationContext());
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBarcodeScan(String barcode) {
        super.onBarcodeScan(barcode);
        this.barcode = barcode;
        tvBarcode.setText(barcode);
        btnOK.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 201 && resultCode == RESULT_FIRST_USER) {
            barcode = "";
            tvBarcode.setText("");
            btnOK.setVisibility(View.GONE);
        }
    }
}