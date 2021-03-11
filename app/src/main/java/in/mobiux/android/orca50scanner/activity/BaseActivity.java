package in.mobiux.android.orca50scanner.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import in.mobiux.android.orca50scanner.R;
import in.mobiux.android.orca50scanner.api.model.Inventory;
import in.mobiux.android.orca50scanner.util.AppLogger;

/**
 * Created by SUJEET KUMAR on 08-Mar-21.
 */
public class BaseActivity extends AppCompatActivity {

    protected AppLogger logger;
    public View parentLayout;
    public static int STORAGE_PERMISSION_CODE = 121;
    public static int CAMERA_PERMISSION_CODE = 123;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logger = AppLogger.getInstance(getApplicationContext());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (parentLayout == null) {
            parentLayout = findViewById(android.R.id.content);
        }
    }

    // Function to check and request permission.
    public void checkPermission(BaseActivity activity, String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(BaseActivity.this, permission)
                == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(BaseActivity.this,
                    new String[]{permission},
                    requestCode);
        } else {
            Toast.makeText(BaseActivity.this,
                    "Permission already granted",
                    Toast.LENGTH_SHORT)
                    .show();
        }
    }

    // This function is called when the user accepts or decline the permission.
    // Request Code is used to check which permission called this function.
    // This request code is provided when the user is prompt for permission.

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this,
                        "Camera Permission Granted",
                        Toast.LENGTH_SHORT)
                        .show();
            } else {
                Toast.makeText(this,
                        "Camera Permission Denied",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        } else if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this,
                        "Storage Permission Granted",
                        Toast.LENGTH_SHORT)
                        .show();
            } else {
                Toast.makeText(this,
                        "Storage Permission Denied",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    protected void exportToCSV(List<Inventory> list) {

//        String fileName = System.currentTimeMillis() + ".csv";
//        File destination = new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
//                fileName);

        StringBuilder data = new StringBuilder();
//        data.append("")

        for (Inventory inventory : list) {
            data.append("\n" + inventory.getInventoryId() + "," + inventory.getEpc() + "," + String.valueOf(inventory.getQuantity()));
        }

        try {
//            destination.createNewFile();
            FileOutputStream out = openFileOutput("data.csv", Context.MODE_PRIVATE);
            out.write((data.toString()).getBytes());
            out.close();
            out.flush();

            Context context = getApplicationContext();

            File fileLocation = new File(getFilesDir(), "data.csv");
            Uri path = FileProvider.getUriForFile(context, "in.mobiux.android.orca50scanner.fileprovider", fileLocation);
            Intent fileIntent = new Intent(Intent.ACTION_SEND);
            fileIntent.setType("text/csv");
            fileIntent.putExtra(Intent.EXTRA_SUBJECT, "Exported Data");
            fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            fileIntent.putExtra(Intent.EXTRA_STREAM, path);
            startActivity(Intent.createChooser(fileIntent, "Export"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
