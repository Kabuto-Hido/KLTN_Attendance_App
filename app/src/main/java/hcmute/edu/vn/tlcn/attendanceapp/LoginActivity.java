package hcmute.edu.vn.tlcn.attendanceapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import at.favre.lib.crypto.bcrypt.BCrypt;
import hcmute.edu.vn.tlcn.attendanceapp.model.User;
import hcmute.edu.vn.tlcn.attendanceapp.pattern.User_singeton;

public class LoginActivity extends AppCompatActivity {

    EditText edittextPhone,edittextPassword;
    TextView txtForgotPassword;
    Button btnLogin;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edittextPhone = (EditText) findViewById(R.id.edittextPhone);
        edittextPassword = (EditText) findViewById(R.id.edittextPassword);
        txtForgotPassword = (TextView) findViewById(R.id.txtForgotPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);

        txtForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,SendOTPActivity.class));
                finish();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressDialog = new ProgressDialog(LoginActivity.this);
                progressDialog.setTitle("Logging...");
                progressDialog.setMessage("Please wait");
                progressDialog.show();

                String phone = edittextPhone.getText().toString();
                String password = edittextPassword.getText().toString();

                if(phone.equals("") || password.equals("") || phone.length() != 10 || password.length() < 6)
                {
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Invalid input!", Toast.LENGTH_SHORT).show();
                }else{
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference("users");

                    myRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User_singeton isUser = User_singeton.getInstance();
                            if(isUser.getUser() != null)
                                return;

                            DataSnapshot dataSnapshot = snapshot.child(phone);
                            if(!dataSnapshot.exists()) {
                                progressDialog.dismiss();
                                Toast.makeText(LoginActivity.this, "Wrong account or password!", Toast.LENGTH_SHORT).show();
                            }

                            User user = dataSnapshot.getValue(User.class);
                            assert user != null;
                            String hashPass = user.getPassword();
                            BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(),hashPass);

                            if(result.verified){
                                User_singeton user_singeton = User_singeton.getInstance();
                                user_singeton.setUser(user);
                                progressDialog.dismiss();
                                Toast.makeText(LoginActivity.this, "Login Successfully!", Toast.LENGTH_SHORT).show();
                                if(user.getRole() == 1) {
                                    startActivity(new Intent(LoginActivity.this,MainActivity.class));
                                }
                                else{
                                    startActivity(new Intent(LoginActivity.this,AdminMainActivity.class));
                                }
                                finish();
                            }
                            else{
                                progressDialog.dismiss();
                                Toast.makeText(LoginActivity.this, "Wrong account or password!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        });
    }

}