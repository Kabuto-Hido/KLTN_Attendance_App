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
        TextView txtEmployeeCode, txtEmployeeName, totalAttend,
                totalAbsent, totalWorkedTime;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null){
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layout,null);

            holder.txtEmployeeName = (TextView) convertView.findViewById(R.id.txtEmployeeName);
            holder.txtEmployeeCode = (TextView) convertView.findViewById(R.id.txtEmployeeCode);
            holder.totalAttend = (TextView) convertView.findViewById(R.id.totalAttend);
            holder.totalAbsent = (TextView) convertView.findViewById(R.id.totalAbsent);
            holder.totalWorkedTime = (TextView) convertView.findViewById(R.id.totalWorkedTime);
            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder) convertView.getTag();
        }

        Statistic statistic = statisticArrayList.get(position);
        User user = userArrayList.get(position);

        holder.totalWorkedTime.setText(String.valueOf(statistic.getHourWorked()));
        int attend = statistic.getOnTime() + statistic.getLate();
        int absent = statistic.getAbsentWithPer() + statistic.getAbsentWithoutPer();
        holder.totalAttend.setText(String.valueOf(attend));
        holder.totalAbsent.setText(String.valueOf(absent));

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users");

        myRef.child(statistic.getUserPhone()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    holder.txtEmployeeName.setText(user.getFullName());
                    holder.txtEmployeeCode.setText(user.getUuid());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return convertView;
    }
}
