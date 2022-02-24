package in.mobiux.android.orca50scanner.sampleapp;

import android.os.Bundle;
import android.widget.Button;

import com.zebra.sdl.BarcodeReaderBaseActivity;

import in.mobiux.android.orca50scanner.common.activity.AppActivity;

public class MainActivity extends AppActivity {

    private Button btnExecute;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnExecute = findViewById(R.id.btnExecute);

        btnExecute.setOnClickListener(view -> {

        });
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}