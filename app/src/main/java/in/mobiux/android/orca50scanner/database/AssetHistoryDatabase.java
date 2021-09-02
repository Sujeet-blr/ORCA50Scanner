package in.mobiux.android.orca50scanner.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import in.mobiux.android.orca50scanner.api.model.AssetHistory;
import in.mobiux.android.orca50scanner.database.dao.AssetHistoryDao;
import in.mobiux.android.orca50scanner.util.Converters;

/**
 * Created by SUJEET KUMAR on 09-Mar-21.
 */

@Database(entities = {AssetHistory.class}, version = 31, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AssetHistoryDatabase extends RoomDatabase {

    private static AssetHistoryDatabase instance;

    public abstract AssetHistoryDao assetHistoryDao();

    public static synchronized AssetHistoryDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), AssetHistoryDatabase.class, "assethistory.db").fallbackToDestructiveMigration().addCallback(roomCallback).build();
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
