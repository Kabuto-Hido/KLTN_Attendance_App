package hcmute.edu.vn.tlcn.attendanceapp;

import static android.Manifest.permission.CAMERA;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.TorchState;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import at.favre.lib.crypto.bcrypt.BCrypt;
import hcmute.edu.vn.tlcn.attendanceapp.model.User;
import hcmute.edu.vn.tlcn.attendanceapp.pattern.User_singeton;

public class QRScannerActivity extends AppCompatActivity {
    private final int PICK_IMAGE_REQUEST = 22;
    ImageView btnQuit, btnOpenGallery, btnFlash;
    PreviewView cameraPreview;
    ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture;
    BarcodeScannerOptions barcodeScannerOptions;
    BarcodeScanner scanner;
    InputImage inputImage;
    ProgressDialog progressDialog;
    Preview preview;
    CameraSelector cameraSelector;
    ProcessCameraProvider cameraProvider;
    ImageAnalysis imageAnalysis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscanner);

        mapping();

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
                startActivityForResult(Intent.createChooser(intent,"Select image..."), PICK_IMAGE_REQUEST);
            }
        });

        btnFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Camera cam = cameraProvider.bindToLifecycle(QRScannerActivity.this,
                        cameraSelector, imageAnalysis, preview);

                if ( cam.getCameraInfo().getTorchState().getValue() == TorchState.ON) {
                    cam.getCameraControl().enableTorch(false);
                    btnFlash.setImageResource(R.drawable.ic_flash_off);
                }
                else{
                    cam.getCameraControl().enableTorch(true);
                    btnFlash.setImageResource(R.drawable.ic_flash_on);
                }
            }
        });

        if(CheckPermissions()){
            scanQRCodeFromCamera();
        }
        else{
            RequestPermissions();
        }
    }

    private void scanQRCodeFromCamera() {
        cameraProviderListenableFuture = ProcessCameraProvider.getInstance(QRScannerActivity.this);
        cameraProviderListenableFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    cameraProvider = cameraProviderListenableFuture.get();
                    bindImageAnalysis();
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(QRScannerActivity.this));
    }

    private void bindImageAnalysis() {
         imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(800,800))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(QRScannerActivity.this),
                new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy image) {
                @SuppressLint("UnsafeOptInUsageError")
                Image mediaImage = image.getImage();
                if(mediaImage != null){
                    inputImage = InputImage.fromMediaImage(mediaImage,
                            image.getImageInfo().getRotationDegrees());

                    detectResultFromImg(inputImage);

                    image.close();
                    mediaImage.close();

                }

            }
        });
        preview = new Preview.Builder().build();
        cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        preview.setSurfaceProvider(cameraPreview.getSurfaceProvider());
        cameraProvider.bindToLifecycle(QRScannerActivity.this, cameraSelector, imageAnalysis, preview);
    }

    public void detectResultFromImg(InputImage inputImage){
        Task<List<Barcode>> results = scanner.process(inputImage);

        results.addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
            @Override
            public void onSuccess(List<Barcode> barcodes) {
                for(Barcode barcode: barcodes){
                    String getValues = barcode.getDisplayValue();

                    if(getValues!=null) {
                        login(getValues);
                        break;
                    }
                    //Toast.makeText(QRScannerActivity.this, getValues, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void login(String data) {
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
                if(isUser.getUser() != null)
                    return;
                if(!snapshot.exists()) {
                    progressDialog.dismiss();
                    Toast.makeText(QRScannerActivity.this, "Can't not recognize your QR Code", Toast.LENGTH_SHORT).show();
                }
                for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User loginUser = dataSnapshot.getValue(User.class);
                    String hashPass = loginUser.getPassword();

                    if(hashPass.equals(cutString[1])){
                        User_singeton user_singeton = User_singeton.getInstance();
                        user_singeton.setUser(loginUser);
                        progressDialog.dismiss();
                        Toast.makeText(QRScannerActivity.this, "Login Successfully!", Toast.LENGTH_SHORT).show();
                        if(loginUser.getRole() == 1) {
                            startActivity(new Intent(QRScannerActivity.this,MainActivity.class));
                        }
                        else{
                            startActivity(new Intent(QRScannerActivity.this,AdminMainActivity.class));
                        }
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void mapping() {
        btnQuit = (ImageView) findViewById(R.id.btnQuit);
        btnOpenGallery = (ImageView) findViewById(R.id.btnOpenGallery);
        cameraPreview = (PreviewView) findViewById(R.id.cameraPreview);
        btnFlash = (ImageView) findViewById(R.id.btnFlash);
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
                    scanQRCodeFromCamera();
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
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                inputImage = InputImage.fromFilePath(QRScannerActivity.this,filePath);
                detectResultFromImg(inputImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}