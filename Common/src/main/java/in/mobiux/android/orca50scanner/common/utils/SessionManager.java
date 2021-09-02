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
            setValue("rssi", "30");
        }
        if (getValue("beeperMode").isEmpty()) {
            setValue("beeperMode", "1");
        }
    }


//    public void setUser(User user) {
//        Gson gson = new Gson();
//        String str;
//        if (user == null) {
//            str = "";
//        } else {
//            str = gson.toJson(user).toString();
//        }
//
//        Log.i(TAG, "saved user is : " + str);
//        SharedPreferences.Editor editor = preferences.edit();
//        editor.putString("user", str);
//        editor.apply();
//    }
//
//    public User getUser() {
//        Gson gson = new Gson();
//        String str = preferences.getString("user", "");
//        if (str.isEmpty()) {
//            Log.i(TAG, str);
//            return null;
//        }
//        User user = gson.fromJson(str, User.class);
//        Log.i(TAG, str);
//        return user;
//    }

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

    public void setValue(String key, String value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
        AppLogger.getInstance(context).i(TAG, "Saved as key :" + key + " value : " + value);
    }

    public String getValue(String key) {
        return preferences.getString(key, "");
    }

    public void logout() {
        saveToken("");
//        setUser(null);
    }

    public LanguageUtils.Language getLanguage() {
        return LanguageUtils.Language.valueByAttr(preferences.getString("language", "en"));
    }

    public void setLanguage(LanguageUtils.Language language) {
        setValue("language", language.getLanguage());
    }
}
