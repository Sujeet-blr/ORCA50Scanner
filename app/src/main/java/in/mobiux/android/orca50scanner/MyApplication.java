package in.mobiux.android.orca50scanner;

import android.app.Application;

import androidx.room.Room;

import in.mobiux.android.orca50scanner.database.AppDatabase;
import in.mobiux.android.orca50scanner.util.AppLogger;

/**
 * Created by SUJEET KUMAR on 08-Mar-21.
 */
public class MyApplication extends Application {

    public AppDatabase db;
    public AppLogger logger;
    private String TAG = MyApplication.class.getName();

    @Override
    public void onCreate() {
        super.onCreate();

        logger = AppLogger.getInstance(getApplicationContext());
        logger.i(TAG, "============App Started....==========\n");

//        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "db_name.db").allowMainThreadQueries().fallbackToDestructiveMigration().build();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();


    }
}
