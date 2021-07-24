package in.mobiux.android.orca50scanner.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import in.mobiux.android.orca50scanner.BuildConfig;
import in.mobiux.android.orca50scanner.R;
import in.mobiux.android.orca50scanner.api.ApiClient;
import in.mobiux.android.orca50scanner.api.model.User;
import in.mobiux.android.orca50scanner.util.LocaleHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends BaseActivity {

    private EditText edtEmail, edtPassword;
    private Button btnLogin;
    private Spinner spnrLanguage;
    private List<String> languages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setTitle(getResources().getString(R.string.label_verification));
        setHomeButtonEnable(false);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        spnrLanguage = findViewById(R.id.spnrLanguage);

        languages.add("English");
        languages.add("German");
        languages.add("French");
        languages.add("Dutch");


        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(LoginActivity.this, android.R.layout.simple_spinner_item, languages);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnrLanguage.setAdapter(arrayAdapter);

        String selectedLanguage = session.getLanguage();

        if (selectedLanguage.equals(LanEnglish)) {
            spnrLanguage.setSelection(0);
        } else if (selectedLanguage.equals(LanGerman)) {
            spnrLanguage.setSelection(1);
        } else if (selectedLanguage.equals(LanFrench)) {
            spnrLanguage.setSelection(2);
        } else if (selectedLanguage.equals(LanDutch)) {
            spnrLanguage.setSelection(3);
        } else {
            spnrLanguage.setSelection(0);
        }


        if (BuildConfig.DEBUG) {
            edtEmail.setText("adminsguldemo@footprints.com");
            edtPassword.setText("vida@123");
//            edtEmail.setText("msunkara@rfiddirect.eu");
//            edtPassword.setText("sofp@123");
        }

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                User user = new User();

                user.setEmail("" + edtEmail.getText());
                user.setPassword("" + edtPassword.getText());

                login(user);
            }
        });

        spnrLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedLan = "en";

                if (spnrLanguage.getSelectedItemPosition() == 0) {
                    selectedLan = LanEnglish;
                } else if (spnrLanguage.getSelectedItemPosition() == 1) {
                    selectedLan = LanGerman;
                } else if (spnrLanguage.getSelectedItemPosition() == 2) {
                    selectedLan = LanFrench;
                } else if (spnrLanguage.getSelectedItemPosition() == 3) {
                    selectedLan = LanDutch;
                } else {
                    selectedLan = LanEnglish;
                }

                if (!session.getLanguage().equals(selectedLan)) {
                    switchLanguage(selectedLan);
//                    session.setLanguage(selectedLan);
                    recreate();
//                    app.switchLanguage(selectedLan);

//                    Context context = LocaleHelper.setLocale(LoginActivity.this, "en");
//                    Resources resources = context.getResources();
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public void onConfigurationChanged(@NonNull @NotNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    //    @Override
//    public void onConfigurationChanged(@NonNull @NotNull Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//    }

    private void login(User user) {

        ApiClient.getApiService().login(user).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    logger.i(TAG, "Login Success " + user.getEmail());
                    User us = response.body();
                    session.setUser(us);
                    session.saveToken(us.getToken());
                    app.clearAllActivity();
                    startActivity(new Intent(app, HomeActivity.class));
                    finish();

                } else {
                    showToast(getResources().getString(R.string.invalid_credentials));
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                showToast(getResources().getString(R.string.something_went_wrong));
                logger.i(TAG, "login failed " + t.getLocalizedMessage());
            }
        });
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }
}