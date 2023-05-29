package hcmute.edu.vn.tlcn.attendanceapp;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.squareup.picasso.Picasso;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;

import hcmute.edu.vn.tlcn.attendanceapp.adapter.EmployeeAdapter;
import hcmute.edu.vn.tlcn.attendanceapp.adapter.FeedbackAdapter;
import hcmute.edu.vn.tlcn.attendanceapp.model.Feedback;
import hcmute.edu.vn.tlcn.attendanceapp.model.User;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ListFeedbackFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListFeedbackFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ListFeedbackFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ListFeedbackFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ListFeedbackFragment newInstance(String param1, String param2) {
        ListFeedbackFragment fragment = new ListFeedbackFragment();
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
    ImageView btnBackFeedback;
    ListView listviewFeedback;
    TextView txtNotification, labelFeedback;
    FeedbackAdapter feedbackAdapter;
    ArrayList<Feedback> arrFeedback;
    ArrayList<Feedback> result;
    SearchView searchFeedback;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_list_feedback, container, false);

        mapping();

        arrFeedback = new ArrayList<>();
        feedbackAdapter = new FeedbackAdapter(arrFeedback, getActivity(), R.layout.feedback_row);
        listviewFeedback.setAdapter(feedbackAdapter);
        putDataToView();

        btnBackFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AdminSettingsFragment adminSettingsFragment = new AdminSettingsFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flAdminFragment, adminSettingsFragment).commit();
            }
        });

        searchFeedback.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                labelFeedback.setVisibility(View.GONE);
            }
        });

        searchFeedback.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                labelFeedback.setVisibility(View.VISIBLE);
                return false;
            }
        });

        searchFeedback.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String keyword) {
                result = new ArrayList<>();
                for(Feedback f: arrFeedback){
                    if(f.getUserUUID().contains(keyword.toLowerCase())){
                        result.add(f);
                    }
                }
                ((FeedbackAdapter) listviewFeedback.getAdapter()).update(result);
                return false;
            }
        });

        listviewFeedback.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Feedback selected = arrFeedback.get(position);
                if(!selected.isSeen()){
                    selected.setSeen(true);
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference feedbackRef = database.getReference("feedback");
                    feedbackRef.child(selected.getId()).setValue(selected);
                }
                showFeedbackDialog(selected);
            }
        });

        return view;
    }

    private void putDataToView() {
        if (result != null) {
            result.clear();
            ((FeedbackAdapter) listviewFeedback.getAdapter()).update(result);
        }
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference feedbackRef = database.getReference("feedback");
        feedbackRef.orderByChild("createAt/time").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrFeedback.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Feedback feedback = dataSnapshot.getValue(Feedback.class);
                    arrFeedback.add(feedback);
                }
                Collections.reverse(arrFeedback);
                feedbackAdapter.notifyDataSetChanged();

                if (arrFeedback.size() == 0) {
                    txtNotification.setVisibility(View.VISIBLE);
                    listviewFeedback.setVisibility(View.GONE);
                } else {
                    txtNotification.setVisibility(View.INVISIBLE);
                    listviewFeedback.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void showFeedbackDialog(Feedback item){
        AlertDialog dialogFeedback;
        AlertDialog.Builder builderFeedback = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View layout_dialog = inflater.inflate(R.layout.dialog_detail_feedback, null);
        builderFeedback.setView(layout_dialog);

        ImageView btnCloseDetailFeedback = layout_dialog.findViewById(R.id.btnCloseDetailFeedback);
        TextView txtUUIDFeedback = layout_dialog.findViewById(R.id.txtUUIDFeedback);
        TextView txtNameFeedback = layout_dialog.findViewById(R.id.txtNameFeedback);
        TextView txtDetailFeedback = layout_dialog.findViewById(R.id.txtDetailFeedback);
        TextView txtLabelContact = layout_dialog.findViewById(R.id.txtLabelContact);
        TextView txtContactFeedback = layout_dialog.findViewById(R.id.txtContactFeedback);
        TextView txtCreateAt = layout_dialog.findViewById(R.id.txtCreateAt);
        ImageView img1Feedback = layout_dialog.findViewById(R.id.img1Feedback);
        ImageView img2Feedback = layout_dialog.findViewById(R.id.img2Feedback);

        //get data
        ArrayList<String> arrImg = item.getImages();
        if(arrImg != null) {
            for (int i = 0; i < arrImg.size(); i++) {
                Picasso.get().load(Uri.parse(arrImg.get(i))).fit().centerCrop().into(img1Feedback);
                img2Feedback.setVisibility(View.GONE);
                if(arrImg.size() == 2) {
                    img2Feedback.setVisibility(View.VISIBLE);
                    Picasso.get().load(Uri.parse(arrImg.get(1))).fit().centerCrop().into(img2Feedback);
                }

            }
        }
        else{
            img1Feedback.setVisibility(View.GONE);
            img2Feedback.setVisibility(View.GONE);
        }
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users");
        myRef.child(item.getUserUUID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    txtNameFeedback.setText(user.getFullName());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        txtUUIDFeedback.setText(item.getUserUUID());

        txtDetailFeedback.setText(item.getDetail());
        if(item.getContact().equals("")){
            txtContactFeedback.setVisibility(View.GONE);
            txtLabelContact.setVisibility(View.GONE);
        }
        else {
            txtContactFeedback.setText(item.getContact());
        }
        Date createAt = item.getCreateAt();
        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MMM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
        String date = dayFormat.format(createAt) + " at " + timeFormat.format(createAt);
        txtCreateAt.setText(date);

        dialogFeedback = builderFeedback.create();
        dialogFeedback.getWindow().setGravity(Gravity.CENTER);
        dialogFeedback.setCancelable(false);
        dialogFeedback.show();

        btnCloseDetailFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogFeedback.dismiss();
            }
        });

        img1Feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ZoomImageDialog(img1Feedback.getDrawable(), item.getUserUUID()+"_1");
            }
        });

        img2Feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ZoomImageDialog(img2Feedback.getDrawable(), item.getUserUUID()+"_2");
            }
        });
    }

    private void ZoomImageDialog(Drawable d, String url){
        Rect displayRectangle = new Rect();
        Window window = getActivity().getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);

        AlertDialog dialogZoomImage;
        AlertDialog.Builder builderZoomImage = new AlertDialog.Builder(getContext(), android.R.style.Theme_NoTitleBar_Fullscreen);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View layout = inflater.inflate(R.layout.dialog_zoom_image, null);
        layout.setMinimumWidth((int)(displayRectangle.width() * 1f));
        layout.setMinimumHeight((int)(displayRectangle.height() * 1f));

        builderZoomImage.setView(layout);

        ImageView imgBack = layout.findViewById(R.id.imgBack);
        ImageView btnDownload = layout.findViewById(R.id.btnDownload);
        ImageView zoomImage = layout.findViewById(R.id.zoomImage);

        zoomImage.setImageDrawable(d);

        dialogZoomImage = builderZoomImage.create();
        dialogZoomImage.getWindow().setGravity(Gravity.CENTER);
        dialogZoomImage.setCancelable(false);
        dialogZoomImage.show();

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogZoomImage.dismiss();
            }
        });

        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String imageName = url + "_" + System.currentTimeMillis();

                Uri imageCollection;
                ContentResolver contentResolver = getActivity().getContentResolver();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    imageCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                } else {
                    imageCollection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                }

                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, imageName + ".jpg");
                contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/*");
                Uri imageUri = contentResolver.insert(imageCollection, contentValues);

                try {
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) zoomImage.getDrawable();
                    Bitmap bitmap = bitmapDrawable.getBitmap();

                    OutputStream outputStream = contentResolver.openOutputStream(Objects.requireNonNull(imageUri));
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    Objects.requireNonNull(outputStream);

                    Toast.makeText(getActivity(), "Image Saved Successfully", Toast.LENGTH_SHORT).show();

                } catch (Exception e) {
                    Toast.makeText(getActivity(), "Image Saved Fail", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
    }

    private void mapping() {
        btnBackFeedback = (ImageView) view.findViewById(R.id.btnBackFeedback);
        listviewFeedback = (ListView) view.findViewById(R.id.listviewFeedback);
        txtNotification = (TextView) view.findViewById(R.id.txtNotification);
        labelFeedback = (TextView) view.findViewById(R.id.labelFeedback);
        searchFeedback= (SearchView) view.findViewById(R.id.searchFeedback);
    }
}