package in.mobiux.android.orca50scanner.util;

import android.util.Log;
import android.widget.Toast;

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
        String formattedEPC = strEPC.replace(" ", "");
        for (Inventory inventory : list) {
            if (formattedEPC.equals(inventory.getFormattedEPC())) {
                return inventory;
            }
        }
        return null;
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
