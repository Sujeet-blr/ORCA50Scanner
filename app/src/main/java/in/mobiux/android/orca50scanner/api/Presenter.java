package in.mobiux.android.orca50scanner.api;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import in.mobiux.android.orca50scanner.MyApplication;
import in.mobiux.android.orca50scanner.api.model.AssetResponse;
import in.mobiux.android.orca50scanner.api.model.DepartmentResponse;
import in.mobiux.android.orca50scanner.api.model.Inventory;
import in.mobiux.android.orca50scanner.api.model.Laboratory;
import in.mobiux.android.orca50scanner.database.LaboratoryRepository;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Presenter {

    String TAG = Presenter.class.getCanonicalName();
    public static Presenter INSTANCE;
    public static boolean configAvailable = false;
    MyApplication app;

    public static void init(Context context) {
        INSTANCE = new Presenter(context);
    }

    private Presenter(Context context) {
        app = (MyApplication) context;
    }

    public void pullLatestData(){
        inventories();
        laboratories();
        departments();
    }

    public void inventories() {
        ApiClient.getApiService().inventoryList(app.session.token()).enqueue(new Callback<List<AssetResponse>>() {
            @Override
            public void onResponse(Call<List<AssetResponse>> call, Response<List<AssetResponse>> response) {
                if (response.isSuccessful()) {

                    List<Inventory> inventories = new ArrayList<>();
                    for (AssetResponse res : response.body()) {

                        Inventory inventory = new Inventory();

                        inventory.setInventoryId(res.getId());
                        inventory.setEpc(res.getAssetId().getRfid());
                        inventory.setName(res.getName());

                        int labId = -1;
                        String labName = "";
                        boolean locationAssigned = false;

                        if (res.getDepartment() != null) {
                            labId = res.getDepartment().getId();
                            labName = res.getDepartment().getName();
                            locationAssigned = true;
                        }

                        inventory.setLabId(labId);
                        inventory.setLaboratoryName(labName);
                        inventory.setLocationAssigned(locationAssigned);


                        inventories.add(inventory);
                    }

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            app.inventoryDatabase.inventoryDao().insertAllWithReplace(inventories);
                        }
                    }).start();
                }
            }

            @Override
            public void onFailure(Call<List<AssetResponse>> call, Throwable t) {

            }
        });
    }

    public void departments() {
        ApiClient.getApiService().departments(app.session.token()).enqueue(new Callback<List<DepartmentResponse>>() {
            @Override
            public void onResponse(Call<List<DepartmentResponse>> call, Response<List<DepartmentResponse>> response) {
                if (response.isSuccessful()) {
                    List<Laboratory> laboratories = new ArrayList<>();

                    for (DepartmentResponse res : response.body()) {
                        for (DepartmentResponse.Child child : res.getChild()) {
                            Laboratory lab = new Laboratory();
                            lab.setLevelId(res.getId());
                            lab.setLevelName(res.getName());
                            lab.setLabId(child.getId());
                            lab.setLabName(child.getName());

                            laboratories.add(lab);
                        }
                    }

                    LaboratoryRepository repository = new LaboratoryRepository(app);
                    repository.insertList(laboratories);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
//                            LaboratoryRepository repository = new LaboratoryRepository(app);
//                            repository.insertList(laboratories);
//                            app.laboratoryDatabase.laboratoryDao().clearAll();
//                            app.laboratoryDatabase.laboratoryDao().insertAllWithReplace(laboratories);
                        }
                    }).start();
                }
            }

            @Override
            public void onFailure(Call<List<DepartmentResponse>> call, Throwable t) {

            }
        });
    }

    public void laboratories() {

    }


}
