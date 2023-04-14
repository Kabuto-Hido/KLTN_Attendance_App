package hcmute.edu.vn.tlcn.attendanceapp.adapter;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

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

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import hcmute.edu.vn.tlcn.attendanceapp.R;
import hcmute.edu.vn.tlcn.attendanceapp.model.Record;
import hcmute.edu.vn.tlcn.attendanceapp.model.Statistic;
import hcmute.edu.vn.tlcn.attendanceapp.model.User;

public class TodayStatisticAdapter extends BaseAdapter {
    private ArrayList<Record> recordArrayList;
    private Context context;
    private int layout ;

    public TodayStatisticAdapter(ArrayList<Record> recordArrayList, Context context, int layout) {
        this.recordArrayList = recordArrayList;
        this.context = context;
        this.layout = layout;
    }

    public void update(ArrayList<Record> result){
        recordArrayList = new ArrayList<>();
        recordArrayList.addAll(result);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return recordArrayList.size();
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
        CircleImageView empImageView;
        TextView txtEmpName, txtStatus;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null){
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layout,null);

            holder.empImageView = (CircleImageView) convertView.findViewById(R.id.empImageView);
            holder.txtEmpName = (TextView) convertView.findViewById(R.id.txtEmpName);
            holder.txtStatus = (TextView) convertView.findViewById(R.id.txtStatus);

            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder) convertView.getTag();
        }

        Record record = recordArrayList.get(position);

        switch (record.getStatus()) {
            case "on time":
                holder.txtStatus.setTextColor(Color.parseColor("#00FF00"));
                break;
            case "late":
                holder.txtStatus.setTextColor(Color.parseColor("#ffff33"));
                break;
            case "absent without permission":
                holder.txtStatus.setTextColor(Color.parseColor("#FF3131"));
                break;
            case "absent with permission":
                holder.txtStatus.setTextColor(Color.parseColor("#00ffff"));
                break;
        }

        holder.txtStatus.setText(record.getStatus());

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users");

        myRef.child(record.getUserUUID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    holder.txtEmpName.setText(user.getFullName());

                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageReference = storage.getReference(user.getAvatar());
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.get().load(uri).fit().centerCrop().into(holder.empImageView);
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
