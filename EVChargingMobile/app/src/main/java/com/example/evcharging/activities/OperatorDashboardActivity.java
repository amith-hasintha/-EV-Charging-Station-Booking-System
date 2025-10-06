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
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.evcharging.R;
import com.example.evcharging.fragments.OperatorBookingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class OperatorDashboardActivity extends AppCompatActivity {

    private String authToken;
    private final FragmentManager fm = getSupportFragmentManager();
    private OperatorBookingsFragment operatorBookingsFragment;
    private TextView toolbarTitle;
    private BottomNavigationView bottomNavigationView;

    // ActivityResultLauncher for the QR Code Scanner
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if(result.getContents() == null) {
                    Toast.makeText(OperatorDashboardActivity.this, "Scan cancelled", Toast.LENGTH_LONG).show();
                } else {
                    // The QR code content is in result.getContents()
                    // Navigate to a confirmation screen with this data
                    Log.d("QR_SCAN", "Scanned: " + result.getContents());
                    Intent intent = new Intent(OperatorDashboardActivity.this, ConfirmBookingActivity.class);
                    intent.putExtra("token", authToken);
                    intent.putExtra("bookingId", result.getContents()); // Pass the scanned booking ID
                    startActivity(intent);
                }
                // IMPORTANT: Reselect the bookings tab after scanning
                bottomNavigationView.setSelectedItemId(R.id.navigation_operator_bookings);
            });


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

        setupFragments();

        bottomNavigationView = findViewById(R.id.operator_bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(this::onNavigationItemSelected);

        // Load the default fragment
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_operator_bookings);
        }
    }

    private void setupFragments() {
        Bundle bundle = new Bundle();
        bundle.putString("token", authToken);

        operatorBookingsFragment = new OperatorBookingsFragment();
        operatorBookingsFragment.setArguments(bundle);

        // Add the fragment and commit
        fm.beginTransaction().add(R.id.operator_fragment_container, operatorBookingsFragment, "1").commit();
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.navigation_operator_bookings) {
            toolbarTitle.setText("Bookings");
            fm.beginTransaction().show(operatorBookingsFragment).commit();
            return true;
        } else if (itemId == R.id.navigation_scan_qr) {
            // Launch the QR Scanner
            launchQrScanner();
            return false; // Return false so the item doesn't stay selected
        }
        return false;
    }

    private void launchQrScanner() {
        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        options.setPrompt("Scan a Booking QR Code");
        options.setCameraId(0);  // Use a specific camera of the device
        options.setBeepEnabled(true);
        options.setBarcodeImageEnabled(true);
        options.setOrientationLocked(true);
        barcodeLauncher.launch(options);
    }

    private void logoutUser() {
        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(LoginActivity.AUTH_TOKEN_KEY).apply();

        Intent intent = new Intent(OperatorDashboardActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
