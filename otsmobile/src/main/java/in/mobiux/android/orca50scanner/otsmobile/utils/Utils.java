package in.mobiux.android.orca50scanner.otsmobile.utils;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Utils {

    public static String encodeBase64(String text) {

        byte[] data = text.getBytes(StandardCharsets.UTF_8);
        String base64 = Base64.encodeToString(text.getBytes(), Base64.NO_WRAP);
        return base64.trim();
    }

    public static String decodeBase64(String base64) {

        byte[] data = Base64.decode(base64, Base64.DEFAULT);
        String text = new String(data, StandardCharsets.UTF_8);
        return text.trim();
    }

    public static String getFormattedCurrentTime() {
        Date currentDate = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String time = dateFormat.format(currentDate);
        return time;

    }

}
