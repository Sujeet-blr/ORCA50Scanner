package in.mobiux.android.orca50scanner.core;

import android.content.Context;
import android.widget.Toast;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import in.mobiux.android.orca50scanner.MyApplication;
import in.mobiux.android.orca50scanner.activity.BaseActivity;
import in.mobiux.android.orca50scanner.activity.SystemLogsManagementActivity;
import in.mobiux.android.orca50scanner.api.ApiClient;
import in.mobiux.android.orca50scanner.api.Presenter;
import in.mobiux.android.orca50scanner.api.model.AssetHistory;
import in.mobiux.android.orca50scanner.api.model.AssetResponse;
import in.mobiux.android.orca50scanner.api.model.DepartmentResponse;
import in.mobiux.android.orca50scanner.api.model.Inventory;
import in.mobiux.android.orca50scanner.api.model.Laboratory;
import in.mobiux.android.orca50scanner.api.model.SyncPayload;
import in.mobiux.android.orca50scanner.database.LaboratoryRepository;
import in.mobiux.android.orca50scanner.util.AppLogger;
import in.mobiux.android.orca50scanner.util.AppUtils;
import in.mobiux.android.orca50scanner.util.SessionManager;
import in.mobiux.android.orca50scanner.viewmodel.InventoryViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by SUJEET KUMAR on 13-Jul-21.
 */

public class ServerClient {

    private static final String TAG = ServerClient.class.getCanonicalName();
    private Context context;
    private static ServerClient CLIENT_INSTANCE;
    private DataSyncListener syncListener;
    private AppLogger logger;
    private BaseActivity activity;

    private List<AssetHistory> histories = new ArrayList<>();
    private InventoryViewModel viewModel;
    private MyApplication app;
    SessionManager session;

    public ServerClient(Context context) {
        this.context = context;
        app = (MyApplication) context.getApplicationContext();
        session = SessionManager.getInstance(context);
        logger = AppLogger.getInstance(context);
    }

    public static ServerClient getInstance(Context context) {
        if (CLIENT_INSTANCE == null) {
            CLIENT_INSTANCE = new ServerClient(context);
        }
        return CLIENT_INSTANCE;
    }

    public void setOnSyncListener(BaseActivity activity, DataSyncListener syncListener) {
        this.activity = activity;
        this.syncListener = syncListener;
        init(activity);
    }

    private void init(BaseActivity activity) {
        viewModel = new ViewModelProvider(activity).get(InventoryViewModel.class);

        viewModel.getHistories().observe(activity, new Observer<List<AssetHistory>>() {
            @Override
            public void onChanged(List<AssetHistory> list) {
                logger.i(TAG, "history size " + list.size());

                histories.clear();
                histories.addAll(list);
            }
        });
    }

    public void sync(BaseActivity activity) {
        this.activity = activity;

        logger.i(TAG, "history size is " + histories.size());
        if (histories.size() > 0) {

            for (AssetHistory history : histories) {
                history.setTime(history.getUpdateTimeIntervalInSeconds());
            }

            SyncPayload payload = new SyncPayload();
            payload.setHistories(histories);
            updateAssetNew(payload);
        } else {
            inventories();
        }
    }

    private void updateAssetNew(SyncPayload payload) {

        ApiClient.getApiService().updateAssetsNew(app.session.rawToken(), payload).enqueue(new Callback<SyncPayload>() {
            @Override
            public void onResponse(Call<SyncPayload> call, Response<SyncPayload> response) {
                if (response.isSuccessful()) {
                    viewModel.clearHistory();
                    inventories();
                    logger.i(TAG, "asset history update success " + response.code());
                } else {
                    logger.e(TAG, "asset upload failed");
                    if (syncListener != null) {
                        syncListener.onSyncFailed();
                    }
                }
            }

            @Override
            public void onFailure(Call<SyncPayload> call, Throwable t) {
                activity.showToast("Something went wrong");
                logger.e(TAG, "" + t.getLocalizedMessage());

                if (syncListener != null) {
                    syncListener.onSyncFailed();
                }
            }
        });
    }

    private void inventories() {
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
                        inventory.setSyncRequired(false);

                        if (inventory.getEpc() != null && (!inventory.getEpc().isEmpty())) {
                            inventories.add(inventory);
                        }
                    }

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            app.inventoryDatabase.inventoryDao().clearAll();
                            app.inventoryDatabase.inventoryDao().insertAllWithReplace(inventories);
                        }
                    }).start();


                    departments();
                } else {
                    logger.e(TAG, "" + response.message());
                    if (syncListener != null) {
                        syncListener.onSyncFailed();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<AssetResponse>> call, Throwable t) {
                activity.showToast("Something went wrong");
                logger.e(TAG, "" + t.getLocalizedMessage());
                if (syncListener != null) {
                    syncListener.onSyncFailed();
                }
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

                    if (syncListener != null) {
                        syncListener.onSyncSuccess();
                        processLogs();
                    }
                } else {
                    logger.e(TAG, "" + response.message());
                    if (syncListener != null) {
                        syncListener.onSyncFailed();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<DepartmentResponse>> call, Throwable t) {
                logger.e(TAG, "" + t.getLocalizedMessage());
                if (syncListener != null) {
                    syncListener.onSyncFailed();
                }
            }
        });
    }

    //    process the logs according to logs settingsA
    private void processLogs() {

        String synSetting = session.getValue(SystemLogsManagementActivity.KEY_RADIO);

        if (synSetting.isEmpty() || synSetting.equals("0")) {
//            send to server then clear device
            File logFile = logger.getLogFile(app);
            if (logFile != null) {
                sendLogsToServer(logger.getLogFile(app));
            } else {
                logger.e(TAG, "logs not found");
            }
        } else if (synSetting.equals("1")) {
//            clear from device only
            logger.clearLogs();
        }
    }

    private void sendLogsToServer(File logFile) {

        ApiClient.getApiService().uploadLogs(session.token(), AppUtils.convertFileToRequestBody(logFile)).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    logger.clearLogs();
                    logger.i(TAG, "logs uploaded succes & clear");
                } else {
                    logger.i(TAG, "logs upload failed");
//                    showToast("logs upload failed");
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                activity.showToast("something went wrong");
                logger.i(TAG, "logs upload Failed " + t.getLocalizedMessage());
            }
        });
    }
}
