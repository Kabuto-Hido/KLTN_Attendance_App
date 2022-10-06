package hcmute.edu.vn.tlcn.attendanceapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
//                Date birth = null;
//                try {
//
//                    birth = format.parse("2001-08-29");
//
//                } catch (ParseException e) {  //Ngày sinh nhập không hợp lệ
//                    e.printStackTrace();
//
//                    Toast.makeText(LoginActivity.this, "Invalid Birthday !", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//
//                String password = "123456";
//                String hashPass = BCrypt.withDefaults().hashToString(12, password.toCharArray());
//                BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(),hashPass);
//                if(result.verified)
//                String birthday = format.format(birth);
//                String defaultAvatar = "https://firebasestorage.googleapis.com/v0/b/cnpm-30771.appspot.com/o/no-user.png?alt=media&token=517e08ab-6aa4-42eb-9547-b1b10f17caf0";
//
//                //admin 0937302331 - 123456
//                User user = new User("admin","0937302331",hashPass,birthday,
//                        "User Administration",true,defaultAvatar,0);
//
//                FirebaseDatabase database = FirebaseDatabase.getInstance();
//                DatabaseReference myRef = database.getReference("users");
//
//                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        if(snapshot.hasChild(user.getPhone()))
//                        {
//                            Toast.makeText(LoginActivity.this, "Phone number is already Taken !", Toast.LENGTH_SHORT).show();
//                            //isAvailable = false;
//                            //progressDialog.dismiss();
//                        }
//                        else{
//                            myRef.child(user.getPhone()).setValue(user);
//                            Toast.makeText(LoginActivity.this, "Successfully", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });

                progressDialog = new ProgressDialog(LoginActivity.this);
                progressDialog.setTitle("Logging...");
                progressDialog.setMessage("Please wait");
                progressDialog.show();

                String phone = edittextPhone.getText().toString();
                String password = edittextPassword.getText().toString();

                if(phone.equals("") || password.equals("") || phone.length() != 10 || password.length() < 6)
                {
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Wrong account or password!", Toast.LENGTH_SHORT).show();
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
                            String hashPass = user.getPassword();
                            BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(),hashPass);

                            if(result.verified){
                                User_singeton user_singeton = User_singeton.getInstance();
                                user_singeton.setUser(user);
                                progressDialog.dismiss();
                                Toast.makeText(LoginActivity.this, "Login Successfully!", Toast.LENGTH_SHORT).show();
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
    //Hash password


}