package hcmute.edu.vn.tlcn.attendanceapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.favre.lib.crypto.bcrypt.BCrypt;
import hcmute.edu.vn.tlcn.attendanceapp.Utility.InternetCheckService;
import hcmute.edu.vn.tlcn.attendanceapp.pattern.User_singeton;

public class ForgetPasswordActivity extends AppCompatActivity {
    TextView txtShowNewPassword;
    EditText edtNewResetPassword, edtConfirmResetNewPassword;
    Button btnConfirmResetPass;
    SharedPreferences sharedPreferences;
    String mPhone, getPhone;
    InternetCheckService internetCheckService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        mapping();

        internetCheckService = new InternetCheckService();

        mPhone = getIntent().getStringExtra("forgetPhone");
        getPhone = "0" + mPhone.substring(3);

        txtShowNewPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txtShowNewPassword.getText().toString().equals("SHOW")) {
                    edtNewResetPassword.setInputType(1);
                    edtConfirmResetNewPassword.setInputType(1);
                    txtShowNewPassword.setText("HIDE");
                } else {
                    edtNewResetPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
                    edtConfirmResetNewPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
                    txtShowNewPassword.setText("SHOW");
                }
            }
        });

        btnConfirmResetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProgressDialog progressDialog = new ProgressDialog(ForgetPasswordActivity.this);
                progressDialog.setTitle("Please wait...");
                progressDialog.show();

                String newPass = edtNewResetPassword.getText().toString();
                String confirmNewPass = edtConfirmResetNewPassword.getText().toString();

                if (isValidPassword(newPass)) {
                    progressDialog.dismiss();
                    edtNewResetPassword.setError("Password must contain at least 8 characters, one digit, one upper case alphabet and one lower case alphabet!");
                } else if (!newPass.equals(confirmNewPass)) {
                    progressDialog.dismiss();
                    edtConfirmResetNewPassword.setError("The confirm password does not match!!");
                } else {
                    String newHashPass = BCrypt.withDefaults().hashToString(12, newPass.toCharArray());
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference("users");
                    myRef.child(getPhone).child("password").setValue(newHashPass);

                    Toast.makeText(ForgetPasswordActivity.this, "Change password successful.", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    logout();
                }
            }
        });

    }

    @Override
    protected void onStart() {
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(internetCheckService, intentFilter);
        super.onStart();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(internetCheckService);
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(internetCheckService, intentFilter);
    }

    public boolean isValidPassword(String password) {
        //password must containing at least 8 characters and at most 20 characters,
        // containing at least one digit, one upper case alphabet and one lower case alphabet1
        String regex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,20}$";
        Pattern p = Pattern.compile(regex);
        if (password == null) {
            return false;
        }
        Matcher m = p.matcher(password);
        return m.matches();
    }

    private void logout() {
        User_singeton user_singeton = User_singeton.getInstance();
        user_singeton.setUser(null);

        sharedPreferences = getSharedPreferences("isVerifyOtp", Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("otp");
        editor.apply();

        startActivity(new Intent(ForgetPasswordActivity.this, LoginActivity.class));
        finish();
    }

    private void mapping() {
        txtShowNewPassword = (TextView) findViewById(R.id.txtShowNewPassword);
        edtNewResetPassword = (EditText) findViewById(R.id.edtNewResetPassword);
        edtConfirmResetNewPassword = (EditText) findViewById(R.id.edtConfirmResetNewPassword);
        btnConfirmResetPass = (Button) findViewById(R.id.btnConfirmResetPass);
    }
}