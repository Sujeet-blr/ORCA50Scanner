package in.mobiux.android.orca50scanner.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import in.mobiux.android.orca50scanner.R;

public class LicenseActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license);

        setTitle(getResources().getString(R.string.label_license));
    }
}