package in.mobiux.android.orca50scanner.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import in.mobiux.android.orca50scanner.R;

public class DataSyncSettingActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_sync_setting);

//        getSupportActionBar().setTitle("ASSET INVENTORY");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}