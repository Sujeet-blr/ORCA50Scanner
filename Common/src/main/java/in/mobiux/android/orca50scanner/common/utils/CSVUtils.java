package in.mobiux.android.orca50scanner.common.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;


public class CSVUtils {

    private static final String TAG = CSVUtils.class.getCanonicalName();
    private Context context;
    private AppLogger logger;

    private String appName = "";
    private String logFileName = "";
    private FileOutputStream out;

    public CSVUtils(Context context) {
        this.context = context;
        logger = AppLogger.getInstance(context);

        appName = context.getPackageManager().getApplicationLabel(context.getApplicationInfo()).toString();
        logFileName = appName + "_data_" + String.valueOf(AppUtils.getFormattedTimestamp()) + "_.csv";
    }


    public void writeToColumn(List<String> logs) {
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

    public void createAndExportLogs(Activity context) {
//        must check storage permission before calling this method

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        try {


            File fileLocation = new File(context.getFilesDir(), logFileName);
            Uri path = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", fileLocation);
            Intent fileIntent = new Intent(Intent.ACTION_SEND);
            fileIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);
            fileIntent.setType("text/csv");
//            fileIntent.putExtra(Intent.EXTRA_SUBJECT, appName + "_data");
            fileIntent.putExtra(Intent.EXTRA_SUBJECT, logFileName);
            fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            fileIntent.putExtra(Intent.EXTRA_STREAM, path);
            context.startActivityForResult(Intent.createChooser(fileIntent, "Export data"), 21);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
