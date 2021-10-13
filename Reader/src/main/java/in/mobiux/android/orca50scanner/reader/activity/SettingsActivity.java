package in.mobiux.android.orca50scanner.reader.activity;

import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import in.mobiux.android.orca50scanner.common.activity.ContactSupportActivity;
import in.mobiux.android.orca50scanner.common.activity.ExportLogsActivity;
import in.mobiux.android.orca50scanner.reader.R;


//we can't move this activity to common module.
public class SettingsActivity extends BaseActivity {

    private ToggleButton toggleBeep;
    private CardView cardRFPowerOutput, cardExportLogs, cardContactSupports;
    private TextView tvRFOutputPower;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        toggleBeep = findViewById(R.id.toggleBeep);
        cardRFPowerOutput = findViewById(R.id.cardRFPowerOutput);
        tvRFOutputPower = findViewById(R.id.tvRFOutputPower);
        cardExportLogs = findViewById(R.id.cardExportLogs);
        cardContactSupports = findViewById(R.id.cardContactSupports);

        toggleBeep.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                session.setBooleanValue(session.KEY_BEEP, b);
            }
        });

        cardRFPowerOutput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RFOutputPowerActivity.class);
                startActivity(intent);
            }
        });

        cardExportLogs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ExportLogsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        cardContactSupports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ContactSupportActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        toggleBeep.setChecked(session.getBooleanValue(session.KEY_BEEP));
        tvRFOutputPower.setText("" + session.getInt(session.KEY_RF_OUTPUT_POWER, 0));
    }


}