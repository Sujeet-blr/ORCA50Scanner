package in.mobiux.android.orca50scanner.stocklitev2.utils;

import in.mobiux.android.orca50scanner.stocklitev2.BuildConfig;
import in.mobiux.android.orca50scanner.stocklitev2.R;

public class Util {

    private static int theme = R.style.Theme_AppTheme;

    public static int getTheme() {
        if (BuildConfig.FLAVOR.equalsIgnoreCase("adept")) {
            theme = R.style.Theme_AdeptSpace;
        } else if (BuildConfig.FLAVOR.equalsIgnoreCase("merit")) {
            theme = R.style.Theme_Merit;
        }
        return theme;
    }
}


