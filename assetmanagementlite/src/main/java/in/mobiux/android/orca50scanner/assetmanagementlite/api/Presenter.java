package in.mobiux.android.orca50scanner.assetmanagementlite.api;

import android.content.Context;

import in.mobiux.android.orca50scanner.assetmanagementlite.util.MyApplication;


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
