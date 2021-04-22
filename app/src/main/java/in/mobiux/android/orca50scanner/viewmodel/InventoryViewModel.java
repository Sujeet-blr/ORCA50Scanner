package in.mobiux.android.orca50scanner.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import in.mobiux.android.orca50scanner.api.model.AssetHistory;
import in.mobiux.android.orca50scanner.api.model.Inventory;
import in.mobiux.android.orca50scanner.database.AssetHistoryRepository;
import in.mobiux.android.orca50scanner.database.InventoryRepository;

/**
 * Created by SUJEET KUMAR on 09-Mar-21.
 */
public class InventoryViewModel extends AndroidViewModel {

    private InventoryRepository repository;
    private LiveData<List<Inventory>> data;
    private AssetHistoryRepository historyRepository;
    private LiveData<List<AssetHistory>> histories;

    public InventoryViewModel(@NonNull Application application) {
        super(application);
        repository = new InventoryRepository(application);
        historyRepository = new AssetHistoryRepository(application);
        data = repository.getAllInventory();
        histories = historyRepository.getAllHistory();
    }

    public void insert(Inventory inventory) {
        repository.insert(inventory);
    }

    public void update(Inventory inventory) {
        repository.update(inventory);
    }

    public LiveData<List<Inventory>> getAllInventory() {
        return data;
    }

    public void refresh() {
        repository.clearAll();
    }

    public void insertAssetHistory(AssetHistory assetHistory) {
        historyRepository.insert(assetHistory);
    }

    public LiveData<List<AssetHistory>> getHistories() {
        return histories;
    }

    public void deleteHistory(AssetHistory history){
        historyRepository.delete(history);
    }

    public void clearHistory(){
        historyRepository.clearAll();
    }

}
