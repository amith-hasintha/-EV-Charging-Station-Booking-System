/*
 * File: DashboardActivity.java
 * Purpose: Dashboard showing counts, active stations, bookings list, and account deactivation
 */
package com.example.evcharging.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog; // Added for confirmation dialog
import android.content.Context;
import android.content.DialogInterface; // Added for dialog buttons
import android.content.Intent; // Added for navigating to LoginActivity
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button; // Added for Deactivate Button
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.widget.Toast;

import com.example.evcharging.R;
import com.example.evcharging.adapters.BookingAdapter;
import com.example.evcharging.api.ApiClient;
import com.example.evcharging.api.ApiService;
import com.example.evcharging.models.Booking;
import com.example.evcharging.models.Station;

import java.util.ArrayList;
import java.util.List;
import java.util.Map; // Added for API response

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity {

    TextView tvPending, tvApproved, tvNearbyStations;
    RecyclerView rvBookings;
    BookingAdapter adapter;
    List<Booking> bookingsList = new ArrayList<>();
    List<Station> activeStationsList = new ArrayList<>();

    Button btnDeactivateAccount; // Added Deactivate Button

    ApiService api;
    String userNic;
    String authToken;

    public static final String PREFS_NAME = "EV_CHARGING_PREFS";
    public static final String AUTH_TOKEN_KEY = "AUTH_TOKEN_KEY";
    public static final String USER_NIC_KEY = "USER_NIC_KEY"; // Added for clearing
    public static final String USER_EMAIL_KEY = "USER_EMAIL_KEY"; // Added for clearing


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        authToken = prefs.getString(AUTH_TOKEN_KEY, null);
        userNic = getIntent().getStringExtra("nic"); // Or retrieve from prefs if stored there primarily

        tvPending = findViewById(R.id.tvPending);
        tvApproved = findViewById(R.id.tvApproved);
        tvNearbyStations = findViewById(R.id.tvNearby);
        rvBookings = findViewById(R.id.rvBookings);

        // Initialize Deactivate Button - Ensure R.id.btnDeactivateAccount exists in your layout
        btnDeactivateAccount = findViewById(R.id.btnDeactivateAccount);
        if (btnDeactivateAccount != null) {
            btnDeactivateAccount.setOnClickListener(v -> showDeactivationConfirmationDialog());
        }


        rvBookings.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookingAdapter(this, bookingsList, new BookingAdapter.OnActionListener() {
            @Override
            public void onModify(Booking b) {
                Toast.makeText(DashboardActivity.this, "Modify clicked for booking: " + b.id, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel(Booking b) {
                cancelBooking(b);
            }
        });
        rvBookings.setAdapter(adapter);

        api = ApiClient.getApiService();

        if (TextUtils.isEmpty(authToken)) {
            Toast.makeText(this, "Authentication error. Please login again.", Toast.LENGTH_LONG).show();
            navigateToLogin();
            return;
        }

        fetchMyBookings();
        fetchActiveStations();
    }

    private void fetchMyBookings() {
        if (TextUtils.isEmpty(authToken)) return;

        Call<List<Booking>> call = api.getMyBookings(authToken);
        call.enqueue(new Callback<List<Booking>>() {
            @Override
            public void onResponse(Call<List<Booking>> call, Response<List<Booking>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    bookingsList.clear();
                    bookingsList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    updateBookingCounts();
                } else {
                    String errorMsg = "Failed to load bookings";
                    if (response.code() == 401) {
                        errorMsg += ". Session expired. Please login again.";
                        navigateToLogin();
                    }
                    Toast.makeText(DashboardActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Booking>> call, Throwable t) {
                Toast.makeText(DashboardActivity.this, "Network error fetching bookings: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchActiveStations() {
        if (TextUtils.isEmpty(authToken)) return;

        Call<List<Station>> call = api.getActiveStations(authToken);
        call.enqueue(new Callback<List<Station>>() {
            @Override
            public void onResponse(Call<List<Station>> call, Response<List<Station>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    activeStationsList.clear();
                    activeStationsList.addAll(response.body());
                    tvNearbyStations.setText(activeStationsList.size() + "\nActive Stations");
                } else {
                    String errorMsg = "Failed to load stations";
                     if (response.code() == 401) {
                        errorMsg += ". Session expired. Please login again.";
                        navigateToLogin();
                    }
                    Toast.makeText(DashboardActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Station>> call, Throwable t) {
                Toast.makeText(DashboardActivity.this, "Network error fetching stations: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateBookingCounts() {
        int pending = 0;
        int approved = 0;
        for (Booking b : bookingsList) {
            if ("pending".equalsIgnoreCase(b.status)) pending++;
            if ("approved".equalsIgnoreCase(b.status)) approved++;
        }
        tvPending.setText(pending + "\nPending");
        tvApproved.setText(approved + "\nApproved");
    }

    private void cancelBooking(Booking bookingToCancel) {
        Toast.makeText(this, "Cancel requested for booking ID: " + bookingToCancel.id + ". (API endpoint TBD)", Toast.LENGTH_LONG).show();
    }

    private void showDeactivationConfirmationDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Deactivate Account")
            .setMessage("Are you sure you want to deactivate your account? This action cannot be undone.")
            .setPositiveButton("Deactivate", (dialog, which) -> deactivateCurrentUserAccount())
            .setNegativeButton("Cancel", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }

    private void deactivateCurrentUserAccount() {
        if (TextUtils.isEmpty(authToken)) {
            Toast.makeText(this, "Cannot deactivate: Not authenticated.", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return;
        }

        Call<Map<String, Object>> call = api.deactivateAccount(authToken);
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) { // Check for 200 OK or 204 No Content typically
                    Toast.makeText(DashboardActivity.this, "Account deactivated successfully.", Toast.LENGTH_LONG).show();
                    // Clear SharedPreferences
                    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.remove(AUTH_TOKEN_KEY);
                    editor.remove(USER_NIC_KEY);
                    editor.remove(USER_EMAIL_KEY);
                    editor.apply();

                    navigateToLogin();
                } else {
                    String errorMessage = "Failed to deactivate account";
                    if (response.code() == 401) {
                        errorMessage += ". Session expired. Please login again.";
                        navigateToLogin();
                    } else if (response.errorBody() != null) {
                        try {
                            // TODO: Parse error body for specific message
                             errorMessage += ": " + response.code() + " " + response.message();
                        } catch (Exception e) {
                             errorMessage += ". Error parsing error response.";
                        }
                    } else if (response.code() > 0){
                        errorMessage += ": " + response.code() + " " + response.message();
                    }
                    Toast.makeText(DashboardActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(DashboardActivity.this, "Network error during deactivation: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
