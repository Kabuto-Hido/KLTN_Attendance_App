package hcmute.edu.vn.tlcn.attendanceapp;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import hcmute.edu.vn.tlcn.attendanceapp.adapter.UpdateHistoryAdapter;
import hcmute.edu.vn.tlcn.attendanceapp.model.Feedback;
import hcmute.edu.vn.tlcn.attendanceapp.model.UpdateHistory;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UpdateHistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UpdateHistoryFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public UpdateHistoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UpdateHistoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UpdateHistoryFragment newInstance(String param1, String param2) {
        UpdateHistoryFragment fragment = new UpdateHistoryFragment();
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
    ImageView btnBackHistory;
    ListView listviewHistory;
    ArrayList<UpdateHistory> arrHistory;
    UpdateHistoryAdapter updateHistoryAdapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_update_history, container, false);

        mapping();
        arrHistory = new ArrayList<>();
        updateHistoryAdapter = new UpdateHistoryAdapter(arrHistory, getActivity(), R.layout.history_row);
        listviewHistory.setAdapter(updateHistoryAdapter);
        putDataToView();

        btnBackHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AdminSettingsFragment adminSettingsFragment = new AdminSettingsFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flAdminFragment, adminSettingsFragment).commit();
            }
        });

        listviewHistory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UpdateHistory selected = arrHistory.get(position);
                showDetailDialog(selected);
            }
        });

        return view;
    }

    private void showDetailDialog(UpdateHistory selected) {
        AlertDialog dialogDetail;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View layout_dialog = inflater.inflate(R.layout.dialog_history_detail, null);
        builder.setView(layout_dialog);

        TextView txtEditedPerson = layout_dialog.findViewById(R.id.txtEditedPerson);
        TextView txtEditedTime = layout_dialog.findViewById(R.id.txtEditedTime);
        TextView txtDescription = layout_dialog.findViewById(R.id.txtDescription);
        TextView textReason = layout_dialog.findViewById(R.id.textReason);
        Button buttonOkay = layout_dialog.findViewById(R.id.buttonOkay);

        txtEditedPerson.setText(selected.getEditedPerson());
        txtDescription.setText(selected.getDescription());
        textReason.setText(selected.getReason());

        Date createAt = selected.getImplDate();
        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MMM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
        String date = dayFormat.format(createAt) + " " + timeFormat.format(createAt);
        txtEditedTime.setText(date);

        dialogDetail = builder.create();
        dialogDetail.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogDetail.getWindow().setGravity(Gravity.CENTER);
        dialogDetail.setCancelable(false);
        dialogDetail.show();

        buttonOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogDetail.dismiss();
            }
        });
    }

    private void putDataToView() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference historyRef = database.getReference("updateHistory");
        historyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrHistory.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    UpdateHistory history = dataSnapshot.getValue(UpdateHistory.class);
                    arrHistory.add(history);
                }
                updateHistoryAdapter.notifyDataSetChanged();

                if (arrHistory.size() == 0) {
                    listviewHistory.setVisibility(View.GONE);
                    Toast.makeText(getActivity(), "No history!!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void mapping() {
        btnBackHistory = view.findViewById(R.id.btnBackHistory);
        listviewHistory = view.findViewById(R.id.listviewHistory);
    }
}