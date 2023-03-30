package hcmute.edu.vn.tlcn.attendanceapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import hcmute.edu.vn.tlcn.attendanceapp.adapter.TodayStatisticAdapter;
import hcmute.edu.vn.tlcn.attendanceapp.model.Record;
import hcmute.edu.vn.tlcn.attendanceapp.model.User;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TodayStatisticFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TodayStatisticFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public TodayStatisticFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StatisticFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TodayStatisticFragment newInstance(String param1, String param2) {
        TodayStatisticFragment fragment = new TodayStatisticFragment();
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
    ImageView btnBackTodayStatistic;
    TextView txtTotalCheckin, txtTotalAbsent,
            txtTotalEmp, txtTb, txtDay, layoutToday;
    ListView lstStat;
    SearchView searchTodayReport;
    private int countCheckIn;
    private int countAbsent;
    private int countEmp;
    ArrayList<Record> recordArrayList;
    ArrayList<Record> result;
    ArrayList<User> arrUser;
    TodayStatisticAdapter todayStatisticAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_today_statistic, container, false);

        mapping();
        recordArrayList = new ArrayList<>();
        arrUser = new ArrayList<>();
        todayStatisticAdapter = new TodayStatisticAdapter(recordArrayList, getActivity(), R.layout.row_stat_day);
        lstStat.setAdapter(todayStatisticAdapter);

        btnBackTodayStatistic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MenuStatisticFragment menuStatisticFragment = new MenuStatisticFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flAdminFragment, menuStatisticFragment).commit();
            }
        });

        Calendar calendar = Calendar.getInstance();
        txtDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int date = calendar.get(Calendar.DATE);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(year, month, dayOfMonth);
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                        String dayGet = format.format(calendar.getTime());
                        txtDay.setText(dayGet);

                        putDataToView(dayGet);
                    }
                }, year, month, date);
                datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                datePickerDialog.show();
            }
        });

        searchTodayReport.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutToday.setVisibility(View.GONE);
            }
        });

        searchTodayReport.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                layoutToday.setVisibility(View.VISIBLE);
                return false;
            }
        });

        searchTodayReport.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String keyword) {
                result = new ArrayList<>();
                for (User u : arrUser) {
                    if (u.getUuid().toLowerCase().contains(keyword.toLowerCase()) ||
                            u.getFullName().toLowerCase().contains(keyword.toLowerCase())) {
                        for (Record r : recordArrayList) {
                            if (r.getUserPhone().equals(u.getPhone())) {
                                result.add(r);
                            }
                        }
                    }
                }
                ((TodayStatisticAdapter) lstStat.getAdapter()).update(result);
                return false;
            }
        });

        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = dateFormat.format(currentTime);
        txtDay.setText(currentDate);
        putDataToView(currentDate);

        return view;
    }

    private void putDataToView(String currentDate) {
        if (result != null) {
            result.clear();
            ((TodayStatisticAdapter) lstStat.getAdapter()).update(result);
        }
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                countEmp = 0;
                countCheckIn = 0;
                countAbsent = 0;
                if (snapshot.exists()) {
                    recordArrayList.clear();
                    arrUser.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        countEmp++;
                        String phone = dataSnapshot.getKey();
                        User getUser = dataSnapshot.getValue(User.class);
                        arrUser.add(getUser);

                        DatabaseReference recordRef = database.getReference("record").child(phone);
                        recordRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                DataSnapshot dataSnapshot1 = snapshot.child(currentDate).child("checkIn");
                                Record checkInRecord = dataSnapshot1.getValue(Record.class);

                                DataSnapshot dataSnapshot2 = snapshot.child(currentDate).child("absent");
                                Record absentRecord = dataSnapshot2.getValue(Record.class);

                                if (checkInRecord != null) {
                                    countCheckIn++;
                                    recordArrayList.add(checkInRecord);
                                } else if (absentRecord != null) {
                                    countAbsent++;
                                    recordArrayList.add(absentRecord);
                                }
                                todayStatisticAdapter.notifyDataSetChanged();


                                if (recordArrayList.size() == 0) {
                                    txtTb.setVisibility(View.VISIBLE);
                                    lstStat.setVisibility(View.INVISIBLE);
                                } else {
                                    lstStat.setVisibility(View.VISIBLE);
                                    txtTb.setVisibility(View.INVISIBLE);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }
                    txtTotalEmp.setText(String.valueOf(countEmp));
                    txtTotalCheckin.setText(String.valueOf(countCheckIn));
                    txtTotalAbsent.setText(String.valueOf(countAbsent));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void mapping() {
        btnBackTodayStatistic = (ImageView) view.findViewById(R.id.btnBackTodayStatistic);
        txtTotalCheckin = (TextView) view.findViewById(R.id.txtTotalCheckin);
        txtTotalAbsent = (TextView) view.findViewById(R.id.txtTotalAbsent);
        txtTotalEmp = (TextView) view.findViewById(R.id.txtTotalEmp);
        lstStat = (ListView) view.findViewById(R.id.lstStat);
        txtTb = (TextView) view.findViewById(R.id.txtTb);
        txtDay = (TextView) view.findViewById(R.id.txtDay);
        layoutToday = (TextView) view.findViewById(R.id.layoutToday);
        searchTodayReport = (SearchView) view.findViewById(R.id.searchTodayReport);
    }
}