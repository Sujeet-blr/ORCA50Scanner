package in.mobiux.android.orca50scanner.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import in.mobiux.android.orca50scanner.api.model.Inventory;
import in.mobiux.android.orca50scanner.database.dao.InventoryDao;

/**
 * Created by SUJEET KUMAR on 08-Mar-21.
 */

@Database(entities = {Inventory.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract InventoryDao getInventoryDao();
}
