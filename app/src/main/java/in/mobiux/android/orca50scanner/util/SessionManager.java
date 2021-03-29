package in.mobiux.android.orca50scanner.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by SUJEET KUMAR on 28-Mar-21.
 */
public class SessionManager {

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
}
