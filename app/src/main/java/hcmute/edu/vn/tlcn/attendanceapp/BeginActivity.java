package hcmute.edu.vn.tlcn.attendanceapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Timer;
import java.util.TimerTask;

import hcmute.edu.vn.tlcn.attendanceapp.model.Config;
import hcmute.edu.vn.tlcn.attendanceapp.pattern.Config_singleton;

public class BeginActivity extends AppCompatActivity {

    Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_begin);

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference configRef = database.getReference("config");
                configRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Config config = snapshot.getValue(Config.class);
                        if(config != null) {
                            Config_singleton singleton = Config_singleton.getInstance();
                            singleton.setConfig(config);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                startActivity(new Intent(BeginActivity.this, LoginActivity.class));
                finish();
            }
        }, 1000);


    }
}