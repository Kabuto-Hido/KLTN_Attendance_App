package hcmute.edu.vn.tlcn.attendanceapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import at.favre.lib.crypto.bcrypt.BCrypt;
import hcmute.edu.vn.tlcn.attendanceapp.model.User;
import hcmute.edu.vn.tlcn.attendanceapp.pattern.User_singeton;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChangePasswordFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChangePasswordFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ChangePasswordFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChangePasswordFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChangePasswordFragment newInstance(String param1, String param2) {
        ChangePasswordFragment fragment = new ChangePasswordFragment();
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
    ImageView btnBackChangePassword;
    TextView txtShowPassword;
    EditText edittextCurrentPassword, edittextNewPassword, edittextConfirmNewPassword;
    Button btnConfirmChangePassword;
    String newPassword;
    User_singeton user_singeton;
    User user;
    SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_change_password, container, false);

        mapping();

        user_singeton = User_singeton.getInstance();
        user = user_singeton.getUser();

        btnBackChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AdminSettingsFragment adminSettingsFragment = new AdminSettingsFragment();
                SettingsFragment settingsFragment = new SettingsFragment();

                if (user.getRole() == 0) {
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flAdminFragment, adminSettingsFragment).commit();
                }
                else{
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, settingsFragment).commit();

                }
            }
        });

        txtShowPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(txtShowPassword.getText().toString().equals("SHOW")) {
                    edittextCurrentPassword.setInputType(1);
                    edittextNewPassword.setInputType(1);
                    edittextConfirmNewPassword.setInputType(1);
                    txtShowPassword.setText("HIDE");
                }
                else{
                    edittextCurrentPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
                    edittextNewPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
                    edittextConfirmNewPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
                    txtShowPassword.setText("SHOW");
                }
            }
        });

        btnConfirmChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProgressDialog progressDialog = new ProgressDialog(getActivity());
                progressDialog.setTitle("Please wait...");
                progressDialog.show();

                String currentPassword = edittextCurrentPassword.getText().toString();
                newPassword = edittextNewPassword.getText().toString();
                String confirmNewPassword = edittextConfirmNewPassword.getText().toString();

                BCrypt.Result result = BCrypt.verifyer().verify(currentPassword.toCharArray(),user.getPassword());

                if(!result.verified){
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(), "Incorrect current password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(newPassword.length() < 6) {
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(), "Password too weak !", Toast.LENGTH_SHORT).show();
                }
                else if(!newPassword.equals(confirmNewPassword)){
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(), "The confirm password does not match!!", Toast.LENGTH_SHORT).show();
                }
                else{
                    progressDialog.dismiss();
                    startActivity(new Intent(getActivity(), SendOTPActivity.class));

                }
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        sharedPreferences = this.getActivity().getSharedPreferences("isVerifyOtp", Context.MODE_MULTI_PROCESS);
        boolean otp = sharedPreferences.getBoolean("otp",false);
        if(otp) {
            String newHashPass = BCrypt.withDefaults().hashToString(12, newPassword.toCharArray());

            user.setPassword(newHashPass);
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("users");
            myRef.child(user.getPhone()).setValue(user);
            Toast.makeText(getActivity(), "Change password successful.", Toast.LENGTH_SHORT).show();
            logout();
        }

    }

    private void logout() {
        user_singeton = User_singeton.getInstance();
        user_singeton.setUser(null);
        user_singeton = null;

        sharedPreferences = this.getActivity().getSharedPreferences("isVerifyOtp", Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("otp");
        editor.apply();

        startActivity(new Intent(getActivity(), LoginActivity.class));
        getActivity().finish();
    }

    private void mapping() {
        btnBackChangePassword = (ImageView) view.findViewById(R.id.btnBackChangePassword);
        txtShowPassword = (TextView) view.findViewById(R.id.txtShowPassword);
        edittextCurrentPassword = (EditText) view.findViewById(R.id.edittextCurrentPassword);
        edittextNewPassword = (EditText) view.findViewById(R.id.edittextNewPassword);
        edittextConfirmNewPassword = (EditText) view.findViewById(R.id.edittextConfirmNewPassword);
        btnConfirmChangePassword = (Button) view.findViewById(R.id.btnConfirmChangePassword);
    }
}