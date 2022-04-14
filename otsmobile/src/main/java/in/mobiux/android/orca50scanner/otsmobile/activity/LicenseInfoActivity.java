package in.mobiux.android.orca50scanner.otsmobile.activity;

import android.os.Bundle;

import in.mobiux.android.orca50scanner.otsmobile.R;


public class LicenseInfoActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license_info);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("License Information");
    }
}