package hcmute.edu.vn.tlcn.attendanceapp;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.mlkit.vision.common.InputImage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import hcmute.edu.vn.tlcn.attendanceapp.adapter.ResignationAdapter;
import hcmute.edu.vn.tlcn.attendanceapp.model.Feedback;
import hcmute.edu.vn.tlcn.attendanceapp.model.User;
import hcmute.edu.vn.tlcn.attendanceapp.pattern.User_singeton;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BlankFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
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
    TextView txtProfile, txtChangePassword, txtLogOut, txtDay_off, txtQRCode, txtFeedback;
    User_singeton user_singeton;
    User user;
    SharedPreferences sharedPreferences;
    private final int PICK_IMAGE1_REQUEST = 25;
    private final int PICK_IMAGE2_REQUEST = 26;
    ViewHolder holder;
    Uri filePath1;
    Uri filePath2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_settings, container, false);

        mapping();

        user_singeton = User_singeton.getInstance();
        user = user_singeton.getUser();

        if (user == null) {
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        }

        txtProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileInformationFragment profileInformationFragment = new ProfileInformationFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, profileInformationFragment).commit();
            }
        });

        txtDay_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestADayOffFragment aDayOffFragment = new RequestADayOffFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, aDayOffFragment).commit();
            }
        });

        txtChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangePasswordFragment changePasswordFragment = new ChangePasswordFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, changePasswordFragment).commit();
            }
        });

        txtLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user_singeton = User_singeton.getInstance();
                user_singeton.setUser(null);
                user_singeton = null;

                sharedPreferences = getActivity().getSharedPreferences("isVerifyOtp", Context.MODE_MULTI_PROCESS);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove("otp");
                editor.apply();

                startActivity(new Intent(getActivity(), LoginActivity.class));
                getActivity().finish();
            }
        });

        txtQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QRCodeFragment qrCodeFragment = new QRCodeFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, qrCodeFragment).commit();

            }
        });

        txtFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });

        return view;
    }

    private static class ViewHolder{
        EditText edtDetails, edtContact;
        ImageView img1,img2, deleteImg1, deleteImg2;
        Button btn_cancelFeedback, btn_sendFeedback;
    }

    private void showDialog(){
        holder = new ViewHolder();
        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View layout_dialog = inflater.inflate(R.layout.dialog_send_feedback, null);
        layout_dialog.setTag(holder);
        builder.setView(layout_dialog);

        holder.edtDetails = layout_dialog.findViewById(R.id.edtDetails);
        holder.img1 = layout_dialog.findViewById(R.id.img1);
        holder.img2 = layout_dialog.findViewById(R.id.img2);
        holder.deleteImg1 = layout_dialog.findViewById(R.id.deleteImg1);
        holder.deleteImg2 = layout_dialog.findViewById(R.id.deleteImg2);
        holder.edtContact = layout_dialog.findViewById(R.id.edtContact);
        holder.btn_cancelFeedback = layout_dialog.findViewById(R.id.btn_cancelFeedback);
        holder.btn_sendFeedback = layout_dialog.findViewById(R.id.btn_sendFeedback);

        dialog = builder.create();
        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.setCancelable(false);
        dialog.show();

        holder.img1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select image..."), PICK_IMAGE1_REQUEST);
            }
        });

        holder.img2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select image..."), PICK_IMAGE2_REQUEST);
            }
        });

        holder.deleteImg1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.img1.setImageResource(R.drawable.ic_add_image);
                filePath1 = null;
                holder.deleteImg1.setVisibility(View.GONE);
            }
        });

        holder.deleteImg2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.img2.setImageResource(R.drawable.ic_add_image);
                filePath2 = null;
                holder.deleteImg2.setVisibility(View.GONE);
            }
        });

        holder.edtDetails.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                holder.btn_sendFeedback.setEnabled(holder.edtDetails.getText().toString().length() != 0);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        holder.btn_cancelFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        holder.btn_sendFeedback.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onClick(View v) {
                String details = holder.edtDetails.getText().toString();
                String contact = "";
                if(holder.edtContact.getText().toString().length() != 0){
                    contact = holder.edtContact.getText().toString();
                }

                final String fbId = UUID.randomUUID().toString();
                Feedback newFeedback = new Feedback(fbId,user.getUuid(), details, contact);
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference feedbackRef = database.getReference("feedback");


                ArrayList<Uri> listUri =  new ArrayList<>();
                ArrayList<String> imgs = new ArrayList<>();
                if(filePath1 != null){
                    listUri.add(filePath1);
                }
                if(filePath2 != null){
                    listUri.add(filePath2);
                }

                if(listUri.size() != 0) {
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference ref = storage.getReference();
                    for (int i = 0; i < listUri.size(); i++) {
                        Uri individualImg = listUri.get(i);
                        int pos = i + 1;
                        ref.child("images/feedback/" + fbId + "_" + pos).putFile(individualImg);

                        imgs.add("images/feedback/" + fbId + "_" + pos);
                    }
                    newFeedback.setImages(imgs);
                }
                feedbackRef.child(fbId).setValue(newFeedback);

                filePath1 = null;
                filePath2 = null;

                Toast.makeText(getActivity(), "Thank you sent us feedback!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE1_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            filePath1 = data.getData();
            holder.img1.setImageURI(filePath1);
            holder.deleteImg1.setVisibility(View.VISIBLE);
        }
        else if (requestCode == PICK_IMAGE2_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            filePath2 = data.getData();
            holder.img2.setImageURI(filePath2);
            holder.deleteImg2.setVisibility(View.VISIBLE);
        }
    }

    private void mapping() {
        txtProfile = (TextView) view.findViewById(R.id.txtProfile);
        txtChangePassword = (TextView) view.findViewById(R.id.txtChangePassword);
        txtLogOut = (TextView) view.findViewById(R.id.txtLogOut);
        txtDay_off = (TextView) view.findViewById(R.id.txtDay_off);
        txtQRCode = (TextView) view.findViewById(R.id.txtQRCode);
        txtFeedback = (TextView) view.findViewById(R.id.txtFeedback);
    }
}