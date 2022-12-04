package hcmute.edu.vn.tlcn.attendanceapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminMainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener{
    BottomNavigationView adminBottomNavigationView;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        adminBottomNavigationView = (BottomNavigationView) findViewById(R.id.adminBottomNavigationView);
        adminBottomNavigationView.setOnNavigationItemSelectedListener(this);
        adminBottomNavigationView.setSelectedItemId(R.id.navAdminHome);
    }

    AdminSettingsFragment adminSettingsFragment = new AdminSettingsFragment();
    HomeFragment homeFragment = new HomeFragment();

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navAdminSettings:
                getSupportFragmentManager().beginTransaction().replace(R.id.flAdminFragment, adminSettingsFragment).commit();
                return true;
            case R.id.navAdminLogs:
                //getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, attendanceCalendarFragment).commit();
                return true;
            case R.id.navAdminHome:
                getSupportFragmentManager().beginTransaction().replace(R.id.flAdminFragment, homeFragment).commit();
                return true;
        }
        return false;
    }
}