package hcmute.edu.vn.tlcn.attendanceapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import hcmute.edu.vn.tlcn.attendanceapp.adapter.FeedbackAdapter;
import hcmute.edu.vn.tlcn.attendanceapp.model.DayOffRequest;
import hcmute.edu.vn.tlcn.attendanceapp.model.Feedback;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ListFeedbackFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListFeedbackFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ListFeedbackFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ListFeedbackFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ListFeedbackFragment newInstance(String param1, String param2) {
        ListFeedbackFragment fragment = new ListFeedbackFragment();
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
    ImageView btnBackFeedback;
    ListView listviewFeedback;
    TextView txtNotification;
    FeedbackAdapter feedbackAdapter;
    ArrayList<Feedback> arrFeedback;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_list_feedback, container, false);

        mapping();

        arrFeedback = new ArrayList<>();
        feedbackAdapter = new FeedbackAdapter(arrFeedback, getActivity(), R.layout.feedback_row);
        listviewFeedback.setAdapter(feedbackAdapter);
        putDataToView();

        btnBackFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AdminSettingsFragment adminSettingsFragment = new AdminSettingsFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flAdminFragment, adminSettingsFragment).commit();
            }
        });

        return view;
    }

    private void putDataToView() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference feedbackRef = database.getReference("feedback");
        feedbackRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrFeedback.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Feedback feedback = dataSnapshot.getValue(Feedback.class);
                    arrFeedback.add(feedback);
                }
                feedbackAdapter.notifyDataSetChanged();

                if (arrFeedback.size() == 0) {
                    txtNotification.setVisibility(View.VISIBLE);
                    listviewFeedback.setVisibility(View.GONE);
                } else {
                    txtNotification.setVisibility(View.INVISIBLE);
                    listviewFeedback.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void mapping() {
        btnBackFeedback = (ImageView) view.findViewById(R.id.btnBackFeedback);
        listviewFeedback = (ListView) view.findViewById(R.id.listviewFeedback);
        txtNotification = (TextView) view.findViewById(R.id.txtNotification);
    }
}