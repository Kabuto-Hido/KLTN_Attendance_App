package hcmute.edu.vn.tlcn.attendanceapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import at.favre.lib.crypto.bcrypt.BCrypt;
import hcmute.edu.vn.tlcn.attendanceapp.adapter.EmployeeAdapter;
import hcmute.edu.vn.tlcn.attendanceapp.model.Record;
import hcmute.edu.vn.tlcn.attendanceapp.model.Statistic;
import hcmute.edu.vn.tlcn.attendanceapp.model.User;
import hcmute.edu.vn.tlcn.attendanceapp.pattern.User_singeton;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Manage_Emp_Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Manage_Emp_Fragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Manage_Emp_Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Manage_Emp_Fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Manage_Emp_Fragment newInstance(String param1, String param2) {
        Manage_Emp_Fragment fragment = new Manage_Emp_Fragment();
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
    TextView textviewNoti, label1;
    ImageView btnBackPageMngEmp;
    ListView listviewEmp;
    SearchView searchEmp;
    EmployeeAdapter employeeAdapter;
    ArrayList<User> empList = new ArrayList<>();
    ArrayList<User> result;
    FloatingActionButton btnAddEmp;
    SimpleDateFormat timeFormat;
    SimpleDateFormat monthFormat;
    SimpleDateFormat yearFormat;
    SimpleDateFormat dayFormat;
    ArrayList<Record> recordArrayList;
    ArrayList<Statistic> arrStatistic;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_manage__emp_, container, false);

        if (User_singeton.getInstance().getUser() == null) {
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        }

        mapping();
        timeFormat = new SimpleDateFormat("HH:mm");
        monthFormat = new SimpleDateFormat("MM");
        yearFormat = new SimpleDateFormat("yyyy");
        dayFormat = new SimpleDateFormat("dd");
        recordArrayList = new ArrayList<>();
        arrStatistic = new ArrayList<>();

        employeeAdapter = new EmployeeAdapter(getActivity(), R.layout.emp_row, empList, Manage_Emp_Fragment.this);
        listviewEmp.setAdapter(employeeAdapter);

        btnBackPageMngEmp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AdminSettingsFragment adminSettingsFragment = new AdminSettingsFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flAdminFragment, adminSettingsFragment).commit();
            }
        });

        btnAddEmp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddEmployee addEmployee = new AddEmployee();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flAdminFragment, addEmployee).commit();
            }
        });

        searchEmp.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                label1.setVisibility(View.GONE);
            }
        });
        searchEmp.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                label1.setVisibility(View.VISIBLE);
                return false;
            }
        });

        searchEmp.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String keyword) {
                result = new ArrayList<>();
                for (User u : empList) {
                    if (u.getUuid().toLowerCase().contains(keyword.toLowerCase()) ||
                            u.getFullName().toLowerCase().contains(keyword.toLowerCase()) ||
                            u.getPhone().contains(keyword)) {
                        result.add(u);
                    }
                }
                ((EmployeeAdapter) listviewEmp.getAdapter()).update(result);
                return false;
            }
        });

        getListEmp();
        return view;
    }

    private void mapping() {
        btnBackPageMngEmp = (ImageView) view.findViewById(R.id.btnBackPageMngEmp);
        listviewEmp = (ListView) view.findViewById(R.id.listviewEmp);
        btnAddEmp = (FloatingActionButton) view.findViewById(R.id.btnAddEmp);
        textviewNoti = (TextView) view.findViewById(R.id.textviewNoti);
        label1 = (TextView) view.findViewById(R.id.label1);
        searchEmp = (SearchView) view.findViewById(R.id.searchEmp);
    }

    private void getListEmp() {
        if (result != null) {
            result.clear();
            ((EmployeeAdapter) listviewEmp.getAdapter()).update(result);
        }
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("users");
        ref.orderByChild("role").equalTo(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                empList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    empList.add(user);
                }
                employeeAdapter.notifyDataSetChanged();

                if (empList.size() == 0) {
                    textviewNoti.setVisibility(View.VISIBLE);
                    listviewEmp.setVisibility(View.INVISIBLE);
                } else {
                    textviewNoti.setVisibility(View.INVISIBLE);
                    listviewEmp.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Get list employees failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void dialogResetPass(User user) {
        AlertDialog.Builder dialogResetPass = new AlertDialog.Builder(getActivity());
        dialogResetPass.setMessage("Do you sure want to reset password for employee " + user.getUuid() + " ?");
        dialogResetPass.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //reset pass emp - default: Kltn2023*
                String defaultPassword = "Kltn2023*";
                String hashPass = BCrypt.withDefaults().hashToString(12, defaultPassword.toCharArray());
                user.setPassword(hashPass);

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference userRef = database.getReference("users");
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(user.getUuid())) {
                            userRef.child(user.getUuid()).setValue(user);
                            Toast.makeText(getActivity(), "New " + user.getUuid()
                                    + "'s password is Kltn2023*", Toast.LENGTH_SHORT).show();

                            dialog.dismiss();

                        } else {
                            Toast.makeText(getContext(), "User not exist!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
            }
        });

        dialogResetPass.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialogResetPass.setCancelable(false);
        dialogResetPass.show();
    }

    public void DialogEmpDelete(User user) {
        Date currentTime = Calendar.getInstance().getTime();
        String currentMonth = monthFormat.format(currentTime);
        String currentYear = yearFormat.format(currentTime);
        String day = dayFormat.format(currentTime);

        prepareData(currentYear, currentMonth, day, user);

        AlertDialog.Builder dialogDelete = new AlertDialog.Builder(getActivity());
        dialogDelete.setMessage("Do you want to save data employee " + user.getUuid()
                + " in "+currentYear+ "/"+ currentMonth + " before delete?");

        dialogDelete.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    if(recordArrayList.size() != 0){
                        generatePDF(currentYear, currentMonth, day, user);
                    }
                    else{
                        dialog.dismiss();
                        Toast.makeText(getActivity(), "There is no data!!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageReference = storage.getReferenceFromUrl(user.getAvatar());
                storageReference.delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference dayOffReportRef = database.getReference("dayoffreport");
                                dayOffReportRef.orderByChild("userUUID")
                                        .startAt(user.getUuid())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                                    String reqId = dataSnapshot.getKey();
                                                    if (reqId != null) {
                                                        dayOffReportRef.child(reqId).removeValue();
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                DatabaseReference feedbackRef = database.getReference("feedback");
                                feedbackRef.orderByChild("userUUID")
                                        .startAt(user.getUuid())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                                    String feedbackId = dataSnapshot.getKey();
                                                    if (feedbackId != null) {
                                                        feedbackRef.child(feedbackId).removeValue();
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });

                                DatabaseReference recordRef = database.getReference("record");
                                recordRef.child(user.getUuid()).removeValue();

                                DatabaseReference statisticRef = database.getReference("statistic");
                                statisticRef.child(user.getUuid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot yearSnapshot : snapshot.getChildren()) {
                                            String year = yearSnapshot.getKey();
                                            for (DataSnapshot monthSnapshot : yearSnapshot.getChildren()) {
                                                String month = monthSnapshot.getKey();
                                                Statistic statistic = monthSnapshot.getValue(Statistic.class);

                                                if (year != null) {
                                                    if (month != null) {
                                                        statisticRef.child(year).child(month).addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                int totalOnTime;
                                                                int totalLate;
                                                                int totalAbsentWithPer;
                                                                int totalAbsentWithoutPer;

                                                                Statistic monthStatistic = snapshot.getValue(Statistic.class);
                                                                if (monthStatistic != null) {
                                                                    totalOnTime = monthStatistic.getOnTime() - statistic.getOnTime();
                                                                    monthStatistic.setOnTime(totalOnTime);

                                                                    totalLate = monthStatistic.getLate() - statistic.getLate();
                                                                    monthStatistic.setLate(totalLate);

                                                                    totalAbsentWithPer = monthStatistic.getAbsentWithPer() - statistic.getAbsentWithPer();
                                                                    monthStatistic.setAbsentWithPer(totalAbsentWithPer);

                                                                    totalAbsentWithoutPer = monthStatistic.getAbsentWithoutPer() - statistic.getAbsentWithoutPer();
                                                                    monthStatistic.setAbsentWithoutPer(totalAbsentWithoutPer);

                                                                    //minus time
                                                                    monthStatistic.setHourWorked(calDiffTime(statistic.getHourWorked(), monthStatistic.getHourWorked()));

                                                                    statisticRef.child(year).child(month).setValue(monthStatistic);
                                                                    statisticRef.child(user.getUuid()).removeValue();
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError error) {

                                                            }
                                                        });
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                                DatabaseReference ref = database.getReference("users");
                                ref.child(user.getUuid()).removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                getListEmp();
                                                dialog.dismiss();
                                                Toast.makeText(getActivity(), "Delete successful!!", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d("deleteEmp", e.getMessage());
                                                dialog.dismiss();
                                                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("deleteEmp", e.getMessage());
                                dialog.dismiss();
                                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        dialogDelete.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialogDelete.setCancelable(false);
        dialogDelete.show();
    }
    private void prepareData(String y, String m, String d, User user){
        recordArrayList.clear();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference recordRef = database.getReference("record").child(user.getUuid());

        DatabaseReference statisticRef = database.getReference("statistic").child(user.getUuid());

        int dayNow = Integer.parseInt(d);
        if(dayNow < 10){
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, -1);
            int n = cal.getInstance().getActualMaximum(cal.DAY_OF_MONTH);

            int nowMonth = Integer.parseInt(m);
            String YMCurr;
            if(nowMonth == 1){
                int previousYear = Integer.parseInt(y) - 1;
                YMCurr = previousYear + "-12";
            }
            else{
                int previousMonth = Integer.parseInt(m) - 1;
                if(String.valueOf(previousMonth).length() == 1){
                    YMCurr = y + "-0" + previousMonth;
                }
                else{
                    YMCurr = y + "-" + previousMonth;
                }
            }

            statisticRef.child(YMCurr.substring(0, 4)).child(YMCurr.substring(5, 7)).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Statistic statistic = snapshot.getValue(Statistic.class);
                        arrStatistic.add(statistic);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            for(int i = 1; i <= n; i++){
                String dateAttend = YMCurr + "-0" + (i);;
                recordRef.child(dateAttend).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        DataSnapshot dataSnapshot = snapshot.child("checkIn");
                        Record checkInRecord = dataSnapshot.getValue(Record.class);
                        DataSnapshot dataSnapshot3 = snapshot.child("checkOut");
                        Record checkOutRecord = dataSnapshot3.getValue(Record.class);
                        DataSnapshot dataSnapshot2 = snapshot.child("absent");
                        Record absentRecord = dataSnapshot2.getValue(Record.class);

                        if(checkInRecord != null){
                            recordArrayList.add(checkInRecord);
                        }
                        if(checkOutRecord != null){
                            recordArrayList.add(checkOutRecord);
                        }
                        if(absentRecord != null){
                            recordArrayList.add(absentRecord);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }
        String YMCurr;
        if(String.valueOf(m).length() == 1){
            YMCurr = y + "-0" + m;
        }
        else{
            YMCurr = y + "-" + m;
        }

        for(int i = 1; i <= dayNow; i++){
            String dateAttend;
            if (String.valueOf(i).length() == 1) {
                dateAttend = YMCurr + "-0" + (i);
            } else {
                dateAttend = YMCurr + "-" + (i);
            }
            recordRef.child(dateAttend).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    DataSnapshot dataSnapshot = snapshot.child("checkIn");
                    Record checkInRecord = dataSnapshot.getValue(Record.class);
                    DataSnapshot dataSnapshot3 = snapshot.child("checkOut");
                    Record checkOutRecord = dataSnapshot3.getValue(Record.class);
                    DataSnapshot dataSnapshot2 = snapshot.child("absent");
                    Record absentRecord = dataSnapshot2.getValue(Record.class);

                    if(checkInRecord != null){
                        recordArrayList.add(checkInRecord);
                    }
                    if(checkOutRecord != null){
                        recordArrayList.add(checkOutRecord);
                    }
                    if(absentRecord != null){
                        recordArrayList.add(absentRecord);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        statisticRef.child(y).child(m).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Statistic statistic = snapshot.getValue(Statistic.class);
                    arrStatistic.add(statistic);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void generatePDF(String y, String m, String d, User user) throws FileNotFoundException {
        int dayNow = Integer.parseInt(d);
        String file_name = "Employees_"+user.getFullName()+"_"+user.getPhone() + "_Report_";
        String sub_title2_name = "";
        if(dayNow < 10){
            int previousMonth = Integer.parseInt(m) - 1;
            file_name += previousMonth +"&" + m + "_" + y + ".pdf";
            sub_title2_name += previousMonth+"&";
        }
        else{
            file_name += m + "_" + y + ".pdf";
        }
        String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
        File file = new File(filePath, file_name);

        PdfWriter pdfWriter = new PdfWriter(file);
        PdfDocument pdfDocument = new PdfDocument(pdfWriter);
        Document document = new Document(pdfDocument);

        pdfDocument.setDefaultPageSize(PageSize.A4);

        Paragraph title1 = new Paragraph(sub_title2_name + m + "/" + y + " EMPLOYEE " +user.getFullName() +" - "+user.getPhone())
                .setBold().setFontSize(20).setTextAlignment(TextAlignment.CENTER);

        float[] columnWidth1 = {80f, 80f, 150f, 80f};
        Table table1 = new Table(columnWidth1);
        table1.setHorizontalAlignment(HorizontalAlignment.CENTER);

        table1.addCell(new Cell().add(new Paragraph("Day").setBold()));
        table1.addCell(new Cell().add(new Paragraph("Type").setBold()));
        table1.addCell(new Cell().add(new Paragraph("Status").setBold()));
        table1.addCell(new Cell().add(new Paragraph("Time").setBold()));

        Collections.reverse(recordArrayList);
        for (Record r : recordArrayList) {
            table1.addCell(new Cell().add(new Paragraph(r.getDay())));
            table1.addCell(new Cell().add(new Paragraph(r.getType())));
            table1.addCell(new Cell().add(new Paragraph(r.getStatus())));
            table1.addCell(new Cell().add(new Paragraph(r.getTime())));
        }

        Paragraph title2 = new Paragraph("Summary")
                .setBold().setFontSize(18).setTextAlignment(TextAlignment.LEFT);


        float[] columnWidth2 = {80f, 80f, 80f, 80f, 80f, 80f};
        Table table2 = new Table(columnWidth2);
        table2.setHorizontalAlignment(HorizontalAlignment.CENTER);

        table2.addCell(new Cell().add(new Paragraph("Date").setBold()));
        table2.addCell(new Cell().add(new Paragraph("On Time").setBold()));
        table2.addCell(new Cell().add(new Paragraph("Late").setBold()));
        table2.addCell(new Cell().add(new Paragraph("Absent With Per").setBold()));
        table2.addCell(new Cell().add(new Paragraph("Absent Without Per").setBold()));
        table2.addCell(new Cell().add(new Paragraph("Worked Time").setBold()));

        Collections.reverse(arrStatistic);
        for (Statistic s : arrStatistic) {
            table2.addCell(new Cell().add(new Paragraph(s.getStatisticMonth()+"/"+s.getStatisticYear())));
            table2.addCell(new Cell().add(new Paragraph(String.valueOf(s.getOnTime()))));
            table2.addCell(new Cell().add(new Paragraph(String.valueOf(s.getLate()))));
            table2.addCell(new Cell().add(new Paragraph(String.valueOf(s.getAbsentWithPer()))));
            table2.addCell(new Cell().add(new Paragraph(String.valueOf(s.getAbsentWithoutPer()))));
            table2.addCell(new Cell().add(new Paragraph(s.getHourWorked())));
        }

        document.add(title1);
        document.add(table1);
        document.add(title2);
        document.add(table2);
        document.close();
        Toast.makeText(getActivity(), "PDF created!!", Toast.LENGTH_SHORT).show();
    }

    private String calDiffTime(String time1, String time2) {
        String diffDate =null;
        try {
            Date t1 = timeFormat.parse(time1);
            Date t2 = timeFormat.parse(time2);

            long diff = t2.getTime() - t1.getTime();
            if (diff > 0) {
                long diffMinutes = TimeUnit.MILLISECONDS.toMinutes(diff);
                long diffHours = TimeUnit.MILLISECONDS.toHours(diff);
                long addMinutes = diffMinutes - (diffHours * 60);

                if(addMinutes < 10){
                    diffDate = diffHours + ":0" + addMinutes;
                }
                else {
                    diffDate = diffHours + ":" + addMinutes;
                }

            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return diffDate;
    }
}