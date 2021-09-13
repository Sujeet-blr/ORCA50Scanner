package in.mobiux.android.orca50scanner.sorfidtobarcode;

import in.mobiux.android.orca50scanner.common.utils.App;
import in.mobiux.android.orca50scanner.common.utils.AppBuildConfig;
import in.mobiux.android.orca50scanner.reader.simulator.AppSimulator;

public class MyApplication extends App {

    @Override
    public void onCreate() {
        super.onCreate();

        setBuildConfig(appBuildConfig());

        AppSimulator.initSimulator(getApplicationContext());
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
}
