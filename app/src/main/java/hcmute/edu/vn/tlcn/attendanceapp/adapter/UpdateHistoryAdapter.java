package hcmute.edu.vn.tlcn.attendanceapp.adapter;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
import hcmute.edu.vn.tlcn.attendanceapp.model.UpdateHistory;
import hcmute.edu.vn.tlcn.attendanceapp.model.User;

public class UpdateHistoryAdapter extends BaseAdapter {
    private ArrayList<UpdateHistory> feedbackArrayList;
    private Context context;
    private int layout;

    public UpdateHistoryAdapter(ArrayList<UpdateHistory> feedbackArrayList, Context context, int layout) {
        this.feedbackArrayList = feedbackArrayList;
        this.context = context;
        this.layout = layout;
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
        TextView txtPerformer, txtPerformDate;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null){
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layout,null);

            holder.txtPerformer = (TextView) convertView.findViewById(R.id.txtPerformer);
            holder.txtPerformDate = (TextView) convertView.findViewById(R.id.txtPerformDate);

            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder) convertView.getTag();
        }

        UpdateHistory history = feedbackArrayList.get(position);

        Date createAt = history.getImplDate();
        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MMM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
        String date = dayFormat.format(createAt) + " at " + timeFormat.format(createAt);
        holder.txtPerformDate.setText(date);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users");
        myRef.child(history.getPerformer()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    holder.txtPerformer.setText(user.getFullName());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return convertView;
    }
}
