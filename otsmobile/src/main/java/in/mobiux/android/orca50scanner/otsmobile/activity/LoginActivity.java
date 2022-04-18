package in.mobiux.android.orca50scanner.otsmobile.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import in.mobiux.android.orca50scanner.otsmobile.BuildConfig;
import in.mobiux.android.orca50scanner.otsmobile.R;
import in.mobiux.android.orca50scanner.otsmobile.api.ApiClient;
import in.mobiux.android.orca50scanner.otsmobile.api.model.ProcessPoint;
import in.mobiux.android.orca50scanner.otsmobile.api.model.UserDetails;
import in.mobiux.android.orca50scanner.otsmobile.database.ProcessPointRepo;
import in.mobiux.android.orca50scanner.otsmobile.utils.TokenManger;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends BaseActivity {

    private static final String TAG = "LoginActivity";

    private Button btnSubmit;
    private EditText edtClockNumber, edtPinNumber;
    private ProcessPointRepo processPointRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().hide();

        edtClockNumber = findViewById(R.id.edtClockNumber);
        edtPinNumber = findViewById(R.id.edtPinNumber);
        btnSubmit = findViewById(R.id.btnSubmit);

        processPointRepo = new ProcessPointRepo(getApplicationContext());

        btnSubmit.setOnClickListener(view -> {

            String clockNumber = edtClockNumber.getText().toString();
            String pinNumber = edtPinNumber.getText().toString();

            if (clockNumber.length() > 0 && pinNumber.length() > 0) {
                UserDetails details = new UserDetails();
                details.setClockNumber(clockNumber);
                details.setPinNumber(pinNumber);

                login(details);

            } else {
                Toast.makeText(app, "Please enter valid credentials", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void login(UserDetails user) {
        String token = TokenManger.getInstance(getApplicationContext()).getPartialAuthorizationToken();

        ApiClient.getAuthService().login(token, user).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {

                    JsonObject res = response.body();
                    JsonObject body = res.getAsJsonObject("body");

                    if (res.get("success").getAsBoolean()) {

                        String token = body.get("token").getAsString();

                        JsonObject universal = body.getAsJsonObject("universal");
                        JsonObject userDetails = universal.getAsJsonObject("userDetails");
                        UserDetails user = new Gson().fromJson(userDetails, UserDetails.class);

                        tokenManger.setApplicationToken(token);

                        Intent intent = new Intent(getApplicationContext(), LocationActivity.class);
                        intent.putExtra("user", user);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(app, "" + body.get("message"), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(app, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });
    }
}