package in.mobiux.android.orca50scanner.stocklitev2.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.cardview.widget.CardView;

import in.mobiux.android.orca50scanner.common.activity.ContactSupportActivity;
import in.mobiux.android.orca50scanner.common.activity.ExportLogsActivity;
import in.mobiux.android.orca50scanner.reader.activity.RFOutputPowerActivity;
import in.mobiux.android.orca50scanner.reader.activity.ReaderStatusActivity;
import in.mobiux.android.orca50scanner.reader.core.RFIDReader;
import in.mobiux.android.orca50scanner.reader.core.Reader;
import in.mobiux.android.orca50scanner.stocklitev2.R;

public class SettingsActivity extends BaseActivity {

    private ToggleButton toggleBeep;
    private CardView cardRFPowerOutput, cardExportLogs, cardContactSupports, cardResetReader, cardReaderStatus, cardReadMe, cardAboutUs, cardLicenseInfo;
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
        cardReaderStatus = findViewById(R.id.cardReaderStatus);
        cardResetReader = findViewById(R.id.cardResetReader);
        cardReadMe = findViewById(R.id.cardReadMe);
        cardAboutUs = findViewById(R.id.cardAboutUs);
        cardLicenseInfo = findViewById(R.id.cardLicenseInfo);

        cardExportLogs.setVisibility(View.GONE);
        cardReaderStatus.setVisibility(View.GONE);

        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toggleBeep.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                session.setBooleanValue(session.KEY_BEEP, b);

            }
        });

        cardRFPowerOutput.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), RFOutputPowerActivity.class);
            startActivity(intent);
        });

        cardExportLogs.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ExportLogsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        cardContactSupports.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ContactUsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        cardReaderStatus.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ReaderStatusActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        cardResetReader.setOnClickListener(view -> {
            cardResetReader.setEnabled(false);
            RFIDReader rfidReader = new RFIDReader(getApplicationContext());
            rfidReader.connect(Reader.ReaderType.RFID);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    int status = rfidReader.reset();
                    cardResetReader.setEnabled(true);

                    if (status == 0) {
                        showToast("Reader Reset Successfully");
                    } else {
                        showToast("Reader Reset Failed!");
                    }

                }
            }, 500);
        });

        cardReadMe.setOnClickListener(view -> {
            Intent intent = new Intent(app, ReadMeActivity.class);
            startActivity(intent);
        });

        cardAboutUs.setOnClickListener(view -> {
            Intent intent = new Intent(app, AboutUsActivity.class);
            startActivity(intent);
        });

        cardLicenseInfo.setOnClickListener(view -> {
            Intent intent = new Intent(app, LicenseInfoActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        toggleBeep.setChecked(session.getBooleanValue(session.KEY_BEEP));
        int rssi = session.getInt(session.KEY_RF_OUTPUT_POWER, 0);
        if (rssi == 0) {
            tvRFOutputPower.setText("-");
        } else {
            tvRFOutputPower.setText("" + rssi);
        }

        logger.i(TAG, "rssi is " + rssi);
    }
}