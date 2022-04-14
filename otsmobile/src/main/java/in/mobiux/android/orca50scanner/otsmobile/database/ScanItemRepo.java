package in.mobiux.android.orca50scanner.otsmobile.database;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.List;

import in.mobiux.android.orca50scanner.otsmobile.api.model.ScanItem;
import in.mobiux.android.orca50scanner.otsmobile.database.dao.ScanItemDao;


public class ScanItemRepo {

    private static final String TAG = "ScanItemRepo";

    private ScanItemDao dao;
    private LiveData<List<ScanItem>> data;
    private Context context;

    public ScanItemRepo(Context context) {
        this.context = context;
        AppDatabase db = AppDatabase.getInstance(context);
        dao = db.getScanItemDao();

        data = db.getScanItemDao().getList();
    }

    public LiveData<List<ScanItem>> getAllScanItemList() {
        return data;
    }

    public void delete(ScanItem item) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                dao.delete(item);
            }
        }).start();
    }

    public void update(ScanItem item) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                dao.update(item);
            }
        }).start();
    }

    public void clearAll() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                dao.clearAll();
            }
        }).start();
    }

    public void insert(ScanItem item) {
        new InsertAsyncTask(dao).execute(item);
    }


    private static class InsertAsyncTask extends AsyncTask<ScanItem, Void, Void> {

        private ScanItemDao dao;

        public InsertAsyncTask(ScanItemDao dao) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(ScanItem... scanItems) {

            ScanItem item = scanItems[0];
            dao.insert(item);
            Log.i(TAG, "doInBackground: data saved to db " + item.getOrderItemId());
            return null;
        }
    }
}
