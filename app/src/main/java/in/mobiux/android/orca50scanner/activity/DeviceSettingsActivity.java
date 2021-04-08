package in.mobiux.android.orca50scanner.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import in.mobiux.android.orca50scanner.R;

public class DeviceSettingsActivity extends BaseActivity {

    private CardView cardRSSI, cardBuzzer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_settings);

        setTitle("Device Settings");
        cardRSSI = findViewById(R.id.cardRSSI);
        cardBuzzer = findViewById(R.id.cardBuzzer);

        cardRSSI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(app, RFOutputPowerSettingActivity.class);
                startActivity(intent);
            }
        });

        cardBuzzer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(app, BuzzerSettingActivity.class);
                startActivity(intent);
            }
        });
    }
}