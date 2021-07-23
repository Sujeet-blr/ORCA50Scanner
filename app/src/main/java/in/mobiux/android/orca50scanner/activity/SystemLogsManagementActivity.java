package in.mobiux.android.orca50scanner.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;

import in.mobiux.android.orca50scanner.R;
import in.mobiux.android.orca50scanner.util.AppUtils;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SystemLogsManagementActivity extends BaseActivity {

    private RadioGroup radioGroup;
    private RadioButton radioSendLogs, radioClearLogs;
    private Button btnSave;
    public static String KEY_RADIO = "logs_choice_setting";
    private int checkedId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_logs_management);

        setTitle("System Logs Management...");

        radioGroup = findViewById(R.id.radioGroup);
        radioSendLogs = findViewById(R.id.rad1);
        radioClearLogs = findViewById(R.id.rad2);
        btnSave = findViewById(R.id.btnSave);

        if (session.getValue(KEY_RADIO).isEmpty()) {
            checkedId = R.id.rad1;
        } else {
            if (session.getValue(KEY_RADIO).equals("0")) {
                checkedId = R.id.rad1;
            } else if (session.getValue(KEY_RADIO).equals("1")) {
                checkedId = R.id.rad2;
            }
        }

        radioGroup.check(checkedId);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                checkedId = i;
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (radioGroup.getCheckedRadioButtonId() == -1) {
                    showToast("Choose any option to update");
                } else {
                    if (radioGroup.getCheckedRadioButtonId() == R.id.rad1) {
                        session.setValue(KEY_RADIO, "0");
                        showToast("Saved Success");
                    } else if (radioGroup.getCheckedRadioButtonId() == R.id.rad2) {
                        session.setValue(KEY_RADIO, "1");
                        showToast("Saved Success");
                    } else {
                        showToast(getResources().getString(R.string.something_went_wrong));
                    }
                }
            }
        });
    }
}