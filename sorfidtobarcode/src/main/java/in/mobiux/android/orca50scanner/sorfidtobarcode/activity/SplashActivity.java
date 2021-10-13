package in.mobiux.android.orca50scanner.sorfidtobarcode.activity;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;


//import com.zebra.sdl.BarcodeScanActivity;
//import com.zebra.sdl.SDLguiActivity;

import com.zebra.model.Barcode;

import in.mobiux.android.orca50scanner.sorfidtobarcode.R;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {

                Intent intent = new Intent(getApplicationContext(), BarcodeScanActivity.class);
                startActivity(intent);
                finish();
            }
        }, 3000);

    }
}