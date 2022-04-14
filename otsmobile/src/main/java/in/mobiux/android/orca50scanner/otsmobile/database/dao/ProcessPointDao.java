package in.mobiux.android.orca50scanner.otsmobile.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import in.mobiux.android.orca50scanner.otsmobile.api.model.ProcessPoint;

@Dao
public interface ProcessPointDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ProcessPoint item);

    @Update
    void update(ProcessPoint item);

    @Query("select * from process_points")
    LiveData<List<ProcessPoint>> getList();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllWithReplace(List<ProcessPoint> list);

    @Query("Delete from process_points")
    void clearAll();
}
