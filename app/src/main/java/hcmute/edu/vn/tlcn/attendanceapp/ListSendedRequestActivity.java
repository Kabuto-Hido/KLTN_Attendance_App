package hcmute.edu.vn.tlcn.attendanceapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import java.util.Calendar;
import java.util.Date;

import hcmute.edu.vn.tlcn.attendanceapp.adapter.RespondedReqAdapter;
import hcmute.edu.vn.tlcn.attendanceapp.adapter.WaitingRequestAdapter;
import hcmute.edu.vn.tlcn.attendanceapp.model.DayOffRequest;
import hcmute.edu.vn.tlcn.attendanceapp.model.Record;
import hcmute.edu.vn.tlcn.attendanceapp.model.User;
import hcmute.edu.vn.tlcn.attendanceapp.pattern.User_singeton;

public class ListSendedRequestActivity extends AppCompatActivity {
    ImageView btnBackLstSent;
    TextView txtWaiting, txtResponded, txtNoRequest;
    ListView lstSentReq;
    ArrayList<DayOffRequest> arrSentReq;
    User_singeton user_singeton = User_singeton.getInstance();
    User user;
    WaitingRequestAdapter waitingRequestAdapter;
    RespondedReqAdapter respondedReqAdapter;
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_sended_request);
        user = user_singeton.getUser();

        mapping();

        arrSentReq = new ArrayList<>();
        waitingRequestAdapter = new WaitingRequestAdapter(arrSentReq,ListSendedRequestActivity.this,R.layout.waiting_req_row);
        respondedReqAdapter = new RespondedReqAdapter(arrSentReq,ListSendedRequestActivity.this,R.layout.responded_req_row);
        lstSentReq.setAdapter(waitingRequestAdapter);

        database = FirebaseDatabase.getInstance();
        DatabaseReference waitingRef = database.getReference("dayoffreport");
        waitingRef.orderByChild("userPhone").startAt(user.getPhone()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrSentReq.clear();
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    DayOffRequest dayOffRequest = dataSnapshot.getValue(DayOffRequest.class);
                    if(dayOffRequest.getStatus().equals("waiting")) {
                        arrSentReq.add(dayOffRequest);
                    }
                    else break;
                }
                if(arrSentReq.size() == 0){
                    txtNoRequest.setText("No waiting request");
                    txtNoRequest.setVisibility(View.VISIBLE);
                    lstSentReq.setVisibility(View.INVISIBLE);
                }
                else{
                    txtNoRequest.setVisibility(View.INVISIBLE);
                    lstSentReq.setVisibility(View.VISIBLE);
                }
                waitingRequestAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.v("",error.getMessage());
            }
        });

        btnBackLstSent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        txtWaiting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                database = FirebaseDatabase.getInstance();
                DatabaseReference waitingRef = database.getReference("dayoffreport");
                waitingRef.orderByChild("userPhone").startAt(user.getPhone()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        arrSentReq.clear();
                        for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                            DayOffRequest dayOffRequest = dataSnapshot.getValue(DayOffRequest.class);
                            if(dayOffRequest.getStatus().equals("waiting")) {
                                arrSentReq.add(dayOffRequest);
                            }
                        }
                        if(arrSentReq.size() == 0){
                            txtNoRequest.setText("No waiting request");
                            txtNoRequest.setVisibility(View.VISIBLE);
                            lstSentReq.setVisibility(View.INVISIBLE);
                        }
                        else{
                            txtNoRequest.setVisibility(View.INVISIBLE);
                            lstSentReq.setVisibility(View.VISIBLE);
                        }
                        lstSentReq.setAdapter(waitingRequestAdapter);
                        waitingRequestAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.v("",error.getMessage());
                    }
                });
                txtWaiting.setBackgroundResource(R.drawable.top_bottom_border);
                txtWaiting.setTextColor(getResources().getColor(R.color.black));
                txtResponded.setBackground(null);
                txtResponded.setTextColor(Color.parseColor("#808080"));
            }
        });

        txtResponded.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                database = FirebaseDatabase.getInstance();
                DatabaseReference waitingRef = database.getReference("dayoffreport");
                waitingRef.orderByChild("userPhone").startAt(user.getPhone()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        arrSentReq.clear();
                        for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                            DayOffRequest dayOffRequest = dataSnapshot.getValue(DayOffRequest.class);
                            System.out.println(dayOffRequest.getStatus());
                            if(dayOffRequest.getStatus().equals("Accept") || dayOffRequest.getStatus().equals("Deny")) {
                                arrSentReq.add(dayOffRequest);
                            }
                        }
                        if(arrSentReq.size() == 0){
                            txtNoRequest.setText("No responded");
                            txtNoRequest.setVisibility(View.VISIBLE);
                            lstSentReq.setVisibility(View.INVISIBLE);
                        }
                        else{
                            txtNoRequest.setVisibility(View.INVISIBLE);
                            lstSentReq.setVisibility(View.VISIBLE);
                        }
                        lstSentReq.setAdapter(respondedReqAdapter);
                        respondedReqAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.v("",error.getMessage());
                    }
                });
                txtResponded.setBackgroundResource(R.drawable.top_bottom_border);
                txtResponded.setTextColor(getResources().getColor(R.color.black));
                txtWaiting.setBackground(null);
                txtWaiting.setTextColor(Color.parseColor("#808080"));
            }
        });

    }


    private void mapping() {
        btnBackLstSent = (ImageView) findViewById(R.id.btnBackLstSent);
        txtWaiting = (TextView) findViewById(R.id.txtWaiting);
        txtResponded = (TextView) findViewById(R.id.txtResponded);
        txtNoRequest = (TextView) findViewById(R.id.txtNoRequest);
        lstSentReq = (ListView) findViewById(R.id.lstSentReq);
    }
}