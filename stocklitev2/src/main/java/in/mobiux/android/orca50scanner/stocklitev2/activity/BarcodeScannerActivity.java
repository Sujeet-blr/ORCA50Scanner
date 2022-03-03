package in.mobiux.android.orca50scanner.stocklitev2.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.zebra.sdl.BarcodeReaderBaseActivity;

import org.jetbrains.annotations.NotNull;

import in.mobiux.android.orca50scanner.stocklitev2.R;
import in.mobiux.android.orca50scanner.stocklitev2.utils.MyApplication;

public class BarcodeScannerActivity extends BarcodeReaderBaseActivity {

    private TextView tvMessage;
    private Button btnOK, btnClear;
    private String barcode = "";
    private MyApplication myApp;
    private EditText edtBarcode;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scanner);

        edtBarcode = findViewById(R.id.edtBarcode);
        tvMessage = findViewById(R.id.tvMessage);
        btnOK = findViewById(R.id.btnOK);
        btnClear = findViewById(R.id.btnClear);
        edtBarcode.setText("");

        getSupportActionBar().setTitle(R.string.app_name);

        myApp = (MyApplication) getApplicationContext();

        btnOK.setOnClickListener(view -> {
            barcode = edtBarcode.getText().toString();

            if (barcode.isEmpty()) {
                return;
            }
            Intent intent = new Intent(getApplicationContext(), RFIDScannerActivity.class);
            intent.putExtra("barcode", barcode);
            startActivityForResult(intent, 201);
        });

        btnClear.setOnClickListener(view -> {
            barcode = "";
            edtBarcode.setText(barcode);
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
        } else if (id == R.id.action_app_settings) {
            startActivity(new Intent(app, AppSettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBarcodeScan(String barcode) {
        super.onBarcodeScan(barcode);
        barcode = barcode.trim();
        this.barcode = barcode;
        edtBarcode.setText(barcode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 201 && resultCode == RESULT_FIRST_USER) {
            barcode = "";
            edtBarcode.setText("");
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
        edtBarcode.setText("");
        myApp.clearStocks();
    }
}