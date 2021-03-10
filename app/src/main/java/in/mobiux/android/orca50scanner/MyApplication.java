package in.mobiux.android.orca50scanner;

import android.app.Application;

import androidx.room.Room;

import in.mobiux.android.orca50scanner.database.AppDatabase;

/**
 * Created by SUJEET KUMAR on 08-Mar-21.
 */
public class MyApplication extends Application {

    public AppDatabase db;

    @Override
    public void onCreate() {
        super.onCreate();

//        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "db_name.db").allowMainThreadQueries().fallbackToDestructiveMigration().build();
    }
}
