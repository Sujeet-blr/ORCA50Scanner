package in.mobiux.android.orca50scanner.assetmanagementlite.database;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import in.mobiux.android.orca50scanner.assetmanagementlite.api.model.Inventory;
import in.mobiux.android.orca50scanner.assetmanagementlite.database.dao.InventoryDao;
import in.mobiux.android.orca50scanner.assetmanagementlite.util.AppLogger;
import in.mobiux.android.orca50scanner.assetmanagementlite.util.MyApplication;

/**
 * Created by SUJEET KUMAR on 09-Mar-21.
 */
public class InventoryRepository {

    public static final String TAG = InventoryRepository.class.getCanonicalName();
    private InventoryDao inventoryDao;
    private LiveData<List<Inventory>> liveData;

    TimerTask timerTask;
    Timer timer;
    public MyApplication app;

    public InventoryRepository(Application application) {
        InventoryDatabase database = InventoryDatabase.getInstance(application);

        inventoryDao = database.inventoryDao();
        liveData = inventoryDao.getList();
        app = (MyApplication) application.getApplicationContext();
    }


    public LiveData<List<Inventory>> getAllInventory() {
        return liveData;
    }

    public void insert(Inventory inventory) {
        new InsertInventoryAsyncTask(app.getApplicationContext(), inventoryDao).execute(inventory);
    }

    public void update(Inventory inventory) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                inventory.setUpdatedAt(System.currentTimeMillis());
                AppLogger.getInstance(app).i(TAG, "update asset " + inventory.getFormattedEPC() + " time " + inventory.getUpdatedAt());
                inventoryDao.update(inventory);
            }
        }).start();
    }

    public void clearAll() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                inventoryDao.clearAll();
            }
        }).start();
    }

    private static class InsertInventoryAsyncTask extends AsyncTask<Inventory, Void, Void> {

        private InventoryDao inventoryDao;
        Context context;

        public InsertInventoryAsyncTask(Context context, InventoryDao inventoryDao) {
            this.inventoryDao = inventoryDao;
            this.context = context;
        }

        @Override
        protected Void doInBackground(Inventory... inventories) {
            Inventory inventory = inventories[0];
            AppLogger.getInstance(context).i(TAG, "Data inserting to database & updated time " + inventory.getUpdatedAt());
            inventoryDao.insert(inventory);
            return null;
        }
    }

}
