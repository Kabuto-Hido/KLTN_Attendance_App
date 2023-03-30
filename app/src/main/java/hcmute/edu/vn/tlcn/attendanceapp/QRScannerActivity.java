package hcmute.edu.vn.tlcn.attendanceapp;

import static android.Manifest.permission.CAMERA;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.budiyev.android.codescanner.AutoFocusMode;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.budiyev.android.codescanner.ScanMode;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.google.zxing.Result;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.util.List;

import hcmute.edu.vn.tlcn.attendanceapp.Utility.InternetCheckService;
import hcmute.edu.vn.tlcn.attendanceapp.model.User;
import hcmute.edu.vn.tlcn.attendanceapp.pattern.User_singeton;

public class QRScannerActivity extends AppCompatActivity {
    private final int PICK_IMAGE_REQUEST = 22;
    ImageView btnQuit, btnOpenGallery, btnFlash;
    CodeScannerView scannerView;
    BarcodeScannerOptions barcodeScannerOptions;
    BarcodeScanner scanner;
    InputImage inputImage;
    ProgressDialog progressDialog;
    InternetCheckService internetCheckService;

    private CodeScanner mCodeScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscanner);

        mapping();

        internetCheckService = new InternetCheckService();

        barcodeScannerOptions = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS).build();
        scanner = BarcodeScanning.getClient(barcodeScannerOptions);

        btnQuit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnOpenGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select image..."), PICK_IMAGE_REQUEST);
            }
        });


        if (CheckPermissions()) {
            codeScanner();
        } else {
            RequestPermissions();
        }
    }

    private void codeScanner() {
        mCodeScanner = new CodeScanner(QRScannerActivity.this, scannerView);
        mCodeScanner.setCamera(CodeScanner.CAMERA_BACK);
        mCodeScanner.setFormats(CodeScanner.ALL_FORMATS);
        mCodeScanner.setAutoFocusMode(AutoFocusMode.SAFE);
        mCodeScanner.setAutoFocusEnabled(true);
        mCodeScanner.setFlashEnabled(false);
        mCodeScanner.setScanMode(ScanMode.CONTINUOUS);

        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (result.getText() != null) {
                            checkInfor(result.getText());
                            mCodeScanner.releaseResources();
                        }
                    }
                });
            }
        });

        scannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCodeScanner.startPreview();
            }
        });
    }

    public void detectResultFromImg(InputImage inputImage) {
        Task<List<Barcode>> results = scanner.process(inputImage);
        results.addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
            @Override
            public void onSuccess(List<Barcode> barcodes) {
                for (Barcode barcode : barcodes) {
                    String getValues = barcode.getDisplayValue();

                    if (getValues != null) {
                        checkInfor(getValues);
                        break;
                    }
                }
            }
        });
    }

    private void checkInfor(String data) {
        progressDialog = new ProgressDialog(QRScannerActivity.this);
        progressDialog.setTitle("Logging...");
        progressDialog.setMessage("Please wait");
        progressDialog.show();

        String[] cutString = data.split("_");
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users");
        myRef.orderByChild("uuid").equalTo(cutString[0]).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User_singeton isUser = User_singeton.getInstance();
                if (isUser.getUser() != null)
                    return;
                if (!snapshot.exists()) {
                    progressDialog.dismiss();
                    Toast.makeText(QRScannerActivity.this, "Can't not recognize your QR Code", Toast.LENGTH_SHORT).show();
                }
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User loginUser = dataSnapshot.getValue(User.class);

                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageReference = storage.getReference(loginUser.getQrcode());
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.get().load(uri)
                                    .into(new Target() {
                                        @Override
                                        public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                                            InputImage img = InputImage.fromBitmap(bitmap, 0);
                                            Task<List<Barcode>> results = scanner.process(img);
                                            results.addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                                                @Override
                                                public void onSuccess(List<Barcode> barcodes) {
                                                    for (Barcode barcode : barcodes) {
                                                        String results = barcode.getDisplayValue();

                                                        if (results != null && results.equals(data)) {
                                                            mCodeScanner.releaseResources();
                                                            User_singeton user_singeton = User_singeton.getInstance();
                                                            user_singeton.setUser(loginUser);
                                                            progressDialog.dismiss();
                                                            Toast.makeText(QRScannerActivity.this, "Login Successfully!", Toast.LENGTH_SHORT).show();

                                                            if (loginUser.getRole() == 1) {
                                                                startActivity(new Intent(QRScannerActivity.this, MainActivity.class));
                                                            } else {
                                                                startActivity(new Intent(QRScannerActivity.this, AdminMainActivity.class));
                                                            }
                                                            finish();
                                                            break;
                                                        } else {
                                                            Toast.makeText(QRScannerActivity.this, "Invalid QR Code!", Toast.LENGTH_SHORT).show();
                                                            progressDialog.dismiss();
                                                        }
                                                    }
                                                }
                                            });

                                        }

                                        @Override
                                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                                        }

                                        @Override
                                        public void onPrepareLoad(Drawable placeHolderDrawable) {

                                        }
                                    });

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("TAG", "onFailure: " + e.getMessage());
                        }
                    });


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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
    protected void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(internetCheckService);
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCodeScanner.startPreview();
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(internetCheckService, intentFilter);
    }

    private void mapping() {
        btnQuit = (ImageView) findViewById(R.id.btnQuit);
        btnOpenGallery = (ImageView) findViewById(R.id.btnOpenGallery);
        scannerView = (CodeScannerView) findViewById(R.id.scannerView);
    }

    public static final int REQUEST_AUDIO_PERMISSION_CODE = 1;

    public boolean CheckPermissions() {
        int result = ContextCompat.checkSelfPermission(QRScannerActivity.this, CAMERA);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void RequestPermissions() {
        ActivityCompat.requestPermissions(QRScannerActivity.this, new String[]{CAMERA}, REQUEST_AUDIO_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_AUDIO_PERMISSION_CODE) {
            if (grantResults.length > 0) {
                boolean permissionToCamera = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (permissionToCamera) {
                    codeScanner();
                    Toast.makeText(QRScannerActivity.this, "Permission Granted", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(QRScannerActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                inputImage = InputImage.fromFilePath(QRScannerActivity.this, filePath);
                detectResultFromImg(inputImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}