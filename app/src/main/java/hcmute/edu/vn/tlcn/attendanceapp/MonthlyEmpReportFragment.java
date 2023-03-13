package hcmute.edu.vn.tlcn.attendanceapp;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import hcmute.edu.vn.tlcn.attendanceapp.adapter.MonthlyEmpReportAdapter;
import hcmute.edu.vn.tlcn.attendanceapp.model.Statistic;
import hcmute.edu.vn.tlcn.attendanceapp.model.User;

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
    ArrayList<User> arrUser;
    FloatingActionButton btnExportPDF;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_monthly_emp_report, container, false);

        mapping();

        arrStatistic = new ArrayList<>();
        arrUser = new ArrayList<>();
        adapter = new MonthlyEmpReportAdapter(arrUser,arrStatistic,getActivity(),R.layout.emp_report_row);
        listviewEmpReport.setAdapter(adapter);

        btnBackMonthlyEmpReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MenuStatisticFragment menuStatisticFragment = new MenuStatisticFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flAdminFragment,menuStatisticFragment).commit();
            }
        });

        btnExportPDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkPermission()){
                    try {
                        generatePDF();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }else{
                    requestPermission();
                }
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
                    arrUser.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String phone = dataSnapshot.getKey();

                        User getUser = dataSnapshot.getValue(User.class);
                        arrUser.add(getUser);

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

    private void generatePDF() throws FileNotFoundException{
        String M_Y = txtMonth.getText().toString();
        String y = M_Y.substring(3,7);
        String m = M_Y.substring(0,2);

        String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
        File file = new File(filePath,"Employees_Report_"+m+"_"+y+".pdf");

        PdfWriter pdfWriter = new PdfWriter(file);
        PdfDocument pdfDocument = new PdfDocument(pdfWriter);
        Document document = new Document(pdfDocument);

        pdfDocument.setDefaultPageSize(PageSize.A4);

        Paragraph title = new Paragraph(m+"/"+y+" EMPLOYEE TIMESHEET")
                .setBold().setFontSize(20).setTextAlignment(TextAlignment.CENTER);

        float columnWidth[] = {80f, 150f,80f,80f,80f,80f,80f};
        Table table = new Table(columnWidth);
        table.setHorizontalAlignment(HorizontalAlignment.CENTER);

        table.addCell(new Cell().add(new Paragraph("Emp No").setBold()));
        table.addCell(new Cell().add(new Paragraph("Emp Name").setBold()));
        table.addCell(new Cell().add(new Paragraph("On Time").setBold()));
        table.addCell(new Cell().add(new Paragraph("Late").setBold()));
        table.addCell(new Cell().add(new Paragraph("Absent With Per").setBold()));
        table.addCell(new Cell().add(new Paragraph("Absent Without Per").setBold()));
        table.addCell(new Cell().add(new Paragraph("Worked Time").setBold()));

        Collections.reverse(arrStatistic);
        for(Statistic s : arrStatistic) {
            for(User u: arrUser){
                if(s.getUserPhone().equals(u.getPhone())){
                    table.addCell(new Cell().add(new Paragraph(u.getUuid())));
                    table.addCell(new Cell().add(new Paragraph(u.getFullName())));
                }
            }
            table.addCell(new Cell().add(new Paragraph(String.valueOf(s.getOnTime()))));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(s.getLate()))));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(s.getAbsentWithPer()))));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(s.getAbsentWithoutPer()))));
            table.addCell(new Cell().add(new Paragraph(s.getHourWorked())));
        }

        document.add(title);
        document.add(table);
        document.close();
        Toast.makeText(getActivity(), "PDF created!!", Toast.LENGTH_SHORT).show();


    }

    private static final int PERMISSION_REQUEST_CODE = 200;
    private boolean checkPermission() {
        int permission1 = ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int permission2 = ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), READ_EXTERNAL_STORAGE);
        return permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                boolean writeStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean readStorage = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                if (writeStorage && readStorage) {
                    Toast.makeText(getActivity(), "Permission Granted..", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Permission Denied.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void mapping() {
        btnBackMonthlyEmpReport = (ImageView) view.findViewById(R.id.btnBackMonthlyEmpReport);
        txtMonth = (TextView) view.findViewById(R.id.txtMonth);
        txtnotifi = (TextView) view.findViewById(R.id.txtnotifi);
        listviewEmpReport = (ListView) view.findViewById(R.id.listviewEmpReport);
        btnExportPDF = (FloatingActionButton) view.findViewById(R.id.btnExportPDF);
    }
}