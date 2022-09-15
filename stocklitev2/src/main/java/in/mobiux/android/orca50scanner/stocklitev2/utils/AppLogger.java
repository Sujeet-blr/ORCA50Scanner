package in.mobiux.android.orca50scanner.stocklitev2.utils;

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

import in.mobiux.android.commonlibs.utils.AppUtils;
import in.mobiux.android.orca50scanner.stocklitev2.BuildConfig;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;


/**
 * Created by SUJEET KUMAR on 10-Mar-21.
 */
public class AppLogger {

    private Context context;
    private static AppLogger instance;
    private StringBuilder data;
    private List<String> logs = new ArrayList<>();
    FileOutputStream out;

    private String appName = "";
    private String logFileName = "";

    private AppLogger(Context context) {
        this.context = context;
        data = new StringBuilder();
        appName = context.getPackageManager().getApplicationLabel(context.getApplicationInfo()).toString();
        logFileName = appName + "_logs.csv";
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

    public void d(String tag, String msg) {
        i(tag, msg);
    }

    public void e(String tag, String msg) {
        i(tag + " ERROR", msg);
    }


    //    instead of calling this method - open 'ExportLogsActivity.class' to export logs.
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


            File fileLocation = new File(context.getFilesDir(), logFileName);
            Uri path = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", fileLocation);
            Intent fileIntent = new Intent(Intent.ACTION_SEND);
            fileIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);
            fileIntent.setType("text/csv");
            fileIntent.putExtra(Intent.EXTRA_SUBJECT, appName + "_logs");
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
                out = context.openFileOutput(logFileName, Context.MODE_APPEND);
                out.write(s.getBytes());
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            iterator.remove();
        }
    }
}
