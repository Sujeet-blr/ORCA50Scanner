package in.mobiux.android.orca50scanner.sgul;

import android.app.Activity;
import android.app.Application;

import java.util.ArrayList;
import java.util.List;

import in.mobiux.android.orca50scanner.common.utils.App;
import in.mobiux.android.orca50scanner.sgul.activity.BaseActivity;
import in.mobiux.android.orca50scanner.sgul.activity.LoginActivity;
import in.mobiux.android.orca50scanner.sgul.api.Presenter;
import in.mobiux.android.orca50scanner.sgul.database.AppDatabase;
import in.mobiux.android.orca50scanner.sgul.database.InventoryDatabase;
import in.mobiux.android.orca50scanner.sgul.database.LaboratoryDatabase;
import in.mobiux.android.orca50scanner.sgul.util.AppLogger;
import in.mobiux.android.orca50scanner.sgul.util.SessionManager;

/**
 * Created by SUJEET KUMAR on 08-Mar-21.
 */
public class MyApplication extends App {

    private static final String TAG = "MyApplication";

    public AppDatabase db;
    public AppLogger logger;
    public SessionManager session;

    public InventoryDatabase inventoryDatabase;
    public LaboratoryDatabase laboratoryDatabase;
    public List<BaseActivity> activities = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();

        logger = AppLogger.getInstance(getApplicationContext());
        logger.i(TAG, "= App Started =\n");
        Presenter.init(getApplicationContext());
        session = SessionManager.getInstance(getApplicationContext());


        inventoryDatabase = InventoryDatabase.getInstance(getApplicationContext());
        laboratoryDatabase = LaboratoryDatabase.getInstance(getApplicationContext());

    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        for (BaseActivity activity : activities) {
            activity.finish();
        }
    }

    public void addActivity(BaseActivity activity) {
        activities.add(activity);
    }

    public void removeActivity(Activity activity) {
        activities.remove(activity);
    }

    public void clearAllActivity() {
        for (BaseActivity activity : activities) {
            activity.finish();
        }
    }

    public void clearStackOnSignOut() {
        for (BaseActivity activity : activities) {
            if (!(activity instanceof LoginActivity)) {
                activity.finish();
            }
        }
    }
}
