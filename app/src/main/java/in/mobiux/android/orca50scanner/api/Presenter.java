package in.mobiux.android.orca50scanner.api;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import in.mobiux.android.orca50scanner.MyApplication;
import in.mobiux.android.orca50scanner.api.model.AssetResponse;
import in.mobiux.android.orca50scanner.api.model.DepartmentResponse;
import in.mobiux.android.orca50scanner.api.model.Inventory;
import in.mobiux.android.orca50scanner.api.model.Laboratory;
import in.mobiux.android.orca50scanner.database.LaboratoryRepository;
import in.mobiux.android.orca50scanner.util.AppLogger;
import in.mobiux.android.orca50scanner.util.AppUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Presenter {

    String TAG = Presenter.class.getCanonicalName();
    public static Presenter INSTANCE;
    public static boolean configAvailable = false;
    MyApplication app;

    public static void init(Context context) {
        INSTANCE = new Presenter(context);
    }

    private Presenter(Context context) {
        app = (MyApplication) context;
    }
}
