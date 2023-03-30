package hcmute.edu.vn.tlcn.attendanceapp;

import android.annotation.SuppressLint;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import hcmute.edu.vn.tlcn.attendanceapp.Utility.InternetCheckService;

public class AdminMainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    BottomNavigationView adminBottomNavigationView;
    InternetCheckService internetCheckService;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        internetCheckService = new InternetCheckService();

        adminBottomNavigationView = (BottomNavigationView) findViewById(R.id.adminBottomNavigationView);
        adminBottomNavigationView.setOnNavigationItemSelectedListener(this);
        adminBottomNavigationView.setSelectedItemId(R.id.navAdminHome);
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

    AdminSettingsFragment adminSettingsFragment = new AdminSettingsFragment();
    HomeFragment homeFragment = new HomeFragment();
    AttendanceCalendarFragment attendanceCalendarFragment = new AttendanceCalendarFragment();
    MenuStatisticFragment menuStatisticFragment = new MenuStatisticFragment();

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navAdminSettings:
                getSupportFragmentManager().beginTransaction().replace(R.id.flAdminFragment, adminSettingsFragment).commit();
                return true;
            case R.id.navAdminLogs:
                getSupportFragmentManager().beginTransaction().replace(R.id.flAdminFragment, attendanceCalendarFragment).commit();
                return true;
            case R.id.navAdminHome:
                getSupportFragmentManager().beginTransaction().replace(R.id.flAdminFragment, homeFragment).commit();
                return true;
            case R.id.navAdminReports:
                getSupportFragmentManager().beginTransaction().replace(R.id.flAdminFragment, menuStatisticFragment).commit();
                return true;
        }
        return false;
    }
}