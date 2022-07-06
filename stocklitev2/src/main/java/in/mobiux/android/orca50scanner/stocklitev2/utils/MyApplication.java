package in.mobiux.android.orca50scanner.stocklitev2.utils;

import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

import in.mobiux.android.orca50scanner.common.utils.App;
import in.mobiux.android.orca50scanner.common.utils.AppBuildConfig;
import in.mobiux.android.orca50scanner.stocklitev2.BuildConfig;
import in.mobiux.android.orca50scanner.stocklitev2.R;
import in.mobiux.android.orca50scanner.stocklitev2.activity.BaseActivity;
import in.mobiux.android.orca50scanner.stocklitev2.model.Stock;

/**
 * Created by SUJEET KUMAR on 08-Mar-21.
 */
public class MyApplication extends App {

    private String TAG = MyApplication.class.getCanonicalName();

    private List<BaseActivity> activities = new ArrayList<>();
    private List<Stock> stocks = new ArrayList<>();
    public String[] scannedRooms = new String[2];

    @Override
    public void onCreate() {
        super.onCreate();
//
//        if (BuildConfig.FLAVOR.equalsIgnoreCase("adept")) {
//            setTheme(R.style.Theme_AdeptSpace);
//        } else {
//            setTheme(R.style.Theme_Merit);
//        }
        logger.i(TAG, "= App Started =\n");
        mHandler = new Handler(getMainLooper());
        resetScannedRoomList();

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

    public void addStock(Stock stock) {
        if (!stocks.contains(stock)) {
            stocks.add(stock);
            addToScannedRoomList(stock.getBarcode());
        }

    }

    public void removeStock(Stock stock) {
        stocks.remove(stock);
    }

    public List<Stock> getStocks() {
        return stocks;
    }

    public void clearStocks() {
        stocks.clear();
    }

    public void addToScannedRoomList(String barcode) {
        scannedRooms[1] = scannedRooms[0];
        scannedRooms[0] = barcode;

        session.setStringValue("room1", scannedRooms[0]);
        session.setStringValue("room2", scannedRooms[1]);
    }

    public void resetScannedRoomList() {
        scannedRooms[0] = session.getStringValue("room1");
        scannedRooms[1] = session.getStringValue("room2");
    }
}
