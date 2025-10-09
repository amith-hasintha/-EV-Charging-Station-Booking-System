package com.example.evcharging.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.example.evcharging.R;
import com.example.evcharging.fragments.CreateBookingFragment;
import com.example.evcharging.fragments.DashboardFragment;
import com.example.evcharging.fragments.MyBookingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.evcharging.fragments.NotificationsFragment;

public class DashboardActivity extends AppCompatActivity {

    private String authToken;
    private final FragmentManager fm = getSupportFragmentManager();


    // Add the new fragment instance
    private final DashboardFragment dashboardFragment = new DashboardFragment();
    private final CreateBookingFragment createBookingFragment = new CreateBookingFragment();
    private final MyBookingsFragment myBookingsFragment = new MyBookingsFragment();
    private final NotificationsFragment notificationsFragment = new NotificationsFragment(); // <-- ADD THIS
    private Fragment activeFragment = dashboardFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        authToken = getIntent().getStringExtra("token");

        if (authToken == null || authToken.isEmpty()) {
            Toast.makeText(this, "Authentication Failed. Please login again.", Toast.LENGTH_LONG).show();
            navigateToLogin();
            return;
        }

        ImageView ivProfile = findViewById(R.id.ivProfile);
        ivProfile.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, ProfileActivity.class);
            intent.putExtra("token", authToken);
            startActivity(intent);
        });

        setupFragments();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(this::onNavigationItemSelected);

        // Set the initial fragment without recreating if it already exists
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_dashboard);
        }
    }

    private void setupFragments() {
        Bundle bundle = new Bundle();
        bundle.putString("token", authToken);

        dashboardFragment.setArguments(bundle);
        createBookingFragment.setArguments(bundle);
        myBookingsFragment.setArguments(bundle);
        notificationsFragment.setArguments(bundle); // <-- Pass arguments to new fragment

        fm.beginTransaction().add(R.id.fragment_container, notificationsFragment, "4").hide(notificationsFragment).commit(); // <-- Add new fragment
        fm.beginTransaction().add(R.id.fragment_container, myBookingsFragment, "3").hide(myBookingsFragment).commit();
        fm.beginTransaction().add(R.id.fragment_container, createBookingFragment, "2").hide(createBookingFragment).commit();
        fm.beginTransaction().add(R.id.fragment_container, dashboardFragment, "1").commit();
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Find the selected fragment, defaulting to null
        Fragment selectedFragment = null;
        int itemId = item.getItemId();

        if (itemId == R.id.navigation_dashboard) {
            selectedFragment = dashboardFragment;
        } else if (itemId == R.id.navigation_create_booking) {
            selectedFragment = createBookingFragment;
        } else if (itemId == R.id.navigation_my_bookings) {
            selectedFragment = myBookingsFragment;
        } else if (itemId == R.id.navigation_notifications) { // <-- Handle new item
            selectedFragment = notificationsFragment;
        }

        // If a valid fragment was selected, show it
        if (selectedFragment != null && selectedFragment != activeFragment) {
            fm.beginTransaction().hide(activeFragment).show(selectedFragment).commit();
            activeFragment = selectedFragment;
        }
        return true; // Always return true to show item as selected
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
