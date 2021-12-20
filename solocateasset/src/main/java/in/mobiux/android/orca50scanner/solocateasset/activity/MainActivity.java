package in.mobiux.android.orca50scanner.solocateasset.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.mobiux.android.orca50scanner.reader.activity.RFIDReaderBaseActivity;
import in.mobiux.android.orca50scanner.reader.activity.SettingsActivity;
import in.mobiux.android.orca50scanner.reader.model.Inventory;
import in.mobiux.android.orca50scanner.solocateasset.R;

public class MainActivity extends RFIDReaderBaseActivity {

    private static final int PICK_FILE = 21;

    private Inventory selectedAsset;
    private List<Inventory> assets = new ArrayList<>();
    private Map<String, Inventory> uniqueAssets = new HashMap<>();
    private Spinner spinnerAssets;

    private ArrayAdapter<Inventory> tagsAdapter;

    private SeekBar seekBar;
    private TextView tvRSSIValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinnerAssets = findViewById(R.id.spinnerAssets);
        seekBar = findViewById(R.id.seekBar);
        tvRSSIValue = findViewById(R.id.tvRSSIValue);

        tagsAdapter = new ArrayAdapter<Inventory>(MainActivity.this, android.R.layout.simple_spinner_item, assets);
        tagsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAssets.setAdapter(tagsAdapter);

        spinnerAssets.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedAsset = assets.get(position);
                logger.i(TAG, selectedAsset.getEpc());
                seekBar.setProgress(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                tvRSSIValue.setText(progress + " %");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        seekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });

    }

    @Override
    public void onInventoryTag(Inventory inventory) {
        super.onInventoryTag(inventory);

        logger.i(TAG, inventory.getEpc());
        if (selectedAsset != null) {
            if (selectedAsset.getFormattedEPC().equalsIgnoreCase(inventory.getFormattedEPC())) {
                selectedAsset.setRssi(inventory.getRssi());
                int rssi = 0;
                rssi = Integer.parseInt(inventory.getRssi());
                seekBar.setProgress(rssi);
            }
        } else {
            Toast.makeText(app, "Select an Asset to Locate", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull @NotNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            SettingsActivity.launchActivity(getApplicationContext());
        } else if (item.getItemId() == R.id.action_upload) {
            showFileChooser();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), PICK_FILE);
        } catch (Exception e) {
            logger.e(TAG, " choose file error " + e.toString());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
//        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == PICK_FILE
                && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                // Perform operations on the document using its URI.

                try {
                    InputStream inputStream = getContentResolver().openInputStream(resultData.getData());
                    CSVReader dataRead = new CSVReader(new InputStreamReader(inputStream));
//                    CSVReader dataRead = new CSVReader(new FileReader(new File(resultData.getData().getPath())));

                    String[] line = null;

                    assets.clear();
                    while ((line = dataRead.readNext()) != null) {
                        Inventory asset = new Inventory();
                        asset.setEpc(line[0]);
                        asset.setName(line[1]);
//                        Long barcodeNumber = Long.valueOf(line[0]);
//                        logger.i(TAG, "data is " + barcodeNumber);
                        assets.add(asset);
                        uniqueAssets.put(asset.getFormattedEPC(), asset);
                    }

                    dataRead.close();
//                    labelsAdapter.notifyDataSetChanged();
                    tagsAdapter.notifyDataSetChanged();


                } catch (IOException | CsvValidationException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}