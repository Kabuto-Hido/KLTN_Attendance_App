package hcmute.edu.vn.tlcn.attendanceapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

import hcmute.edu.vn.tlcn.attendanceapp.model.DayOffRequest;
import hcmute.edu.vn.tlcn.attendanceapp.model.User;
import hcmute.edu.vn.tlcn.attendanceapp.pattern.User_singeton;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RequestADayOffFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RequestADayOffFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public RequestADayOffFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RequestADayOffFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RequestADayOffFragment newInstance(String param1, String param2) {
        RequestADayOffFragment fragment = new RequestADayOffFragment();
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
    ImageView btnBackDayOff;
    EditText edtDayToOff, edtReason;
    Button btnSendRequestDayOff;
    TextView btnCancelRequest;
    User_singeton user_singeton = User_singeton.getInstance();;
    User user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_request_a_day_off, container, false);

        mapping();
        user = user_singeton.getUser();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference dayOffReportRef = database.getReference("dayoffreport");
        dayOffReportRef.orderByChild("userPhone").startAt(user.getPhone()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                System.out.println(snapshot);
                if(snapshot.exists()){
                    for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                        DayOffRequest dayOffRequest = dataSnapshot.getValue(DayOffRequest.class);
                        System.out.println(dayOffRequest.getUserPhone());
                        System.out.println(dataSnapshot);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        btnBackDayOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingsFragment settingsFragment = new SettingsFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, settingsFragment).commit();
            }
        });

        btnCancelRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingsFragment settingsFragment = new SettingsFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, settingsFragment).commit();
            }
        });

        edtDayToOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int date = calendar.get(Calendar.DATE);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(year,month,dayOfMonth);
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                        edtDayToOff.setText(format.format(calendar.getTime()));
                    }
                },year,month,date);
                datePickerDialog.show();
            }
        });

        btnSendRequestDayOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String reason = edtReason.getText().toString();
                String dateDayOff = edtDayToOff.getText().toString();

                if(dateDayOff.equals("")){
                    Toast.makeText(getActivity(), "Please choose a day you want to work off !", Toast.LENGTH_SHORT).show();
                    return;
                }
                final String reqId = UUID.randomUUID().toString();

                DayOffRequest dayOffRequest = new DayOffRequest(user.getPhone(),reason,"waiting",dateDayOff);

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference dayOffReportRef = database.getReference("dayoffreport");

                dayOffReportRef.child(reqId).setValue(dayOffRequest);
                Toast.makeText(getActivity(), "Send successfully", Toast.LENGTH_SHORT).show();

                SettingsFragment settingsFragment = new SettingsFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, settingsFragment).commit();


            }
        });

        return view;
    }

    private void mapping() {
        btnBackDayOff = (ImageView) view.findViewById(R.id.btnBackDayOff);
        edtDayToOff = (EditText) view.findViewById(R.id.edtDayToOff);
        edtReason = (EditText) view.findViewById(R.id.edtReason);
        btnSendRequestDayOff = (Button) view.findViewById(R.id.btnSendRequestDayOff);
        btnCancelRequest = (TextView) view.findViewById(R.id.btnCancelRequest);
    }
}