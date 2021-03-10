package in.mobiux.android.orca50scanner.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import in.mobiux.android.orca50scanner.api.model.Inventory;
import in.mobiux.android.orca50scanner.database.InventoryRepository;

/**
 * Created by SUJEET KUMAR on 09-Mar-21.
 */
public class  InventoryViewModel extends AndroidViewModel {

    private InventoryRepository repository;
    private LiveData<List<Inventory>> data;

    public InventoryViewModel(@NonNull Application application) {
        super(application);
        repository = new InventoryRepository(application);
        data = repository.getAllInventory();
    }

    public void insert(Inventory inventory){
        repository.insert(inventory);
    }

    public LiveData<List<Inventory>> getAllInventory(){
        return data;
    }

    public void refresh(){
        repository.clearAll();
    }
}
