package in.mobiux.android.orca50scanner.common.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import in.mobiux.android.orca50scanner.common.R;
import in.mobiux.android.orca50scanner.common.api.ApiClient;
import in.mobiux.android.orca50scanner.common.api.model.ContactSupport;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContactSupportActivity extends AppActivity {

    private EditText edtName, edtSubject, edtMessage;
    private Button btnSubmit;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_support);

        edtName = findViewById(R.id.edtName);
        edtSubject = findViewById(R.id.edtSubject);
        edtMessage = findViewById(R.id.edtMessage);
        btnSubmit = findViewById(R.id.btnSubmit);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        edtSubject.setText(getResources().getString(R.string.app_name));

        btnSubmit.setOnClickListener(view -> {
            ContactSupport support = new ContactSupport();

            if (edtName.length() < 1) {
                edtName.setError("Enter name");
                return;
            } else if (edtSubject.length() < 1) {
                edtSubject.setError("Enter subject");
                return;
            } else if (edtMessage.length() < 1) {
                edtMessage.setError("Enter your message here");
                return;
            }

            support.setSubject(edtSubject.getText().toString());
            support.setName(edtName.getText().toString());
            support.setMessage(edtMessage.getText().toString() + "\n\nAppName : " + getResources().getString(R.string.app_name));
            messageToSupport(support);
        });
    }

    private void messageToSupport(ContactSupport support) {

        progressBar.setVisibility(View.VISIBLE);
        btnSubmit.setEnabled(false);

        ApiClient.getApiService().sendRequest(support).enqueue(new Callback<ContactSupport>() {
            @Override
            public void onResponse(Call<ContactSupport> call, Response<ContactSupport> response) {
                logger.i(TAG, "messageToSupport " + response.code());

                if (response.isSuccessful()) {
                    showToast("Message sent successfully");
                    finish();
                } else {

                }

                progressBar.setVisibility(View.GONE);
                btnSubmit.setEnabled(true);
            }

            @Override
            public void onFailure(Call<ContactSupport> call, Throwable t) {
                logger.e(TAG, "messageToSupport " + t.getLocalizedMessage());
                progressBar.setVisibility(View.GONE);
                btnSubmit.setEnabled(true);
            }
        });
    }
}