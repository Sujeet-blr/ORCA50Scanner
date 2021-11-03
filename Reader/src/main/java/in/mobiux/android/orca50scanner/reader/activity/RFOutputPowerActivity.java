package in.mobiux.android.orca50scanner.reader.activity;


import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import in.mobiux.android.orca50scanner.reader.R;
import in.mobiux.android.orca50scanner.reader.core.RFIDReader;
import in.mobiux.android.orca50scanner.reader.core.Reader;

public class RFOutputPowerActivity extends BaseActivity {

    private EditText edtRFOutput;
    private Button btnGet, btnSet;
    private TextView tvStatus;
    private int rfOutputPower = 0;
    private int RF_MIN_VALUE = 3;
    private int RF_MAX_VALUE = 50;

    private RFIDReader rfidReader;
    private boolean isFreshConnection = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rfoutput_power);

        edtRFOutput = findViewById(R.id.edtRFOutput);
        btnGet = findViewById(R.id.btnGet);
        btnSet = findViewById(R.id.btnSet);
        tvStatus = findViewById(R.id.tvStatus);

        rfOutputPower = session.getInt(session.KEY_RF_OUTPUT_POWER, rfOutputPower);
        edtRFOutput.setText("" + rfOutputPower);

        setToolbarTitle("RF Output Power");

        rfidReader = new RFIDReader(getApplicationContext());
        rfidReader.connect(Reader.ReaderType.RFID);

        if (rfidReader.isConnected()) {
            tvStatus.setText("Connected");
        } else {
            tvStatus.setText("Not Connected");
            rfidReader.connect(Reader.ReaderType.RFID);
        }

        btnGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rfidReader.isConnected()) {
                    rfOutputPower = session.getInt(session.KEY_RF_OUTPUT_POWER, rfOutputPower);
                    edtRFOutput.setText("" + rfOutputPower);
                } else {
                    showToast("RFID Reader is not connected");
                }
            }
        });

        btnSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (edtRFOutput.length() == 0) {
                    edtRFOutput.setError("Enter value");
                    return;
                }

                rfOutputPower = Integer.valueOf(edtRFOutput.getText().toString());

                if (rfidReader.isConnected()) {
                    int status = rfidReader.setRFOutputPower(rfOutputPower);
                    if (status == 0) {
                        showToast("RF Output Power set to " + rfOutputPower);
                        finish();
                    } else {
                        showToast("RF Output Power Not saved");
                    }

                } else {
                    showToast("RFID Reader is not Connected");
                }
            }
        });

        edtRFOutput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0) {
                    if (TextUtils.isDigitsOnly(charSequence)) {
                        int value = Integer.parseInt(charSequence.toString());
                        if (value < RF_MIN_VALUE || value > RF_MAX_VALUE) {
                            edtRFOutput.setError("Range must be " + RF_MIN_VALUE + " - " + RF_MAX_VALUE);
                            btnSet.setEnabled(false);
                        } else {
                            btnSet.setEnabled(true);
                        }
                    } else {
                        edtRFOutput.setError("Enter Number only");
                        btnSet.setEnabled(false);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (rfidReader.isConnected()) {
                            tvStatus.setText("Connected");
                        } else {
                            tvStatus.setText("Not Connected");
                        }
                    }
                });
            }
        }, 1000, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        rfidReader.releaseResources();
    }
}