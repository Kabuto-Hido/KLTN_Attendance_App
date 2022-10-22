package hcmute.edu.vn.tlcn.attendanceapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
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
//                //BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(),hashPass);
//                String birthday = format.format(birth);
//
////                //admin 0937302331 - 123456
//                User user = new User("admin","0937302331",hashPass,birthday,
//                        "User Administration",true,"",0);
//
//                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.man_placeholder);
//                ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
//                byte[] bitMapData = stream.toByteArray();
//
//                FirebaseStorage storage = FirebaseStorage.getInstance();
//                StorageReference ref = storage.getReference();
//                UploadTask uploadTask = ref.child("images/" + user.getPhone() + "_avatar").putBytes(bitMapData);
//                uploadTask
//                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                            @Override
//                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                                user.setAvatar("images/" + user.getPhone() + "_avatar");
//                                FirebaseDatabase database = FirebaseDatabase.getInstance();
//                                DatabaseReference myRef = database.getReference("users");
//                                myRef.child(user.getPhone()).setValue(user);
//                                Toast.makeText(LoginActivity.this, "Update successful!!", Toast.LENGTH_SHORT).show();
//                            }
//                        })
//                        .addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                Toast.makeText(LoginActivity.this,"Update failed. Error: " + e.getMessage(),Toast.LENGTH_SHORT).show();
//                            }
//                        });


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
                                if(user.getRole() == 0) {
                                    Toast.makeText(LoginActivity.this, "Login Successfully!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(LoginActivity.this,MainActivity.class));
                                    finish();
                                }
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