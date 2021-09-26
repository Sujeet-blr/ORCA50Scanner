package in.mobiux.android.orca50scanner.common.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import in.mobiux.android.orca50scanner.common.R;
import in.mobiux.android.orca50scanner.common.utils.App;
import in.mobiux.android.orca50scanner.common.utils.AppLogger;
import in.mobiux.android.orca50scanner.common.utils.LanguageUtils;
import in.mobiux.android.orca50scanner.common.utils.SessionManager;


/**
 * Created by SUJEET KUMAR on 08-Mar-21.
 */
public class AppActivity extends AppCompatActivity {

    public enum ACTIVITY_STATE {
        CREATED, ACTIVE, INACTIVE, FINISHED
    }

    public ACTIVITY_STATE state = ACTIVITY_STATE.FINISHED;

    protected AppLogger logger;
    public View parentLayout;
    public static int STORAGE_PERMISSION_CODE = 121;
    public static int CAMERA_PERMISSION_CODE = 123;

    protected App app;

    protected static String TAG = AppActivity.class.getCanonicalName();
    protected SessionManager session;

    protected ProgressDialog progressDialog;
    private VirtualKeyListenerBroadcastReceiver mVirtualKeyListenerBroadcastReceiver;

    protected LanguageUtils languageUtils;
    protected LanguageUtils.Language activityLanguage;

    private Toast toast;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        logger = AppLogger.getInstance(getApplicationContext());
        app = (App) getApplicationContext();
        session = SessionManager.getInstance(getApplicationContext());
        app.addActivity(this);

        TAG = this.getClass().getCanonicalName();

        registerVirtualKeyListener();

        logger.i(TAG, "created Activity : " + this.getClass().getCanonicalName());

        languageUtils = new LanguageUtils(getApplicationContext());
        languageUtils.switchLanguage(this, session.getLanguage());
        activityLanguage = session.getLanguage();

//        toast = new Toast(this);
        toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        setActivityState(ACTIVITY_STATE.CREATED);
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

        setActivityState(ACTIVITY_STATE.ACTIVE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        setActivityState(ACTIVITY_STATE.INACTIVE);
    }


    // Function to check and request permission.
    public void checkPermission(AppActivity activity, String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(AppActivity.this, permission)
                == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(AppActivity.this,
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

    protected void showToast(String message) {
        toast.setText(message);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }

    protected void showToast(int resId) {
        toast.setText(resId);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
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
//                        for (BaseActivity activity : app.activities) {
////                            if (!(activity instanceof HomeActivity)) {
////                                activity.finish();
////                            }
//                        }
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

        setActivityState(ACTIVITY_STATE.FINISHED);
    }

    private void setActivityState(ACTIVITY_STATE state) {
        this.state = state;
    }
}
