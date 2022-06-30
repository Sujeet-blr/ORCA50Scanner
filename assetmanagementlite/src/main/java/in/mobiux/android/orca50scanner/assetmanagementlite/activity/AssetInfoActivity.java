package in.mobiux.android.orca50scanner.assetmanagementlite.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import in.mobiux.android.orca50scanner.assetmanagementlite.R;
import in.mobiux.android.orca50scanner.assetmanagementlite.api.ApiClient;
import in.mobiux.android.orca50scanner.assetmanagementlite.api.ApiUtils;
import in.mobiux.android.orca50scanner.assetmanagementlite.api.model.Asset;
import in.mobiux.android.orca50scanner.assetmanagementlite.api.model.AssetAttribute;
import in.mobiux.android.orca50scanner.assetmanagementlite.api.model.AssetHistory;
import in.mobiux.android.orca50scanner.assetmanagementlite.api.model.AssetResponse;
import in.mobiux.android.orca50scanner.assetmanagementlite.util.AppAlertDialog;
import in.mobiux.android.orca50scanner.reader.activity.RFIDReaderBaseActivity;
import in.mobiux.android.orca50scanner.reader.model.Inventory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AssetInfoActivity extends RFIDReaderBaseActivity {

    private static final String TAG = "AssetInfoActivity";

    private TextView tvRFID, tvName, tvDescription, tvDate, tvTime;
    private Button btnSubmit;
    private ImageView ivHome;
    private TextView textToolbarTitle;
    private ImageButton ibSearch;
    private ProgressBar progressBar;
    private AutoCompleteTextView edtName;

    private Inventory inventory;
    private List<Asset> assets = new ArrayList<>();

    private String searchKey;
    private ArrayAdapter<Asset> arrayAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset_info);

        initViews();
        setTitle(getResources().getString(R.string.label_asset_information));
        setHomeButtonEnable(true);

        arrayAdapter = new ArrayAdapter<Asset>(this, android.R.layout.simple_list_item_1, assets);
        edtName.setAdapter(arrayAdapter);

        edtName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                hideKeyboard(AssetInfoActivity.this);
            }
        });

//        edtName.addTextChangedListener(new TextWatcher() {
//
//            private Timer timer = new Timer();
//            private final long DELAY = 1000;
//
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                searchKey = s.toString();
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                timer.cancel();
//                timer = new Timer();
//                timer.schedule(new TimerTask() {
//                    @Override
//                    public void run() {
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
////                                assetDetails();
//                            }
//                        });
//                    }
//                }, DELAY);
//            }
//        });

        edtName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                inventory = new Inventory();
                inventory.setName(assets.get(position).getName());
                inventory.setEpc(assets.get(position).getEpc());

                assetDetails();
            }
        });

        ibSearch.setOnClickListener(view -> {
            if (edtName.length() > 0) {
                searchKey = edtName.getText().toString();
                inventory = new Inventory();
                inventory.setName(searchKey);
                inventory.setEpc(searchKey);

                assetDetails();
            }
        });

        btnSubmit.setOnClickListener(view -> {
            finish();
        });

        assets();
    }

    @Override
    public void onInventoryTag(Inventory inventory) {
        this.inventory = inventory;
        inventory.setName(inventory.getFormattedEPC());

        Log.i(TAG, "onInventoryTag: " + inventory.getFormattedEPC());
    }

    @Override
    public void onInventoryTagEnd(Inventory.InventoryTagEnd tagEnd) {
        if (inventory != null) {
            tvRFID.setText(inventory.getFormattedEPC());
            edtName.setText(inventory.getFormattedEPC());
            assetDetails();

//            for (Asset asset : assets) {
//                if (asset.getEpc().equalsIgnoreCase(inventory.getFormattedEPC())) {
//
//                }
//            }
        }
    }

    private void initViews() {
        tvRFID = findViewById(R.id.tvRFID);
        tvName = findViewById(R.id.tvName);
        tvDescription = findViewById(R.id.tvDescription);
        tvDate = findViewById(R.id.tvDate);
        tvTime = findViewById(R.id.tvTime);
        edtName = findViewById(R.id.edtName);
        ibSearch = findViewById(R.id.ibSearch);
        btnSubmit = findViewById(R.id.btnSubmit);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        ibSearch.setVisibility(View.GONE);
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
        hideKeyboard(AssetInfoActivity.this);

        ApiClient.getApiService().assetDetails(session.token(), inventory.getFormattedEPC()).enqueue(new Callback<AssetResponse>() {
            @Override
            public void onResponse(Call<AssetResponse> call, Response<AssetResponse> response) {
                if (response.isSuccessful()) {
                    AssetResponse assetResponse = response.body();

                    String desc = "";
                    for (AssetAttribute attrs : assetResponse.getAssetAttributes()) {
                        if (attrs.getAttributes().getName().equalsIgnoreCase("Description")) {
                            desc = attrs.getValues().getChoice();
                        }
                    }

                    tvName.setText(assetResponse.getName());
                    String dept = assetResponse.getDepartment().getName().replace("CICO-", "");
                    assetResponse.getDepartment().setName(dept);
                    tvDescription.setText("Department : " + dept + "\n\n" + desc);
                } else {
                    if (response.code() == 404) {
                        AppAlertDialog.showMessage(AssetInfoActivity.this, "Asset Not Found", inventory.getName());
                    } else {
                        ApiUtils.processApiError(AssetInfoActivity.this, response);
                    }
                }

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<AssetResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                ApiUtils.processApiFailure(AssetInfoActivity.this, t);
            }
        });

    }

    private void assets() {

        progressBar.setVisibility(View.VISIBLE);

        ApiClient.getApiService().inventoryList(session.token()).enqueue(new Callback<List<AssetResponse>>() {
            @Override
            public void onResponse(Call<List<AssetResponse>> call, Response<List<AssetResponse>> response) {
                if (response.isSuccessful()) {
                    assets = new ArrayList<>();

                    for (AssetResponse res : response.body()) {
                        Asset asset = new Asset();
                        asset.setEpc(res.getAssetId().getRfid());
                        asset.setName(res.getName());

                        assets.add(asset);
                    }

                    arrayAdapter = new ArrayAdapter<>(AssetInfoActivity.this, android.R.layout.simple_list_item_1, assets);
                    edtName.setAdapter(arrayAdapter);
                }

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<List<AssetResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                ApiUtils.processApiFailureWithRetry(AssetInfoActivity.this, t, new AppAlertDialog.OnInternetError() {
                    @Override
                    public void onRetry() {
                        assets();
                    }
                });
            }
        });
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}