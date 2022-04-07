package in.mobiux.android.orca50scanner.sgul.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import in.mobiux.android.orca50scanner.sgul.R;

public class BuzzerSettingActivity extends BaseActivity {

    private RadioGroup radioGroup;
    private Button btnSave;
    private byte beeperMode = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buzzer_setting);

        setTitle(getResources().getString(R.string.label_buzzer_behavior));

        radioGroup = findViewById(R.id.radioGroup);
        btnSave = findViewById(R.id.btnSave);

        try {
            beeperMode = Byte.parseByte(session.getValue("beeperMode"));
//            radioGroup.getChildAt((int)beeperMode).setSelected(true);
            radioGroup.check(radioGroup.getChildAt((int) beeperMode).getId());

        } catch (Exception e) {
            logger.e(TAG, "" + e.getLocalizedMessage());
        }


        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                int position = radioGroup.indexOfChild(radioGroup.findViewById(id));
                beeperMode = (byte) position;
                logger.i(TAG, "position " + position);
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (radioGroup.getCheckedRadioButtonId() > -1) {

//                    todo

//                    if (app.connector.isConnected()) {
//                        app.rfidReaderHelper.setBeeperMode(ReaderSetting.newInstance().btReadId, beeperMode);
//                        ReaderSetting.newInstance().btBeeperMode = beeperMode;
//                    }
                    session.setValue("beeperMode", String.valueOf(beeperMode));
                    showDialog(getResources().getString(R.string.buzzer_mode_set_to) + " \n" + ((RadioButton) radioGroup.getChildAt(beeperMode)).getText().toString());
                } else {
                    showToast(getResources().getString(R.string.select_option));
                }
            }
        });
    }

    private void showDialog(String message) {
        AlertDialog.Builder builder = new MaterialAlertDialogBuilder(BuzzerSettingActivity.this);
        builder.setMessage(message);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                finish();
            }
        });

        builder.show();

    }
}