package hcmute.edu.vn.tlcn.attendanceapp;

import static android.Manifest.permission.CAMERA;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

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
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import hcmute.edu.vn.tlcn.attendanceapp.model.User;
import hcmute.edu.vn.tlcn.attendanceapp.pattern.User_singeton;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link QRCodeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class QRCodeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public QRCodeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment QRCodeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static QRCodeFragment newInstance(String param1, String param2) {
        QRCodeFragment fragment = new QRCodeFragment();
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

    ImageView btnBackQRCode, idIVQRCode;
    Button btn_recreate_QR, btn_save_QR;
    View view;User_singeton user_singeton;
    User user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_q_r_code, container, false);

        mapping();

        user_singeton = User_singeton.getInstance();
        user = user_singeton.getUser();

        putDataToView();

        btnBackQRCode.setOnClickListener(new View.OnClickListener() {
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

        btn_recreate_QR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateQR(user);
            }
        });

        btn_save_QR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(CheckPermissions()){
                    saveImg();
                }
                else{
                    RequestPermissions();
                }
            }
        });

        return view;
    }

    private void putDataToView() {
        if(user == null) {
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference(user.getQrcode());
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).fit().centerCrop().into(idIVQRCode);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("TAG", "onFailure: " + e.getMessage());
            }
        });
    }

    private void mapping() {
        btnBackQRCode = (ImageView) view.findViewById(R.id.btnBackQRCode);
        idIVQRCode = (ImageView) view.findViewById(R.id.idIVQRCode);
        btn_recreate_QR = (Button) view.findViewById(R.id.btn_recreate_QR);
        btn_save_QR = (Button) view.findViewById(R.id.btn_save_QR);
    }

    private void generateQR(User user){
        MultiFormatWriter writer = new MultiFormatWriter();
        try {
            String timeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
            BitMatrix matrix = writer.encode(user.getUuid() + "_" + user.getPassword() + "_" + timeStamp,
                    BarcodeFormat.QR_CODE, 800, 800);

            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.createBitmap(matrix);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG,100,stream);
            byte[] byteArray = stream.toByteArray();

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference ref = storage.getReference();

            String url = "images/" + user.getUuid() + "_qrcode" ;

            UploadTask uploadTask = ref.child(url).putBytes(byteArray);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    idIVQRCode.setImageBitmap(bitmap);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }catch (WriterException e){
            e.printStackTrace();
        }
    }

    public static final int REQUEST_STORAGE_CODE = 1;
    public boolean CheckPermissions() {
        int result = ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void RequestPermissions() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_CODE) {
            if (grantResults.length > 0) {
                boolean permissionToStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (permissionToStorage) {
                    saveImg();
                    Toast.makeText(getActivity().getApplicationContext(), "Permission Granted", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "Permission Denied", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public void saveImg(){
        String imageName = user.getUuid()+"_"+System.currentTimeMillis();

        Uri imageCollection;
        ContentResolver contentResolver = getActivity().getContentResolver();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            imageCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        }
        else {
            imageCollection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, imageName + ".jpg");
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/*");
        Uri imageUri = contentResolver.insert(imageCollection,contentValues);

        try {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) idIVQRCode.getDrawable();
            Bitmap bitmap = bitmapDrawable.getBitmap();

            OutputStream outputStream = contentResolver.openOutputStream(Objects.requireNonNull(imageUri));
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
            Objects.requireNonNull(outputStream);

            Toast.makeText(getActivity(), "Image Saved Successfully", Toast.LENGTH_SHORT).show();

        }catch (Exception e){
            Toast.makeText(getActivity(), "Image Saved Fail", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }
}