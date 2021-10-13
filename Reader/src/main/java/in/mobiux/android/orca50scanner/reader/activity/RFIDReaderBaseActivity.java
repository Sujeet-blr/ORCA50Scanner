package in.mobiux.android.orca50scanner.reader.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;

import in.mobiux.android.orca50scanner.reader.core.RFIDReader;
import in.mobiux.android.orca50scanner.reader.core.RFIDReaderListener;
import in.mobiux.android.orca50scanner.reader.core.Reader;

public class RFIDReaderBaseActivity extends BaseActivity {

    private RFIDReader rfidReader;
    private RFIDReaderListener rfidReaderListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rfidReader = new RFIDReader(getApplicationContext());
        rfidReader.connect(Reader.ReaderType.RFID);
    }
}
