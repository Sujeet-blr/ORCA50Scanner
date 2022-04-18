package in.mobiux.android.orca50scanner.otsmobile.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import in.mobiux.android.orca50scanner.otsmobile.R;
import in.mobiux.android.orca50scanner.otsmobile.api.ApiClient;
import in.mobiux.android.orca50scanner.otsmobile.api.model.BaseModel;
import in.mobiux.android.orca50scanner.otsmobile.api.model.ProcessPoint;
import in.mobiux.android.orca50scanner.otsmobile.api.model.UserDetails;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LocationActivity extends BaseActivity {

    private static final String TAG = "LocationActivity";

    private Button btnLocation1, btnLocation2;
    private TextView tvUserName;
    private String userName = "Les Davies";
    private UserDetails userDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        getSupportActionBar().setTitle("Select Scan Location");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btnLocation1 = findViewById(R.id.btnLocation1);
        btnLocation2 = findViewById(R.id.btnLocation2);
        tvUserName = findViewById(R.id.tvUserName);

        userName = getIntent().getStringExtra("username");
        userDetails = (UserDetails) getIntent().getSerializableExtra("user");


        if (userDetails != null) {
            userName = userDetails.getFirstName() + " " + userDetails.getLastName();
        }

        tvUserName.setText("" + userName);

        btnLocation1.setOnClickListener(view -> {
            ProcessPoint processPoint = new ProcessPoint();
            processPoint.setId(58);
            processPoint.setName("Sherburn Loading Scan");

            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("processPoint", processPoint);
            intent.putExtra("user", userDetails);
            startActivity(intent);
        });

        btnLocation2.setOnClickListener(view -> {
            ProcessPoint processPoint = new ProcessPoint();
            processPoint.setId(97);
            processPoint.setName("All Delivery Scan");

            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("processPoint", processPoint);
            intent.putExtra("user", userDetails);
            startActivity(intent);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull @NotNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(app, SettingsActivity.class));
        } else if (id == R.id.action_logout) {
            logout();
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        ApiClient.getAuthService().logout(tokenManger.getPartialAuthorizationToken()).enqueue(new Callback<BaseModel>() {
            @Override
            public void onResponse(Call<BaseModel> call, Response<BaseModel> response) {
                if (response.isSuccessful()) {
                    if (response.body().isSuccess()) {
                        finish();
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                    } else {
                        Log.i(TAG, "onResponse: " + response.body().getMessage());
                    }
                } else {
                    Toast.makeText(app, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseModel> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t.getLocalizedMessage());
                Toast.makeText(app, "" + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}