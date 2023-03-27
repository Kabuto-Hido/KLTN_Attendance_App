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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import hcmute.edu.vn.tlcn.attendanceapp.model.DayOffRequest;
import hcmute.edu.vn.tlcn.attendanceapp.model.User;
import hcmute.edu.vn.tlcn.attendanceapp.pattern.User_singeton;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AdminSettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AdminSettingsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AdminSettingsFragment() {
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
    public static AdminSettingsFragment newInstance(String param1, String param2) {
        AdminSettingsFragment fragment = new AdminSettingsFragment();
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
    TextView txtEmployee, txtAdminProfile, txtAdminChangePassword,
            txtAdminLogOut, txtListResignations, txtQuantityResignation, txtAdminQRCode;
    User_singeton user_singeton;
    SharedPreferences sharedPreferences;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_admin_settings, container, false);
        user_singeton = User_singeton.getInstance();

        if(user_singeton.getUser() == null)
        {
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        }
        
        mapping();
        getData();

        txtEmployee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Manage_Emp_Fragment manage_emp_fragment = new Manage_Emp_Fragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flAdminFragment,manage_emp_fragment).commit();
            }
        });

        txtAdminProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileInformationFragment profileInformationFragment = new ProfileInformationFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flAdminFragment,profileInformationFragment).commit();
            }
        });

        txtAdminChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangePasswordFragment changePasswordFragment = new ChangePasswordFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flAdminFragment,changePasswordFragment).commit();
            }
        });

        txtAdminLogOut.setOnClickListener(new View.OnClickListener() {
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
                getActivity().finish();
            }
        });

        txtListResignations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListResignationFragment listResignationFragment = new ListResignationFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flAdminFragment,listResignationFragment).commit();

            }
        });

        txtAdminQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QRCodeFragment qrCodeFragment = new QRCodeFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flAdminFragment,qrCodeFragment).commit();

            }
        });
        return view;
    }

    private void getData() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference dayOffReportRef = database.getReference("dayoffreport");

        dayOffReportRef.orderByChild("status").startAt("waiting").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long amountWaitingForm = snapshot.getChildrenCount();
                if(amountWaitingForm != 0L){
                    txtQuantityResignation.setText(String.valueOf(amountWaitingForm));
                    txtQuantityResignation.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void mapping(){
        txtEmployee = (TextView) view.findViewById(R.id.txtEmployee);
        txtAdminProfile = (TextView) view.findViewById(R.id.txtAdminProfile);
        txtAdminChangePassword = (TextView) view.findViewById(R.id.txtAdminChangePassword);
        txtAdminLogOut = (TextView) view.findViewById(R.id.txtAdminLogOut);
        txtListResignations = (TextView) view.findViewById(R.id.txtListResignations);
        txtQuantityResignation = (TextView) view.findViewById(R.id.txtQuantityResignation);
        txtAdminQRCode = (TextView) view.findViewById(R.id.txtAdminQRCode);
    }
}