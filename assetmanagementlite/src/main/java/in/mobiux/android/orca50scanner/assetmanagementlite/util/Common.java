package in.mobiux.android.orca50scanner.assetmanagementlite.util;

import android.content.Context;

import java.io.File;

import in.mobiux.android.orca50scanner.assetmanagementlite.R;


public class Common {
    private static final String TAG = Common.class.getCanonicalName();

    public static String getAppPath(Context context) {

        String path = "";
        File dir = new File(android.os.Environment.getExternalStorageDirectory()
                + File.separator
                + context.getResources().getString(R.string.app_name)
                + File.separator);

        if (!dir.exists()) {
            dir.mkdir();
        }

        path = dir.getPath() + File.separator;
        return path;
    }
}
