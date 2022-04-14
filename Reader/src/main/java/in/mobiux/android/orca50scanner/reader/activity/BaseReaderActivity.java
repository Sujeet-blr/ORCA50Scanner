package in.mobiux.android.orca50scanner.reader.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

import in.mobiux.android.orca50scanner.reader.core.RFIDReader;
import in.mobiux.android.orca50scanner.reader.model.Inventory;


//todo
// this is class will have both implementation rfid & barcode reader
public class BaseReaderActivity extends AppCompatActivity {

    private static final String TAG = "BaseReaderActivity";

    private RFIDReader rfidReader;
    protected Map<String, Inventory> tags = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
