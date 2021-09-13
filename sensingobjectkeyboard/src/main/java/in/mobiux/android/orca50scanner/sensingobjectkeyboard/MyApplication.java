package in.mobiux.android.orca50scanner.sensingobjectkeyboard;

import android.media.MediaPlayer;
import android.os.Handler;

import in.mobiux.android.orca50scanner.common.utils.App;
import in.mobiux.android.orca50scanner.common.utils.AppBuildConfig;
import in.mobiux.android.orca50scanner.common.utils.AppLogger;
import in.mobiux.android.orca50scanner.common.utils.SessionManager;
import in.mobiux.android.orca50scanner.reader.core.RFIDReader;
import in.mobiux.android.orca50scanner.reader.core.RFIDReaderListener;
import in.mobiux.android.orca50scanner.reader.simulator.AppSimulator;


/**
 * Created by SUJEET KUMAR on 08-Mar-21.
 */
public class MyApplication extends App {


    private String TAG = MyApplication.class.getName();

    private MediaPlayer mediaPlayer;

    @Override
    public void onCreate() {
        super.onCreate();

        logger = AppLogger.getInstance(getApplicationContext());
        logger.i(TAG, "= App Started =\n");
        mHandler = new Handler(getMainLooper());
        session = SessionManager.getInstance(getApplicationContext());

        mediaPlayer = MediaPlayer.create(this, R.raw.beeper_short);


        AppSimulator.initSimulator(this);

        setBuildConfig(appBuildConfig());
    }

    private AppBuildConfig appBuildConfig() {

        AppBuildConfig appBuildConfig = new AppBuildConfig();

        appBuildConfig.DEBUG = BuildConfig.DEBUG;
        appBuildConfig.APPLICATION_ID = BuildConfig.APPLICATION_ID;
        appBuildConfig.BUILD_TYPE = BuildConfig.BUILD_TYPE;
        appBuildConfig.VERSION_CODE = BuildConfig.VERSION_CODE;
        appBuildConfig.VERSION_NAME = BuildConfig.VERSION_NAME;

        return appBuildConfig;
    }

    public void playBeep() {

        logger.i(TAG, "playing beep");
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.beeper_short);
        }
        if (mediaPlayer != null)
            mediaPlayer.start();

    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        logger.i(TAG, "### App Terminated ###");
        System.exit(0);
    }
}
