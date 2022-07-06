package in.mobiux.android.orca50scanner.stocklitev2.activity;


import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import in.mobiux.android.orca50scanner.stocklitev2.BuildConfig;
import in.mobiux.android.orca50scanner.stocklitev2.R;
import in.mobiux.android.orca50scanner.stocklitev2.activity.BarcodeScannerActivity;
import in.mobiux.android.orca50scanner.stocklitev2.activity.BaseActivity;
import in.mobiux.android.orca50scanner.stocklitev2.utils.MyApplication;

public class SplashActivity extends BaseActivity {

    private static final String TAG = "SplashActivity";

    private RelativeLayout rltContent;
    private ImageView ivLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        getSupportActionBar().hide();

        rltContent = findViewById(R.id.rltContent);
        ivLogo = findViewById(R.id.ivLogo);

        rltContent.setBackgroundColor(getResources().getColor(R.color.adept_colorWindowBackground));
        ivLogo.setImageResource(R.drawable.adept_space_logo);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(getApplicationContext(), BarcodeScannerActivity.class));
                finish();
            }
        }, 3000);
    }
}