package hcmute.edu.vn.tlcn.attendanceapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import hcmute.edu.vn.tlcn.attendanceapp.model.Statistic;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MonthStatisticFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MonthStatisticFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MonthStatisticFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MonthStatisticFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MonthStatisticFragment newInstance(String param1, String param2) {
        MonthStatisticFragment fragment = new MonthStatisticFragment();
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
    PieChart pieChartMonth;
    TextView txtChooseMonth;
    ImageView btnBackMonthStatistic;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_month_statistic, container, false);

        mapping();

        btnBackMonthStatistic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MenuStatisticFragment menuStatisticFragment = new MenuStatisticFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flAdminFragment,menuStatisticFragment).commit();
            }
        });

        Calendar calendar = Calendar.getInstance();
        txtChooseMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int date = calendar.get(Calendar.DATE);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);
                DatePickerDialog monthDatePickerDialog = new DatePickerDialog(getActivity(),
                        AlertDialog.THEME_HOLO_LIGHT, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(year,month,dayOfMonth);
                        SimpleDateFormat monthFormat = new SimpleDateFormat("MM");
                        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
                        String yearGet = yearFormat.format(calendar.getTime());
                        String monthGet = monthFormat.format(calendar.getTime());

                        txtChooseMonth.setText(monthGet+"/"+yearGet);
                        putDataToView(yearGet,monthGet);
                    }
                },year,month,date){
                    @Override
                    protected void onCreate(Bundle savedInstanceState) {
                        super.onCreate(savedInstanceState);
                        getDatePicker().findViewById(getResources().getIdentifier("day","id","android")).setVisibility(View.GONE);
                    }
                };
                monthDatePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                monthDatePickerDialog.show();
            }
        });

        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MM");
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");

        String monthCurrent = monthFormat.format(currentTime);
        String yearCurrent = yearFormat.format(currentTime);

        txtChooseMonth.setText(monthCurrent+"/"+yearCurrent);
        putDataToView(yearCurrent,monthCurrent);

        return view;

    }

    private void putDataToView(String yearCurrent, String monthCurrent){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference statisticRef = database.getReference("statistic");
        statisticRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DataSnapshot dataSnapshot = snapshot.child(yearCurrent).child(monthCurrent);
                Statistic statistic = dataSnapshot.getValue(Statistic.class);
                ArrayList<PieEntry> entries = new ArrayList<>();
                if(statistic!=null){
                    entries.add(new PieEntry(statistic.getOnTime(), "Attendance On Time"));
                    entries.add(new PieEntry(statistic.getLate(), "Attendance Late"));
                    entries.add(new PieEntry(statistic.getAbsentWithPer(), "Absent With Permission"));
                    entries.add(new PieEntry(statistic.getAbsentWithoutPer(), "Absent Without Permission"));

                    ArrayList<Integer> colors = new ArrayList<>();
                    for (int color: ColorTemplate.MATERIAL_COLORS) {
                        colors.add(color);
                    }

                    for (int color: ColorTemplate.VORDIPLOM_COLORS) {
                        colors.add(color);
                    }

                    PieDataSet dataSet = new PieDataSet(entries, "Attendance Status");
                    dataSet.setColors(colors);
                    dataSet.setDrawValues(false);

                    PieData data = new PieData(dataSet);
                    data.setDrawValues(true);
                    data.setValueFormatter(new PercentFormatter(pieChartMonth));
                    data.setValueTextSize(12f);
                    data.setValueTextColor(Color.BLACK);

                    pieChartMonth.setData(data);
                    pieChartMonth.invalidate();

                    pieChartMonth.animateY(1400, Easing.EaseInOutQuad);

                    setupPieChart("Attendance Statistic for "+monthCurrent+"/"+yearCurrent);

                }
                else{
                    entries.clear();
                    pieChartMonth.clear();
                    pieChartMonth.setNoDataText("No data available in "+monthCurrent+"/"+yearCurrent);
                    pieChartMonth.invalidate();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void mapping() {
        pieChartMonth = (PieChart) view.findViewById(R.id.pieChartMonth);
        txtChooseMonth = (TextView) view.findViewById(R.id.txtChooseMonth);
        btnBackMonthStatistic = (ImageView) view.findViewById(R.id.btnBackMonthStatistic);
    }

    private void setupPieChart(String name) {
        pieChartMonth.setDrawHoleEnabled(true);
        pieChartMonth.setUsePercentValues(true);
        pieChartMonth.setEntryLabelTextSize(8);
        pieChartMonth.setEntryLabelColor(Color.BLACK);
        pieChartMonth.setCenterText(name);
        pieChartMonth.setCenterTextSize(18);
        pieChartMonth.getDescription().setEnabled(false);

        Legend l = pieChartMonth.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setEnabled(true);
    }

}