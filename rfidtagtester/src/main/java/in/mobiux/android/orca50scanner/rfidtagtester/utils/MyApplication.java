package in.mobiux.android.orca50scanner.rfidtagtester.utils;

import android.media.MediaPlayer;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

import in.mobiux.android.orca50scanner.common.utils.App;
import in.mobiux.android.orca50scanner.common.utils.AppBuildConfig;
import in.mobiux.android.orca50scanner.reader.simulator.AppSimulator;
import in.mobiux.android.orca50scanner.rfidtagtester.BuildConfig;
import in.mobiux.android.orca50scanner.rfidtagtester.R;
import in.mobiux.android.orca50scanner.rfidtagtester.activity.BaseActivity;

/**
 * Created by SUJEET KUMAR on 08-Mar-21.
 */
public class MyApplication extends App {

    private String TAG = MyApplication.class.getCanonicalName();

    private Handler mHandler;


    private List<BaseActivity> activities = new ArrayList<>();
    private MediaPlayer mediaPlayer;

    @Override
    public void onCreate() {
        super.onCreate();


        logger.i(TAG, "= App Started =\n");
        mHandler = new Handler(getMainLooper());

        mediaPlayer = MediaPlayer.create(this, R.raw.beeper_short);

        setBuildConfig(appBuildConfig());
        AppSimulator.initSimulator(this);
    }

    private AppBuildConfig appBuildConfig() {

        AppBuildConfig appBuildConfig = new AppBuildConfig();

        appBuildConfig.DEBUG = BuildConfig.DEBUG;
        appBuildConfig.APPLICATION_ID = BuildConfig.APPLICATION_ID;
        appBuildConfig.BUILD_TYPE = BuildConfig.BUILD_TYPE;
        appBuildConfig.VERSION_CODE = BuildConfig.VERSION_CODE;
        appBuildConfig.VERSION_NAME = BuildConfig.VERSION_NAME;
//
        return appBuildConfig;
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
