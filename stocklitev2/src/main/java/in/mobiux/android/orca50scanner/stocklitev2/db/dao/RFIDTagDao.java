package in.mobiux.android.orca50scanner.stocklitev2.db.dao;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import in.mobiux.android.orca50scanner.stocklitev2.db.model.RFIDTag;

@Dao
public interface RFIDTagDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(RFIDTag tag);

    @Query("select * from tags")
    LiveData<List<RFIDTag>> getList();

    @Query("select * from tags WHERE epc = (:epc)")
    public RFIDTag[] getMatchingInventory(String epc);

    @Delete
    void delete(RFIDTag tag);

    @Query("Delete from tags")
    void clearAll();
}
