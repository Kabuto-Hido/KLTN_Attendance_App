package hcmute.edu.vn.tlcn.attendanceapp;

import static android.Manifest.permission.CAMERA;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
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
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import hcmute.edu.vn.tlcn.attendanceapp.model.User;
import hcmute.edu.vn.tlcn.attendanceapp.pattern.User_singeton;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileInformationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileInformationFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileInformationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileInformationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileInformationFragment newInstance(String param1, String param2) {
        ProfileInformationFragment fragment = new ProfileInformationFragment();
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

    View view;
    ImageView btnBackProfile;
    Button btn_update;
    CircleImageView imgProfile;
    RadioButton rMale, rFemale;
    EditText edittext_name, edittext_phone, edittext_description, edittext_birthday;
    User_singeton user_singeton = User_singeton.getInstance();
    User user;
    private final int PICK_IMAGE_REQUEST = 22;
    private boolean isCamera = false;
    private Uri filePath;
    private byte[] byteArray;
    private Dialog dialog;
    boolean isRecognizeFace = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile_information, container, false);

        mapping();

        user = user_singeton.getUser();

        putDataToView();

        btnBackProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AdminSettingsFragment adminSettingsFragment = new AdminSettingsFragment();
                SettingsFragment settingsFragment = new SettingsFragment();
                if(user.getRole() == 0) {
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flAdminFragment, adminSettingsFragment).commit();
                }
                else{
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, settingsFragment).commit();

                }
            }
        });

        rMale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rFemale.setChecked(false);
            }
        });

        rFemale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rMale.setChecked(false);
            }
        });

        Calendar calendar = Calendar.getInstance();
        edittext_birthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int date = calendar.get(Calendar.DATE);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(year,month,dayOfMonth);
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                        edittext_birthday.setText(format.format(calendar.getTime()));
                    }
                },year,month,date);
                datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                datePickerDialog.show();
            }
        });

        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = new Dialog(getActivity());
                dialog.setContentView(R.layout.dialog_image_picker);
                dialog.show();

                ImageView photo = dialog.findViewById(R.id.photo);
                ImageView camera = dialog.findViewById(R.id.camera);

                photo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent,"Select image..."), PICK_IMAGE_REQUEST);
                    }
                });

                camera.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(CheckPermissions()) {
                            Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(takePicture, 0);
                        }else {
                            RequestPermissions();
                        }
                    }
                });
            }
        });

        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fullName = edittext_name.getText().toString();
                String birthday = edittext_birthday.getText().toString();
                String description = edittext_description.getText().toString();
                boolean sex = !rFemale.isChecked();

                ProgressDialog progressDialog = new ProgressDialog(getActivity());
                progressDialog.setTitle("Checking...");
                progressDialog.setMessage("Please wait");
                progressDialog.show();

                if(fullName.equals("") || birthday.equals("")) {
                    Toast.makeText(getActivity(), "Invalid input !", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    return;
                }

                if(!isRecognizeFace){
                    Toast.makeText(getActivity(), "Please take a photo with your face !", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    return;
                }

                user.setFullName(fullName);
                user.setBirthday(birthday);
                user.setDescription(description);
                user.setSex(sex);

                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference ref = storage.getReference();

                UploadTask uploadTask;
                if(imgProfile.isSelected()){
                    if(filePath == null){
                        Bitmap bitmap = ((BitmapDrawable) imgProfile.getDrawable()).getBitmap();
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                        byte[] imageInByte = stream.toByteArray();

                        uploadTask = ref.child("images/" + user.getPhone() + "_avatar").putBytes(imageInByte);
                    }

                    else if(isCamera){
                        uploadTask = ref.child("images/" + user.getPhone() + "_avatar").putBytes(byteArray);
                    }
                    else {
                        uploadTask = ref.child("images/" + user.getPhone() + "_avatar").putFile(filePath);
                    }

                }
                else{
                    Bitmap bitmap = ((BitmapDrawable) imgProfile.getDrawable()).getBitmap();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byte[] imageInByte = stream.toByteArray();

                    uploadTask = ref.child("images/" + user.getPhone() + "_avatar").putBytes(imageInByte);
                }

                uploadTask
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference userRef = database.getReference("users");

                                userRef.child(user.getPhone()).setValue(user);
                                user_singeton.setUser(user);

                                putDataToView();
                                progressDialog.dismiss();
                                Toast.makeText(getActivity(), "Update successful.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK
                && data != null && data.getData() != null) {
            isCamera = false;
            filePath = data.getData();
            imgProfile.setImageURI(filePath);

            BitmapDrawable drawable = (BitmapDrawable) imgProfile.getDrawable();
            Bitmap bitmapOrigin = Bitmap.createBitmap( drawable.getBitmap());
            //check face
            face_detector(bitmapOrigin);

            dialog.dismiss();
        }
        else if(requestCode == 0 && resultCode == getActivity().RESULT_OK
                && data != null){
            isCamera = true;
            Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            selectedImage.compress(Bitmap.CompressFormat.PNG,100,stream);
            byteArray = stream.toByteArray();

            //check face
            face_detector(selectedImage);

            Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.length);
            imgProfile.setImageBitmap(bitmap);
            dialog.dismiss();

        }
    }

    public void face_detector(Bitmap bitmap){
        InputImage image = InputImage.fromBitmap(bitmap,0);
        FaceDetector detector = FaceDetection.getClient();
        detector.process(image)
                .addOnSuccessListener(new OnSuccessListener<List<Face>>() {
                    @Override
                    public void onSuccess(List<Face> faces) {
                        if(faces.size() != 0){
                            isRecognizeFace = true;
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void putDataToView(){
        if(user == null) {
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        }

        edittext_name.setText(user.getFullName());
        edittext_description.setText(user.getDescription());
        edittext_birthday.setText(user.getBirthday());
        String getPhone = user.getPhone();
        String phone = "+84" + getPhone.substring(1);
        edittext_phone.setText(phone);
        if(user.getSex()){
            rMale.setChecked(true);
        }
        else{
            rFemale.setChecked(true);
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference(user.getAvatar());
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).fit().centerCrop().into(imgProfile);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("TAG", "onFailure: " + e.getMessage());
            }
        });

    }

    private void mapping(){
        btnBackProfile = (ImageView) view.findViewById(R.id.btnBackProfile);
        imgProfile = (CircleImageView) view.findViewById(R.id.imgProfile);
        rMale = (RadioButton) view.findViewById(R.id.rMale);
        rFemale = (RadioButton) view.findViewById(R.id.rFemale);
        edittext_name = (EditText) view.findViewById(R.id.edittext_name);
        edittext_phone = (EditText) view.findViewById(R.id.edittext_phone);
        edittext_description = (EditText) view.findViewById(R.id.edittext_description);
        edittext_birthday = (EditText) view.findViewById(R.id.edittext_birthday);
        btn_update = (Button) view.findViewById(R.id.btn_update);
    }

    public static final int REQUEST_AUDIO_PERMISSION_CODE = 1;
    public boolean CheckPermissions() {
        int result = ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), CAMERA);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void RequestPermissions() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{CAMERA}, REQUEST_AUDIO_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_AUDIO_PERMISSION_CODE) {
            if (grantResults.length > 0) {
                boolean permissionToCamera = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (permissionToCamera) {
                    Toast.makeText(getActivity().getApplicationContext(), "Permission Granted", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "Permission Denied", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}