package in.mobiux.android.orca50scanner.otsmobile.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import in.mobiux.android.orca50scanner.otsmobile.api.model.ScanItem;

@Dao
public interface ScanItemDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ScanItem item);

    @Update
    void update(ScanItem item);

    @Query("select * from scanItems")
    LiveData<List<ScanItem>> getList();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllWithReplace(List<ScanItem> list);

    @Query("Delete from scanItems")
    void clearAll();

    @Delete
    void delete(ScanItem item);
}
