package hcmute.edu.vn.tlcn.attendanceapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
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
import hcmute.edu.vn.tlcn.attendanceapp.model.Record;
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
    ImageView btnBackDayOff, btnSeeListReq;
    EditText edtDayToOff, edtReason;
    Button btnSendRequestDayOff;
    TextView btnCancelRequest;
    User_singeton user_singeton = User_singeton.getInstance();
    ;
    User user;
    Calendar calendar;
    FirebaseDatabase database;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_request_a_day_off, container, false);

        mapping();
        user = user_singeton.getUser();

        btnBackDayOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingsFragment settingsFragment = new SettingsFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, settingsFragment).commit();
            }
        });

        btnSeeListReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), ListSendedRequestActivity.class));
            }
        });

        btnCancelRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingsFragment settingsFragment = new SettingsFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, settingsFragment).commit();
            }
        });

        calendar = Calendar.getInstance();
        edtDayToOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int date = calendar.get(Calendar.DATE);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), datePickerListener, year, month, date);
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                datePickerDialog.show();
            }
        });

        btnSendRequestDayOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String reason = edtReason.getText().toString();
                String dateDayOff = edtDayToOff.getText().toString();

                if (dateDayOff.equals("")) {
                    Toast.makeText(getActivity(), "Please choose a day you want to work off !", Toast.LENGTH_SHORT).show();
                    return;
                }
                final String reqId = UUID.randomUUID().toString();

                DayOffRequest dayOffRequest = new DayOffRequest(user.getPhone(), reason, "waiting", dateDayOff);

                database = FirebaseDatabase.getInstance();
                DatabaseReference dayOffReportRef = database.getReference("dayoffreport");

                dayOffReportRef.child(reqId).setValue(dayOffRequest);
                Toast.makeText(getActivity(), "Send successfully", Toast.LENGTH_SHORT).show();

                SettingsFragment settingsFragment = new SettingsFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, settingsFragment).commit();


            }
        });

        return view;
    }

    private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            calendar.set(year, month, dayOfMonth);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String selectedDate = format.format(calendar.getTime());
            edtDayToOff.setText(selectedDate);

            if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                btnSendRequestDayOff.setEnabled(false);
                Toast.makeText(getActivity(), "The day you choose is Sunday!!", Toast.LENGTH_SHORT).show();
                return;
            }

            btnSendRequestDayOff.setEnabled(true);
            database = FirebaseDatabase.getInstance();
            DatabaseReference dayOffRef = database.getReference("dayoffreport");

            dayOffRef.orderByChild("userPhone").startAt(user.getPhone()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        DayOffRequest dayOff = dataSnapshot.getValue(DayOffRequest.class);
                        String day = dayOff.getDateOff();

                        if (day.equals(selectedDate)) {
                            btnSendRequestDayOff.setEnabled(false);
                            Toast.makeText(getActivity(), "You have already sent request for " + day, Toast.LENGTH_SHORT).show();
                            break;
                        } else {
                            database = FirebaseDatabase.getInstance();
                            DatabaseReference recordRef = database.getReference("record").child(user.getPhone());
                            recordRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    DataSnapshot dataSnapshot1 = snapshot.child(selectedDate).child("checkIn");
                                    Record checkInRecord = dataSnapshot1.getValue(Record.class);
                                    DataSnapshot dataSnapshot3 = snapshot.child(selectedDate).child("absent");
                                    Record absentRecord = dataSnapshot3.getValue(Record.class);
                                    if (checkInRecord != null) {
                                        btnSendRequestDayOff.setEnabled(false);
                                        Toast.makeText(getActivity(), "You have already check in!!", Toast.LENGTH_SHORT).show();
                                    } else if (absentRecord != null) {
                                        btnSendRequestDayOff.setEnabled(false);
                                        if (absentRecord.getStatus().equals("absent with permission")) {
                                            Toast.makeText(getActivity(), "You have already sent request for " + absentRecord.getDay(), Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getActivity(), "You have already absent!!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
    };

    private void mapping() {
        btnBackDayOff = (ImageView) view.findViewById(R.id.btnBackDayOff);
        edtDayToOff = (EditText) view.findViewById(R.id.edtDayToOff);
        edtReason = (EditText) view.findViewById(R.id.edtReason);
        btnSendRequestDayOff = (Button) view.findViewById(R.id.btnSendRequestDayOff);
        btnCancelRequest = (TextView) view.findViewById(R.id.btnCancelRequest);
        btnSeeListReq = (ImageView) view.findViewById(R.id.btnSeeListReq);
    }
}