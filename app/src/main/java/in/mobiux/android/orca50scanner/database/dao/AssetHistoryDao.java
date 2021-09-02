package in.mobiux.android.orca50scanner.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import in.mobiux.android.orca50scanner.api.model.AssetHistory;

/**
 * Created by SUJEET KUMAR on 08-Mar-21.
 */

@Dao
public interface AssetHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(AssetHistory assetHistory);

    @Query("select * from assetHistory")
    LiveData<List<AssetHistory>> getList();

    @Query("select * from assetHistory WHERE epc = (:epc)")
    public AssetHistory[] getMatchingInventory(String epc);

    @Delete
    void delete(AssetHistory history);

    @Query("Delete from assetHistory")
    void clearAll();
}
