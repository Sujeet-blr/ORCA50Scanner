package in.mobiux.android.orca50scanner.util;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;

import java.util.Locale;

import in.mobiux.android.orca50scanner.activity.BaseActivity;

import static in.mobiux.android.orca50scanner.util.LanguageUtils.Language.DUTCH;
import static in.mobiux.android.orca50scanner.util.LanguageUtils.Language.ENGLISH;
import static in.mobiux.android.orca50scanner.util.LanguageUtils.Language.FRENCH;
import static in.mobiux.android.orca50scanner.util.LanguageUtils.Language.GERMAN;

public class LanguageUtils {

    private static final String TAG = LanguageUtils.class.getCanonicalName();
    private Context context;
    private AppLogger logger;
    private SessionManager session;

    public enum Language {

        ENGLISH("en"),
        GERMAN("de"),
        FRENCH("fr"),
        DUTCH("nl");

        private String language;

        Language(String language) {
            this.language = language;
        }

        public String getLanguage() {
            return language == null ? "en" : language;
        }

        public static Language valueByAttr(String key) {
            for (Language l : Language.values()) {
                if (l.getLanguage().equals(key)) {
                    return l;
                }
            }
            return ENGLISH;
        }
    }

    public LanguageUtils(Context context) {
        this.context = context;
        logger = AppLogger.getInstance(context);
        session = SessionManager.getInstance(context);
    }

    public void switchLanguage(BaseActivity activity, Language language) {

        logger.i(TAG, "Switching Language to " + language);
        Resources resources = context.getApplicationContext().getResources();
        Configuration config = resources.getConfiguration();
        DisplayMetrics dm = resources.getDisplayMetrics();
        if (language.equals(ENGLISH)) {
            config.locale = Locale.ENGLISH;
        } else if (language.equals(GERMAN)) {
            config.locale = Locale.GERMAN;
        } else if (language.equals(FRENCH)) {
            config.locale = Locale.FRENCH;
        } else if (language.equals(DUTCH)) {
            config.locale = new Locale("nl");
        } else {
            config.locale = Locale.ENGLISH;
        }

        resources.updateConfiguration(config, dm);

        activity.onConfigurationChanged(config);
        session.setLanguage(language);
    }
}
