package in.mobiux.android.orca50scanner.database;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import in.mobiux.android.orca50scanner.api.model.Inventory;
import in.mobiux.android.orca50scanner.database.dao.InventoryDao;

/**
 * Created by SUJEET KUMAR on 09-Mar-21.
 */
public class InventoryRepository {

    private InventoryDao inventoryDao;
    private LiveData<List<Inventory>> liveData;

    TimerTask timerTask;
    Timer timer;

    public InventoryRepository(Application application) {
        InventoryDatabase database = InventoryDatabase.getInstance(application);

        inventoryDao = database.inventoryDao();
        liveData = inventoryDao.getList();
    }


    public LiveData<List<Inventory>> getAllInventory() {
        return liveData;
    }

    public void insert(Inventory inventory) {
        new InsertInventoryAsyncTask(inventoryDao).execute(inventory);
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

        public InsertInventoryAsyncTask(InventoryDao inventoryDao) {
            this.inventoryDao = inventoryDao;
        }

        @Override
        protected Void doInBackground(Inventory... inventories) {
            inventoryDao.insert(inventories[0]);
            return null;
        }
    }

}
