package hcmute.edu.vn.tlcn.attendanceapp.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import hcmute.edu.vn.tlcn.attendanceapp.R;
import hcmute.edu.vn.tlcn.attendanceapp.model.DayOffRequest;

public class RespondedReqAdapter extends BaseAdapter {
    private ArrayList<DayOffRequest> arrRespondedReq;
    private Context context;
    private int layout ;

    public RespondedReqAdapter(ArrayList<DayOffRequest> arrRespondedReq, Context context, int layout) {
        this.arrRespondedReq = arrRespondedReq;
        this.context = context;
        this.layout = layout;
    }

    @Override
    public int getCount() {
        return arrRespondedReq.size();
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
        TextView txtDayResponded, txtRespondedStatus;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layout,null);

            holder.txtDayResponded = (TextView) convertView.findViewById(R.id.txtDayResponded);
            holder.txtRespondedStatus = (TextView) convertView.findViewById(R.id.txtRespondedStatus);

            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder) convertView.getTag();
        }

        DayOffRequest respondedReq = arrRespondedReq.get(position);
        holder.txtDayResponded.setText(respondedReq.getDateOff());

        String status = respondedReq.getStatus();
        if(status.equals("Accept")){
            holder.txtRespondedStatus.setTextColor(Color.parseColor("#00FF00"));
        }
        else if(status.equals("Deny")){
            holder.txtRespondedStatus.setTextColor(Color.parseColor("#FF3131"));
        }
        holder.txtRespondedStatus.setText(status);

        return convertView;
    }
}
