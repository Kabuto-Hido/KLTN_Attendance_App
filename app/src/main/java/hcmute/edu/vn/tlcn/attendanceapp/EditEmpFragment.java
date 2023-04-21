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
import android.util.Log;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EditEmpFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditEmpFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public EditEmpFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EditEmpFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EditEmpFragment newInstance(String param1, String param2) {
        EditEmpFragment fragment = new EditEmpFragment();
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
    TextView labelCodeEmp, btnCancelEdit;
    CircleImageView imgAvatarProfile;
    EditText edtPhonenum, edtName, edtBirthday1;
    RadioButton radioMale, radioFemale;
    Button btnConfirmEdit;
    private final int PICK_IMAGE_REQUEST = 22;
    private boolean isCamera = false;
    private Uri filePath;
    private byte[] byteArray;
    private Dialog dialog;
    User editUser;
    boolean isRecognizeFace = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_edit_emp, container, false);

        mapping();
        if (getArguments() != null) {
            editUser = (User) getArguments().getSerializable("edtUser");
        }

        putDataToView();

        btnCancelEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Manage_Emp_Fragment manage_emp_fragment = new Manage_Emp_Fragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flAdminFragment, manage_emp_fragment).commit();
            }
        });

        radioMale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioFemale.setChecked(false);
            }
        });

        radioFemale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioMale.setChecked(false);
            }
        });

        Calendar calendar = Calendar.getInstance();
        edtBirthday1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

        btnConfirmEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fullName = edtName.getText().toString();
                String birthday = edtBirthday1.getText().toString();
                String phone = edtPhonenum.getText().toString();
                boolean sex = !radioFemale.isChecked();

                ProgressDialog progressDialog = new ProgressDialog(getActivity());
                progressDialog.setTitle("Checking...");
                progressDialog.setMessage("Please wait");
                progressDialog.show();

                if (fullName.equals("") || birthday.equals("")) {
                    Toast.makeText(getActivity(), "Invalid input !", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    return;
                }

                if (!isRecognizeFace) {
                    Toast.makeText(getActivity(), "Please take a photo with your face !", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    return;
                }

                editUser.setFullName(fullName);
                editUser.setBirthday(birthday);
                editUser.setSex(sex);

                if(!phone.equals("")){
                    if (phone.length() != 10 || !phone.matches(getString(R.string.regexPhone))) {
                        edtPhonenum.setError("Invalid phone !");
                        return;
                    }
                    editUser.setPhone(phone);
                }

                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference ref = storage.getReference();

                UploadTask uploadTask;
                if (imgAvatarProfile.isSelected()) {
                    if (filePath == null) {
                        Bitmap bitmap = ((BitmapDrawable) imgAvatarProfile.getDrawable()).getBitmap();
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                        byte[] imageInByte = stream.toByteArray();

                        uploadTask = ref.child("images/" + editUser.getUuid()+ "_avatar").putBytes(imageInByte);
                    } else if (isCamera) {
                        uploadTask = ref.child("images/" + editUser.getUuid() + "_avatar").putBytes(byteArray);
                    } else {
                        uploadTask = ref.child("images/" + editUser.getUuid() + "_avatar").putFile(filePath);
                    }


                } else {
                    Bitmap bitmap = ((BitmapDrawable) imgAvatarProfile.getDrawable()).getBitmap();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byte[] imageInByte = stream.toByteArray();

                    uploadTask = ref.child("images/" + editUser.getUuid() + "_avatar").putBytes(imageInByte);
                }

                uploadTask
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference userRef = database.getReference("users");
                                userRef.child(editUser.getUuid()).setValue(editUser);

                                progressDialog.dismiss();
                                Toast.makeText(getContext(), "Update Successful", Toast.LENGTH_SHORT).show();
                                putDataToView();
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

    private void putDataToView() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference(editUser.getAvatar());
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).fit().centerCrop().into(imgAvatarProfile);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("TAG", "onFailure: " + e.getMessage());
            }
        });

        labelCodeEmp.setText(editUser.getUuid());
        edtName.setText(editUser.getFullName());
        edtBirthday1.setText(editUser.getBirthday());
        String getPhone = editUser.getPhone();
        String phone = "+84" + getPhone.substring(1);
        edtPhonenum.setText(phone);
        //edtPhonenum.setBackgroundColor(Color.parseColor("#D9D9D9"));
        if (editUser.getSex()) {
            radioMale.setChecked(true);
        } else {
            radioFemale.setChecked(true);
        }

    }

    private void mapping() {
        labelCodeEmp = (TextView) view.findViewById(R.id.labelCodeEmp);
        btnCancelEdit = (TextView) view.findViewById(R.id.btnCancelEdit);
        imgAvatarProfile = (CircleImageView) view.findViewById(R.id.imgAvatarProfile);
        edtPhonenum = (EditText) view.findViewById(R.id.edtPhonenum);
        edtName = (EditText) view.findViewById(R.id.edtName);
        edtBirthday1 = (EditText) view.findViewById(R.id.edtBirthday1);
        radioMale = (RadioButton) view.findViewById(R.id.radioMale);
        radioFemale = (RadioButton) view.findViewById(R.id.radioFemale);
        btnConfirmEdit = (Button) view.findViewById(R.id.btnConfirmEdit);
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

            //detect face
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

}