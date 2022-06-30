package in.mobiux.android.orca50scanner.assetmanagementlite.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import in.mobiux.android.orca50scanner.assetmanagementlite.R;
import in.mobiux.android.orca50scanner.assetmanagementlite.activity.BaseActivity;
import in.mobiux.android.orca50scanner.assetmanagementlite.activity.CheckInActivity;
import in.mobiux.android.orca50scanner.assetmanagementlite.activity.LoginActivity;
import in.mobiux.android.orca50scanner.assetmanagementlite.activity.MainActivity;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {

                if (session.rawToken().isEmpty()) {
                    startActivity(new Intent(app, LoginActivity.class));
//                    startActivity(new Intent(app, AssetInfoActivity.class));
                } else {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                }
                finish();
            }
        }, 3000);
    }
}