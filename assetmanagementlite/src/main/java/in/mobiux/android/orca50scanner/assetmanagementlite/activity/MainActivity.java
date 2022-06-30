package in.mobiux.android.orca50scanner.assetmanagementlite.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

import in.mobiux.android.orca50scanner.assetmanagementlite.R;
import in.mobiux.android.orca50scanner.assetmanagementlite.api.ApiClient;
import in.mobiux.android.orca50scanner.assetmanagementlite.api.model.AssetResponse;
import in.mobiux.android.orca50scanner.assetmanagementlite.api.model.Inventory;
import in.mobiux.android.orca50scanner.assetmanagementlite.api.model.User;
import in.mobiux.android.orca50scanner.assetmanagementlite.util.SessionManager;
import in.mobiux.android.orca50scanner.assetmanagementlite.viewmodel.InventoryViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private CardView cardCheckIn, cardCheckOut, cardInformation, cardSettings;
    private TextView tvLoginAs;
    private InventoryViewModel viewModel;
    private List<Inventory> inventories = new ArrayList<>();
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        session = SessionManager.getInstance(app);

        cardCheckIn = findViewById(R.id.cardCheckIn);
        cardCheckOut = findViewById(R.id.cardCheckOut);
        cardInformation = findViewById(R.id.cardInformation);
        cardSettings = findViewById(R.id.cardSettings);
        tvLoginAs = findViewById(R.id.tvLoginAs);

        cardCheckIn.setOnClickListener(this);
        cardCheckOut.setOnClickListener(this);
        cardInformation.setOnClickListener(this);
        cardSettings.setOnClickListener(this);

        if (session.hasCredentials()) {
            User user = session.getUser();
            tvLoginAs.setText(getResources().getString(R.string.you_are_logged_in_as) + user.getFirstName() + " " + user.getLastName());
        }

        checkPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!session.hasCredentials()) {
            Intent intent = new Intent(app, LoginActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cardCheckIn:
                startActivity(new Intent(getApplicationContext(), CheckInActivity.class));

                break;
            case R.id.cardCheckOut:
                logger.i(TAG, "Check Out");
                startActivity(new Intent(getApplicationContext(), CheckOutActivity.class));

                break;
            case R.id.cardInformation:
                logger.i(TAG, "Asset Information");
                startActivity(new Intent(app, AssetInfoActivity.class));

                break;
            case R.id.cardSettings:
                logger.i(TAG, "SignOut");
                startActivity(new Intent(app, DeviceSettingsActivity.class));
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (!session.isRememberMe()) {
            session.logout();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        app.onTerminate();
    }
}