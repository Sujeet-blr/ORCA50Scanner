package in.mobiux.android.orca50scanner.assetmanagementlite.util;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import in.mobiux.android.orca50scanner.assetmanagementlite.BuildConfig;

/**
 * Created by SUJEET KUMAR on 10-Mar-21.
 */
public class AppLogger {

    private static final String TAG = AppLogger.class.getCanonicalName();
    private Context context;
    private static AppLogger instance;
    private StringBuilder data;
    private FileOutputStream out;
    private final String FILE_NAME = "logs.csv";

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

    public synchronized void i(String tag, String msg) {
        if (BuildConfig.DEBUG){
            Log.i(tag, msg);
        }
        data = new StringBuilder(("\n" + tag + "\t, " + msg + "\t," + AppUtils.getFormattedTimestamp()));
        appendToLogs(data.toString());
    }

    public void e(String tag, String msg) {
        i("ERROR " + tag, msg);
    }

    public void createAndExportLogs(Context context) {
//        must check storage permission before calling this method

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        try {

            File fileLocation = new File(context.getFilesDir(), FILE_NAME);
            Uri path = FileProvider.getUriForFile(context, "in.mobiux.android.orca50scanner.sgul.fileprovider", fileLocation);
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

    private void appendToLogs(String s) {
        if (context == null)
            return;

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            return;
        try {
            out = context.openFileOutput(FILE_NAME, Context.MODE_APPEND);
            out.write(s.getBytes());
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clearLogs() {
        if (context == null) {
            return;
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        try {
            File fileLocation = new File(context.getFilesDir(), FILE_NAME);

            if (fileLocation.exists()) {
                fileLocation.delete();
            }

            Log.i(TAG, "log file deleted");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public File getLogFile(Context context) {
//        must check storage permission before calling this method

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            return null;
        }

        try {

            File fileLocation = new File(context.getFilesDir(), FILE_NAME);

            return fileLocation;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
