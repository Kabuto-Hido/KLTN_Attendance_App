package hcmute.edu.vn.tlcn.attendanceapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import hcmute.edu.vn.tlcn.attendanceapp.adapter.ResignationAdapter;
import hcmute.edu.vn.tlcn.attendanceapp.model.DayOffRequest;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ListResignationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListResignationFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ListResignationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ListResignationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ListResignationFragment newInstance(String param1, String param2) {
        ListResignationFragment fragment = new ListResignationFragment();
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

    View view;
    TextView textviewNotifi;
    ImageView btnBackResignation;
    ListView listviewResignation;
    ResignationAdapter adapter;
    ArrayList<DayOffRequest> requestList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_list_resignation, container, false);

        mapping();

        adapter = new ResignationAdapter(getActivity(), R.layout.resignation_row, requestList);
        listviewResignation.setAdapter(adapter);

        btnBackResignation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AdminSettingsFragment adminSettingsFragment = new AdminSettingsFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flAdminFragment, adminSettingsFragment).commit();
            }
        });

        putDataToView();

        return view;
    }

    private void putDataToView() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference dayOffReportRef = database.getReference("dayoffreport");
        dayOffReportRef.orderByChild("status").equalTo("waiting").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                requestList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    DayOffRequest dayOffRequest = dataSnapshot.getValue(DayOffRequest.class);
                    requestList.add(dayOffRequest);
                }

                requestList.sort(new Comparator<DayOffRequest>() {
                    @Override
                    public int compare(DayOffRequest o1, DayOffRequest o2) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        Date strDate1 = null;
                        Date strDate2 = null;
                        try {
                            strDate1 = sdf.parse(o1.getDateOff());
                            strDate2 = sdf.parse(o2.getDateOff());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        if (strDate1 != null && strDate2 != null) {
                            return strDate1.compareTo(strDate2);
                        }
                        return 0;
                    }
                });
                Collections.reverse(requestList);
                adapter.notifyDataSetChanged();

                if (requestList.size() == 0) {
                    textviewNotifi.setVisibility(View.VISIBLE);
                    listviewResignation.setVisibility(View.INVISIBLE);
                } else {
                    textviewNotifi.setVisibility(View.INVISIBLE);
                    listviewResignation.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.v("", error.getMessage());
            }
        });
    }

    private void mapping() {
        btnBackResignation = (ImageView) view.findViewById(R.id.btnBackResignation);
        listviewResignation = (ListView) view.findViewById(R.id.listviewResignation);
        textviewNotifi = (TextView) view.findViewById(R.id.textviewNotifi);
    }
}