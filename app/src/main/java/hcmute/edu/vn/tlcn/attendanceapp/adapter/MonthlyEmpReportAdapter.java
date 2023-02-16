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
import hcmute.edu.vn.tlcn.attendanceapp.model.Statistic;
import hcmute.edu.vn.tlcn.attendanceapp.model.User;

public class MonthlyEmpReportAdapter extends BaseAdapter {
    private ArrayList<User> userArrayList;
    private ArrayList<Statistic> statisticArrayList;
    private Context context;
    private int layout ;

    public MonthlyEmpReportAdapter(ArrayList<User> userArrayList,
                                   ArrayList<Statistic> statisticArrayList, Context context, int layout) {
        this.userArrayList = userArrayList;
        this.statisticArrayList = statisticArrayList;
        this.context = context;
        this.layout = layout;
    }

    @Override
    public int getCount() {
        return statisticArrayList.size();
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
        CircleImageView empImg;
        TextView txtEmployeeName, totalOnTime, totalLate, totalAbsentWithPer, totalAbsentWithoutPer;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null){
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layout,null);

            holder.empImg = (CircleImageView) convertView.findViewById(R.id.empImg);
            holder.txtEmployeeName = (TextView) convertView.findViewById(R.id.txtEmployeeName);
            holder.totalOnTime = (TextView) convertView.findViewById(R.id.totalOnTime);
            holder.totalLate = (TextView) convertView.findViewById(R.id.totalLate);
            holder.totalAbsentWithPer = (TextView) convertView.findViewById(R.id.totalAbsentWithPer);
            holder.totalAbsentWithoutPer = (TextView) convertView.findViewById(R.id.totalAbsentWithoutPer);
            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder) convertView.getTag();
        }

        Statistic statistic = statisticArrayList.get(position);
        User user = userArrayList.get(position);

        holder.totalOnTime.setText(String.valueOf(statistic.getOnTime()));
        holder.totalLate.setText(String.valueOf(statistic.getLate()));
        holder.totalAbsentWithPer.setText(String.valueOf(statistic.getAbsentWithPer()));
        holder.totalAbsentWithoutPer.setText(String.valueOf(statistic.getAbsentWithoutPer()));

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users");

        myRef.child(statistic.getUserPhone()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    holder.txtEmployeeName.setText(user.getFullName());

                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageReference = storage.getReference(user.getAvatar());
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.get().load(uri).fit().centerCrop().into(holder.empImg);
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
