package in.mobiux.android.orca50scanner.api;

import android.content.Context;

import in.mobiux.android.orca50scanner.MyApplication;

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
