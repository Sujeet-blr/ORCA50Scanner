package in.mobiux.android.orca50scanner.sologistics.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import in.mobiux.android.orca50scanner.sologistics.R;

public class ReadMeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_me);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Read Me");
    }
}