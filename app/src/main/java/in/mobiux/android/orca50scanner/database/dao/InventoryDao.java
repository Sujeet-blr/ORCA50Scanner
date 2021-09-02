package in.mobiux.android.orca50scanner.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import in.mobiux.android.orca50scanner.reader.model.Inventory;

/**
 * Created by SUJEET KUMAR on 08-Mar-21.
 */

@Dao
public interface InventoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Inventory inventory);

    @Query("select * from inventory")
    LiveData<List<Inventory>> getList();

    @Query("select * from inventory WHERE epc = (:epc)")
    public Inventory[] getMatchingInventory(String epc);

    @Update
    void update(Inventory inventory);

    @Delete
    void delete(Inventory inventory);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUpdate(Inventory inventory);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllWithReplace(List<Inventory> list);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void sync(List<Inventory> list);

    @Query("Delete from inventory")
    void clearAll();
}
