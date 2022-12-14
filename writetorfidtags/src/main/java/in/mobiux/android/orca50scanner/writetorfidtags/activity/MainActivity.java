package in.mobiux.android.orca50scanner.writetorfidtags.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import in.mobiux.android.orca50scanner.common.utils.AppUtils;
import in.mobiux.android.orca50scanner.reader.activity.SettingsActivity;
import in.mobiux.android.orca50scanner.reader.core.RFIDReader;
import in.mobiux.android.orca50scanner.reader.core.RFIDReaderListener;
import in.mobiux.android.orca50scanner.reader.core.Reader;
import in.mobiux.android.orca50scanner.reader.model.Barcode;
import in.mobiux.android.orca50scanner.reader.model.Inventory;
import in.mobiux.android.orca50scanner.reader.model.OperationTag;
import in.mobiux.android.orca50scanner.writetorfidtags.R;

public class MainActivity extends BaseActivity {

    private Button btnUpload, btnAssign;
    private Spinner spnrLabels, spnrTags;
    private TextView tvMessage;
    private ImageView ivSettings;

    private List<String> labels = new ArrayList<>();
    private HashMap<String, Inventory> tags = new HashMap<>();
    private List<Inventory> tagList = new ArrayList<>();

    private ArrayAdapter<String> labelsAdapter;
    private ArrayAdapter<Inventory> tagsAdapter;

    private RFIDReader rfidReader;
    private RFIDReaderListener rfidReaderListener;
    private int selectStatus = 1;
    private String selectedLabel = "";
    private Inventory selectedInventory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvMessage = findViewById(R.id.tvMessage);
        btnUpload = findViewById(R.id.btnUpload);
        btnAssign = findViewById(R.id.btnAssign);
        spnrLabels = findViewById(R.id.spnrLabels);
        ivSettings = findViewById(R.id.ivSettings);
        spnrTags = findViewById(R.id.spnrTags);
        btnAssign.setVisibility(View.GONE);

        ivSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SettingsActivity.launchActivity(getApplicationContext());
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFileChooser();
            }
        });

        btnAssign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logger.i(TAG, "clicked on btnAssign");

                if (TextUtils.isEmpty(selectedLabel) || selectedInventory == null || selectStatus == 1) {
                    showToast("Select Label & Tags properly to proceed");
                    return;
                }

                if (selectStatus == 0) {

                    selectStatus = rfidReader.selectAccessEpcMatch(selectedInventory.getEpc());

                    new Handler(getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Barcode barcode = new Barcode();
                            barcode.setName(selectedLabel);
                            barcode.setHex(AppUtils.generateHexEPC(barcode.getName()));
                            rfidReader.writeToTag(barcode, selectedInventory);
                        }
                    }, 500);

                } else {
                    showToast("RFID tag is not selected");
                }
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
                selectedLabel = spnrLabels.getSelectedItem().toString();
                logger.i(TAG, "selected barcode is " + selectedLabel);
                tvMessage.setText("Hex is : " + AppUtils.generateHexEPC(selectedLabel));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                selectedLabel = spnrLabels.getSelectedItem().toString();
                if (!TextUtils.isEmpty(selectedLabel)) {
                    logger.i(TAG, "selected barcode is -" + selectedLabel);
                    tvMessage.setText("Hex is : " + AppUtils.generateHexEPC(selectedLabel));
                }
            }
        });

        spnrTags.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedInventory = tagList.get(spnrTags.getSelectedItemPosition());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                btnAssign.setVisibility(View.GONE);
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

        rfidReader = new RFIDReader(getApplicationContext());
        rfidReader.connect(Reader.ReaderType.RFID);
        registerRfidListener();
    }

    @Override
    protected void onStop() {
        super.onStop();

        tags.clear();
        tagList.clear();
        btnAssign.setVisibility(View.GONE);
        tagsAdapter.notifyDataSetChanged();

        rfidReader.releaseResources();
        rfidReader.unregisterListener(rfidReaderListener);
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
                logger.i(TAG, "scanned tag is " + inventory.getEpc());
                inventory.setName(inventory.getFormattedEPC());
                tags.put(inventory.getFormattedEPC(), inventory);
                selectStatus = 0;
            }

            @Override
            public void onOperationTag(OperationTag operationTag) {
                logger.i(TAG, "onOperationTag is called " + operationTag.strEPC);
                Inventory operatedTag = tags.remove(AppUtils.getFormattedEPC(operationTag.strEPC));
                tagList.remove(operatedTag);
                tagsAdapter.notifyDataSetChanged();
                showMessageDialog("", "Assigned Success to \n\n" + operationTag.strEPC);
            }

            @Override
            public void onInventoryTagEnd(Inventory.InventoryTagEnd tagEnd) {
                tagList.clear();
                tagList.addAll(tags.values());
                tagsAdapter.notifyDataSetChanged();

                if (!TextUtils.isEmpty(selectedLabel))
                    tvMessage.setText("Hex is : " + AppUtils.generateHexEPC(selectedLabel));

                if (tagEnd.mTotalRead > 0) {
                    btnAssign.setVisibility(View.VISIBLE);
                } else {
                    btnAssign.setVisibility(View.GONE);
                }
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
                        Long barcodeNumber = Long.valueOf(line[0]);
                        logger.i(TAG, "data is " + barcodeNumber);
                        labels.add(String.valueOf(barcodeNumber));
                    }

                    dataRead.close();
                    labelsAdapter.notifyDataSetChanged();


                } catch (IOException | CsvValidationException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        rfidReader.releaseResources();
        rfidReader.unregisterListener(rfidReaderListener);
    }


    protected void showMessageDialog(String title, String message) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        builder.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_F4) {
            rfidReader.startScan();
        }
        return super.onKeyDown(keyCode, event);
    }
}