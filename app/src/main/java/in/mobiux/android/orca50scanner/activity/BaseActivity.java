package in.mobiux.android.orca50scanner.activity;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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

import com.nativec.tools.ModuleManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import in.mobiux.android.orca50scanner.MyApplication;
import in.mobiux.android.orca50scanner.R;
import in.mobiux.android.orca50scanner.api.model.Inventory;
import in.mobiux.android.orca50scanner.util.AppLogger;
import in.mobiux.android.orca50scanner.util.SessionManager;

/**
 * Created by SUJEET KUMAR on 08-Mar-21.
 */
public class BaseActivity extends AppCompatActivity {

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

//        if (!session.hasCredentials() && !(this instanceof LoginActivity)) {
//            Intent intent = new Intent(app, LoginActivity.class);
//            startActivity(intent);
//        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (parentLayout == null) {
            parentLayout = findViewById(android.R.id.content);
        }
    }

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
                        "Camera Permission Granted",
                        Toast.LENGTH_SHORT)
                        .show();
            } else {
                Toast.makeText(this,
                        "Camera Permission Denied",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        } else if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                logger.i(TAG, "Storage Permission Granted");
            } else {
                logger.e(TAG, "Storage Permission Denied");
            }
        }
    }

    protected void exportToCSV(List<Inventory> list) {

        StringBuilder data = new StringBuilder();
//        data.append("")

        for (Inventory inventory : list) {
            data.append("\n" + inventory.getInventoryId() + "," + inventory.getEpc() + "," + String.valueOf(inventory.getQuantity()));
        }

        try {

            Context context = getApplicationContext();

            File fileLocation = new File(getFilesDir(), "data.csv");
            Uri path = FileProvider.getUriForFile(context, "in.mobiux.android.orca50scanner.fileprovider", fileLocation);
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
        Toast.makeText(app, "Sync required to proceed", Toast.LENGTH_SHORT).show();
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
    }
}
