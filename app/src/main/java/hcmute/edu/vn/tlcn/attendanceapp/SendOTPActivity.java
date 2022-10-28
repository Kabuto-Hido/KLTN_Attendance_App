package hcmute.edu.vn.tlcn.attendanceapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import hcmute.edu.vn.tlcn.attendanceapp.model.User;
import hcmute.edu.vn.tlcn.attendanceapp.pattern.User_singeton;

public class SendOTPActivity extends AppCompatActivity {
    ImageView btn_back_send_otp;
    Button btn_sendOtp;
    EditText edittext_phone_SendOtp;
    String phone;
    FirebaseAuth firebaseAuth;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    PhoneAuthProvider.ForceResendingToken token;
    SharedPreferences sharedPreferences;
    User_singeton user_singeton;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_otp);

        mapping();
        sharedPreferences = getSharedPreferences("isVerifyOtp", Context.MODE_MULTI_PROCESS);
        firebaseAuth = FirebaseAuth.getInstance();

        user_singeton = User_singeton.getInstance();
        user = user_singeton.getUser();

        edittext_phone_SendOtp.setText(user.getPhone());
        edittext_phone_SendOtp.setFocusable(false);

        btn_back_send_otp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("otp",false);
                editor.apply();
                finish();
            }
        });

        btn_sendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //String getPhone = edittext_phone_SendOtp.getText().toString().trim();
                String getPhone = user.getPhone();
                phone = "+84" + getPhone.substring(1);

                if(phone.isEmpty()){
                    edittext_phone_SendOtp.setError("Required");
                    return;
                }
                if(phone.length()<10){
                    edittext_phone_SendOtp.setError("The phone number must have 10 digits");
                    return;
                }

                PhoneAuthOptions options =  PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber(phone)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(SendOTPActivity.this)                 // Activity (for callback binding)
                        .setForceResendingToken(token)
                        .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                        .build();
                PhoneAuthProvider.verifyPhoneNumber(options);

            }
        });

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(SendOTPActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                token = forceResendingToken;
                Intent gotoConfirmActivity = new Intent(SendOTPActivity.this,ConfirmOTPActivity.class);
                gotoConfirmActivity.putExtra("verificationId",s);
                gotoConfirmActivity.putExtra("phone",phone);
                startActivity(gotoConfirmActivity);
                finish();
            }

            @Override
            public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
                super.onCodeAutoRetrievalTimeOut(s);
            }
        };

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SendOTPActivity.this,
                                    "signInWithCredential:success", Toast.LENGTH_SHORT).show();

                            FirebaseUser user = task.getResult().getUser();
                            // Update UI
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(SendOTPActivity.this, "failure", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void mapping() {
        btn_sendOtp = (Button) findViewById(R.id.btn_sendOtp);
        edittext_phone_SendOtp = (EditText) findViewById(R.id.edittext_phone_SendOtp);
        btn_back_send_otp = (ImageView) findViewById(R.id.btn_back_send_otp);
    }
}