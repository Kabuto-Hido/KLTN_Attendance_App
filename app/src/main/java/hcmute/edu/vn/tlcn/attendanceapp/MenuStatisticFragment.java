package hcmute.edu.vn.tlcn.attendanceapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MenuStatisticFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MenuStatisticFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public MenuStatisticFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MenuStatisticFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MenuStatisticFragment newInstance(String param1, String param2) {
        MenuStatisticFragment fragment = new MenuStatisticFragment();
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

    TextView txtTodayStatistic, txtMonthStatistic, txtEmpReport;
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_menu_statistic, container, false);

        mapping();

        txtTodayStatistic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TodayStatisticFragment todayStatisticFragment = new TodayStatisticFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flAdminFragment, todayStatisticFragment).commit();
            }
        });

        txtMonthStatistic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MonthStatisticFragment monthStatisticFragment = new MonthStatisticFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flAdminFragment, monthStatisticFragment).commit();
            }
        });

        txtEmpReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MonthlyEmpReportFragment monthlyEmpReportFragment = new MonthlyEmpReportFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flAdminFragment, monthlyEmpReportFragment).commit();

            }
        });

        return view;
    }

    private void mapping() {
        txtTodayStatistic = (TextView) view.findViewById(R.id.txtTodayStatistic);
        txtMonthStatistic = (TextView) view.findViewById(R.id.txtMonthStatistic);
        txtEmpReport = (TextView) view.findViewById(R.id.txtEmpReport);
    }
}