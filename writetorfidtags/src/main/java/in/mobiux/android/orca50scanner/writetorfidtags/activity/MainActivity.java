package in.mobiux.android.orca50scanner.writetorfidtags.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import in.mobiux.android.orca50scanner.reader.core.RFIDReader;
import in.mobiux.android.orca50scanner.reader.core.RFIDReaderListener;
import in.mobiux.android.orca50scanner.reader.core.Reader;
import in.mobiux.android.orca50scanner.reader.model.Inventory;
import in.mobiux.android.orca50scanner.reader.model.OperationTag;
import in.mobiux.android.orca50scanner.writetorfidtags.R;

public class MainActivity extends BaseActivity {

    private Button btnUpload, btnAssign;
    private Spinner spnrLabels, spnrTags;
    private TextView tvMessage;

    private List<String> labels = new ArrayList<>();
    private HashMap<String, Inventory> tags = new HashMap<>();
    private List<Inventory> tagList = new ArrayList<>();

    private ArrayAdapter<String> labelsAdapter;
    private ArrayAdapter<Inventory> tagsAdapter;

    private RFIDReader rfidReader;
    private RFIDReaderListener rfidReaderListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvMessage = findViewById(R.id.tvMessage);
        btnUpload = findViewById(R.id.btnUpload);
        btnAssign = findViewById(R.id.btnAssign);
        spnrLabels = findViewById(R.id.spnrLabels);
        spnrTags = findViewById(R.id.spnrTags);


        rfidReader = new RFIDReader(getApplicationContext());
        rfidReader.connect(Reader.ReaderType.RFID);

        registerRfidListener();

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFileChooser();
            }
        });

        labelsAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, labels);
        labelsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnrLabels.setAdapter(labelsAdapter);

        tagsAdapter = new ArrayAdapter<Inventory>(MainActivity.this, android.R.layout.simple_spinner_item, tagList);
        tagsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnrTags.setAdapter(tagsAdapter);

        spnrLabels.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spnrTags.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (rfidReader.isConnected()) {
                    Inventory inventory = tagList.get(spnrTags.getSelectedItemPosition());
                    int selectStatus = rfidReader.selectAccessEpcMatch(inventory.getEpc());

                    if (selectStatus == 0) {
                        tvMessage.setText("Selected RFID Tag is " + inventory.getEpc());
                        btnAssign.setVisibility(View.VISIBLE);
                    } else {
                        tvMessage.setText("RFID tag is not selected, pls repeat");
                        btnAssign.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                btnAssign.setVisibility(View.GONE);
            }
        });


    }

    private void registerRfidListener() {

        rfidReaderListener = new RFIDReaderListener() {
            @Override
            public void onScanningStatus(boolean status) {
                if (status) {
                    tvMessage.setText("Scanning");
                } else {
                    tvMessage.setText("pull trigger to scan");
                }
            }

            @Override
            public void onInventoryTag(Inventory inventory) {
                tags.put(inventory.getFormattedEPC(), inventory);
            }

            @Override
            public void onOperationTag(OperationTag operationTag) {
                tvMessage.setText("Assigned Success to " + operationTag.strEPC);
            }

            @Override
            public void onInventoryTagEnd(Inventory.InventoryTagEnd tagEnd) {
                tagList.clear();
                tagList.addAll(tags.values());
                tagsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onConnection(boolean status) {
                if (status) {
                    showToast(R.string.connected);
                } else {
                    showToast(R.string.connection_lost);
                }
            }
        };

        rfidReader.setOnRFIDReaderListener(rfidReaderListener);
    }

    // Request code for selecting a PDF document.
    private static final int PICK_PDF_FILE = 2;

    private void openFile(Uri pickerInitialUri) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/csv");

        // Optionally, specify a URI for the file that should appear in the
        // system file picker when it loads.
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);

        startActivityForResult(intent, PICK_PDF_FILE);
    }

    private void selectCSVFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("application/csv");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Open CSV"), PICK_PDF_FILE);
    }


    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), PICK_PDF_FILE);
        } catch (Exception e) {
            logger.e(TAG, " choose file error " + e.toString());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
//        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == PICK_PDF_FILE
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

                    labels.clear();
                    while ((line = dataRead.readNext()) != null) {
                        logger.i(TAG, "data is " + line[0]);
                        labels.add(line[0]);
                    }

                    dataRead.close();
                    labelsAdapter.notifyDataSetChanged();


                } catch (IOException | CsvValidationException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}