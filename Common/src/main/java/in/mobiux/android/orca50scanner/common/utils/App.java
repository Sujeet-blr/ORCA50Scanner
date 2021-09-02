package in.mobiux.android.orca50scanner.common.utils;

import android.app.Activity;
import android.app.Application;
import android.media.MediaPlayer;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

import in.mobiux.android.orca50scanner.common.R;

public class App extends Application {

    private static final String TAG = App.class.getCanonicalName();

    protected AppLogger logger;
    protected SessionManager session;
    private MediaPlayer mediaPlayer;
    private List<Activity> activities = new ArrayList<>();
    protected Handler mHandler;
    public static AppBuildConfig AppBuildConfig;

    @Override
    public void onCreate() {
        super.onCreate();

        logger = AppLogger.getInstance(getApplicationContext());
        logger.i(TAG, "= App Started =\n");
        mHandler = new Handler(getMainLooper());
        session = SessionManager.getInstance(getApplicationContext());

        mediaPlayer = MediaPlayer.create(this, R.raw.beeper_short);

    }


    public void playBeep() {

        logger.i(TAG, "playing beep");
        try {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(this, R.raw.beeper_short);
            }
            if (mediaPlayer != null)
                mediaPlayer.start();
        } catch (Exception e) {
            logger.e(TAG, "Error is playing beep " + e.getLocalizedMessage());
        }

    }

    public boolean isDebugBuild() {
        return AppBuildConfig.DEBUG;
    }

    public AppBuildConfig getBuildConfig() {
        return AppBuildConfig;
    }

    public void setBuildConfig(AppBuildConfig appBuildConfig) {
        this.AppBuildConfig = appBuildConfig;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        logger.i(TAG, "### App Terminated ###");
        System.exit(0);
    }

    public void addActivity(Activity activity) {
        activities.add(activity);
    }

    public void removeActivity(Activity activity) {
        activities.remove(activity);
    }
}
