package in.mobiux.android.orca50scanner.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.module.interaction.ModuleConnector;
import com.module.interaction.ReaderHelper;
import com.nativec.tools.ModuleManager;

import java.util.ArrayList;
import java.util.List;

import in.mobiux.android.orca50scanner.R;
import in.mobiux.android.orca50scanner.adapter.BarcodeAdapter;
import in.mobiux.android.orca50scanner.api.model.Barcode;

public class ODScannerActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private BarcodeAdapter adapter;
    private List<Barcode> barcodes = new ArrayList<>();

    ModuleConnector connector;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_odscanner);

        recyclerView = findViewById(R.id.recyclerView);

        connector.connectCom("dev/ttyS1",115200);
        ModuleManager.newInstance().setScanStatus(true);


        adapter = new BarcodeAdapter(app, barcodes);
        recyclerView.setAdapter(adapter);
    }
}