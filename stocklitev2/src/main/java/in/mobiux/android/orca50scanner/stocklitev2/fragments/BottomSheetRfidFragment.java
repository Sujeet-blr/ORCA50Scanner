package in.mobiux.android.orca50scanner.stocklitev2.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import in.mobiux.android.orca50scanner.stocklitev2.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BottomSheetRfidFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BottomSheetRfidFragment extends BottomSheetDialogFragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public BottomSheetRfidFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BottomSheetRfidFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BottomSheetRfidFragment newInstance(String param1, String param2) {
        BottomSheetRfidFragment fragment = new BottomSheetRfidFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_bottom_sheet_rfid, container, false);

        return view;
    }
}