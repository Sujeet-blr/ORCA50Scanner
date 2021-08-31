package in.mobiux.android.orca50scanner.common.utils;

import android.app.Application;
import android.media.MediaPlayer;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

import in.mobiux.android.orca50scanner.common.R;
import in.mobiux.android.orca50scanner.common.activity.BaseActivity;
import in.mobiux.android.orca50scanner.reader.simulator.AppSimulator;

public class App extends Application {

    private static final String TAG = App.class.getCanonicalName();

    private AppLogger logger;
    private SessionManager session;
    private MediaPlayer mediaPlayer;
    private List<BaseActivity> activities = new ArrayList<>();
    private Handler mHandler;
    private boolean debugBuild = false;

    @Override
    public void onCreate() {
        super.onCreate();

        logger = AppLogger.getInstance(getApplicationContext());
        logger.i(TAG, "= App Started =\n");
        mHandler = new Handler(getMainLooper());
        session = SessionManager.getInstance(getApplicationContext());

        mediaPlayer = MediaPlayer.create(this, R.raw.beeper_short);

        setDebugBuild(debugBuild);
        AppSimulator.initSimulator(this);
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
        return debugBuild;
    }

    public void setDebugBuild(boolean debugBuild) {
        this.debugBuild = debugBuild;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        logger.i(TAG, "### App Terminated ###");
        System.exit(0);
    }

    public void addActivity(BaseActivity activity) {
        activities.add(activity);
    }

    public void removeActivity(BaseActivity activity) {
        activities.remove(activity);
    }
}
