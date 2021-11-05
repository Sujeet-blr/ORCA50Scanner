package in.mobiux.android.orca50scanner.sologistics.utils;

import android.media.MediaPlayer;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

import in.mobiux.android.orca50scanner.common.utils.App;
import in.mobiux.android.orca50scanner.common.utils.AppBuildConfig;
import in.mobiux.android.orca50scanner.reader.simulator.AppSimulator;
import in.mobiux.android.orca50scanner.sologistics.BuildConfig;
import in.mobiux.android.orca50scanner.sologistics.R;
import in.mobiux.android.orca50scanner.sologistics.activity.BaseActivity;

/**
 * Created by SUJEET KUMAR on 08-Mar-21.
 */
public class MyApplication extends App {

    private String TAG = MyApplication.class.getCanonicalName();

    private List<BaseActivity> activities = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();


        logger.i(TAG, "= App Started =\n");
        mHandler = new Handler(getMainLooper());

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
