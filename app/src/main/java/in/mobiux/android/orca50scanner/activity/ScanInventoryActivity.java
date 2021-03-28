package in.mobiux.android.orca50scanner.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import in.mobiux.android.orca50scanner.R;

public class ScanInventoryActivity extends BaseActivity implements View.OnClickListener {

    private Button btnStart, btnSave, btnClear;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_inventory);

        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btnStart = findViewById(R.id.btnStart);
        btnSave = findViewById(R.id.btnSave);
        btnClear = findViewById(R.id.btnClear);
        recyclerView = findViewById(R.id.recyclerView);

        btnStart.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        btnClear.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnStart:
                logger.i(TAG, "Start");
                break;
            case R.id.btnClear:
                logger.i(TAG, "Clear");
                break;
            case R.id.btnSave:
                logger.i(TAG, "Save");
                Toast.makeText(app, "Saving data", Toast.LENGTH_SHORT).show();
                finish();
                break;
        }
    }
}