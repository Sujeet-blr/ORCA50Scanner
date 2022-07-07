package in.mobiux.android.orca50scanner.stocklitev2.components;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import in.mobiux.android.orca50scanner.stocklitev2.R;

public class AppDialog {

    private static final String TAG = "AppDialog";


    private static OnInternetError onInternetError;

    public static AlertDialog.Builder showAlert(Activity activity, String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setTitle(title);
        builder.setMessage(message);

        AlertDialog alertDialog = builder.create();
//        alertDialog.show();
        return builder;
    }

    public static void showMessage(Activity activity, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        final View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_message, null);
        builder.setView(dialogView);

        TextView tvTitle = dialogView.findViewById(R.id.tvTitle);
        TextView tvMessage = dialogView.findViewById(R.id.tvMessage);
        Button btnOK = dialogView.findViewById(R.id.btnOK);

        tvTitle.setText(title);
        tvMessage.setText(message);

        AlertDialog alertDialog = builder.create();

        btnOK.setOnClickListener(view -> {
            alertDialog.dismiss();
        });

        alertDialog.show();
    }

    public static void showInternetError(Activity activity, OnInternetError listener) {

        onInternetError = listener;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        final View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_no_internet, null);
        builder.setView(dialogView);

        Button btnOK = dialogView.findViewById(R.id.btnOK);
        AlertDialog alertDialog = builder.create();

        btnOK.setOnClickListener(view -> {
            alertDialog.dismiss();

            if (onInternetError != null) {
                onInternetError.onRetry();
            }
        });

        alertDialog.show();
    }

    public interface OnInternetError {
        void onRetry();
    }

    public void setOnRetryClickListener(OnInternetError listener) {
        onInternetError = listener;
    }

}
