package in.mobiux.android.orca50scanner;

import android.app.Application;

import androidx.room.Room;

import com.module.interaction.ModuleConnector;
import com.rfid.RFIDReaderHelper;
import com.rfid.ReaderConnector;

import in.mobiux.android.orca50scanner.database.AppDatabase;
import in.mobiux.android.orca50scanner.util.AppLogger;
import in.mobiux.android.orca50scanner.util.DeviceConnector;
import in.mobiux.android.orca50scanner.util.RFIDReaderListener;

/**
 * Created by SUJEET KUMAR on 08-Mar-21.
 */
public class MyApplication extends Application {

    public AppDatabase db;
    public AppLogger logger;
    private String TAG = MyApplication.class.getName();

//    public ModuleConnector connector = new ReaderConnector();
    public RFIDReaderHelper rfidReaderHelper;
    private RFIDReaderListener listener;
    boolean connectionStatus = false;

    @Override
    public void onCreate() {
        super.onCreate();

        logger = AppLogger.getInstance(getApplicationContext());
        logger.i(TAG, "============App Started....==========\n");

//        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "db_name.db").allowMainThreadQueries().fallbackToDestructiveMigration().build();
    }

    public void connectRFID() {

//        if (connector.connectCom(DeviceConnector.PORT, DeviceConnector.BOUD_RATE)) {
//
//        }

        if (listener != null) {
//            listener.onConnection(connector.isConnected());
        }
    }

    public void setOnEventListener(RFIDReaderListener listener) {
        this.listener = listener;
    }


    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
