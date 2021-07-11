package in.mobiux.android.orca50scanner.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import in.mobiux.android.orca50scanner.R;
import in.mobiux.android.orca50scanner.api.Presenter;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getSupportActionBar().hide();


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if (session.rawToken().isEmpty()) {
                    startActivity(new Intent(app, LoginActivity.class));
                } else {

//                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
//                    Intent intent = new Intent(getApplicationContext(), RFIDActivity.class);
                    Intent intent = new Intent(getApplicationContext(), SampleActivity.class);
                    startActivity(intent);
                }
                finish();
            }
        }, 3000);
    }
}