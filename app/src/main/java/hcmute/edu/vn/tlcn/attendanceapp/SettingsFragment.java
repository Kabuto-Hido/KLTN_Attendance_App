package hcmute.edu.vn.tlcn.attendanceapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import hcmute.edu.vn.tlcn.attendanceapp.model.Record;
import hcmute.edu.vn.tlcn.attendanceapp.model.User;
import hcmute.edu.vn.tlcn.attendanceapp.pattern.User_singeton;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BlankFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
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
    TextView txtProfile, txtChangePassword, txtLogOut, txtDay_off;
    User_singeton user_singeton;
    User user;
    SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_settings, container, false);

        mapping();

        user_singeton = User_singeton.getInstance();
        user = user_singeton.getUser();

        if(user == null)
        {
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        }

        txtProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileInformationFragment profileInformationFragment = new ProfileInformationFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flFragment,profileInformationFragment).commit();
            }
        });

        txtDay_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestADayOffFragment aDayOffFragment = new RequestADayOffFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flFragment,aDayOffFragment).commit();
//                Date currentTime = Calendar.getInstance().getTime();
//                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//                String currentDate = dateFormat.format(currentTime);
//
//                FirebaseDatabase database = FirebaseDatabase.getInstance();
//                DatabaseReference recordRef = database.getReference("record").child(user.getPhone());
//                recordRef.addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        DataSnapshot dataSnapshot1 = snapshot.child(currentDate).child("checkIn");
//                        Record checkInRecord = dataSnapshot1.getValue(Record.class);
//
//                        DataSnapshot dataSnapshot3 = snapshot.child(currentDate).child("absent");
//                        Record absentRecord = dataSnapshot3.getValue(Record.class);
//                        if (checkInRecord != null) {
//                            Toast.makeText(ListSendedRequestActivity.this, "You have already check in!!", Toast.LENGTH_SHORT).show();
//                        }
//                        else if (absentRecord != null) {
//                            Toast.makeText(ListSendedRequestActivity.this, "You have already absent!!", Toast.LENGTH_SHORT).show();
//                        }
//                        else{
//
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });
            }
        });

        txtChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangePasswordFragment changePasswordFragment = new ChangePasswordFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flFragment,changePasswordFragment).commit();
            }
        });

        txtLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user_singeton = User_singeton.getInstance();
                user_singeton.setUser(null);
                user_singeton = null;

                sharedPreferences = getActivity().getSharedPreferences("isVerifyOtp", Context.MODE_MULTI_PROCESS);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove("otp");
                editor.apply();

                startActivity(new Intent(getActivity(), LoginActivity.class));
                getActivity().finish();            }
        });

        return view;
    }

    private void mapping() {
        txtProfile = (TextView) view.findViewById(R.id.txtProfile);
        txtChangePassword = (TextView) view.findViewById(R.id.txtChangePassword);
        txtLogOut = (TextView) view.findViewById(R.id.txtLogOut);
        txtDay_off = (TextView) view.findViewById(R.id.txtDay_off);
    }
}