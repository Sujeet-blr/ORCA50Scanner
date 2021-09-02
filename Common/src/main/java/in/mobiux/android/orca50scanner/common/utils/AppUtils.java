package in.mobiux.android.orca50scanner.common.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;



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

//    public static Inventory getMatchingInventory(String strEPC, List<Inventory> list) {
//        String formattedEPC = strEPC.replace(" ", "");
//        for (Inventory inventory : list) {
//            if (formattedEPC.equals(inventory.getFormattedEPC())) {
//                return inventory;
//            }
//        }
//        return null;
//    }

    public static String decimalToHex(long value) {
        return Long.toHexString(value).toUpperCase();
    }

    public static String decimalToHex(String value) {
        long l = Long.parseLong(value);
        return Long.toHexString(l).toUpperCase();
    }

    public static String hexToDecimal(String string) {
        try {
            return "" + Long.parseLong(string, 16);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String getFormattedTimestampUpToSeconds() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = null;
        try {
            format = simpleDateFormat.format(new Date());
        } catch (Exception e) {
            return "" + format;
        }
        return format;
    }
}
