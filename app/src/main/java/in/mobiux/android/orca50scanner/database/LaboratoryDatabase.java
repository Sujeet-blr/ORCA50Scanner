package in.mobiux.android.orca50scanner.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import in.mobiux.android.orca50scanner.api.model.Inventory;
import in.mobiux.android.orca50scanner.api.model.Laboratory;
import in.mobiux.android.orca50scanner.database.dao.InventoryDao;
import in.mobiux.android.orca50scanner.database.dao.LaboratoryDao;
import in.mobiux.android.orca50scanner.util.Converters;

/**
 * Created by SUJEET KUMAR on 09-Mar-21.
 */

@Database(entities = {Laboratory.class}, version = 31, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class LaboratoryDatabase extends RoomDatabase {

    private static LaboratoryDatabase instance;

    public abstract LaboratoryDao laboratoryDao();

    public static synchronized LaboratoryDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), LaboratoryDatabase.class, "laboratory.db").fallbackToDestructiveMigration().addCallback(roomCallback).build();
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
