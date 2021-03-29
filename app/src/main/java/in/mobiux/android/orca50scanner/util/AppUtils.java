package in.mobiux.android.orca50scanner.util;

import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import in.mobiux.android.orca50scanner.api.model.Inventory;

/**
 * Created by SUJEET KUMAR on 10-Mar-21.
 */
public class AppUtils {

    static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS'Z'");
    static String format;

    public static String getFormattedTimestamp() {

        try {
            format = simpleDateFormat.format(new Date());
        } catch (Exception e) {
            return "" + format;
        }
        return format;
    }

    public static Inventory getMatchingInventory(String strEPC, List<Inventory> list) {
        for (Inventory inventory : list) {
            if (inventory.getEpc().equals(strEPC)){
                return inventory;
            }
        }
        return null;
    }
}
