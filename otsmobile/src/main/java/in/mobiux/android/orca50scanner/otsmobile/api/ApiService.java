package in.mobiux.android.orca50scanner.otsmobile.api;

import com.google.gson.JsonObject;

import java.util.List;

import in.mobiux.android.orca50scanner.otsmobile.api.model.BaseModel;
import in.mobiux.android.orca50scanner.otsmobile.api.model.ProcessPoint;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {

    @GET(Endpoints.PROCESS_POINTS)
    Call<List<ProcessPoint>> getAllProcessPoints(@Header("Authorization") String fullToken);

    @POST(Endpoints.SCAN)
    Call<BaseModel> uploadScanItem(@Header("Authorization") String fullToken, @Body JsonObject payload);

}
