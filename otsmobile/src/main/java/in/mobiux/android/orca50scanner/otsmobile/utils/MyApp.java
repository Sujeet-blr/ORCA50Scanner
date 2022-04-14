package in.mobiux.android.orca50scanner.otsmobile.utils;

import android.app.Application;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.lifecycle.Observer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import in.mobiux.android.orca50scanner.common.utils.App;
import in.mobiux.android.orca50scanner.otsmobile.activity.MainActivity;
import in.mobiux.android.orca50scanner.otsmobile.api.ApiClient;
import in.mobiux.android.orca50scanner.otsmobile.api.BaseModel;
import in.mobiux.android.orca50scanner.otsmobile.api.model.ProcessPoint;
import in.mobiux.android.orca50scanner.otsmobile.api.model.ScanItem;
import in.mobiux.android.orca50scanner.otsmobile.database.ScanItemRepo;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyApp extends App {

    private static final String TAG = "MyApp";

    private ScanItemRepo scanItemRepo;
    private List<ScanItem> items = new ArrayList<>();
    private TokenManger tokenManger;
    private boolean onStartSyncStatus = false;

    @Override
    public void onCreate() {
        super.onCreate();

        tokenManger = TokenManger.getInstance(this);
        scanItemRepo = new ScanItemRepo(getApplicationContext());

        scanItemRepo.getAllScanItemList().observeForever(new Observer<List<ScanItem>>() {
            @Override
            public void onChanged(List<ScanItem> list) {
                items = list;

                if (!onStartSyncStatus) {
                    sendScanItems(items);
                    onStartSyncStatus = true;
                }
            }
        });
    }


    public void sendScanItems(List<ScanItem> list) {

        JsonObject payload = new JsonObject();
        JsonArray orderItems = new JsonArray();
        for (ScanItem item : list) {
            JsonObject object = new JsonObject();
            object.addProperty("orderItemId", item.getOrderItemId());
            object.addProperty("processPointId", item.getProcessPointId());
            object.addProperty("scanTime", item.getScanTime());
            orderItems.add(object);
        }

        payload.add("orderItems", orderItems);

        ApiClient.getApiService().uploadScanItem(tokenManger.getFullAuthorizationToken(), payload).enqueue(new Callback<BaseModel>() {
            @Override
            public void onResponse(Call<BaseModel> call, Response<BaseModel> response) {
                if (response.isSuccessful()) {
                    if (response.body().isSuccess()) {

                        for (ScanItem item : list) {
                            item.setUploaded(true);
                            scanItemRepo.delete(item);
                        }

                        Log.i(TAG, "onResponse: Sync success");
                    } else {
                        Log.e(TAG, "onResponse: " + response.body().getBody().getMessage());
                    }
                } else {
                    Log.e(TAG, "onResponse: " + response.toString());
                }
            }

            @Override
            public void onFailure(Call<BaseModel> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });

    }

}
