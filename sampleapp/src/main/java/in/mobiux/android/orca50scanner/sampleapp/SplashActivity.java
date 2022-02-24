package in.mobiux.android.orca50scanner.sampleapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

//import in.mobiux.android.orca50scanner.unitech.activity.UnitechScannerActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//                Intent intent = new Intent(getApplicationContext(), RFIDActivity.class);
//                Intent intent = new Intent(getApplicationContext(), UnitechScannerActivity.class);
                startActivity(intent);
                finish();
            }
        }, 1000);
    }
}