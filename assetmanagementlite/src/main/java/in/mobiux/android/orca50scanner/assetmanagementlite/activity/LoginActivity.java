package in.mobiux.android.orca50scanner.assetmanagementlite.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import in.mobiux.android.orca50scanner.assetmanagementlite.BuildConfig;
import in.mobiux.android.orca50scanner.assetmanagementlite.R;
import in.mobiux.android.orca50scanner.assetmanagementlite.api.ApiClient;
import in.mobiux.android.orca50scanner.assetmanagementlite.api.model.User;
import in.mobiux.android.orca50scanner.assetmanagementlite.util.AppConfig;
import in.mobiux.android.orca50scanner.assetmanagementlite.util.LanguageUtils;
import in.mobiux.android.orca50scanner.assetmanagementlite.util.MyApplication;
import in.mobiux.android.orca50scanner.assetmanagementlite.util.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends BaseActivity {

    private EditText edtEmail, edtPassword;
    private Button btnLogin;
    private Spinner spnrLanguage;
    private CheckBox checkbox;
    private List<LanguageUtils.Language> languages = new ArrayList<>();

    private MyApplication app;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        app = (MyApplication) getApplicationContext();
        session = SessionManager.getInstance(getApplicationContext());

        setTitle(getResources().getString(R.string.app_name_label));
        setHomeButtonEnable(false);

        checkbox = findViewById(R.id.checkbox);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        spnrLanguage = findViewById(R.id.spnrLanguage);

        if (AppConfig.MULTI_LANGUAGE) {
            spnrLanguage.setVisibility(View.VISIBLE);
        } else {
            spnrLanguage.setVisibility(View.GONE);
        }

        languages.addAll(Arrays.asList(LanguageUtils.Language.values()));


        ArrayAdapter<LanguageUtils.Language> arrayAdapter = new ArrayAdapter<LanguageUtils.Language>(LoginActivity.this, android.R.layout.simple_spinner_item, languages);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnrLanguage.setAdapter(arrayAdapter);

//        LanguageUtils.Language selectedLanguage = session.getLanguage();
//        int position = languages.indexOf(selectedLanguage);
//        spnrLanguage.setSelection(position);


        if (BuildConfig.DEBUG) {
//            edtEmail.setText("anshuman@mobiux.in");
//            edtPassword.setText("hito@123");
            edtEmail.setText("msunkara@rfiddirect.eu");
            edtPassword.setText("rfid@123");
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
                LanguageUtils.Language language = (LanguageUtils.Language) spnrLanguage.getSelectedItem();

                logger.i(TAG, "selected language is " + language);

//                if (!session.getLanguage().equals(language)) {
//                    languageUtils.switchLanguage(LoginActivity.this, language);
//                    recreate();
//                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                session.setRememberMe(isChecked);
            }
        });
    }

    @Override
    public void onConfigurationChanged(@NonNull @NotNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

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

                    app.departments();

                    startActivity(new Intent(app, MainActivity.class));
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