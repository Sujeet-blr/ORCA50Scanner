package in.mobiux.android.orca50scanner.rfidtagtester.activity;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import in.mobiux.android.orca50scanner.rfidtagtester.R;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(app, MainActivity.class));
//                startActivity(new Intent(app, BarcodeScannerActivity.class));
                finish();
            }
        }, 3000);
    }
}