package in.mobiux.android.orca50scanner.stocklitev2.activity;

import android.os.Bundle;

import in.mobiux.android.orca50scanner.stocklitev2.R;

public class ContactUsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Contact Support");
    }
}