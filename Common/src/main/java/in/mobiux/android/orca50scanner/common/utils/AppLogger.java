package in.mobiux.android.orca50scanner.common.utils;

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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import in.mobiux.android.orca50scanner.BuildConfig;

/**
 * Created by SUJEET KUMAR on 10-Mar-21.
 */
public class AppLogger {

    private Context context;
    private static AppLogger instance;
    private StringBuilder data;
    private List<String> logs = new ArrayList<>();
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

    public synchronized void i(String tag, String msg) {
        Log.i(tag, msg);
        data = new StringBuilder(("\n" + BuildConfig.VERSION_CODE + tag + "\t, " + msg + "\t," + String.valueOf(AppUtils.getFormattedTimestamp())));
        logs.add(data.toString());
        appendToLogs(logs);
    }

    public void e(String tag, String msg) {
        i("ERROR " + tag, msg);
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

    private void appendToLogs(List<String> logs) {
        if (context == null)
            return;

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            return;

        Iterator<String> iterator = logs.listIterator();
        while (iterator.hasNext()) {

            String s = iterator.next();
            try {
                out = context.openFileOutput("logs.csv", Context.MODE_APPEND);
                out.write(s.getBytes());
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            iterator.remove();
        }
    }
}