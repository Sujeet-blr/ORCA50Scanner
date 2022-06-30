package in.mobiux.android.orca50scanner.assetmanagementlite.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import in.mobiux.android.orca50scanner.assetmanagementlite.R;
import in.mobiux.android.orca50scanner.assetmanagementlite.api.ApiClient;
import in.mobiux.android.orca50scanner.assetmanagementlite.api.ApiUtils;
import in.mobiux.android.orca50scanner.assetmanagementlite.api.model.AssetAttribute;
import in.mobiux.android.orca50scanner.assetmanagementlite.api.model.AssetHistory;
import in.mobiux.android.orca50scanner.assetmanagementlite.api.model.AssetResponse;
import in.mobiux.android.orca50scanner.assetmanagementlite.api.model.SyncPayload;
import in.mobiux.android.orca50scanner.assetmanagementlite.util.AppAlertDialog;
import in.mobiux.android.orca50scanner.assetmanagementlite.util.AppUtils;
import in.mobiux.android.orca50scanner.assetmanagementlite.util.Constraints;
import in.mobiux.android.orca50scanner.assetmanagementlite.util.DateUtils;
import in.mobiux.android.orca50scanner.reader.activity.RFIDReaderBaseActivity;
import in.mobiux.android.orca50scanner.reader.model.Inventory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckInActivity extends RFIDReaderBaseActivity {

    private static final String TAG = "CheckInActivity";

    private TextView tvRFID, tvName, tvDescription, tvDate, tvTime;
    private Button btnSubmit;
    private ProgressBar progressBar;

    private String selectedDate = "";
    private String selectedTime = "";
    private Inventory inventory;
    private ImageView ivHome;
    private TextView textToolbarTitle;
    private AssetHistory asset;
    private int assetDepartmentId = 0;

    private Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in);

        initViews();
        setTitle(getResources().getString(R.string.label_asset_check_in));
        setHomeButtonEnable(true);

        tvDate.setOnClickListener(view -> {
            DateUtils.openDatePicker(calendar, tvDate);
        });

        tvTime.setOnClickListener(view -> {
            DateUtils.openTimePicker(calendar, tvTime);
        });

        btnSubmit.setOnClickListener(view -> {
            if (inventory == null) {
                showToast("Press trigger to Scan");
            } else {
                if (asset != null) {

                    SyncPayload payload = new SyncPayload();
                    List<AssetHistory> histories = new ArrayList<>();
                    histories.add(asset);
                    payload.setHistories(histories);


                    if (asset.getDepartment() == Constraints.CHECK_IN_DEPARTMENT) {
                        AppAlertDialog.showErrorMessage(CheckInActivity.this,"Asset already in Checked-IN status",null);

                    } else {
                        asset.setDepartment(Constraints.CHECK_IN_DEPARTMENT);
                        asset.setTime((Calendar.getInstance().getTimeInMillis() - calendar.getTimeInMillis()) / 1000);
                        checkIn(payload);
                    }

                } else {
                    showToast("Asset Details not available");
                }
            }
        });
    }

    @Override
    public void onInventoryTag(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public void onInventoryTagEnd(Inventory.InventoryTagEnd tagEnd) {
        if (inventory != null) {
            tvRFID.setText(inventory.getFormattedEPC());
            assetDetails();

            DateUtils.setDate(calendar, tvDate);
            DateUtils.setTime(calendar, tvTime);
        }
    }

    private void initViews() {
        tvRFID = findViewById(R.id.tvRFID);
        tvName = findViewById(R.id.tvName);
        tvDescription = findViewById(R.id.tvDescription);
        tvDate = findViewById(R.id.tvDate);
        tvTime = findViewById(R.id.tvTime);
        btnSubmit = findViewById(R.id.btnSubmit);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
    }

    protected void setTitle(String title) {
        textToolbarTitle = findViewById(R.id.textToolbarTitle);
        textToolbarTitle.setText(title);
        setHomeButtonEnable(true);
    }

    protected void setHomeButtonEnable(boolean enable) {
        ivHome = findViewById(R.id.ivHome);
        if (enable) {
            ivHome.setVisibility(View.VISIBLE);
        } else {
            ivHome.setVisibility(View.GONE);
        }

        ivHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void assetDetails() {

        progressBar.setVisibility(View.VISIBLE);
        tvName.setText("");
        tvDescription.setText("");
        asset = null;


        ApiClient.getApiService().assetDetails(session.token(), inventory.getFormattedEPC()).enqueue(new Callback<AssetResponse>() {
            @Override
            public void onResponse(Call<AssetResponse> call, Response<AssetResponse> response) {
                if (response.isSuccessful()) {
                    AssetResponse assetResponse = response.body();

                    asset = new AssetHistory();
                    asset.setId(assetResponse.getId());
                    asset.setEpc(inventory.getFormattedEPC());
                    assetDepartmentId = assetResponse.getDepartment().getId();
                    asset.setDepartment(assetDepartmentId);
                    asset.setTime(asset.getUpdateTimeIntervalInSeconds());

                    String desc = "";
                    for (AssetAttribute attrs : assetResponse.getAssetAttributes()) {
                        if (attrs.getAttributes().getName().equalsIgnoreCase("Description")) {
                            desc = attrs.getValues().getChoice();
                        }
                    }

                    tvName.setText(assetResponse.getName());
                    tvDescription.setText(desc);

                } else {
                    ApiUtils.processApiError(CheckInActivity.this, response);
                }

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<AssetResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                ApiUtils.processApiFailure(CheckInActivity.this, t);
            }
        });

    }

    private void checkIn(SyncPayload payload) {

        progressBar.setVisibility(View.VISIBLE);

        ApiClient.getApiService().updateAssetsNew(session.token(), payload).enqueue(new Callback<SyncPayload>() {
            @Override
            public void onResponse(Call<SyncPayload> call, Response<SyncPayload> response) {
                if (response.isSuccessful()) {
                    logger.i(TAG, "Checked-In Success " + response.code());
                    AppAlertDialog.showSuccessMessage(CheckInActivity.this, "Asset Checked-In", new AppAlertDialog.ClickListener() {
                        @Override
                        public void onClick() {
                            finish();
                        }
                    });
                } else {
                    logger.e(TAG, "Checked-In Failed");
                    asset.setDepartment(assetDepartmentId);
                    ApiUtils.processApiError(CheckInActivity.this, response);
                }

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<SyncPayload> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                asset.setDepartment(assetDepartmentId);
                ApiUtils.processApiFailure(CheckInActivity.this, t);
            }
        });
    }
}