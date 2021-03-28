package in.mobiux.android.orca50scanner.api;

import java.util.List;

import in.mobiux.android.orca50scanner.api.model.AssetResponse;
import in.mobiux.android.orca50scanner.api.model.DepartmentResponse;
import in.mobiux.android.orca50scanner.api.model.Inventory;
import in.mobiux.android.orca50scanner.api.model.User;
import in.mobiux.android.orca50scanner.util.SessionManager;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;


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
}
