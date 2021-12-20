package in.mobiux.android.orca50scanner.solocateasset.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import in.mobiux.android.orca50scanner.reader.activity.SettingsActivity;
import in.mobiux.android.orca50scanner.solocateasset.R;

public class MainActivity extends BaseActivity {

    private static final int PICK_FILE = 21;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

//                    labels.clear();
                    while ((line = dataRead.readNext()) != null) {
                        Long barcodeNumber = Long.valueOf(line[0]);
                        logger.i(TAG, "data is " + barcodeNumber);
//                        labels.add(String.valueOf(barcodeNumber));
                    }

                    dataRead.close();
//                    labelsAdapter.notifyDataSetChanged();


                } catch (IOException | CsvValidationException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}