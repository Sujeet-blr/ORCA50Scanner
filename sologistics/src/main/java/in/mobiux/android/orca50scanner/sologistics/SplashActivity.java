package in.mobiux.android.orca50scanner.sologistics;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import in.mobiux.android.orca50scanner.sologistics.activity.BarcodeScannerActivity;
import in.mobiux.android.orca50scanner.sologistics.activity.BaseActivity;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(getApplicationContext(), BarcodeScannerActivity.class));
                finish();
            }
        }, 3000);
    }
}