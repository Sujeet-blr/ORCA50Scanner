package in.mobiux.android.orca50scanner.sensingobjectbarcode.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.zebra.sdl.SDLguiActivity;
import com.zebra.sdl.MainActivity;

import in.mobiux.android.orca50scanner.sensingobjectbarcode.R;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
//                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                startActivity(new Intent(getApplicationContext(), BarcodeScannerActivity.class));
//                startActivity(new Intent(getApplicationContext(), SDLguiActivity.class));
                finish();
            }
        }, 3000);
    }
}