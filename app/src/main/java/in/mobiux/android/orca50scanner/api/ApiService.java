package in.mobiux.android.orca50scanner.api;

import java.util.List;

import in.mobiux.android.orca50scanner.api.model.AssetResponse;
import in.mobiux.android.orca50scanner.api.model.DepartmentResponse;
import in.mobiux.android.orca50scanner.api.model.Laboratory;
import in.mobiux.android.orca50scanner.api.model.User;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {

//    @GET(Endpoints.API_CONFIG)
//    Call<AppConfig> apiConfig(@Query("token") String token);

    //    Users


    @POST(Endpoints.LOGIN)
    Call<User> login(@Body User user);

    @GET(Endpoints.ASSETS)
    Call<List<AssetResponse>> inventoryList(@Header("Authorization") String token);

    @GET(Endpoints.DEPARTMENTS)
    Call<List<DepartmentResponse>> departments(@Header("Authorization") String token);

    @POST(Endpoints.UPDATE_ASSETS)
    Call<Laboratory> updateAssets(@Header("Authentication") String token, @Body Laboratory laboratory);

    @Multipart
    @POST(Endpoints.LOGS)
    Call<String> uploadLogs(@Header("Authorization") String token, @Part MultipartBody.Part file);
}
