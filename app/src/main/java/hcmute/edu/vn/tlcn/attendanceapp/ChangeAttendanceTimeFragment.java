package hcmute.edu.vn.tlcn.attendanceapp;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import hcmute.edu.vn.tlcn.attendanceapp.model.Config;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChangeAttendanceTimeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChangeAttendanceTimeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ChangeAttendanceTimeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChangeAttendanceTimeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChangeAttendanceTimeFragment newInstance(String param1, String param2) {
        ChangeAttendanceTimeFragment fragment = new ChangeAttendanceTimeFragment();
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
    ImageView btnBackConfig;
    EditText edittextStartCheckIn, edittextEndCheckIn,edittextPeriod;
    Button btnConfirmConfig;
    SimpleDateFormat timeFormat;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_change_attendance_time, container, false);

        mapping();

        putDataToView();

        timeFormat = new SimpleDateFormat("HH:mm");

        btnBackConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AdminSettingsFragment adminSettingsFragment = new AdminSettingsFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flAdminFragment, adminSettingsFragment).commit();
            }
        });
        Calendar calendar = Calendar.getInstance();
        edittextStartCheckIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minutes = calendar.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),
                        AlertDialog.THEME_HOLO_LIGHT,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                calendar.set(0,0,0,hourOfDay,minute,0);
                                String startTime = timeFormat.format(calendar.getTime());
                                edittextStartCheckIn.setText(startTime);
                            }
                        },hour,minutes,true);
                timePickerDialog.show();
            }
        });

        edittextEndCheckIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minutes = calendar.get(Calendar.MINUTE);
                TimePickerDialog endDialog = new TimePickerDialog(getActivity(),
                        AlertDialog.THEME_HOLO_LIGHT,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                calendar.set(0,0,0,hourOfDay,minute,0);
                                String endTime = timeFormat.format(calendar.getTime());

                                if(!checkTimeEnd(endTime)){
                                    Toast.makeText(getContext(), "The time end check in must be greater than start check in!!", Toast.LENGTH_SHORT).show();
                                    btnConfirmConfig.setClickable(false);
                                }else{
                                    btnConfirmConfig.setClickable(true);
                                    edittextEndCheckIn.setText(endTime);
                                }
                            }
                        },hour,minutes,true);
                endDialog.show();
            }
        });

        btnConfirmConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String startCheckIn = edittextStartCheckIn.getText().toString();
                String endCheckIn= edittextEndCheckIn.getText().toString();
                String period = edittextPeriod.getText().toString();

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference configRef = database.getReference("config");
                configRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Config config = snapshot.getValue(Config.class);
                        if(config != null) {
                            config.setStartCheckIn(startCheckIn);
                            config.setEndCheckIn(endCheckIn);
                            config.setPeriod(period);

                            configRef.setValue(config);
                            Toast.makeText(getContext(), "Change successful", Toast.LENGTH_SHORT).show();

                            AdminSettingsFragment adminSettingsFragment = new AdminSettingsFragment();
                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flAdminFragment, adminSettingsFragment).commit();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        edittextPeriod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog dialog;
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                LayoutInflater inflater = LayoutInflater.from(getContext());
                View layout_dialog = inflater.inflate(R.layout.dialog_minute, null);
                builder.setView(layout_dialog);

                NumberPicker minutePicker = layout_dialog.findViewById(R.id.minutePicker);
                TextView btnCancelPeriod = layout_dialog.findViewById(R.id.btnCancelPeriod);
                TextView btnOkPeriod = layout_dialog.findViewById(R.id.btnOkPeriod);

                minutePicker.setMinValue(0);
                minutePicker.setMaxValue(59);
                minutePicker.setWrapSelectorWheel(true);

                dialog = builder.create();
                dialog.getWindow().setGravity(Gravity.CENTER);
                dialog.show();

                minutePicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                        edittextPeriod.setText(String.valueOf(newVal));
                    }
                });

                btnOkPeriod.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int minute = Integer.parseInt(edittextPeriod.getText().toString());
                        if(!checkPeriod(minute)){
                            Toast.makeText(getContext(), "The period is invalid!!", Toast.LENGTH_SHORT).show();
                            btnConfirmConfig.setClickable(false);
                        }else{
                            btnConfirmConfig.setClickable(true);
                        }
                        dialog.dismiss();
                    }
                });

                btnCancelPeriod.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        putDataToView();
                        dialog.dismiss();
                    }
                });
            }
        });

        return view;
    }

    private boolean checkTimeEnd(String timeEnd){
        String timeStart = edittextStartCheckIn.getText().toString();
        String[] cutTimeStart = timeStart.split(":");
        String[] cutTimeEnd = timeEnd.split(":");

        if(Integer.parseInt(cutTimeStart[0]) > Integer.parseInt(cutTimeEnd[0])){
            return false;
        }
        else if(Integer.parseInt(cutTimeStart[0]) == Integer.parseInt(cutTimeEnd[0]) &&
                Integer.parseInt(cutTimeStart[1]) > Integer.parseInt(cutTimeEnd[1])){
            return false;
        }
        return true;
    }

    private boolean checkPeriod(Integer period){
        String timeStart = edittextStartCheckIn.getText().toString();
        String timeEnd = edittextEndCheckIn.getText().toString();
        String[] cutTimeStart = timeStart.split(":");
        String[] cutTimeEnd = timeEnd.split(":");

        int newHour = Integer.parseInt(cutTimeStart[0]);
        int newMinutes = Integer.parseInt(cutTimeStart[1]) + period;
        if (newMinutes >= 60) {
            newHour++;
            newMinutes = newMinutes % 60;
        }

        if(newHour == Integer.parseInt(cutTimeEnd[0])
                && newMinutes > Integer.parseInt(cutTimeEnd[1])){
            return false;
        }
        if(newHour > Integer.parseInt(cutTimeEnd[0])){
            return false;
        }

        return true;
    }

    private void putDataToView() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference configRef = database.getReference("config");
        configRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Config config = snapshot.getValue(Config.class);
                if(config != null) {
                    edittextStartCheckIn.setText(config.getStartCheckIn());
                    edittextEndCheckIn.setText(config.getEndCheckIn());
                    edittextPeriod.setText(config.getPeriod());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void mapping() {
        btnBackConfig = (ImageView) view.findViewById(R.id.btnBackConfig);
        edittextStartCheckIn = (EditText) view.findViewById(R.id.edittextStartCheckIn);
        edittextEndCheckIn = (EditText) view.findViewById(R.id.edittextEndCheckIn);
        edittextPeriod = (EditText) view.findViewById(R.id.edittextPeriod);
        btnConfirmConfig = (Button) view.findViewById(R.id.btnConfirmConfig);
    }
}