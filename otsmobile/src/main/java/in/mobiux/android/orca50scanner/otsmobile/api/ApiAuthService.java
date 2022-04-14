package in.mobiux.android.orca50scanner.otsmobile.api;

import com.google.gson.JsonObject;

import in.mobiux.android.orca50scanner.otsmobile.api.model.UserDetails;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiAuthService {

    //    call on app loading
    @POST(Endpoints.UNIVERSAL_TOKEN)
    Call<UserDetails> getUniversalToken(@Body UserDetails user);

    //    call for login
    @POST(Endpoints.LOGIN)
    Call<JsonObject> login(@Header("Authorization") String partialToken, @Body UserDetails user);

    //    call for validation
    @GET(Endpoints.VALIDATE_APPLICATION_TOKEN)
    Call<UserDetails> validateApplicationToken(@Header("Authorization") String fullToken);

    @GET(Endpoints.LOGOUT)
    Call<BaseModel> logout(@Header("Authorization") String partialToken);
}
