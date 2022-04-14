package in.mobiux.android.orca50scanner.otsmobile.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import in.mobiux.android.orca50scanner.otsmobile.api.model.ProcessPoint;
import in.mobiux.android.orca50scanner.otsmobile.api.model.ScanItem;
import in.mobiux.android.orca50scanner.otsmobile.database.dao.ProcessPointDao;
import in.mobiux.android.orca50scanner.otsmobile.database.dao.ScanItemDao;

@Database(entities = {ScanItem.class, ProcessPoint.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ScanItemDao getScanItemDao();

    public abstract ProcessPointDao getProcessPointDao();

    private static AppDatabase instance;

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "otsmobile.db").fallbackToDestructiveMigration().addCallback(roomCallback).build();
        }

        return instance;
    }

    private static Callback roomCallback = new Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
        }
    };

}
