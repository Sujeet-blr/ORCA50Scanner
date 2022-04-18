package in.mobiux.android.orca50scanner.otsmobile.api;

import android.content.Context;
import android.util.Log;

import java.util.List;

import in.mobiux.android.orca50scanner.otsmobile.api.model.ScanItem;
import in.mobiux.android.orca50scanner.otsmobile.api.model.UserDetails;
import in.mobiux.android.orca50scanner.otsmobile.utils.SessionManager;
import in.mobiux.android.orca50scanner.otsmobile.utils.TokenManger;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Presenter {

    private static final String TAG = "Presenter";

    private static Presenter instance;
    private Context context;

    public void syncScanItems(List<ScanItem> items) {

        TokenManger tokenManger = TokenManger.getInstance(context);
    }
}
