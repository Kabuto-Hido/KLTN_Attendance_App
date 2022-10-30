package hcmute.edu.vn.tlcn.attendanceapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.navHome);
    }

    AdminSettingsFragment adminSettingsFragment = new AdminSettingsFragment();
    AttendanceCalendarFragment attendanceCalendarFragment = new AttendanceCalendarFragment();

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.navSettings:
                getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, adminSettingsFragment).commit();
                return true;
            case R.id.navLogs:
                getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, attendanceCalendarFragment).commit();
                return true;
        }
        return false;
    }
}