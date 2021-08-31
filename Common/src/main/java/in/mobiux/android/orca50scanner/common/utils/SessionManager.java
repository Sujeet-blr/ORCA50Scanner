package in.mobiux.android.orca50scanner.common.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by SUJEET KUMAR on 28-Mar-21.
 */
public class SessionManager {

    public static final String TAG = SessionManager.class.getCanonicalName();
    private Context context;
    private SharedPreferences preferences;
    private static SessionManager INSTANCE;

    public static SessionManager getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new SessionManager(context);
        }
        return INSTANCE;
    }

    private SessionManager(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences("session", Context.MODE_PRIVATE);

//        setting initial default value
        if (getValue("rssi").isEmpty()) {
            setValue("rssi", "80");
        }
        if (getValue("beeperMode").isEmpty()) {
            setValue("beeperMode", "1");
        }
    }

    public void saveToken(String token) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("token", token);
        editor.apply();
    }

    public String token() {
        return "Token " + preferences.getString("token", "");
    }

    public String rawToken() {
        return preferences.getString("token", "");
    }

    public void setValue(String key, String value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
        AppLogger.getInstance(context).i(TAG, "Saved as key :" + key + " value :" + value);
    }

    public String getValue(String key) {
        return preferences.getString(key, "");
    }
}
