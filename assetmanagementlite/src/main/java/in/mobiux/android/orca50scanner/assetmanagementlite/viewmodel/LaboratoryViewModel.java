package in.mobiux.android.orca50scanner.assetmanagementlite.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import in.mobiux.android.orca50scanner.assetmanagementlite.api.model.Laboratory;
import in.mobiux.android.orca50scanner.assetmanagementlite.database.LaboratoryRepository;


/**
 * Created by SUJEET KUMAR on 09-Mar-21.
 */
public class LaboratoryViewModel extends AndroidViewModel {

    private LaboratoryRepository repository;
    private LiveData<List<Laboratory>> data;

    public LaboratoryViewModel(@NonNull Application application) {
        super(application);
        repository = new LaboratoryRepository(application);
        data = repository.getAll();
    }

    public void insertAll(List<Laboratory> list) {
        repository.insertList(list);
    }

    public LiveData<List<Laboratory>> getAllInventory() {
        return data;
    }

    public void refresh() {
        repository.clearAll();
    }
}
