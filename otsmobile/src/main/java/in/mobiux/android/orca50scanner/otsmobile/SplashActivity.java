package in.mobiux.android.orca50scanner.otsmobile;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import in.mobiux.android.orca50scanner.otsmobile.activity.BaseActivity;
import in.mobiux.android.orca50scanner.otsmobile.activity.LoginActivity;
import in.mobiux.android.orca50scanner.otsmobile.api.ApiClient;
import in.mobiux.android.orca50scanner.otsmobile.api.model.UserDetails;
import in.mobiux.android.orca50scanner.otsmobile.utils.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashActivity extends BaseActivity {

    private static final String TAG = "SplashActivity";
    private UserDetails user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getSupportActionBar().hide();

        user = new UserDetails();
        user.setUuid(tokenManger.getUUID());

        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                getUniversalToken(user);
            }
        }, 3000);
    }

    private void getUniversalToken(UserDetails user) {

        ApiClient.getAuthService().getUniversalToken(user).enqueue(new Callback<UserDetails>() {
            @Override
            public void onResponse(Call<UserDetails> call, Response<UserDetails> response) {
                if (response.isSuccessful()) {
                    if (response.body().isSuccess()) {

                        UserDetails user = new Gson().fromJson(response.body().getJsonObject(), UserDetails.class);
                        tokenManger.setUniversalToken(user.getToken());
//                        sessionManager.setStringValue(SessionManager.KEY_UNIVERSAL_TOKEN, response.body().getToken());
                        launchActivity(LoginActivity.class);
                        finish();
                    } else {
//                        getUniversalToken(user);
                    }
                } else {
//                    getUniversalToken(user);
                }
            }

            @Override
            public void onFailure(Call<UserDetails> call, Throwable t) {
//                getUniversalToken(user);
                Log.i(TAG, "onFailure: " + t.getLocalizedMessage());
                Toast.makeText(SplashActivity.this, "" + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}