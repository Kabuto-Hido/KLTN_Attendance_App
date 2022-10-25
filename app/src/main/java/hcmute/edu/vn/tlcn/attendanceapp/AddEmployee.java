package hcmute.edu.vn.tlcn.attendanceapp;


import static android.Manifest.permission.CAMERA;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import at.favre.lib.crypto.bcrypt.BCrypt;
import de.hdodenhof.circleimageview.CircleImageView;
import hcmute.edu.vn.tlcn.attendanceapp.model.User;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddEmployee#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddEmployee extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AddEmployee() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddEmployee.
     */
    // TODO: Rename and change types and number of parameters
    public static AddEmployee newInstance(String param1, String param2) {
        AddEmployee fragment = new AddEmployee();
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
    CircleImageView imgAvatarProfile;
    EditText edtPhonenum, edtName, edtBirthday1, edtPassword1;
    Button btnAdd;
    TextView btnCancelAdd;
    RadioButton checkMale, checkFemale;
    private final int PICK_IMAGE_REQUEST = 22;
    private boolean isCamera = true;
    private Uri filePath;
    private byte[] byteArray;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_add_employee, container, false);

        mapping();

        checkMale.setChecked(true);

        btnCancelAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Manage_Emp_Fragment manage_emp_fragment = new Manage_Emp_Fragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, manage_emp_fragment).commit();
            }
        });

        edtBirthday1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int date = calendar.get(Calendar.DATE);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(year,month,dayOfMonth);
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                        edtBirthday1.setText(format.format(calendar.getTime()));
                    }
                },year,month,date);
                datePickerDialog.show();
            }
        });

        checkMale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkFemale.setChecked(false);
            }
        });

        checkFemale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkMale.setChecked(false);
            }
        });

        imgAvatarProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(getActivity());
                dialog.setContentView(R.layout.dialog_image_picker);
                dialog.show();

                ImageView photo = dialog.findViewById(R.id.photo);
                ImageView camera = dialog.findViewById(R.id.camera);

                photo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isCamera = false;
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

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = edtPhonenum.getText().toString();
                String fullName = edtName.getText().toString();
                String birthday = edtBirthday1.getText().toString();
                String password = edtPassword1.getText().toString();
                boolean sex = !checkFemale.isChecked();

                if(validate(phoneNumber,fullName,birthday,password)) {
                    //hash password
                    String hashPass = BCrypt.withDefaults().hashToString(12, password.toCharArray());

                    ProgressDialog progressDialog = new ProgressDialog(getActivity());
                    progressDialog.setTitle("Checking...");
                    progressDialog.setMessage("Please wait");
                    progressDialog.show();

                    User user = new User(fullName, phoneNumber, hashPass, birthday, "", sex, "", 1);

                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference ref = storage.getReference();
                    if(filePath == null){
                        filePath = Uri.parse("android.resource://"+ R.class.getPackage().getName()+"/"+R.drawable.man_placeholder);
                    }
                    UploadTask uploadTask;
                    if(isCamera){
                        uploadTask = ref.child("images/" + user.getPhone() + "_avatar").putBytes(byteArray);
                    }
                    else {
                        uploadTask = ref.child("images/" + user.getPhone() + "_avatar").putFile(filePath);
                    }
                    uploadTask
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    DatabaseReference userRef = database.getReference("users");

                                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(snapshot.hasChild(user.getPhone())) {
                                                Toast.makeText(getContext(), "Phone number is already taken !", Toast.LENGTH_SHORT).show();
                                                progressDialog.dismiss();
                                            }
                                            else{
                                                user.setAvatar("images/" + user.getPhone() + "_avatar");
                                                userRef.child(user.getPhone()).setValue(user);
                                                progressDialog.dismiss();
                                                Toast.makeText(getContext(), "New employee added", Toast.LENGTH_SHORT).show();

                                                //return list emp
                                                Manage_Emp_Fragment manage_emp_fragment = new Manage_Emp_Fragment();
                                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, manage_emp_fragment).commit();

                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                }
                            });


                }
            }
        });

        return view;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK
                && data != null && data.getData() != null) {
            filePath = data.getData();
            imgAvatarProfile.setImageURI(filePath);
        }
        else if(requestCode == 0 && resultCode == getActivity().RESULT_OK
                && data != null){
            Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            selectedImage.compress(Bitmap.CompressFormat.PNG,100,stream);
            byteArray = stream.toByteArray();

            Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.length);
            imgAvatarProfile.setImageBitmap(bitmap);

        }
    }

    private void mapping(){
        imgAvatarProfile = (CircleImageView) view.findViewById(R.id.imgAvatarProfile);
        edtPhonenum = (EditText) view.findViewById(R.id.edtPhonenum);
        edtName = (EditText) view.findViewById(R.id.edtName);
        edtBirthday1 = (EditText) view.findViewById(R.id.edtBirthday1);
        edtPassword1 = (EditText) view.findViewById(R.id.edtPassword1);
        btnAdd = (Button) view.findViewById(R.id.btnAdd);
        btnCancelAdd = (TextView) view.findViewById(R.id.btnCancelAdd);
        checkMale = (RadioButton) view.findViewById(R.id.checkMale);
        checkFemale = (RadioButton) view.findViewById(R.id.checkFemale);
    }

    private boolean validate(String phoneNumber,String fullName,String birthday,String password){
        if(phoneNumber.equals("") || fullName.equals("") || birthday.equals("") || password.equals("")) {
            Toast.makeText(getActivity(), "Invalid input !", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(phoneNumber.length()!=10){
            Toast.makeText(getActivity(), "Invalid phone !", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(password.length() < 6){
            Toast.makeText(getActivity(), "Password too weak !", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
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
        switch (requestCode) {
            case REQUEST_AUDIO_PERMISSION_CODE:
                if (grantResults.length > 0) {
                    boolean permissionToCamera = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (permissionToCamera) {
                        Toast.makeText(getActivity().getApplicationContext(), "Permission Granted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getActivity().getApplicationContext(), "Permission Denied", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }
}