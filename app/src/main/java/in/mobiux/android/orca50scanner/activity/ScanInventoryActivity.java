package in.mobiux.android.orca50scanner.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.nativec.tools.ModuleManager;
import com.rfid.rxobserver.ReaderSetting;
import com.rfid.rxobserver.bean.RXInventoryTag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import in.mobiux.android.orca50scanner.R;
import in.mobiux.android.orca50scanner.adapter.InventoryAdapter;
import in.mobiux.android.orca50scanner.api.model.DepartmentResponse;
import in.mobiux.android.orca50scanner.api.model.Inventory;
import in.mobiux.android.orca50scanner.util.RFIDReaderListener;

public class ScanInventoryActivity extends BaseActivity implements View.OnClickListener, RFIDReaderListener {

    private Button btnStart, btnSave, btnClear;
    private TextView tvCount;
    private RecyclerView recyclerView;
    private DepartmentResponse.Child laboratory;
    private List<Inventory> inventories = new ArrayList<>();
    //    private Set<String> uniqueAsset = new HashSet<>();
    private Map<String, Inventory> map = new HashMap<>();
    private InventoryAdapter adapter;
    boolean startButtonStatus = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_inventory);

        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tvCount = findViewById(R.id.tvCount);
        btnStart = findViewById(R.id.btnStart);
        btnSave = findViewById(R.id.btnSave);
        btnClear = findViewById(R.id.btnClear);
        recyclerView = findViewById(R.id.recyclerView);
        tvCount.setText("");

        btnStart.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        btnClear.setOnClickListener(this);
        btnStart.setTag(startButtonStatus);

        laboratory = (DepartmentResponse.Child) getIntent().getSerializableExtra("laboratory");
        if (laboratory != null) {
            getSupportActionBar().setTitle("You are in " + laboratory.getName());
        }

        if (app.connector.isConnected()) {
            logger.i(TAG, "Connected");
        } else {
            app.connectRFID();
        }

        adapter = new InventoryAdapter(ScanInventoryActivity.this, inventories);
        recyclerView.setAdapter(adapter);
        tvCount.setText(adapter.getItemCount() + " PCS");
    }

    @Override
    protected void onStart() {
        super.onStart();
        app.setOnRFIDListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnStart:
                logger.i(TAG, "Start");
                btnStart.setTag(!(boolean) btnStart.getTag());

                if ((boolean) btnStart.getTag()) {
                    if (app.connector.isConnected()) {
                        ModuleManager.newInstance().setScanStatus(true);
                        btnStart.setText("Scanning");
                        app.rfidReaderHelper.realTimeInventory(ReaderSetting.newInstance().btReadId, (byte) 0x01);
                    } else {
                        app.reconnectRFID();
                    }
                } else {
                    app.scanningStatus = false;
                    btnStart.setText("Inventory Start");
                }


                break;
            case R.id.btnClear:
                logger.i(TAG, "Clear");
                inventories.clear();
                map.clear();
                adapter.notifyDataSetChanged();
                break;
            case R.id.btnSave:
                logger.i(TAG, "Save");
                Toast.makeText(app, "Saving data", Toast.LENGTH_SHORT).show();
                finish();
                break;
        }
    }

    @Override
    public void onInventoryTag(Inventory inventory) {
        map.put(inventory.getEpc(), inventory);
        Inventory matching = null;
        for (Inventory inv : inventories) {
            if (inv.getEpc().equals(inventory.getEpc())) {
                matching = inv;
            }
        }

        if (matching == null) {
            inventories.add(inventory);
        }

        adapter.notifyDataSetChanged();
        tvCount.setText(adapter.getItemCount() + " PCS");
    }

    @Override
    public void onScanningStatus(boolean status) {
        if (status) {
            btnStart.setText("Scanning");
            btnStart.setTag(true);
        } else {
            btnStart.setText("Inventory Start");
            btnStart.setTag(false);
        }
    }

    @Override
    public void onInventoryTagEnd(RXInventoryTag.RXInventoryTagEnd tagEnd) {

    }

    @Override
    public void onConnection(boolean status) {
        if (!status)
            app.reconnectRFID();
    }
}