package in.mobiux.android.orca50scanner.common.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by SUJEET KUMAR on 28-Mar-21.
 */
public class SessionManager {

    public static final String TAG = SessionManager.class.getCanonicalName();
    private Context context;
    private SharedPreferences preferences;
    private static SessionManager INSTANCE;
    private static String KEY_TOKEN = "token";


    public String KEY_BEEP = "";
    public String KEY_RF_OUTPUT_POWER = "";
    public String KEY_APP_LANGUAGE = "";

    private void initKeys(Context context) {
        String appName = context.getPackageName();

        KEY_BEEP = appName + "__beep";
        KEY_RF_OUTPUT_POWER = "rf_output_power";
        KEY_APP_LANGUAGE = "app_language";
    }

    public static SessionManager getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new SessionManager(context);
        }
        return INSTANCE;
    }

    private SessionManager(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences("session", Context.MODE_PRIVATE);
        initKeys(context);
    }

    public boolean hasCredentials() {
        if (!rawToken().isEmpty()) {
            return true;
        }
        return false;
    }

    public void saveToken(String token) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

    public String token() {
        return "Token " + preferences.getString(KEY_TOKEN, "");
    }

    public String rawToken() {
        return preferences.getString(KEY_TOKEN, "");
    }

    public void setStringValue(String key, String value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
        AppLogger.getInstance(context).i(TAG, "Saved as key :" + key + " value : " + value);
    }

    public String getStringValue(String key) {
        return preferences.getString(key, "");
    }

    public void setInt(String key, int value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.apply();
        AppLogger.getInstance(context).i(TAG, "Saved as key :" + key + " value : " + value);
    }

    public int getInt(String key, int defaultValue) {
        return preferences.getInt(key, defaultValue);
    }

    public void setBooleanValue(String key, boolean value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
        AppLogger.getInstance(context).i(TAG, "Saved as key :" + key + " value : " + value);
    }

    public Boolean getBooleanValue(String key) {
        return preferences.getBoolean(key, true);
    }

    public void logout() {
        saveToken("");
//        setUser(null);
    }

    public LanguageUtils.Language getLanguage() {
        return LanguageUtils.Language.valueByAttr(preferences.getString(KEY_APP_LANGUAGE, "en"));
    }

    public void setLanguage(LanguageUtils.Language language) {
        setStringValue(KEY_APP_LANGUAGE, language.getLanguage());
    }


}
