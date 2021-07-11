package in.mobiux.android.orca50scanner.activity;

import androidx.appcompat.app.AppCompatActivity;

import in.mobiux.android.orca50scanner.R;

import android.os.Bundle;

public class AboutUsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        setTitle(getResources().getString(R.string.label_about_us));
    }
}