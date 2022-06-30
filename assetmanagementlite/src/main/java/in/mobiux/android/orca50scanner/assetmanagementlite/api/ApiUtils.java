package in.mobiux.android.orca50scanner.assetmanagementlite.api;

import android.app.Activity;
import android.content.Intent;

import java.io.IOException;

import in.mobiux.android.orca50scanner.assetmanagementlite.activity.LoginActivity;
import in.mobiux.android.orca50scanner.assetmanagementlite.util.AppAlertDialog;
import in.mobiux.android.orca50scanner.assetmanagementlite.util.Constraints;
import retrofit2.Response;


public class ApiUtils {

    public static void processApiError(Activity activity, Response<?> response) {
        String apiResponse = Constraints.SOMETHING_WENT_WRONG;

        if (response.code() == 401) {
            Intent intent = new Intent(activity, LoginActivity.class);
            activity.startActivity(intent);
            activity.finish();
            return;
        }

        try {
            apiResponse = response.errorBody().string();
        } catch (IOException e) {
            e.printStackTrace();
            apiResponse = "" + e.getMessage();
        }

        AppAlertDialog.showMessage(activity, "Error", apiResponse);
    }

    public static void processApiFailure(Activity activity, Throwable throwable) {
        if (throwable instanceof IOException) {
            AppAlertDialog.showInternetError(activity, null);
        } else {
            AppAlertDialog.showMessage(activity, Constraints.SOMETHING_WENT_WRONG, "Error" + throwable.getMessage());
        }
    }

    public static void processApiFailureWithRetry(Activity activity, Throwable t, AppAlertDialog.OnInternetError onInternetError) {

        if (t instanceof IOException) {
            AppAlertDialog.showInternetError(activity, onInternetError);
        } else {
            processApiFailure(activity, t);
        }
    }
}
