package hcmute.edu.vn.tlcn.attendanceapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
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
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import at.favre.lib.crypto.bcrypt.BCrypt;
import hcmute.edu.vn.tlcn.attendanceapp.Utility.InternetCheckService;
import hcmute.edu.vn.tlcn.attendanceapp.model.User;
import hcmute.edu.vn.tlcn.attendanceapp.pattern.User_singeton;

public class LoginActivity extends AppCompatActivity {

    EditText edittextEmpCode, edittextPassword;
    TextView txtForgotPassword, txtLoginWithQRCode;
    Button btnLogin;
    InternetCheckService internetCheckService;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edittextEmpCode = (EditText) findViewById(R.id.edittextEmpCode);
        edittextPassword = (EditText) findViewById(R.id.edittextPassword);
        txtForgotPassword = (TextView) findViewById(R.id.txtForgotPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        txtLoginWithQRCode = (TextView) findViewById(R.id.txtLoginWithQRCode);

        internetCheckService = new InternetCheckService();

        txtForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SendOTPActivity.class));
                finish();
            }
        });

        txtLoginWithQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, QRScannerActivity.class));
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressDialog = new ProgressDialog(LoginActivity.this);
                progressDialog.setTitle("Logging...");
                progressDialog.setMessage("Please wait");
                progressDialog.show();

                String code = edittextEmpCode.getText().toString();
                String password = edittextPassword.getText().toString();

                if (code.equals("") || password.equals("") || password.length() < 6) {
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Invalid input!", Toast.LENGTH_SHORT).show();
                } else {
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference("users");

                    myRef.child(code).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User_singeton isUser = User_singeton.getInstance();
                            if (isUser.getUser() != null)
                                return;
                            if (!snapshot.exists()) {
                                progressDialog.dismiss();
                                Toast.makeText(LoginActivity.this, "Wrong account or password!", Toast.LENGTH_SHORT).show();
                            }
                            User loginUser = snapshot.getValue(User.class);
                            String hashPass = loginUser.getPassword();
                            BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), hashPass);

                            if (result.verified) {
                                generateQR(loginUser);
                                myRef.child(loginUser.getUuid()).setValue(loginUser);
                                User_singeton user_singeton = User_singeton.getInstance();
                                user_singeton.setUser(loginUser);


                                progressDialog.dismiss();
                                Toast.makeText(LoginActivity.this, "Login Successfully!", Toast.LENGTH_SHORT).show();
                                if (loginUser.getRole() == 1) {
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                } else {
                                    startActivity(new Intent(LoginActivity.this, AdminMainActivity.class));
                                }
                                finish();
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(LoginActivity.this, "Wrong account or password!", Toast.LENGTH_SHORT).show();
                            }
                            Log.d("tag", loginUser.getPhone());

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        });
    }

    private void generateQR(User user) {
        MultiFormatWriter writer = new MultiFormatWriter();
        try {
            String timeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
            BitMatrix matrix = writer.encode(user.getUuid() + "_" + user.getPhone() + "_" + timeStamp,
                    BarcodeFormat.QR_CODE, 800, 800);

            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.createBitmap(matrix);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference ref = storage.getReference();

            String url = "images/" + user.getUuid() + "_qrcode";

            UploadTask uploadTask = ref.child(url).putBytes(byteArray);
            user.setQrcode(url);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    user.setQrcode(url);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });
        } catch (WriterException e) {
            e.printStackTrace();
        }
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
}