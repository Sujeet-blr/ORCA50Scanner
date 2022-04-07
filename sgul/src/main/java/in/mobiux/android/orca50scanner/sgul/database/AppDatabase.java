package in.mobiux.android.orca50scanner.sgul.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import in.mobiux.android.orca50scanner.sgul.api.model.AssetHistory;
import in.mobiux.android.orca50scanner.sgul.api.model.Inventory;
import in.mobiux.android.orca50scanner.sgul.api.model.Laboratory;
import in.mobiux.android.orca50scanner.sgul.database.dao.AssetHistoryDao;
import in.mobiux.android.orca50scanner.sgul.database.dao.InventoryDao;
import in.mobiux.android.orca50scanner.sgul.database.dao.LaboratoryDao;

/**
 * Created by SUJEET KUMAR on 08-Mar-21.
 */

@Database(entities = {Inventory.class, Laboratory.class, AssetHistory.class}, version = 4, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract InventoryDao getInventoryDao();

    public abstract LaboratoryDao getlaboratoryDao();

    public abstract AssetHistoryDao getAssetHistoryDao();
}
