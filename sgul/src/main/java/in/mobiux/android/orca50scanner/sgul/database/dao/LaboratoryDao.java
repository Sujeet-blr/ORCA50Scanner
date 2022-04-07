package in.mobiux.android.orca50scanner.sgul.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import in.mobiux.android.orca50scanner.sgul.api.model.Laboratory;

/**
 * Created by SUJEET KUMAR on 08-Mar-21.
 */

@Dao
public interface LaboratoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Laboratory laboratory);

    @Query("select * from laboratory")
    LiveData<List<Laboratory>> getList();

    @Update
    void update(Laboratory laboratory);

    @Delete
    void delete(Laboratory laboratory);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUpdate(Laboratory laboratory);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllWithReplace(List<Laboratory> list);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void sync(List<Laboratory> list);

    @Query("Delete from laboratory")
    void clearAll();
}
