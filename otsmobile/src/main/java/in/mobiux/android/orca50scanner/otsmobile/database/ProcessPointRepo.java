package in.mobiux.android.orca50scanner.otsmobile.database;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;

import in.mobiux.android.orca50scanner.otsmobile.api.model.ProcessPoint;
import in.mobiux.android.orca50scanner.otsmobile.database.dao.ProcessPointDao;

public class ProcessPointRepo {

    private static final String TAG = "ProcessPointRepo";

    private ProcessPointDao dao;
    private LiveData<List<ProcessPoint>> data;
    private Context context;

    public ProcessPointRepo(Context context) {
        this.context = context;

        dao = AppDatabase.getInstance(context).getProcessPointDao();

        data = dao.getList();
    }

    public LiveData<List<ProcessPoint>> getAllProcessPoints() {
        return data;
    }

    public void insert(ProcessPoint processPoint) {
        List<ProcessPoint> points = new ArrayList<>();
        points.add(processPoint);
        new InsertAsyncTask(dao).execute(points);
    }

    public void insertAll(List<ProcessPoint> processPoints) {
        new InsertAsyncTask(dao).execute(processPoints);
    }


    private static class InsertAsyncTask extends AsyncTask<List<ProcessPoint>, Void, Void> {

        private ProcessPointDao dao;

        public InsertAsyncTask(ProcessPointDao dao) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(List<ProcessPoint>... lists) {
            List<ProcessPoint> points = lists[0];
            dao.insertAllWithReplace(points);
            Log.i(TAG, "doInBackground: size " + points.size());
            return null;
        }
    }
}
