package in.mobiux.android.orca50scanner.sampleapp;

import android.os.Bundle;

import com.zebra.sdl.BarcodeReaderBaseActivity;

public class MainActivity extends BarcodeReaderBaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}