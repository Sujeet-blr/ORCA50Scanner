package in.mobiux.android.orca50scanner.assetmanagementlite.util;

import android.app.Activity;
import android.app.Application;

import java.util.ArrayList;
import java.util.List;

import in.mobiux.android.orca50scanner.assetmanagementlite.activity.BaseActivity;
import in.mobiux.android.orca50scanner.assetmanagementlite.activity.LoginActivity;
import in.mobiux.android.orca50scanner.assetmanagementlite.api.ApiClient;
import in.mobiux.android.orca50scanner.assetmanagementlite.api.Presenter;
import in.mobiux.android.orca50scanner.assetmanagementlite.api.model.DepartmentResponse;
import in.mobiux.android.orca50scanner.assetmanagementlite.api.model.Laboratory;
import in.mobiux.android.orca50scanner.assetmanagementlite.database.AppDatabase;
import in.mobiux.android.orca50scanner.assetmanagementlite.database.InventoryDatabase;
import in.mobiux.android.orca50scanner.assetmanagementlite.database.LaboratoryDatabase;
import in.mobiux.android.orca50scanner.assetmanagementlite.database.LaboratoryRepository;
import in.mobiux.android.orca50scanner.common.utils.App;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyApplication extends App {

    private static final String TAG = "MyApplication";

    public AppDatabase db;
    public AppLogger logger;
    public SessionManager session;

    public InventoryDatabase inventoryDatabase;
    public LaboratoryDatabase laboratoryDatabase;
    public List<BaseActivity> activities = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();

        logger = AppLogger.getInstance(getApplicationContext());
        logger.i(TAG, "= App Started =\n");
        Presenter.init(getApplicationContext());
        session = SessionManager.getInstance(getApplicationContext());


        inventoryDatabase = InventoryDatabase.getInstance(getApplicationContext());
        laboratoryDatabase = LaboratoryDatabase.getInstance(getApplicationContext());

        departments();
    }

    @Override
    public void onTerminate() {

        if (!session.isRememberMe()){
            session.logout();
        }

        for (BaseActivity activity : activities) {
            activity.finish();
        }
        super.onTerminate();
    }

    public void addActivity(BaseActivity activity) {
        activities.add(activity);
    }

    public void removeActivity(Activity activity) {
        activities.remove(activity);
    }

    public void clearAllActivity() {
        for (BaseActivity activity : activities) {
            activity.finish();
        }
    }

    public void clearStackOnSignOut() {
        for (BaseActivity activity : activities) {
            if (!(activity instanceof LoginActivity)) {
                activity.finish();
            }
        }
    }

    public void departments() {
        ApiClient.getApiService().departments(session.token()).enqueue(new Callback<List<DepartmentResponse>>() {
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

                            if (lab.getLabName().startsWith("CICO") && lab.getLabId() != Constraints.CHECK_IN_DEPARTMENT) {
                                lab.setLabName(lab.getLabName().replace("CICO-", ""));
                                laboratories.add(lab);
                            }
                        }
                    }

                    MyApplication app = (MyApplication) getApplicationContext();
                    LaboratoryRepository repository = new LaboratoryRepository(app);
                    repository.clearAll();
                    repository.insertList(laboratories);
                } else {
                    logger.e(TAG, "" + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<DepartmentResponse>> call, Throwable t) {
                logger.e(TAG, "" + t.getLocalizedMessage());
            }
        });
    }
}
