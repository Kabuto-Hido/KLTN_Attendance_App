package hcmute.edu.vn.tlcn.attendanceapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import hcmute.edu.vn.tlcn.attendanceapp.ListSendedRequestActivity;
import hcmute.edu.vn.tlcn.attendanceapp.R;
import hcmute.edu.vn.tlcn.attendanceapp.model.DayOffRequest;

public class WaitingRequestAdapter extends BaseAdapter {
    private ArrayList<DayOffRequest> arrWaitingReq;
    private ListSendedRequestActivity context;
    private int layout ;

    public WaitingRequestAdapter(ArrayList<DayOffRequest> arrWaitingReq, ListSendedRequestActivity context, int layout) {
        this.arrWaitingReq = arrWaitingReq;
        this.context = context;
        this.layout = layout;
    }

    @Override
    public int getCount() {
        return arrWaitingReq.size();
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
        TextView txtDayWaiting;
        Button btnCancelSentReq;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layout,null);

            holder.txtDayWaiting = (TextView) convertView.findViewById(R.id.txtDayWaiting);
            holder.btnCancelSentReq = (Button) convertView.findViewById(R.id.btnCancelSentReq);

            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder) convertView.getTag();
        }

        DayOffRequest waitingReq = arrWaitingReq.get(position);
        holder.txtDayWaiting.setText(waitingReq.getDateOff());

        holder.btnCancelSentReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.cancelWaitingRequest(waitingReq);
            }
        });

        return convertView;
    }
}
