package in.mobiux.android.orca50scanner.util;

import android.util.Log;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import in.mobiux.android.orca50scanner.api.model.Inventory;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * Created by SUJEET KUMAR on 10-Mar-21.
 */
public class AppUtils {

    public static final String TAG = AppUtils.class.getCanonicalName();
//    static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS'Z'");
//    static String format;

    public static String getFormattedTimestamp() {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS'Z'");
        String format = null;

        try {
            format = simpleDateFormat.format(new Date());
        } catch (Exception e) {
            return "" + format;
        }
        return format;
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

    public static Inventory getMatchingInventory(String strEPC, List<Inventory> list) {
        String formattedEPC = strEPC.replace(" ", "");
        for (Inventory inventory : list) {
            if (isTagMatching(formattedEPC, inventory.getFormattedEPC())) {
                return inventory;
            }

//            if (formattedEPC.equals(inventory.getFormattedEPC())) {
//                return inventory;
//            }
        }
        return null;
    }

    public static boolean isTagMatching(String strEpc, String epc) {
        String epc1 = strEpc.replace(" ", "");
        epc1 = epc1.toLowerCase();
        epc1 = removeLeadingZerosFromString(epc1);


        String epc2 = epc.replace(" ", "");
        epc2 = epc2.toLowerCase();
        epc2 = removeLeadingZerosFromString(epc2);

        if (epc1.equals(epc2)) {
            return true;
        }

        if (epc1.contains(epc2)) {
            return true;
        }

        if (epc2.contains(epc1)) {
            return true;
        }

        return false;
    }

    private static String removeLeadingZerosFromString(String str) {

        // Count leading zeros
        int i = 0;
        while (i < str.length() && str.charAt(i) == '0')
            i++;

        // Convert str into StringBuffer as Strings
        // are immutable.
        StringBuffer sb = new StringBuffer(str);

        // The  StringBuffer replace function removes
        // i characters from given index (0 here)
        sb.replace(0, i, "");

        return sb.toString();  // return in String
    }

    public static MultipartBody.Part convertFileToRequestBody(File file) {
        RequestBody requestBody = null;
        MultipartBody.Part body = null;
        try {
            requestBody = RequestBody.create(file, MediaType.parse("multipart/form-data"));
            // MultipartBody.Part is used to send also the actual file name
            body = MultipartBody.Part.createFormData("log-file", file.getName(), requestBody);

        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
        }

        return body;
    }
}
