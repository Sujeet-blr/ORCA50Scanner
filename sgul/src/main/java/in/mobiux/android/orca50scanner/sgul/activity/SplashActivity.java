package in.mobiux.android.orca50scanner.sgul.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import in.mobiux.android.orca50scanner.sgul.R;


public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getSupportActionBar().hide();


        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {

                if (session.rawToken().isEmpty()) {
                    startActivity(new Intent(app, LoginActivity.class));
                } else {

                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                    startActivity(intent);
                }
                finish();
            }
        }, 3000);
    }
}