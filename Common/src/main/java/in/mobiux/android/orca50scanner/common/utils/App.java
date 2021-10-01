package in.mobiux.android.orca50scanner.common.utils;

import android.app.Activity;
import android.app.Application;
import android.media.MediaPlayer;
import android.os.Handler;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import in.mobiux.android.orca50scanner.common.R;

public class App extends Application {

    private static String TAG = App.class.getCanonicalName();

    protected AppLogger logger;
    protected SessionManager session;
    private MediaPlayer mediaPlayer;
    private List<Activity> activities = new ArrayList<>();
    protected Handler mHandler;
    public static AppBuildConfig AppBuildConfig;

    @Override
    public void onCreate() {
        super.onCreate();

        TAG = this.getClass().getCanonicalName();

        logger = AppLogger.getInstance(getApplicationContext());
        logger.i(TAG, "= App Started =\n");
        mHandler = new Handler(getMainLooper());
        session = SessionManager.getInstance(getApplicationContext());

        mediaPlayer = MediaPlayer.create(this, R.raw.beeper_short);

//        System.out.println(getApplicationContext().getApplicationInfo().nativeLibraryDir);
        logger.i(TAG, "native library " + getApplicationContext().getApplicationInfo().nativeLibraryDir);
        checkNativeLibrary();
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

    private void checkNativeLibrary() {

        try {
            Set<String> libs = new HashSet<String>();
            String mapsFile = "/proc/" + android.os.Process.myPid() + "/maps";
            BufferedReader reader = new BufferedReader(new FileReader(mapsFile));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.endsWith(".so")) {
                    int n = line.lastIndexOf(" ");
                    libs.add(line.substring(n + 1));
                }
            }

            logger.i(TAG, libs.size() + " libraries:");
            for (String lib : libs) {
                logger.i(TAG, lib);
            }
        } catch (FileNotFoundException e) {
            logger.e(TAG, "" + e.getLocalizedMessage());
            // Do some error handling...
        } catch (IOException e) {
            // Do some error handling...
            logger.e(TAG, "" + e.getLocalizedMessage());
        }
    }
}
