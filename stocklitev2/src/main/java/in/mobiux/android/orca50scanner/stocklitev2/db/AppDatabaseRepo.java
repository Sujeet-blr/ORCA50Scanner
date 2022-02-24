package in.mobiux.android.orca50scanner.stocklitev2.db;

import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

import in.mobiux.android.orca50scanner.stocklitev2.db.dao.RFIDTagDao;
import in.mobiux.android.orca50scanner.stocklitev2.db.model.RFIDTag;
import in.mobiux.android.orca50scanner.stocklitev2.utils.MyApplication;

public class AppDatabaseRepo {

    private static final String TAG = "AppDatabaseRepo";

    private RFIDTagDao rfidTagDao;
    private LiveData<List<RFIDTag>> data;

    public MyApplication app;
    private AppDatabase db;


    public AppDatabaseRepo(Context context) {
        this.app = (MyApplication) context;
        db = AppDatabase.getInstance(app);

        rfidTagDao = db.rfidTagDao();
        data = rfidTagDao.getList();
    }

    public LiveData<List<RFIDTag>> getRFIDTagsList() {
        return data;
    }

    public void clearAll(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                rfidTagDao.clearAll();
            }
        }).start();
    }

    public void insertAll(List<RFIDTag> tags) {
        new InsertAsyncTask(app, rfidTagDao).execute(tags);
    }

    private static class InsertAsyncTask extends AsyncTask<List<RFIDTag>, Void, Void> {
        private Context context;
        private RFIDTagDao rfidTagDao;

        public InsertAsyncTask(Context context, RFIDTagDao rfidTagDao) {
            this.context = context;
            this.rfidTagDao = rfidTagDao;
        }

        @Override
        protected Void doInBackground(List<RFIDTag>... lists) {

            List<RFIDTag> rfidTags = lists[0];
            for (RFIDTag tag : rfidTags) {
                rfidTagDao.insert(tag);
            }

            return null;
        }
    }
}
