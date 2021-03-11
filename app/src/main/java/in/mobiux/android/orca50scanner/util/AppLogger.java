package in.mobiux.android.orca50scanner.util;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by SUJEET KUMAR on 10-Mar-21.
 */
public class AppLogger {

    private Context context;
    private static AppLogger instance;
    private StringBuilder data;
    FileOutputStream out;

    private AppLogger(Context context) {
        this.context = context;
        data = new StringBuilder();

    }

    public static AppLogger getInstance(Context context) {
        if (instance == null) {
            instance = new AppLogger(context);
        }
        return instance;
    }

    public void i(String tag, String msg) {
        Log.i(tag, msg);
        data = new StringBuilder(("\n" + tag + "\t:>, " + msg + "\t," + String.valueOf(AppUtils.getFormattedTimestamp())));
        appendToLogs(data.toString());
    }

    public void createAndExportLogs(Context context) {
//        must check storage permission before calling this method

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

//        FileOutputStream out = null;
        try {
//            out = context.openFileOutput("logs.csv", Context.MODE_PRIVATE);
//            out.write((data.toString()).getBytes());
//            out.close();
//            out.flush();


            File fileLocation = new File(context.getFilesDir(), "logs.csv");
            Uri path = FileProvider.getUriForFile(context, "in.mobiux.android.orca50scanner.fileprovider", fileLocation);
            Intent fileIntent = new Intent(Intent.ACTION_SEND);
            fileIntent.setType("text/csv");
            fileIntent.putExtra(Intent.EXTRA_SUBJECT, "Logs Data");
            fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            fileIntent.putExtra(Intent.EXTRA_STREAM, path);
            context.startActivity(Intent.createChooser(fileIntent, "Export logs"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void appendToLogs(String data) {
        if (context == null)
            return;

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            return;
        try {
            out = context.openFileOutput("logs.csv", Context.MODE_APPEND);
            out.write(data.getBytes());
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
