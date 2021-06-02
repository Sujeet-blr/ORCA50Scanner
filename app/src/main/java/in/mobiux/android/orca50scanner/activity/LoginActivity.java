package in.mobiux.android.orca50scanner.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import in.mobiux.android.orca50scanner.BuildConfig;
import in.mobiux.android.orca50scanner.R;
import in.mobiux.android.orca50scanner.api.ApiClient;
import in.mobiux.android.orca50scanner.api.Presenter;
import in.mobiux.android.orca50scanner.api.model.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends BaseActivity {

    private EditText edtEmail, edtPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setTitle("Verification");
        setHomeButtonEnable(false);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);

//        TODO
//        if (BuildConfig.DEBUG) {
            edtEmail.setText("demouser@sensingobjects.tech");
            edtPassword.setText("footprints@123");
//        }

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                User user = new User();

                user.setEmail("" + edtEmail.getText());
                user.setPassword("" + edtPassword.getText());

                login(user);
            }
        });
    }

    private void login(User user) {

        ApiClient.getApiService().login(user).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    User us = response.body();
                    session.setUser(us);
                    session.saveToken(us.getToken());
                    startActivity(new Intent(app, HomeActivity.class));
                    finish();


                } else {
                    Toast.makeText(app, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(app, "Something went wrong", Toast.LENGTH_SHORT).show();
                logger.i(TAG, "login failed " + t.getLocalizedMessage());
            }
        });
    }
}