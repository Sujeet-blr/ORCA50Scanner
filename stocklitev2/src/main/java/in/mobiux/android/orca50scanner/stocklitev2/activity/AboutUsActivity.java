package in.mobiux.android.orca50scanner.stocklitev2.activity;

import android.os.Bundle;
import android.widget.TextView;

import in.mobiux.android.orca50scanner.stocklitev2.R;

public class AboutUsActivity extends BaseActivity {

    private static final String TAG = "AboutUsActivity";
    private TextView tvContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("About Developers");

        tvContent = findViewById(R.id.tvContent);

        tvContent.setText(R.string.about_us_content);
    }
}