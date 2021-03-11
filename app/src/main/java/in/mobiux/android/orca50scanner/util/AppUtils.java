package in.mobiux.android.orca50scanner.util;

import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by SUJEET KUMAR on 10-Mar-21.
 */
public class AppUtils {

    public static String getFormattedTimestamp() {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS'Z'");
        String format = simpleDateFormat.format(new Date());
        Log.i("Date", format);
        return format;
    }
}
