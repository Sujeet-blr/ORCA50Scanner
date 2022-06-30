package in.mobiux.android.orca50scanner.assetmanagementlite.database;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

import in.mobiux.android.orca50scanner.assetmanagementlite.api.model.Laboratory;
import in.mobiux.android.orca50scanner.assetmanagementlite.database.dao.LaboratoryDao;
import in.mobiux.android.orca50scanner.assetmanagementlite.util.AppLogger;
import in.mobiux.android.orca50scanner.assetmanagementlite.util.MyApplication;


/**
 * Created by SUJEET KUMAR on 09-Mar-21.
 */
public class LaboratoryRepository {

    public static final String TAG = LaboratoryRepository.class.getCanonicalName();
    private LaboratoryDao laboratoryDao;
    private LiveData<List<Laboratory>> liveData;

    public MyApplication app;

    public LaboratoryRepository(Application application) {
        LaboratoryDatabase database = LaboratoryDatabase.getInstance(application);

        laboratoryDao = database.laboratoryDao();
        liveData = laboratoryDao.getList();
        app = (MyApplication) application.getApplicationContext();
    }


    public LiveData<List<Laboratory>> getAll() {
        return liveData;
    }

    public void insertList(List<Laboratory> list) {
        new InsertInventoryAsyncTask(app.getApplicationContext(), laboratoryDao).execute(list);
    }

    public void clearAll() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                laboratoryDao.clearAll();
            }
        }).start();
    }

    private static class InsertInventoryAsyncTask extends AsyncTask<List<Laboratory>, Void, Void> {

        private LaboratoryDao laboratoryDao;
        Context context;

        public InsertInventoryAsyncTask(Context context, LaboratoryDao laboratoryDao) {
            this.laboratoryDao = laboratoryDao;
            this.context = context;
        }

        @Override
        protected Void doInBackground(List<Laboratory>... laboratories) {
            List<Laboratory> laboratoryList = laboratories[0];
            AppLogger.getInstance(context).i(TAG, "Data inserting to database");
            laboratoryDao.clearAll();
            laboratoryDao.insertAllWithReplace(laboratoryList);
            return null;
        }
    }

}
