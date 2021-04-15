package in.mobiux.android.orca50scanner.activity;

import androidx.cardview.widget.CardView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import in.mobiux.android.orca50scanner.MainActivity;
import in.mobiux.android.orca50scanner.R;
import in.mobiux.android.orca50scanner.api.ApiClient;
import in.mobiux.android.orca50scanner.api.Presenter;
import in.mobiux.android.orca50scanner.api.model.AssetResponse;
import in.mobiux.android.orca50scanner.api.model.Inventory;
import in.mobiux.android.orca50scanner.viewmodel.InventoryViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends BaseActivity implements View.OnClickListener {

    private CardView cardInventory, cardLocate, cardTransfer, cardSync;
    private InventoryViewModel viewModel;
    private List<Inventory> inventories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

//        getSupportActionBar().hide();

        cardInventory = findViewById(R.id.cardInventory);
        cardLocate = findViewById(R.id.cardLocate);
        cardTransfer = findViewById(R.id.cardTransfer);
        cardSync = findViewById(R.id.cardSync);

        cardInventory.setOnClickListener(this);
        cardLocate.setOnClickListener(this);
        cardTransfer.setOnClickListener(this);
        cardSync.setOnClickListener(this);

        viewModel = new ViewModelProvider(this).get(InventoryViewModel.class);
        viewModel.getAllInventory().observe(this, new Observer<List<Inventory>>() {
            @Override
            public void onChanged(List<Inventory> list) {
                inventories = list;

                if (inventories.isEmpty()) {
                    Toast.makeText(app, "Sync with server to proceed", Toast.LENGTH_SHORT).show();
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
//        app.inventories();

//        Presenter.INSTANCE.pullLatestData();
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
    protected void onDestroy() {
        super.onDestroy();
        app.onTerminate();
    }
}