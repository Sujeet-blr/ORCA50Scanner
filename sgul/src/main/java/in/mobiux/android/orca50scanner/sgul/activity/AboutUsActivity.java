package in.mobiux.android.orca50scanner.sgul.activity;

import android.os.Bundle;

import in.mobiux.android.orca50scanner.sgul.R;


public class AboutUsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        setTitle(getResources().getString(R.string.label_about_us));
    }
}