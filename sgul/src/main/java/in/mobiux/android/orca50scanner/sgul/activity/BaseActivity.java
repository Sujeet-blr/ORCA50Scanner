package in.mobiux.android.orca50scanner.sgul.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;


import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

import in.mobiux.android.orca50scanner.reader.activity.RFIDReaderBaseActivity;
import in.mobiux.android.orca50scanner.sgul.MyApplication;
import in.mobiux.android.orca50scanner.sgul.R;
import in.mobiux.android.orca50scanner.sgul.api.model.Inventory;
import in.mobiux.android.orca50scanner.sgul.util.AppLogger;
import in.mobiux.android.orca50scanner.sgul.util.LanguageUtils;
import in.mobiux.android.orca50scanner.sgul.util.SessionManager;

/**
 * Created by SUJEET KUMAR on 08-Mar-21.
 */
public class BaseActivity extends RFIDReaderBaseActivity {

    protected AppLogger logger;
    public View parentLayout;
    public static int STORAGE_PERMISSION_CODE = 121;
    public static int CAMERA_PERMISSION_CODE = 123;

    protected MyApplication app;

    protected static String TAG = BaseActivity.class.getCanonicalName();
    protected SessionManager session;

    protected ProgressDialog progressDialog;
    private ImageView ivHome;
    private TextView textToolbarTitle;
    private VirtualKeyListenerBroadcastReceiver mVirtualKeyListenerBroadcastReceiver;

    protected LanguageUtils languageUtils;
    protected LanguageUtils.Language activityLanguage;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logger = AppLogger.getInstance(getApplicationContext());
        app = (MyApplication) getApplicationContext();
        session = SessionManager.getInstance(getApplicationContext());
        app.addActivity(this);
        TAG = this.getClass().getCanonicalName();
        registerVirtualKeyListener();

        logger.i(TAG, "created Activity : " + this.getClass().getCanonicalName());

        languageUtils = new LanguageUtils(getApplicationContext());
        languageUtils.switchLanguage(this, session.getLanguage());
        activityLanguage = session.getLanguage();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (parentLayout == null) {
            parentLayout = findViewById(android.R.id.content);
        }

        if (!activityLanguage.equals(session.getLanguage())) {
            recreate();
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull @NotNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        Log.i(TAG, "onConfigurationChanged: ");
    }


//    protected void switchLanguage(String language) {
//        logger.i(TAG, "Language is " + language);
//        Resources resources = getResources();
//        Configuration config = resources.getConfiguration();
//        DisplayMetrics dm = resources.getDisplayMetrics();
//        if (language.equals("en")) {
//            config.locale = Locale.ENGLISH;
//        } else if (language.equals("de")) {
//            config.locale = Locale.GERMAN;
//        } else if (language.equals("fr")) {
//            config.locale = Locale.FRENCH;
//        } else if (language.equals("nl")) {
//            config.locale = new Locale("nl");
//        } else {
//            config.locale = Locale.ENGLISH;
//        }
//        resources.updateConfiguration(config, dm);
//
//        onConfigurationChanged(config);
//
//        session.setLanguage(language);
////        PreferenceUtil.commitString("language", language);
//    }

    // Function to check and request permission.
    public void checkPermission(BaseActivity activity, String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(BaseActivity.this, permission)
                == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(BaseActivity.this,
                    new String[]{permission},
                    requestCode);
        } else {
            logger.i(TAG, "Permission already granted");
        }
    }

    // This function is called when the user accepts or decline the permission.
    // Request Code is used to check which permission called this function.
    // This request code is provided when the user is prompt for permission.

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this,
                        R.string.camera_permission_granted,
                        Toast.LENGTH_SHORT)
                        .show();
            } else {
                Toast.makeText(this,
                        R.string.camera_permission_denied,
                        Toast.LENGTH_SHORT)
                        .show();
            }
        } else if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                logger.i(TAG, getResources().getString(R.string.storage_permission_granted));
            } else {
                logger.e(TAG, getResources().getString(R.string.storage_permission_denied));
            }
        }
    }

    protected void exportToCSV(List<Inventory> list) {

        StringBuilder data = new StringBuilder();
//        data.append("")

        for (Inventory inventory : list) {
            data.append("\n" + inventory.getInventoryId() + "," + inventory.getEpc() + "," + inventory.getQuantity());
        }

        try {

            Context context = getApplicationContext();

            File fileLocation = new File(getFilesDir(), "data.csv");
            Uri path = FileProvider.getUriForFile(context, "in.mobiux.android.orca50scanner.sgul.fileprovider", fileLocation);
            Intent fileIntent = new Intent(Intent.ACTION_SEND);
            fileIntent.setType("text/csv");
            fileIntent.putExtra(Intent.EXTRA_SUBJECT, "Exported Data");
            fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            fileIntent.putExtra(Intent.EXTRA_STREAM, path);
            startActivity(Intent.createChooser(fileIntent, "Export"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
//            if (!AppPrefs.Instance.hasCredentials()) {
//                Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
//                startActivity(intent);
//            }
            finish();
        }
        return true;
    }

    protected void syncRequired() {
        Toast.makeText(app, R.string.sync_required, Toast.LENGTH_SHORT).show();
    }

    protected void setTitle(String title) {
        textToolbarTitle = findViewById(R.id.textToolbarTitle);
        textToolbarTitle.setText(title);
        setHomeButtonEnable(true);
    }

    protected void setHomeButtonEnable(boolean enable) {
        ivHome = findViewById(R.id.ivHome);
        if (enable) {
            ivHome.setVisibility(View.VISIBLE);
        } else {
            ivHome.setVisibility(View.GONE);
        }

        ivHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    public void showToast(String message) {
        Toast.makeText(app, "" + message, Toast.LENGTH_SHORT).show();
    }

    private void registerVirtualKeyListener() {
        mVirtualKeyListenerBroadcastReceiver = new VirtualKeyListenerBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        this.registerReceiver(mVirtualKeyListenerBroadcastReceiver, intentFilter);
    }

    private class VirtualKeyListenerBroadcastReceiver extends BroadcastReceiver {
        private final String SYSTEM_REASON = "reason";
        private final String SYSTEM_HOME_KEY = "homekey";
        private final String SYSTEM_RECENT_APPS = "recentapps";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String systemReason = intent.getStringExtra(SYSTEM_REASON);
                if (systemReason != null) {
                    if (systemReason.equals(SYSTEM_HOME_KEY)) {
                        System.out.println("Press HOME key");
                        for (BaseActivity activity : app.activities) {
                            if (!(activity instanceof HomeActivity)) {
                                activity.finish();
                            }
                        }
                    } else if (systemReason.equals(SYSTEM_RECENT_APPS)) {
                        System.out.println("Press RECENT_APPS key");

                    }
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(mVirtualKeyListenerBroadcastReceiver);

        app.removeActivity(this);
    }
}
