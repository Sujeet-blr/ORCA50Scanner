package in.mobiux.android.orca50scanner.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.nativec.tools.ModuleManager;

import in.mobiux.android.orca50scanner.R;

public class RFOutputPowerSettingActivity extends BaseActivity {


    private EditText edtRSSI;
    private Button btnGet, btnSet;
    private String rssiValue = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_r_f_output_power_setting);

        setTitle("RF Power Output");

        edtRSSI = findViewById(R.id.edtRSSI);
        btnGet = findViewById(R.id.btnGet);
        btnSet = findViewById(R.id.btnSet);

        rssiValue = session.getValue("rssi");
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
                        session.setValue("rssi", rssiValue);
                        app.setOutputPower(session.getValue("rssi"));
                        showDialog("RF Power Output set successfully");
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

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                finish();
            }
        });

        builder.show();
    }
}