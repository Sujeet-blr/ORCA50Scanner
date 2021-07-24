package in.mobiux.android.orca50scanner.activity;

import androidx.cardview.widget.CardView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import in.mobiux.android.orca50scanner.R;
import in.mobiux.android.orca50scanner.api.model.Inventory;
import in.mobiux.android.orca50scanner.api.model.User;
import in.mobiux.android.orca50scanner.viewmodel.InventoryViewModel;

public class HomeActivity extends BaseActivity implements View.OnClickListener {

    private CardView cardInventory, cardLocate, cardTransfer, cardSync;
    private TextView tvLoginAs;
    private InventoryViewModel viewModel;
    private List<Inventory> inventories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        cardInventory = findViewById(R.id.cardInventory);
        cardLocate = findViewById(R.id.cardLocate);
        cardTransfer = findViewById(R.id.cardTransfer);
        cardSync = findViewById(R.id.cardSync);
        tvLoginAs = findViewById(R.id.tvLoginAs);

        cardInventory.setOnClickListener(this);
        cardLocate.setOnClickListener(this);
        cardTransfer.setOnClickListener(this);
        cardSync.setOnClickListener(this);

        if (session.hasCredentials()) {
            User user = session.getUser();
            tvLoginAs.setText("You are logged in as : " + user.getFirstName() + " " + user.getLastName());
        }

        viewModel = new ViewModelProvider(this).get(InventoryViewModel.class);
        viewModel.getAllInventory().observe(this, new Observer<List<Inventory>>() {
            @Override
            public void onChanged(List<Inventory> list) {
                inventories = list;

                if (inventories.isEmpty()) {
                    showToast(getResources().getString(R.string.sync_with_server_to_proceed));
                    Intent intent = new Intent(app, DataSyncSettingActivity.class);
                    startActivity(intent);
                }
            }
        });

        checkPermission(HomeActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);
        try {
            app.connectRFID();
        } catch (Exception e) {
            logger.e(TAG, "" + e.getLocalizedMessage());
        }
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
            case R.id.cardInventory:
                logger.i(TAG, "inventory");
                if (inventories.isEmpty()) {
                    syncRequired();
                } else {
                    startActivity(new Intent(getApplicationContext(), AssetInventoryActivity.class));
                }
                break;
            case R.id.cardLocate:
                logger.i(TAG, "locate");
                if (inventories.isEmpty()) {
                    syncRequired();
                } else {
                    startActivity(new Intent(getApplicationContext(), LocateAssetActivity.class));
                }
                break;
            case R.id.cardTransfer:
                logger.i(TAG, "transfer");
                if (inventories.isEmpty()) {
                    syncRequired();
                } else {
                    startActivity(new Intent(app, TransferAndAssignActivity.class));
                }
                break;
            case R.id.cardSync:
                logger.i(TAG, "sync");
                startActivity(new Intent(app, DataSyncSettingActivity.class));

                break;
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        app.onTerminate();
    }
}