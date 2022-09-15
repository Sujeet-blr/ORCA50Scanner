package in.mobiux.android.orca50scanner.stocklitev2.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

import in.mobiux.android.commonlibs.utils.LanguageUtils;

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
    public String KEY_READER_TYPE = "";
    private String appName = "";

    private void initKeys(Context context) {

        KEY_BEEP = "beep";
        KEY_RF_OUTPUT_POWER = "rf_output_power";
        KEY_APP_LANGUAGE = "app_language";
        KEY_READER_TYPE = appName + "_reader_type";
    }

    public static SessionManager getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new SessionManager(context);
        }
        return INSTANCE;
    }

    private SessionManager(Context context) {
        this.context = context;
        appName = context.getPackageName();
//        preferences = context.getSharedPreferences("session", Context.MODE_PRIVATE);
        this.preferences = context.getSharedPreferences(this.appName + "_session", 0);
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
    }

    public String getStringValue(String key) {
        return preferences.getString(key, "");
    }

    public void setStringSet(String key, Set<String> value) {
        SharedPreferences.Editor editor = preferences.edit();
        Set<String> set = new HashSet<>(value);
        editor.remove(key);
        editor.commit();
        editor.putStringSet(key, set);
        editor.apply();
    }

    public Set<String> getStringSet(String key) {
        return preferences.getStringSet(key, new HashSet<>());
    }

    public void setInt(String key, int value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public int getInt(String key, int defaultValue) {
        return preferences.getInt(key, defaultValue);
    }

    public void setBooleanValue(String key, boolean value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public Boolean getBooleanValue(String key) {
        return preferences.getBoolean(key, true);
    }

    public void logout() {
        saveToken("");
//        setUser(null);
    }

//    public LanguageUtils.Language getLanguage() {
//        return LanguageUtils.Language.valueByAttr(preferences.getString(KEY_APP_LANGUAGE, "en"));
//    }

    public void setLanguage(LanguageUtils.Language language) {
        setStringValue(KEY_APP_LANGUAGE, language.getLanguage());
    }


}
