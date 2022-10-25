package hcmute.edu.vn.tlcn.attendanceapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import hcmute.edu.vn.tlcn.attendanceapp.pattern.User_singeton;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AdminSettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AdminSettingsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AdminSettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BlankFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AdminSettingsFragment newInstance(String param1, String param2) {
        AdminSettingsFragment fragment = new AdminSettingsFragment();
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
    TextView txtEmployee, txtProfile;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_admin_settings, container, false);
        User_singeton user_singeton = User_singeton.getInstance();

        if(user_singeton.getUser() == null)
        {
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        }
        
        mapping();

        txtEmployee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Manage_Emp_Fragment manage_emp_fragment = new Manage_Emp_Fragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flFragment,manage_emp_fragment).commit();
            }
        });

        txtProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileInformationFragment profileInformationFragment = new ProfileInformationFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flFragment,profileInformationFragment).commit();
            }
        });

        return view;
    }

    private void mapping(){
        txtEmployee = (TextView) view.findViewById(R.id.txtEmployee);
        txtProfile = (TextView) view.findViewById(R.id.txtProfile);
    }
}