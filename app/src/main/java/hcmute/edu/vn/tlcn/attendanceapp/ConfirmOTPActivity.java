package hcmute.edu.vn.tlcn.attendanceapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import hcmute.edu.vn.tlcn.attendanceapp.model.User;
import hcmute.edu.vn.tlcn.attendanceapp.pattern.User_singeton;

public class ConfirmOTPActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    PhoneAuthProvider.ForceResendingToken token;
    Button btn_confirmOtp;
    TextView nofi3, btn_resendCode;
    EditText editTextInput1, editTextInput2, editTextInput3,
            editTextInput4, editTextInput5, editTextInput6;

    String mPhone, mVerificationId;
    SharedPreferences sharedPreferences;

    User_singeton user_singeton = User_singeton.getInstance();
    User loginUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_otp);

        sharedPreferences = getSharedPreferences("isVerifyOtp", Context.MODE_MULTI_PROCESS);
        loginUser = user_singeton.getUser();
        mapping();
        firebaseAuth = FirebaseAuth.getInstance();

        mPhone = getIntent().getStringExtra("phone");
        mVerificationId = getIntent().getStringExtra("verificationId");

        nofi3.setText("We have sent you an SMS with the code to " + mPhone);

//        btn_resendCode.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                PhoneAuthOptions options =  PhoneAuthOptions.newBuilder(firebaseAuth)
//                        .setPhoneNumber(mPhone)       // Phone number to verify
//                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
//                        .setActivity(ConfirmOTPActivity.this)                 // Activity (for callback binding)
//                        .setForceResendingToken(token)
//                        .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
//                        .build();
//                PhoneAuthProvider.verifyPhoneNumber(options);
//            }
//        });
//
//        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
//            @Override
//            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
//                signInWithPhoneAuthCredential(phoneAuthCredential);
//            }
//
//            @Override
//            public void onVerificationFailed(@NonNull FirebaseException e) {
//                Toast.makeText(ConfirmOTPActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
//                super.onCodeSent(s, forceResendingToken);
//                mVerificationId = s;
//                token = forceResendingToken;
//            }
//
//            @Override
//            public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
//                super.onCodeAutoRetrievalTimeOut(s);
//            }
//        };

        editTextInput1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (editTextInput1.getText().toString().length() == 1) {
                    editTextInput2.requestFocus();
                }
            }
        });

        editTextInput2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (editTextInput2.getText().toString().length() == 1) {
                    editTextInput3.requestFocus();
                }
            }
        });

        editTextInput3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (editTextInput3.getText().toString().length() == 1) {
                    editTextInput4.requestFocus();
                }
            }
        });

        editTextInput4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (editTextInput4.getText().toString().length() == 1) {
                    editTextInput5.requestFocus();
                }
            }
        });

        editTextInput5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (editTextInput5.getText().toString().length() == 1) {
                    editTextInput6.requestFocus();
                }
            }
        });

        btn_confirmOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editTextInput1.getText().toString().isEmpty()) {
                    editTextInput1.setError("Required");
                    return;
                }
                if (editTextInput2.getText().toString().isEmpty()) {
                    editTextInput2.setError("Required");
                    return;
                }
                if (editTextInput3.getText().toString().isEmpty()) {
                    editTextInput3.setError("Required");
                    return;
                }
                if (editTextInput4.getText().toString().isEmpty()) {
                    editTextInput4.setError("Required");
                    return;
                }
                if (editTextInput5.getText().toString().isEmpty()) {
                    editTextInput5.setError("Required");
                    return;
                }
                if (editTextInput6.getText().toString().isEmpty()) {
                    editTextInput6.setError("Required");
                    return;
                }

                String code =
                        editTextInput1.getText().toString() +
                                editTextInput2.getText().toString() +
                                editTextInput3.getText().toString() +
                                editTextInput4.getText().toString() +
                                editTextInput5.getText().toString() +
                                editTextInput6.getText().toString();
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
                signInWithPhoneAuthCredential(credential);
            }
        });

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = task.getResult().getUser();
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("otp", true);
                            editor.apply();

                            if (loginUser == null) {
                                Intent goToForgetPasswordActivity = new Intent(ConfirmOTPActivity.this, ForgetPasswordActivity.class);
                                goToForgetPasswordActivity.putExtra("forgetPhone", mPhone);
                                startActivity(goToForgetPasswordActivity);
                                finish();
                            } else {
                                finish();
                            }
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(ConfirmOTPActivity.this, "Wrong OTP code", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void mapping() {
        btn_confirmOtp = (Button) findViewById(R.id.btn_confirmOtp);
        btn_resendCode = (TextView) findViewById(R.id.btn_resendCode);
        nofi3 = (TextView) findViewById(R.id.nofi3);
        editTextInput1 = (EditText) findViewById(R.id.editTextInput1);
        editTextInput2 = (EditText) findViewById(R.id.editTextInput2);
        editTextInput3 = (EditText) findViewById(R.id.editTextInput3);
        editTextInput4 = (EditText) findViewById(R.id.editTextInput4);
        editTextInput5 = (EditText) findViewById(R.id.editTextInput5);
        editTextInput6 = (EditText) findViewById(R.id.editTextInput6);
    }
}