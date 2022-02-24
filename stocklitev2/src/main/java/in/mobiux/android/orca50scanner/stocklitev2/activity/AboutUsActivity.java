package in.mobiux.android.orca50scanner.stocklitev2.activity;

import android.os.Bundle;

import in.mobiux.android.orca50scanner.stocklitev2.R;

public class AboutUsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("About Us");
    }
}