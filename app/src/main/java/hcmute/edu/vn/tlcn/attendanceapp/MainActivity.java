package hcmute.edu.vn.tlcn.attendanceapp;

import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import hcmute.edu.vn.tlcn.attendanceapp.Utility.InternetCheckService;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    InternetCheckService internetCheckService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        internetCheckService = new InternetCheckService();

        SettingsFragment settingsFragment = new SettingsFragment();
        HomeFragment homeFragment = new HomeFragment();
        AttendanceCalendarFragment attendanceCalendarFragment = new AttendanceCalendarFragment();

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navSettings:
                        getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, settingsFragment).commit();
                        break;
                    case R.id.navLogs:
                        getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, attendanceCalendarFragment).commit();
                        break;
                    case R.id.navHome:
                        getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, homeFragment).commit();
                        break;
                }
                return true;
            }
        });
        bottomNavigationView.setSelectedItemId(R.id.navHome);
    }

    @Override
    protected void onStart() {
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(internetCheckService, intentFilter);
        super.onStart();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(internetCheckService);
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(internetCheckService, intentFilter);
    }
}