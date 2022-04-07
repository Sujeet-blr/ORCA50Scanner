package in.mobiux.android.orca50scanner.sgul.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import in.mobiux.android.orca50scanner.sgul.R;

public class RFOutputPowerSettingActivity extends BaseActivity {


    private EditText edtRSSI;
    private Button btnGet, btnSet;
    private String rssiValue = "30";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_r_f_output_power_setting);

        setTitle(getResources().getString(R.string.label_rf_power_output));

        edtRSSI = findViewById(R.id.edtRSSI);
        btnGet = findViewById(R.id.btnGet);
        btnSet = findViewById(R.id.btnSet);

        rssiValue = session.getValue("rssi");
        logger.i(TAG, "rssi value from session " + rssiValue);
        edtRSSI.setText(rssiValue);

        btnGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logger.i(TAG, "clicked GET");
                edtRSSI.setText(rssiValue);
            }
        });

        btnSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logger.i(TAG, "clicked SET");
                if (edtRSSI.getText().length() > 0) {
                    rssiValue = edtRSSI.getText().toString();
                    if (TextUtils.isDigitsOnly(rssiValue)) {

                        int value = Integer.parseInt(rssiValue);

                        if (value >= 3 && value <= 30) {
                            session.setValue("rssi", rssiValue);
                            setRFOutputPower(Integer.parseInt(rssiValue));
                            showDialog(getResources().getString(R.string.rf_power_output_set_to) + value + " dBm");
                        } else {
                            edtRSSI.setError("valid range is 3dBm to 30dBm");
                        }
                    } else {
                        showToast("Invalid value");
                    }

                }
            }
        });
    }

    private void showDialog(String message) {
        AlertDialog.Builder builder = new MaterialAlertDialogBuilder(RFOutputPowerSettingActivity.this);
        builder.setMessage(message);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                finish();
            }
        });

        builder.show();
    }
}