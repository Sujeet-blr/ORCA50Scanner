package in.mobiux.android.orca50scanner.otsmobile.api;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import in.mobiux.android.orca50scanner.otsmobile.api.model.BaseModel;
import in.mobiux.android.orca50scanner.otsmobile.api.model.UserDetails;
import in.mobiux.android.orca50scanner.otsmobile.utils.TokenManger;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthClient {

    private static final String TAG = "AuthClient";

    private static AuthClient instance;
    private Context context;
    private TokenManger tokenManger;
    private OnAuthValidation onAuthValidation;

    private AuthClient(Context context) {
        this.context = context;
        tokenManger = TokenManger.getInstance(context);
    }

    public static AuthClient getInstance(Context context) {
        if (instance == null) {
            instance = new AuthClient(context);
        }
        return instance;
    }


    public void setOnAuthValidation(OnAuthValidation onAuthValidation) {
        this.onAuthValidation = onAuthValidation;
        validateUser();
    }

    public interface OnAuthValidation {
        void validate(boolean status);
    }

    private void validateUser() {
        String fullToken = TokenManger.getInstance(context).getFullAuthorizationToken();

        if (onAuthValidation != null) {
            ApiClient.getAuthService().validateApplicationToken(fullToken).enqueue(new Callback<BaseModel>() {
                @Override
                public void onResponse(Call<BaseModel> call, Response<BaseModel> response) {
                    if (response.isSuccessful()) {
                        if (response.body().isSuccess()) {

                            String token = response.body().getJsonObject().get("token").getAsString();

                            JsonObject universal = response.body().getJsonObject().getAsJsonObject("universal");
                            UserDetails user = new Gson().fromJson(universal.getAsJsonObject("userDetails"), UserDetails.class);
                            tokenManger.setApplicationToken(token);

                            tokenManger.setApplicationToken(token);
                            onAuthValidation.validate(true);
                            Log.i(TAG, "onResponse: validate success");
                        } else {
                            onAuthValidation.validate(false);
                            Log.i(TAG, "onResponse: validate failed");
                        }
                    } else {
                        onAuthValidation.validate(false);
                        Log.i(TAG, "onResponse: validate failed");
                    }
                }

                @Override
                public void onFailure(Call<BaseModel> call, Throwable t) {
                    Log.e(TAG, "onFailure: " + t.getLocalizedMessage());
                }
            });
        }
    }
}
