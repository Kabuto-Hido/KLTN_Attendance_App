package hcmute.edu.vn.tlcn.attendanceapp;


import static android.Manifest.permission.CAMERA;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private boolean isCamera = false;
    private Uri filePath;
    private byte[] byteArray;
    private Dialog dialog;
    boolean isRecognizeFace = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_add_employee, container, false);

        mapping();

        checkMale.setChecked(true);
        edtPassword1.setBackgroundColor(Color.parseColor("#D9D9D9"));

        btnCancelAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Manage_Emp_Fragment manage_emp_fragment = new Manage_Emp_Fragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flAdminFragment, manage_emp_fragment).commit();
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
                        calendar.set(year, month, dayOfMonth);
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                        edtBirthday1.setText(format.format(calendar.getTime()));
                    }
                }, year, month, date);
                datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
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
                        startActivityForResult(Intent.createChooser(intent, "Select image..."), PICK_IMAGE_REQUEST);
                    }
                });

                camera.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (CheckPermissions()) {
                            Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(takePicture, 0);
                        } else {
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

                ProgressDialog progressDialog = new ProgressDialog(getActivity());
                progressDialog.setTitle("Checking...");
                progressDialog.setMessage("Please wait");
                progressDialog.show();

                if (validate(phoneNumber, fullName, birthday, password, isRecognizeFace)) {
                    //hash password
                    String hashPass = BCrypt.withDefaults().hashToString(12, password.toCharArray());

                    User user = new User("", fullName, phoneNumber, hashPass, birthday, "", sex, "", 1, "");

                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference ref = storage.getReference();
                    if (filePath == null) {
                        filePath = Uri.parse("android.resource://" + R.class.getPackage().getName() + "/" + R.drawable.man_placeholder);
                    }
                    UploadTask uploadTask;
                    if (isCamera) {
                        uploadTask = ref.child("images/" + user.getPhone() + "_avatar").putBytes(byteArray);
                    } else {
                        uploadTask = ref.child("images/" + user.getPhone() + "_avatar").putFile(filePath);
                    }
                    uploadTask
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    DatabaseReference userRef = database.getReference("users");

                                    userRef.orderByChild("phone").equalTo(phoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                Toast.makeText(getContext(), "Phone number is already taken !", Toast.LENGTH_SHORT).show();
                                                progressDialog.dismiss();
                                            } else {
                                                userRef.orderByKey().limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                                            User latestUser = dataSnapshot.getValue(User.class);
                                                            if (latestUser != null) {
                                                                String getNum = latestUser.getUuid().substring(3, 8);
                                                                user.setUuid("ATD" + increaseOneUnit(getNum));
                                                            }
                                                            user.setAvatar("images/" + user.getUuid() + "_avatar");
                                                            userRef.child(user.getUuid()).setValue(user);

                                                            progressDialog.dismiss();
                                                            Toast.makeText(getContext(), "New employee added", Toast.LENGTH_SHORT).show();

                                                            //return list emp
                                                            Manage_Emp_Fragment manage_emp_fragment = new Manage_Emp_Fragment();
                                                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flAdminFragment, manage_emp_fragment).commit();
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });


                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                        }
                                    });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }
                            });


                } else {
                    progressDialog.dismiss();
                }
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK
                && data != null && data.getData() != null) {
            isCamera = false;
            filePath = data.getData();
            imgAvatarProfile.setImageURI(filePath);

            BitmapDrawable drawable = (BitmapDrawable) imgAvatarProfile.getDrawable();
            Bitmap bitmapOrigin = Bitmap.createBitmap(drawable.getBitmap());
            //check face
            face_detector(bitmapOrigin);

            dialog.dismiss();
        } else if (requestCode == 0 && resultCode == getActivity().RESULT_OK
                && data != null) {
            isCamera = true;
            Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            selectedImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byteArray = stream.toByteArray();

            //check face
            face_detector(selectedImage);

            Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            imgAvatarProfile.setImageBitmap(bitmap);
            dialog.dismiss();
        }
    }

    public void face_detector(Bitmap bitmap) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        FaceDetector detector = FaceDetection.getClient();
        detector.process(image)
                .addOnSuccessListener(new OnSuccessListener<List<Face>>() {
                    @Override
                    public void onSuccess(List<Face> faces) {
                        if (faces.size() != 0) {
                            isRecognizeFace = true;
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void mapping() {
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

    private boolean validate(String phoneNumber, String fullName, String birthday, String password, Boolean isFace) {
        if (fullName.equals("") || birthday.equals("") || password.equals("")) {
            Toast.makeText(getActivity(), "Invalid input !", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(!phoneNumber.equals("")) {
            if (phoneNumber.length() != 10 || !phoneNumber.matches(getString(R.string.regexPhone))) {
                edtPhonenum.setError("Invalid phone !");
                return false;
            }
        }

        if (!isValidPassword(password)) {
            edtPassword1.setError("Password must contain at least 8 characters, one digit, one upper case alphabet and one lower case alphabet!");
            return false;
        }

        if (!isFace) {
            Toast.makeText(getActivity(), "Please take a photo with your face !", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public boolean isValidPassword(String password) {
        //password must containing at least 8 characters and at most 20 characters,
        // containing at least one digit, one upper case alphabet and one lower case alphabet1
        String regex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,20}$";
        Pattern p = Pattern.compile(regex);
        if (password == null) {
            return false;
        }
        Matcher m = p.matcher(password);
        return m.matches();
    }

    private String increaseOneUnit(String num) {
        int n = Integer.parseInt(num) + 1;
        StringBuilder kq = new StringBuilder(String.valueOf(n));
        int len = kq.length();
        if (len != 5) {
            for (int i = 0; i < (5 - len); i++) {
                kq.insert(0, 0);
            }
        }
        return String.valueOf(kq);
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