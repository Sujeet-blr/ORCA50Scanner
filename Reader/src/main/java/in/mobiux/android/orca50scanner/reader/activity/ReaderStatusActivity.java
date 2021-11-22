package in.mobiux.android.orca50scanner.reader.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import in.mobiux.android.orca50scanner.reader.R;
import in.mobiux.android.orca50scanner.reader.core.RFIDReader;
import in.mobiux.android.orca50scanner.reader.core.RFIDReaderListener;
import in.mobiux.android.orca50scanner.reader.core.Reader;
import in.mobiux.android.orca50scanner.reader.model.Inventory;
import in.mobiux.android.orca50scanner.reader.model.OperationTag;

public class ReaderStatusActivity extends BaseActivity {

    private RFIDReader rfidReader;
    private EditText edtStatus;
    private Button btnGet, btnSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader_status);

        edtStatus = findViewById(R.id.edtStatus);
        btnGet = findViewById(R.id.btnGet);
        btnSet = findViewById(R.id.btnSet);

        rfidReader = new RFIDReader(getApplicationContext());
        rfidReader.connect(Reader.ReaderType.RFID);

        btnGet.setOnClickListener(view -> {
            rfidReader.getStatus();
        });

        btnSet.setOnClickListener(view -> {
            int val = Integer.parseInt(edtStatus.getText().toString());
            logger.i(TAG, "status value is " + val);
            rfidReader.setStatus(val);
        });
    }

    private void updateView() {
//        edtStatus.setText(String.valueOf(mReaderSetting.btAntDetector & 0xFF));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        rfidReader.releaseResources();
    }
}