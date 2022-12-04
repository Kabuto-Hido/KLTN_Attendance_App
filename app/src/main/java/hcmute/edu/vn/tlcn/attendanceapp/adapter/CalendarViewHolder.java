package hcmute.edu.vn.tlcn.attendanceapp.adapter;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import hcmute.edu.vn.tlcn.attendanceapp.R;

public class CalendarViewHolder extends RecyclerView.ViewHolder {

    TextView cellDayText;
    private final CalendarAdapter.OnItemListener onItemListener;

    public CalendarViewHolder(@NonNull View itemView, CalendarAdapter.OnItemListener onItemListener) {
        super(itemView);
        cellDayText = itemView.findViewById(R.id.cellDayText);
        this.onItemListener = onItemListener;

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemListener.onItemClick(getAdapterPosition(), (String) cellDayText.getText());
            }
        });
    }
}
