package in.mobiux.android.orca50scanner.stocklitev2.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import in.mobiux.android.orca50scanner.reader.model.Inventory;
import in.mobiux.android.orca50scanner.stocklitev2.R;
import in.mobiux.android.orca50scanner.stocklitev2.db.AppDatabaseRepo;
import in.mobiux.android.orca50scanner.stocklitev2.db.model.RFIDTag;
import in.mobiux.android.orca50scanner.stocklitev2.utils.RFIDUtils;

import static in.mobiux.android.orca50scanner.stocklitev2.utils.RFIDUtils.MatchingRule.MR1;
import static in.mobiux.android.orca50scanner.stocklitev2.utils.RFIDUtils.MatchingRule.MR2;
import static in.mobiux.android.orca50scanner.stocklitev2.utils.RFIDUtils.MatchingRule.MR3;

public class AppSettingsActivity extends BaseActivity {

    private static final String TAG = "AppSettingsActivity";

    private static final int PICK_FILE = 21;
    private CheckBox checkBox31;
    private RadioGroup rg1, rg2, rg3;
    private ChipGroup chipGroup;
    private EditText editText;
    private LinearLayout lltFileUpload;
    private TextView tvAttach;

    private RFIDUtils rfidUtils;
//    private List<String> patterList = new ArrayList<>();
    private Set<String> acronyms = new HashSet<>();

    private AppDatabaseRepo dbRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_settings);

        checkBox31 = findViewById(R.id.checkBox31);
        rg1 = findViewById(R.id.rg1);
        rg2 = findViewById(R.id.rg2);
        rg3 = findViewById(R.id.rg3);
        chipGroup = findViewById(R.id.chip_group);
        editText = findViewById(R.id.editText);
        lltFileUpload = findViewById(R.id.lltFileUpload);
        tvAttach = findViewById(R.id.tvAttach);

        checkBox31.setVisibility(View.GONE);

        rfidUtils = RFIDUtils.getInstance(getApplicationContext());
        acronyms = rfidUtils.getAcronyms();

        if (!acronyms.isEmpty()) {
            for (String str : acronyms) {
                addChipToMR3(str);
            }
        }

        dbRepo = new AppDatabaseRepo(app);

        initState();

        dbRepo.getRFIDTagsList().observe(this, new Observer<List<RFIDTag>>() {
            @Override
            public void onChanged(List<RFIDTag> rfidTags) {
                Log.i(TAG, "onChanged: tag count " + rfidTags.size());
            }
        });

        tvAttach.setOnClickListener(view -> {
            showFileChooser();
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String str = editable.toString();

                str = str.trim();
                str.replace(" ", "");
                if (str.length() == 4) {

//                    Chip chip = new Chip(AppSettingsActivity.this);
//                    chip.setText(str);
//                    chip.setCloseIconVisible(true);
//
//                    chip.setOnCloseIconClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            chipGroup.removeView(chip);
//                            patterList.remove(chip.getText().toString());
//                            acronyms.remove(chip.getText().toString());
//                            session.setStringSet("MR3", acronyms);
//                        }
//                    });
//
//                    patterList.add(str);
//                    chipGroup.addView(chip);
//                    editText.setText("");
//
//                    acronyms.add(str);
//                    session.setStringSet("MR3", acronyms);

                    addChipToMR3(str);
                }
            }
        });

        rg1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int pos) {
                int checkedId = radioGroup.getCheckedRadioButtonId();

                lltFileUpload.setVisibility(View.GONE);
                if (checkedId == R.id.rbM1) {
                    rfidUtils.setMatchingRule(MR1);
                } else if (checkedId == R.id.rbM2) {
                    lltFileUpload.setVisibility(View.VISIBLE);
                    rfidUtils.setMatchingRule(RFIDUtils.MatchingRule.MR2);

                } else if (checkedId == R.id.rbM3) {
                    rfidUtils.setMatchingRule(RFIDUtils.MatchingRule.MR3);
                }
            }
        });

        rg2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int pos) {
                int checkedId = radioGroup.getCheckedRadioButtonId();
                if (checkedId == R.id.rbNM1) {
                    rfidUtils.setNonMatchingRule(RFIDUtils.NonMatchingRule.NMR1);
                } else if (checkedId == R.id.rbNM2) {
                    rfidUtils.setNonMatchingRule(RFIDUtils.NonMatchingRule.NMR2);
                }
            }
        });

        rg3.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int pos) {
                checkBox31.setVisibility(View.GONE);
                int checkedId = radioGroup.getCheckedRadioButtonId();
                if (checkedId == R.id.rbD1) {
                    rfidUtils.setDisplayRule(RFIDUtils.DisplayRule.D1);
                } else if (checkedId == R.id.rbD2) {
                    checkBox31.setVisibility(View.VISIBLE);

                    checkBox31.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            if (b) {
                                rfidUtils.setDisplayRule(RFIDUtils.DisplayRule.D21);
                            } else {
                                rfidUtils.setDisplayRule(RFIDUtils.DisplayRule.D2);
                            }
                        }
                    });

                    rfidUtils.setDisplayRule(RFIDUtils.DisplayRule.D2);

                } else if (checkedId == R.id.rbD3) {
                    rfidUtils.setDisplayRule(RFIDUtils.DisplayRule.D3);
                }

                showToast("" + pos);
            }
        });

        checkBox31.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    rfidUtils.setDisplayRule(RFIDUtils.DisplayRule.D21);
                } else {
                    rfidUtils.setDisplayRule(RFIDUtils.DisplayRule.D2);
                }
            }
        });
    }

    private void initState() {
        RFIDUtils.MatchingRule matchingRule = rfidUtils.getMatchingRule();
        lltFileUpload.setVisibility(View.GONE);
        if (matchingRule == MR1) {
            rg1.check(R.id.rbM1);
        } else if (matchingRule == RFIDUtils.MatchingRule.MR2) {
            rg1.check(R.id.rbM2);
            lltFileUpload.setVisibility(View.VISIBLE);
        } else if (matchingRule == RFIDUtils.MatchingRule.MR3) {
            rg1.check(R.id.rbM3);
        } else {
            rg1.check(R.id.rbM1);
        }

        RFIDUtils.NonMatchingRule nMRule = rfidUtils.getNonMatchingRule();
        if (nMRule == RFIDUtils.NonMatchingRule.NMR2) {
            rg2.check(R.id.rbNM2);
        } else {
            rg2.check(R.id.rbNM1);
        }

        RFIDUtils.DisplayRule displayRule = rfidUtils.getDisplayRule();
        checkBox31.setChecked(false);
        if (displayRule == RFIDUtils.DisplayRule.D1) {
            rg3.check(R.id.rbD1);
        } else if (displayRule == RFIDUtils.DisplayRule.D2) {
            rg3.check(R.id.rbD2);
            checkBox31.setChecked(false);
            checkBox31.setVisibility(View.VISIBLE);
        } else if (displayRule == RFIDUtils.DisplayRule.D21) {
            rg3.check(R.id.rbD2);
            checkBox31.setChecked(true);
            checkBox31.setVisibility(View.VISIBLE);

        } else if (displayRule == RFIDUtils.DisplayRule.D3) {
            rg3.check(R.id.rbD3);
        }
    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), PICK_FILE);
        } catch (Exception e) {
            logger.e(TAG, " choose file error " + e.toString());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
//        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == PICK_FILE
                && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                // Perform operations on the document using its URI.

                try {
                    InputStream inputStream = getContentResolver().openInputStream(resultData.getData());
                    CSVReader dataRead = new CSVReader(new InputStreamReader(inputStream));
//                    CSVReader dataRead = new CSVReader(new FileReader(new File(resultData.getData().getPath())));

                    String[] line = null;

                    dbRepo.clearAll();
                    List<RFIDTag> assets = new ArrayList<>();
                    while ((line = dataRead.readNext()) != null) {
                        RFIDTag asset = new RFIDTag();
                        asset.setEpc(line[0]);
                        asset.setName(line[1]);
//                        Long barcodeNumber = Long.valueOf(line[0]);
//                        logger.i(TAG, "data is " + barcodeNumber);
                        assets.add(asset);

                    }

                    dbRepo.insertAll(assets);


                } catch (IOException | CsvValidationException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void addChipToMR3(String str) {
        Chip chip = new Chip(AppSettingsActivity.this);
        chip.setText(str);
        chip.setCloseIconVisible(true);

        chip.setOnCloseIconClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chipGroup.removeView(chip);

                rfidUtils.removeAcronym(chip.getText().toString());
            }
        });

        chipGroup.addView(chip);
        editText.setText("");

        rfidUtils.addAcronym(str);
    }
}