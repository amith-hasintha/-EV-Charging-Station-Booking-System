package com.example.evcharging.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.evcharging.R;
import com.example.evcharging.fragments.CreateBookingFragment;
import com.example.evcharging.fragments.DashboardFragment;
import com.example.evcharging.fragments.MyBookingsFragment;
import com.example.evcharging.fragments.NotificationsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DashboardActivity extends AppCompatActivity {

    private String authToken;
    private final FragmentManager fm = getSupportFragmentManager();

    // 1. Declare fragments but DO NOT initialize them here.
    // They will be created and managed properly by the FragmentManager.
    private DashboardFragment dashboardFragment;
    private CreateBookingFragment createBookingFragment;
    private MyBookingsFragment myBookingsFragment;
    private NotificationsFragment notificationsFragment;
    private Fragment activeFragment;

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

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(this::onNavigationItemSelected);

        // This is the key change: Handle both first-time creation and recreation.
        if (savedInstanceState == null) {
            setupFragments();
            // Set the starting tab
            bottomNavigationView.setSelectedItemId(R.id.navigation_dashboard);
        } else {
            // On recreation, find the fragments by their tag instead of creating new ones.
            dashboardFragment = (DashboardFragment) fm.findFragmentByTag("1");
            createBookingFragment = (CreateBookingFragment) fm.findFragmentByTag("2");
            myBookingsFragment = (MyBookingsFragment) fm.findFragmentByTag("3");
            notificationsFragment = (NotificationsFragment) fm.findFragmentByTag("4");

            // Find which fragment was last active to restore it.
            // This loop is a robust way to find the visible fragment.
            for (Fragment fragment : fm.getFragments()) {
                if (fragment.isVisible()) {
                    activeFragment = fragment;
                    break;
                }
            }
        }
    }

    private void setupFragments() {
        // 2. Use the safe `newInstance()` pattern we created before for ALL fragments.
        // This ensures the token is always passed correctly.
        dashboardFragment = DashboardFragment.newInstance(authToken);
        createBookingFragment = CreateBookingFragment.newInstance(authToken);
        myBookingsFragment = MyBookingsFragment.newInstance(authToken);
        notificationsFragment = NotificationsFragment.newInstance(authToken);

        // 3. Add all fragments in a single transaction.
        // This is more efficient and ensures they all exist before being shown.
        fm.beginTransaction()
                .add(R.id.fragment_container, notificationsFragment, "4").hide(notificationsFragment)
                .add(R.id.fragment_container, myBookingsFragment, "3").hide(myBookingsFragment)
                .add(R.id.fragment_container, createBookingFragment, "2").hide(createBookingFragment)
                .add(R.id.fragment_container, dashboardFragment, "1")
                .commit();

        // 4. Set the initial active fragment.
        activeFragment = dashboardFragment;
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;
        int itemId = item.getItemId();

        if (itemId == R.id.navigation_dashboard) {
            selectedFragment = dashboardFragment;
        } else if (itemId == R.id.navigation_create_booking) {
            selectedFragment = createBookingFragment;
        } else if (itemId == R.id.navigation_my_bookings) {
            selectedFragment = myBookingsFragment;
        } else if (itemId == R.id.navigation_notifications) {
            selectedFragment = notificationsFragment;
        }

        // Only perform a transaction if the selected fragment is valid and not already active.
        if (selectedFragment != null && selectedFragment != activeFragment) {
            fm.beginTransaction().hide(activeFragment).show(selectedFragment).commit();
            activeFragment = selectedFragment;
        }
        return true;
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
