package in.mobiux.android.orca50scanner.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.module.interaction.ModuleConnector;
import com.nativec.tools.ModuleManager;
import com.rfid.rxobserver.bean.RXInventoryTag;

import in.mobiux.android.orca50scanner.BuildConfig;
import in.mobiux.android.orca50scanner.MyApplication;
import in.mobiux.android.orca50scanner.R;
import in.mobiux.android.orca50scanner.activity.HomeActivity;
import in.mobiux.android.orca50scanner.api.model.Inventory;
import in.mobiux.android.orca50scanner.util.AppLogger;
import in.mobiux.android.orca50scanner.util.RFIDReaderListener;
import in.mobiux.android.orca50scanner.util.SessionManager;

public class OrcaReaderForegroundService extends Service {

    private static final String TAG = OrcaReaderForegroundService.class.getCanonicalName();
    private static final String CHANNEL_ID = "foregroundservice";
    private Context context;
    private SessionManager session;
    private AppLogger logger;
    private ModuleConnector connector;
    private MyApplication app;


    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();
        app = (MyApplication) context;
        session = SessionManager.getInstance(context);
        logger = AppLogger.getInstance(context);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        return super.onStartCommand(intent, flags, startId);

        createNotificationChannel();
        Intent notificationIntent = new Intent(this, HomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText("Service is running")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        if (BuildConfig.DEBUG) {

        } else {
            if (connector != null && !connector.isConnected()) {
                app.connectRFID();
                ModuleManager.newInstance().setUHFStatus(true);
            } else {
                ModuleManager.newInstance().setUHFStatus(true);
                logger.i(TAG, "connected");
            }
        }

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "ForegroundServiceChannel", NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
}
