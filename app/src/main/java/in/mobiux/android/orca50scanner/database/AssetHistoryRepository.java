package in.mobiux.android.orca50scanner.database;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.rfid.rxobserver.RXObserver;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import in.mobiux.android.orca50scanner.MyApplication;
import in.mobiux.android.orca50scanner.api.model.AssetHistory;
import in.mobiux.android.orca50scanner.api.model.Inventory;
import in.mobiux.android.orca50scanner.database.dao.AssetHistoryDao;
import in.mobiux.android.orca50scanner.database.dao.InventoryDao;
import in.mobiux.android.orca50scanner.util.AppLogger;

/**
 * Created by SUJEET KUMAR on 09-Mar-21.
 */
public class AssetHistoryRepository {

    public static final String TAG = AssetHistoryRepository.class.getCanonicalName();
    private AssetHistoryDao assetHistoryDao;
    private LiveData<List<AssetHistory>> liveData;

    public MyApplication app;

    public AssetHistoryRepository(Application application) {
        AssetHistoryDatabase database = AssetHistoryDatabase.getInstance(application);

        assetHistoryDao = database.assetHistoryDao();
        liveData = assetHistoryDao.getList();
        app = (MyApplication) application.getApplicationContext();
    }


    public LiveData<List<AssetHistory>> getAllHistory() {
        return liveData;
    }

    public void insert(AssetHistory assetHistory) {
        new InsertAsyncTask(app.getApplicationContext(), assetHistoryDao).execute(assetHistory);
    }

    public void clearAll() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                assetHistoryDao.clearAll();
            }
        }).start();
    }

    private static class InsertAsyncTask extends AsyncTask<AssetHistory, Void, Void> {

        private AssetHistoryDao  assetHistoryDao;
        Context context;

        public InsertAsyncTask(Context context, AssetHistoryDao assetHistoryDao) {
            this.assetHistoryDao = assetHistoryDao;
            this.context = context;
        }

        @Override
        protected Void doInBackground(AssetHistory... assetHistories) {
            AssetHistory assetHistory = assetHistories[0];
            AppLogger.getInstance(context).i(TAG, "Data inserting to database & updated time " + assetHistory.getCreatedAt());
            assetHistoryDao.insert(assetHistory);
            return null;
        }
    }

}
