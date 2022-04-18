package in.mobiux.android.orca50scanner.otsmobile.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import in.mobiux.android.orca50scanner.otsmobile.SplashActivity;
import in.mobiux.android.orca50scanner.otsmobile.api.AuthClient;
import in.mobiux.android.orca50scanner.otsmobile.utils.MyApp;
import in.mobiux.android.orca50scanner.otsmobile.utils.SessionManager;
import in.mobiux.android.orca50scanner.otsmobile.utils.TokenManger;

public class BaseActivity extends AppCompatActivity implements AuthClient.OnAuthValidation {

    public MyApp app;
    public SessionManager sessionManager;
    public TokenManger tokenManger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        app = (MyApp) getApplicationContext();
        sessionManager = SessionManager.getInstance(app);
        tokenManger = TokenManger.getInstance(app);

        if ((this instanceof LoginActivity) || (this instanceof SplashActivity)) {
        } else {
            AuthClient.getInstance(app).setOnAuthValidation(this);
        }
    }

    protected void launchActivity(Class<?> cls) {
        Intent intent = new Intent(this, cls);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return true;
    }

    public void showToast(String message) {
        Toast.makeText(app, "" + message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void validate(boolean status) {
        if (!status) {
            finish();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }
}
