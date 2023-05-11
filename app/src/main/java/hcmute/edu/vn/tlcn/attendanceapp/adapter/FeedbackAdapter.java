package hcmute.edu.vn.tlcn.attendanceapp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import hcmute.edu.vn.tlcn.attendanceapp.R;
import hcmute.edu.vn.tlcn.attendanceapp.model.Feedback;
import hcmute.edu.vn.tlcn.attendanceapp.model.User;

public class FeedbackAdapter extends BaseAdapter {
    private ArrayList<Feedback> feedbackArrayList;
    private Context context;
    private int layout;

    public FeedbackAdapter(ArrayList<Feedback> feedbackArrayList, Context context, int layout) {
        this.feedbackArrayList = feedbackArrayList;
        this.context = context;
        this.layout = layout;
    }

    public void update(ArrayList<Feedback> result){
        feedbackArrayList = new ArrayList<>();
        feedbackArrayList.addAll(result);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return feedbackArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    private class ViewHolder{
        CircleImageView empFeedbackImg;
        TextView txtEmpUUID, txtDetail, txtDate;
        ConstraintLayout backgroundRow;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null){
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layout,null);

            holder.empFeedbackImg = (CircleImageView) convertView.findViewById(R.id.empFeedbackImg);
            holder.txtEmpUUID = (TextView) convertView.findViewById(R.id.txtEmpUUID);
            holder.txtDetail = (TextView) convertView.findViewById(R.id.txtDetail);
            holder.txtDate = (TextView) convertView.findViewById(R.id.txtDate);
            holder.backgroundRow = (ConstraintLayout) convertView.findViewById(R.id.backgroundRow);

            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder) convertView.getTag();
        }
        Feedback feedback = feedbackArrayList.get(position);
        holder.txtEmpUUID.setText(feedback.getUserUUID());
        holder.txtDetail.setText(feedback.getDetail());

        Date createAt = feedback.getCreateAt();
        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MMM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
        String date = dayFormat.format(createAt) + " at " + timeFormat.format(createAt);
        holder.txtDate.setText(date);

        if(!feedback.isSeen()){
            holder.backgroundRow.setBackgroundColor(Color.parseColor("#E7F3FF"));
        }
        else{
            holder.backgroundRow.setBackgroundColor(context.getResources().getColor(R.color.white));
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users");
        myRef.child(feedback.getUserUUID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);

                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageReference = storage.getReference(user.getAvatar());
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.get().load(uri).fit().centerCrop().into(holder.empFeedbackImg);
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

        return convertView;
    }
}
