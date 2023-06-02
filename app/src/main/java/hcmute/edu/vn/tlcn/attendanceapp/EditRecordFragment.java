package hcmute.edu.vn.tlcn.attendanceapp;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import hcmute.edu.vn.tlcn.attendanceapp.model.Record;
import hcmute.edu.vn.tlcn.attendanceapp.model.Statistic;
import hcmute.edu.vn.tlcn.attendanceapp.model.UpdateHistory;
import hcmute.edu.vn.tlcn.attendanceapp.model.User;
import hcmute.edu.vn.tlcn.attendanceapp.pattern.User_singeton;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EditRecordFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditRecordFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public EditRecordFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EditRecordFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EditRecordFragment newInstance(String param1, String param2) {
        EditRecordFragment fragment = new EditRecordFragment();
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
    ImageView btnBackEditRecord;
    TextView txtRecordDay, txtRecordEmpCode, txtRecordEmpName;
    EditText edtReasonChange;
    CircleImageView imgRecordEmp;
    Spinner spinnerStatus;
    Button btnConfirmUpdateRecord;
    Record editRecord;
    User_singeton user_singeton;
    User currentUser;
    ArrayList<String> arrStatus;
    ArrayAdapter<String> adapter;
    SimpleDateFormat dateFormat;
    private static final String status1 = "absent without permission";
    private static final String status2 = "absent with permission";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_edit_record, container, false);;

        mapping();
        user_singeton = User_singeton.getInstance();
        currentUser = user_singeton.getUser();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        if (getArguments() != null) {
            editRecord = (Record) getArguments().getSerializable("editRecord");
        }

        arrStatus = new ArrayList<>();
        arrStatus.add(status1);
        arrStatus.add(status2);
        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, arrStatus);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapter);
        putDataToView();

        btnBackEditRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TodayStatisticFragment todayStatisticFragment = new TodayStatisticFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flAdminFragment, todayStatisticFragment).commit();

            }
        });

        btnConfirmUpdateRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newStatus = spinnerStatus.getSelectedItem().toString();
                String reason = edtReasonChange.getText().toString();
                Date currentDate;

                ProgressDialog progressDialog = new ProgressDialog(getActivity());
                progressDialog.setTitle("Checking...");
                progressDialog.setMessage("Please wait");
                progressDialog.show();

                FirebaseDatabase database = FirebaseDatabase.getInstance();

                if(!newStatus.equals(editRecord.getStatus())){
                    String currentMonth = editRecord.getDay().substring(5,7);
                    String currentYear = editRecord.getDay().substring(0,4);
                    DatabaseReference statisticRef = database.getReference("statistic");
                    statisticRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            DataSnapshot dataSnapshot = snapshot.child(currentYear).child(currentMonth);
                            Statistic monthStatistic = dataSnapshot.getValue(Statistic.class);

                            DataSnapshot dataSnapshot2 = snapshot.child(editRecord.getUserUUID()).child(currentYear).child(currentMonth);
                            Statistic empStatistic = dataSnapshot2.getValue(Statistic.class);

                            int countMonthAbsentWithoutPer;
                            int countMonthAbsentWithPer;
                            int countAbsentWithoutPer;
                            int countAbsentWithPer;
                            if(newStatus.equals(status1)){
                                countMonthAbsentWithoutPer = monthStatistic.getAbsentWithoutPer();
                                countMonthAbsentWithPer = monthStatistic.getAbsentWithPer();
                                countMonthAbsentWithPer--;
                                countMonthAbsentWithoutPer++;
                                monthStatistic.setAbsentWithoutPer(countMonthAbsentWithoutPer);
                                monthStatistic.setAbsentWithPer(countMonthAbsentWithPer);

                                countAbsentWithoutPer = empStatistic.getAbsentWithoutPer();
                                countAbsentWithPer = empStatistic.getAbsentWithPer();
                                countAbsentWithoutPer++;
                                countAbsentWithPer--;
                                empStatistic.setAbsentWithoutPer(countAbsentWithoutPer);
                                empStatistic.setAbsentWithPer(countAbsentWithPer);

                            }
                            else if(newStatus.equals(status2)){
                                countMonthAbsentWithoutPer = monthStatistic.getAbsentWithoutPer();
                                countMonthAbsentWithPer = monthStatistic.getAbsentWithPer();
                                countMonthAbsentWithPer++;
                                countMonthAbsentWithoutPer--;
                                monthStatistic.setAbsentWithoutPer(countMonthAbsentWithoutPer);
                                monthStatistic.setAbsentWithPer(countMonthAbsentWithPer);

                                countAbsentWithoutPer = empStatistic.getAbsentWithoutPer();
                                countAbsentWithPer = empStatistic.getAbsentWithPer();
                                countAbsentWithoutPer--;
                                countAbsentWithPer++;
                                empStatistic.setAbsentWithoutPer(countAbsentWithoutPer);
                                empStatistic.setAbsentWithPer(countAbsentWithPer);
                            }
                            statisticRef.child(currentYear).child(currentMonth).setValue(monthStatistic);
                            statisticRef.child(editRecord.getUserUUID()).child(currentYear).child(currentMonth).setValue(monthStatistic);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    DatabaseReference historyRef = database.getReference("updateHistory");
                    final String historyId = UUID.randomUUID().toString();
                    String description = "User "+currentUser.getUuid() + " changed the time attendance status from "+
                            editRecord.getStatus() +" to " + newStatus;
                    UpdateHistory updateHistory = new UpdateHistory(historyId, currentUser.getUuid(),
                            editRecord.getUserUUID(), new Date(), description, reason);
                    historyRef.child(historyId).setValue(updateHistory);
                }

                editRecord.setStatus(newStatus);
                DatabaseReference recordRef = database.getReference("record")
                        .child(editRecord.getUserUUID()).child(editRecord.getDay()).child("absent");
                recordRef.setValue(editRecord);

                progressDialog.dismiss();
                Toast.makeText(getContext(), "Update Successful", Toast.LENGTH_SHORT).show();

                TodayStatisticFragment todayStatisticFragment = new TodayStatisticFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flAdminFragment, todayStatisticFragment).commit();

            }
        });

        return view;
    }

    private void putDataToView() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users");
        myRef.child(editRecord.getUserUUID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                txtRecordEmpName.setText(user.getFullName());
                Picasso.get().load(Uri.parse(user.getAvatar())).fit().centerCrop().into(imgRecordEmp);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        txtRecordEmpCode.setText(editRecord.getUserUUID());
        txtRecordDay.setText(editRecord.getDay());
        if(editRecord.getStatus().equals(status1)){
            spinnerStatus.setSelection(adapter.getPosition(status1));
        }
        else if(editRecord.getStatus().equals(status2)){
            spinnerStatus.setSelection(adapter.getPosition(status2));
        }
    }

    private void mapping() {
        btnBackEditRecord = (ImageView) view.findViewById(R.id.btnBackEditRecord);
        txtRecordDay = (TextView) view.findViewById(R.id.txtRecordDay);
        txtRecordEmpCode = (TextView) view.findViewById(R.id.txtRecordEmpCode);
        txtRecordEmpName = (TextView) view.findViewById(R.id.txtRecordEmpName);
        edtReasonChange = (EditText) view.findViewById(R.id.edtReasonChange);
        imgRecordEmp = (CircleImageView) view.findViewById(R.id.imgRecordEmp);
        spinnerStatus = (Spinner) view.findViewById(R.id.spinnerStatus);
        btnConfirmUpdateRecord = (Button) view.findViewById(R.id.btnConfirmUpdateRecord);
    }
}