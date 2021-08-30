package in.mobiux.android.orca50scanner;

import android.app.Activity;
import android.app.Application;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

import in.mobiux.android.orca50scanner.activity.BaseActivity;
import in.mobiux.android.orca50scanner.api.Presenter;
import in.mobiux.android.orca50scanner.database.AppDatabase;
import in.mobiux.android.orca50scanner.util.AppLogger;
import in.mobiux.android.orca50scanner.util.AppSimulator;
import in.mobiux.android.orca50scanner.util.SessionManager;

/**
 * Created by SUJEET KUMAR on 08-Mar-21.
 */
public class MyApplication extends Application {

    private String TAG = MyApplication.class.getName();

    public AppDatabase db;
    public AppLogger logger;

    private Handler mHandler;
    public SessionManager session;

    private List<BaseActivity> activities = new ArrayList<>();
    private MediaPlayer mediaPlayer;

    @Override
    public void onCreate() {
        super.onCreate();

        logger = AppLogger.getInstance(getApplicationContext());
        logger.i(TAG, "= App Started =\n");
        mHandler = new Handler(getMainLooper());
        Presenter.init(getApplicationContext());
        session = SessionManager.getInstance(getApplicationContext());

        mediaPlayer = MediaPlayer.create(this, R.raw.beeper_short);


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
