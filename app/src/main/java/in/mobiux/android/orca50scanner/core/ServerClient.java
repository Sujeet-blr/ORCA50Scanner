package in.mobiux.android.orca50scanner.core;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import in.mobiux.android.orca50scanner.api.ApiClient;
import in.mobiux.android.orca50scanner.api.Presenter;
import in.mobiux.android.orca50scanner.api.model.AssetHistory;
import in.mobiux.android.orca50scanner.api.model.Inventory;
import in.mobiux.android.orca50scanner.api.model.Laboratory;
import in.mobiux.android.orca50scanner.util.AppLogger;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by SUJEET KUMAR on 13-Jul-21.
 */

//TODO
public class ServerClient {

    private static final String TAG = ServerClient.class.getCanonicalName();
    private Context context;
    private static ServerClient CLIENT_INSTANCE;
    private DataSyncListener syncListener;
    private AppLogger logger;

    public ServerClient(Context context) {
        this.context = context;
        logger = AppLogger.getInstance(context);
    }

    public static ServerClient getInstance(Context context) {
        if (CLIENT_INSTANCE == null) {
            CLIENT_INSTANCE = new ServerClient(context);
        }
        return CLIENT_INSTANCE;
    }

    public void setOnSyncListener(DataSyncListener syncListener) {
        this.syncListener = syncListener;
    }


    private List<Laboratory> laboratories = new ArrayList<>();

    private void formatPayload(List<Inventory> list, List<AssetHistory> histories) {

        List<Inventory> inventoryList = new ArrayList<>();
        HashMap<String, Laboratory> historyLabs = new HashMap<>();

        for (AssetHistory history : histories) {
            logger.i(TAG, "" + history.getEpc() + "    " + history.getDepartment() + "  " + history.getUpdateTimeIntervalInSeconds());

            String departmentId = String.valueOf(history.getDepartment());

            Laboratory laboratory = historyLabs.get(departmentId);
            if (laboratory == null) {
                laboratory = new Laboratory();
                laboratory.setDepartment(Integer.parseInt(departmentId));
                historyLabs.put(departmentId, laboratory);
            }

            history.setTime(history.getUpdateTimeIntervalInSeconds());
            laboratory.getAssets().add(history);
        }

        laboratories.addAll(historyLabs.values());
        for (Laboratory l : laboratories) {
            logger.i(TAG, "lab " + l.getDepartment() + " assets " + l.getAssets().size());
        }
    }

    public void sync(List<Inventory> list, List<AssetHistory> histories) {

        formatPayload(list, histories);

        if (laboratories.size() > 0) {
            updateAsset(laboratories.get(0));
        } else {
            Presenter.INSTANCE.pullLatestData();
//            logger.processLogs();
            if (syncListener != null) {
                syncListener.onSyncSuccess();
            }
        }
    }

    private void updateAsset(Laboratory laboratory) {

        for (AssetHistory history : laboratory.getAssets()) {
            logger.i(TAG, "payload " + history.getEpc() + " dept " + history.getTime());
        }

//        ApiClient.getApiService().updateAssets(session.rawToken(), laboratory).enqueue(new Callback<Laboratory>() {
//            @Override
//            public void onResponse(Call<Laboratory> call, Response<Laboratory> response) {
//                if (response.isSuccessful()) {
//                    laboratories.remove(laboratory);
//                    for (AssetHistory history : laboratory.getAssets()) {
//                        viewModel.deleteHistory(history);
//                    }
//
//                    if (laboratories.size() > 0) {
//                        updateAsset(laboratories.get(0));
//                    } else {
//                        viewModel.clearHistory();
//                        Presenter.INSTANCE.pullLatestData();
//                        progressDialog.dismiss();
//                        processLogs();
//                    }
//                } else {
//                    logger.e(TAG, "" + response.message());
//                    progressDialog.dismiss();
//
//                }
//
//            }
//
//            @Override
//            public void onFailure(Call<Laboratory> call, Throwable t) {
//                logger.e(TAG, "" + t.getLocalizedMessage());
//                progressDialog.dismiss();
//            }
//        });
    }
}
