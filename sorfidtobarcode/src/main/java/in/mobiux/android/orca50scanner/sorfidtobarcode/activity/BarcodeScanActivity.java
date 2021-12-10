package in.mobiux.android.orca50scanner.sorfidtobarcode.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.zebra.adc.decoder.BarCodeReader;
import com.zebra.model.Barcode;
import com.zebra.sdl.BarcodeReaderBaseActivity;
import com.zebra.util.BeeperHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.mobiux.android.orca50scanner.reader.activity.SettingsActivity;
import in.mobiux.android.orca50scanner.sorfidtobarcode.R;

public class BarcodeScanActivity extends BarcodeReaderBaseActivity {


    private TextView tvMessage;
    private Button btnConfirmBarcode, btnExportLogs;
    private ImageView ivBarcodeStatus, ivHome;
    private TextView textToolbarTitle;

    private Barcode barcode = new Barcode();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scanner);

        tvMessage = findViewById(R.id.tvMessage);
        ivBarcodeStatus = findViewById(R.id.ivBarcodeStatus);
        btnConfirmBarcode = findViewById(R.id.btnConfirmBarcode);
        btnExportLogs = findViewById(R.id.btnExportLogs);
        textToolbarTitle = findViewById(R.id.textToolbarTitle);

        ivBarcodeStatus.setVisibility(View.GONE);
        btnConfirmBarcode.setVisibility(View.GONE);


        setTitle(getString(R.string.home_title));

        btnConfirmBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!TextUtils.isDigitsOnly(barcode.getName())) {
                    showToast("Barcode is Not a Number");
                    return;
                }

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

        checkPermission(BarcodeScanActivity.this, Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);
    }

    @Override
    public void onBarcodeScan(String b) {
        super.onBarcodeScan(b);

        barcode.setName(b.trim());
        tvMessage.setText(b);

        ivBarcodeStatus.setVisibility(View.VISIBLE);
        btnConfirmBarcode.setVisibility(View.VISIBLE);
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