package hcmute.edu.vn.tlcn.attendanceapp.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import hcmute.edu.vn.tlcn.attendanceapp.R;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarViewHolder> {
    private final LinkedHashMap<String,String> recordDay;
    private final OnItemListener onItemListener;
    private Context context;

    public CalendarAdapter(LinkedHashMap<String, String> recordDay,
                           Context context,
                           OnItemListener onItemListener) {
        this.recordDay = recordDay;
        this.context = context;
        this.onItemListener = onItemListener;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.calendar_cell,parent,false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) (parent.getHeight() * 0.166666666);
        return new CalendarViewHolder(view, onItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        ArrayList<String> day = new ArrayList<String>(recordDay.keySet());
        ArrayList<String> type = new ArrayList<String>(recordDay.values());
        switch (type.get(position)) {
            case "on time":
                holder.cellDayText.setBackgroundColor(Color.parseColor("#00FF00"));
                break;
            case "late":
                holder.cellDayText.setBackgroundColor(Color.parseColor("#ffff33"));
                break;
            case "absent without permission":
                holder.cellDayText.setBackgroundColor(Color.parseColor("#FF3131"));
                break;
            case "absent with permission":
                holder.cellDayText.setBackgroundColor(Color.parseColor("#00ffff"));
                break;
        }

        int dayValue = Integer.parseInt(day.get(position));
        if(dayValue <= 0){
            day.set(position,"");
        }

        holder.cellDayText.setText(day.get(position));
    }

    @Override
    public int getItemCount() {
        return recordDay.size();
    }

    public interface OnItemListener{
        void onItemClick(int pos, String dayText);
    }
}
