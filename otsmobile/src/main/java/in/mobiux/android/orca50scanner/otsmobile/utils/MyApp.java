package in.mobiux.android.orca50scanner.otsmobile.utils;

import android.util.Log;

import androidx.lifecycle.Observer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import in.mobiux.android.orca50scanner.common.utils.App;
import in.mobiux.android.orca50scanner.otsmobile.api.ApiClient;
import in.mobiux.android.orca50scanner.otsmobile.api.model.BaseModel;
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
    private Timer timer;

    @Override
    public void onCreate() {
        super.onCreate();

        tokenManger = TokenManger.getInstance(this);
        session.setInt(session.KEY_RF_OUTPUT_POWER, 30);
        scanItemRepo = new ScanItemRepo(getApplicationContext());
        timer = new Timer();

        scanItemRepo.getAllScanItemList().observeForever(new Observer<List<ScanItem>>() {
            @Override
            public void onChanged(List<ScanItem> list) {
                items = list;
            }
        });

//        sending scan data every 60 minutes to server
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                sendScanItems(items);
            }
        }, 1000 * 60, (1000 * 60 * 60));
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
                        Log.e(TAG, "onResponse: " + response.body().getMessage());
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
