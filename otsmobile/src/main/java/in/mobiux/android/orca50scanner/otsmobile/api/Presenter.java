package in.mobiux.android.orca50scanner.otsmobile.api;

import android.content.Context;

import java.util.List;

import in.mobiux.android.orca50scanner.otsmobile.api.model.ScanItem;
import in.mobiux.android.orca50scanner.otsmobile.utils.TokenManger;

public class Presenter {

    private static final String TAG = "Presenter";

    private static Presenter instance;
    private Context context;

    public void syncScanItems(List<ScanItem> items){

        TokenManger tokenManger = TokenManger.getInstance(context);
    }
}
