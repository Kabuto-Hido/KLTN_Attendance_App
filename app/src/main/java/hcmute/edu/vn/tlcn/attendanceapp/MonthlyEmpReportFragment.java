package hcmute.edu.vn.tlcn.attendanceapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import hcmute.edu.vn.tlcn.attendanceapp.adapter.MonthlyEmpReportAdapter;
import hcmute.edu.vn.tlcn.attendanceapp.model.Statistic;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MonthlyEmpReportFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MonthlyEmpReportFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MonthlyEmpReportFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MonthlyEmpReportFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MonthlyEmpReportFragment newInstance(String param1, String param2) {
        MonthlyEmpReportFragment fragment = new MonthlyEmpReportFragment();
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
    ImageView btnBackMonthlyEmpReport;
    TextView txtMonth, txtnotifi;
    ListView listviewEmpReport;
    MonthlyEmpReportAdapter adapter;
    ArrayList<Statistic> arrStatistic;
    ArrayList<String> arrPhone;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_monthly_emp_report, container, false);

        mapping();

        arrStatistic = new ArrayList<>();
        arrPhone = new ArrayList<>();
        adapter = new MonthlyEmpReportAdapter(arrPhone,arrStatistic,getActivity(),R.layout.emp_report_row);
        listviewEmpReport.setAdapter(adapter);

        btnBackMonthlyEmpReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MenuStatisticFragment menuStatisticFragment = new MenuStatisticFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flAdminFragment,menuStatisticFragment).commit();
            }
        });

        Calendar calendar = Calendar.getInstance();
        txtMonth.setOnClickListener(new View.OnClickListener() {
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

                        txtMonth.setText(monthGet+"/"+yearGet);
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

        txtMonth.setText(monthCurrent+"/"+yearCurrent);
        putDataToView(yearCurrent,monthCurrent);

        return view;
    }

    private void putDataToView(String yearCurrent, String monthCurrent) {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userRef = database.getReference("users");
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    arrStatistic.clear();
                    arrPhone.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String phone = dataSnapshot.getKey();
                        arrPhone.add(phone);

                        DatabaseReference statisticRef = database.getReference("statistic").child(phone);
                        statisticRef.addValueEventListener(new ValueEventListener() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                DataSnapshot dataSnapshot1 = snapshot.child(yearCurrent).child(monthCurrent);
                                Statistic statistic = dataSnapshot1.getValue(Statistic.class);
                                if(statistic != null){
                                    arrStatistic.add(statistic);
                                }
                                adapter.notifyDataSetChanged();

                                if(arrStatistic.size() == 0){
                                    txtnotifi.setVisibility(View.VISIBLE);
                                    txtnotifi.setText("No data in "+monthCurrent+"/"+yearCurrent);
                                    listviewEmpReport.setVisibility(View.INVISIBLE);
                                }
                                else{
                                    listviewEmpReport.setVisibility(View.VISIBLE);
                                    txtnotifi.setVisibility(View.INVISIBLE);
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void mapping() {
        btnBackMonthlyEmpReport = (ImageView) view.findViewById(R.id.btnBackMonthlyEmpReport);
        txtMonth = (TextView) view.findViewById(R.id.txtMonth);
        txtnotifi = (TextView) view.findViewById(R.id.txtnotifi);
        listviewEmpReport = (ListView) view.findViewById(R.id.listviewEmpReport);
    }
}