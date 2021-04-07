package in.mobiux.android.orca50scanner.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import in.mobiux.android.orca50scanner.R;
import in.mobiux.android.orca50scanner.api.model.DepartmentResponse;
import in.mobiux.android.orca50scanner.api.model.Laboratory;
import in.mobiux.android.orca50scanner.util.AppUtils;
import in.mobiux.android.orca50scanner.viewmodel.LaboratoryViewModel;

public class AssetInventoryActivity extends BaseActivity {

    private CardView cardInventory, cardSave;
    private LaboratoryViewModel laboratoryViewModel;

    private Spinner spinnerLevel, spinnerLab;
    private ArrayAdapter<DepartmentResponse> levelAdapter;
    private ArrayAdapter<DepartmentResponse.Child> labAdapter;

    private List<DepartmentResponse> responses = new ArrayList<>();
    private DepartmentResponse selectedLevel;
    private DepartmentResponse.Child selectedLab;
    private TextView tvLab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset_inventory);

        setTitle("ASSET INVENTORY");

        cardInventory = findViewById(R.id.cardInventory);
        spinnerLevel = findViewById(R.id.spinnerLevel);
        spinnerLab = findViewById(R.id.spinnerLab);
        cardSave = findViewById(R.id.cardSave);
        tvLab = findViewById(R.id.tvLab);
        tvLab.setText("");

        laboratoryViewModel = new ViewModelProvider(this).get(LaboratoryViewModel.class);

        laboratoryViewModel.getAllInventory().observe(this, new Observer<List<Laboratory>>() {
            @Override
            public void onChanged(List<Laboratory> list) {
                responses.clear();
                HashMap<Integer, DepartmentResponse> levels = new HashMap<Integer, DepartmentResponse>();
                HashSet<String> hashSet = new HashSet<>();


                for (Laboratory laboratory : list) {
//                    hashSet.add("" + laboratory.getLevelId());
                    DepartmentResponse d = new DepartmentResponse();
                    d.setId(laboratory.getLevelId());
                    d.setName(laboratory.getLevelName());
                    d.setChild(new ArrayList<>());
                    if (levels.get(laboratory.getLevelId()) == null) {
                        levels.put(laboratory.getLevelId(), d);
                        responses.add(d);
                    }

                    DepartmentResponse.Child child = new DepartmentResponse.Child();
                    child.setId(laboratory.getLabId());
                    child.setName(laboratory.getLabName());
                    levels.get(laboratory.getLevelId()).getChild().add(child);
                }

                levelAdapter = new ArrayAdapter<DepartmentResponse>(AssetInventoryActivity.this, android.R.layout.simple_spinner_item, responses);
                levelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerLevel.setAdapter(levelAdapter);

                String sessionLevel = session.getValue("level");
                if (!sessionLevel.isEmpty()) {
                    for (DepartmentResponse l : responses) {
                        if (String.valueOf(l.getId()).equals(sessionLevel)) {
                            spinnerLevel.setSelection(responses.indexOf(l));
                        }
                    }
                }
            }
        });

        cardInventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ScanInventoryActivity.class);
                intent.putExtra("laboratory", selectedLab);
                startActivity(intent);
            }
        });

        cardSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        spinnerLevel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if (responses.size() > 0) {
                    selectedLevel = responses.get(position);

                    session.setValue("level", "" + selectedLevel.getId());

                    List<DepartmentResponse.Child> labList = responses.get(position).getChild();

                    labAdapter = new ArrayAdapter<DepartmentResponse.Child>(AssetInventoryActivity.this, android.R.layout.simple_spinner_item, labList);
                    labAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerLab.setAdapter(labAdapter);

                    String sessionLabId = session.getValue("lab");
                    if (!sessionLabId.isEmpty()) {
                        for (DepartmentResponse.Child child : labList) {
                            if (String.valueOf(child.getId()).equals(sessionLabId)) {
                                spinnerLab.setSelection(labList.indexOf(child));
                            }
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spinnerLab.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if (selectedLevel != null && selectedLevel.getChild().size() > 0) {
                    selectedLab = selectedLevel.getChild().get(position);

                    session.setValue("lab", "" + selectedLab.getId());
                    tvLab.setText("" + selectedLab.getName());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
}