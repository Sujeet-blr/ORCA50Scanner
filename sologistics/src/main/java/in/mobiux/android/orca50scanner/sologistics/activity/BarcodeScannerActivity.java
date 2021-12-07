package in.mobiux.android.orca50scanner.sologistics.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.zebra.sdl.BarcodeReaderBaseActivity;

import org.jetbrains.annotations.NotNull;

import in.mobiux.android.orca50scanner.sologistics.R;
import in.mobiux.android.orca50scanner.sologistics.utils.MyApplication;

public class BarcodeScannerActivity extends BarcodeReaderBaseActivity {

    private TextView tvBarcode, tvMessage;
    private Button btnOK, btnClear;
    private String barcode = "";
    private MyApplication myApp;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scanner);

        tvBarcode = findViewById(R.id.tvBarcode);
        tvMessage = findViewById(R.id.tvMessage);
        btnOK = findViewById(R.id.btnOK);
        btnClear = findViewById(R.id.btnClear);
        tvBarcode.setText("");
        btnOK.setVisibility(View.GONE);
        btnClear.setVisibility(View.GONE);

        getSupportActionBar().setTitle("Scan QR/Barcode");

        myApp = (MyApplication) getApplicationContext();

        btnOK.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), RFIDScannerActivity.class);
            intent.putExtra("barcode", barcode);
            startActivityForResult(intent, 201);
        });

        btnClear.setOnClickListener(view -> {
            barcode = "";
            btnOK.setVisibility(View.GONE);
            tvBarcode.setText(barcode);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.barcode_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull @NotNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(app, SettingsActivity.class));
        } else if (id == R.id.action_create) {
            askForNewTask();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBarcodeScan(String barcode) {
        super.onBarcodeScan(barcode);
        this.barcode = barcode;
        tvBarcode.setText(barcode);
        btnOK.setVisibility(View.VISIBLE);
        btnClear.setVisibility(View.VISIBLE);
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

    @Override
    public void onBackPressed() {
        askForExit();
    }

    private void askForExit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you really want to close the app ?");

        builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                myApp.onTerminate();
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("no", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void askForNewTask() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Start New Task");
        builder.setMessage("Would you like to clear all data & start a fresh task ?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startFreshTask();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void startFreshTask() {
        barcode = "";
        tvBarcode.setText("");
        btnOK.setVisibility(View.GONE);
        btnClear.setVisibility(View.GONE);
        myApp.clearStocks();
        showToast("New Task");
    }
}