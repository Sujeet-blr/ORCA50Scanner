package in.mobiux.android.orca50scanner.sensingobjectkeyboard;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import in.mobiux.android.orca50scanner.common.utils.App;
import in.mobiux.android.orca50scanner.sensingobjectkeyboard.activity.BaseActivity;

public class MainActivity extends BaseActivity {

    private App app;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}