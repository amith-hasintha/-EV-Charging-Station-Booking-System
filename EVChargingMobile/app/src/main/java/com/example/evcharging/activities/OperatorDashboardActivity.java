package com.example.evcharging.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.evcharging.R;
import com.example.evcharging.fragments.NotificationsFragment;
import com.example.evcharging.fragments.OperatorBookingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class OperatorDashboardActivity extends AppCompatActivity {

    private String authToken;
    private final FragmentManager fm = getSupportFragmentManager();

    // Declare fragments but DO NOT initialize them here
    private OperatorBookingsFragment operatorBookingsFragment;
    private NotificationsFragment notificationsFragment;
    private Fragment activeFragment;

    private TextView toolbarTitle;
    private BottomNavigationView bottomNavigationView;

    // ActivityResultLauncher for the QR Code Scanner (this is correct)
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if(result.getContents() == null) {
                    Toast.makeText(OperatorDashboardActivity.this, "Scan cancelled", Toast.LENGTH_LONG).show();
                } else {
                    Log.d("QR_SCAN", "Scanned: " + result.getContents());
                    Intent intent = new Intent(OperatorDashboardActivity.this, ConfirmBookingActivity.class);
                    intent.putExtra("token", authToken);
                    intent.putExtra("bookingId", result.getContents());
                    startActivity(intent);
                }
                bottomNavigationView.setSelectedItemId(R.id.navigation_operator_bookings);
            });

    /**
     * Called when the activity is first created.
     * Initializes authentication, toolbar, bottom navigation, and fragments.
     * Restores fragments on configuration change.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_dashboard);

        authToken = getIntent().getStringExtra("token");
        if (authToken == null || authToken.isEmpty()) {
            Toast.makeText(this, "Authentication Error", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        toolbarTitle = findViewById(R.id.toolbar_title);
        ImageView ivLogout = findViewById(R.id.ivLogout);
        ivLogout.setOnClickListener(v -> logoutUser());

        bottomNavigationView = findViewById(R.id.operator_bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(this::onNavigationItemSelected);

        // Setup fragments only if the activity is newly created
        if (savedInstanceState == null) {
            setupFragments();
            bottomNavigationView.setSelectedItemId(R.id.navigation_operator_bookings);
        } else {
            // Re-find fragments on configuration change
            operatorBookingsFragment = (OperatorBookingsFragment) fm.findFragmentByTag("1");
            notificationsFragment = (NotificationsFragment) fm.findFragmentByTag("2");
            // Determine active fragment after recreation
            if (operatorBookingsFragment != null && !operatorBookingsFragment.isHidden()) {
                activeFragment = operatorBookingsFragment;
            } else if (notificationsFragment != null && !notificationsFragment.isHidden()) {
                activeFragment = notificationsFragment;
            }
        }
    }

    /**
     * Initializes and adds the operator and notification fragments.
     * Sets the initial active fragment.
     */
    private void setupFragments() {
        // 1. Retrieve the token and stationId ONCE from the Intent.
        String stationId = getIntent().getStringExtra("stationId");

        // 2. Use the safe newInstance factory method to create each fragment.
        operatorBookingsFragment = OperatorBookingsFragment.newInstance(authToken, stationId);
        notificationsFragment = NotificationsFragment.newInstance(authToken);

        // 3. Add the fragments to the FragmentManager with unique tags.
        fm.beginTransaction()
                .add(R.id.operator_fragment_container, notificationsFragment, "2")
                .hide(notificationsFragment)
                .add(R.id.operator_fragment_container, operatorBookingsFragment, "1")
                .commit();

        // 4. Set the initial active fragment.
        activeFragment = operatorBookingsFragment;
    }

    /**
     * Handles bottom navigation item clicks.
     * Switches between operator bookings, notifications, and launches QR scanner.
     */
    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.navigation_operator_bookings) {
            toolbarTitle.setText("Bookings");
            if(activeFragment != operatorBookingsFragment) {
                fm.beginTransaction().hide(activeFragment).show(operatorBookingsFragment).commit();
                activeFragment = operatorBookingsFragment;
            }
            return true;
        } else if (itemId == R.id.navigation_operator_notifications) {
            toolbarTitle.setText("Notifications");
            if(activeFragment != notificationsFragment) {
                fm.beginTransaction().hide(activeFragment).show(notificationsFragment).commit();
                activeFragment = notificationsFragment;
            }
            return true;
        } else if (itemId == R.id.navigation_scan_qr) {
            launchQrScanner();
            return false; // Return false so the item doesn't stay selected
        }
        return false;
    }

    // --- THIS IS THE FIX FOR THE ORIENTATION ---
    // Replace the entire launchQrScanner method with this new version
    /**
     * Launches the custom QR scanner with orientation lock and other options.
     * Ensures the scanned booking ID is sent to ConfirmBookingActivity.
     */
    private void launchQrScanner() {
        ScanOptions options = new ScanOptions();
        options.setOrientationLocked(true); // Locks orientation to prevent rotation issues
        options.setCaptureActivity(CustomScannerActivity.class); // Use our custom activity
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        options.setPrompt("Scan a Booking QR Code");
        options.setCameraId(0);
        options.setBeepEnabled(true);
        options.setBarcodeImageEnabled(true);

        barcodeLauncher.launch(options);
    }

    /**
     * Logs out the user by clearing stored auth token and stationId.
     * Redirects to LoginActivity and clears the back stack.
     */
    private void logoutUser() {
        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(LoginActivity.AUTH_TOKEN_KEY).remove(LoginActivity.STATION_ID_KEY).apply();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
