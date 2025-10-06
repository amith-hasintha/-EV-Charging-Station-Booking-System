package com.example.evcharging.activities;

import android.os.Bundle;
import android.view.View;import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.evcharging.R;
import com.example.evcharging.api.ApiClient;
import com.example.evcharging.api.ApiService;
import com.example.evcharging.models.Booking;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfirmBookingActivity extends AppCompatActivity {

    TextView tvConfirmBookingId, tvConfirmStationId, tvConfirmStatus;
    Button btnConfirmBooking;
    ApiService api;
    String authToken, bookingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_booking);

        authToken = getIntent().getStringExtra("token");
        bookingId = getIntent().getStringExtra("bookingId");

        tvConfirmBookingId = findViewById(R.id.tvConfirmBookingId);
        tvConfirmStationId = findViewById(R.id.tvConfirmStationId);
        tvConfirmStatus = findViewById(R.id.tvConfirmStatus);
        btnConfirmBooking = findViewById(R.id.btnConfirmBooking);

        api = ApiClient.getApiService();

        fetchBookingDetails();

        btnConfirmBooking.setOnClickListener(v -> confirmBooking());
    }

    private void fetchBookingDetails() {
        api.getBookingById(authToken, bookingId).enqueue(new Callback<Booking>() {
            @Override
            public void onResponse(Call<Booking> call, Response<Booking> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Booking booking = response.body();
                    tvConfirmBookingId.setText("Booking ID: " + booking.id);
                    tvConfirmStationId.setText("Station ID: " + booking.stationId);
                    tvConfirmStatus.setText("Status: " + booking.status);

                    // Only allow confirmation if status is "approved"
                    if ("approved".equalsIgnoreCase(booking.status)) {
                        btnConfirmBooking.setEnabled(true);
                    } else {
                        btnConfirmBooking.setEnabled(false);
                        btnConfirmBooking.setText("Cannot Confirm (Status: " + booking.status + ")");
                    }
                } else {
                    Toast.makeText(ConfirmBookingActivity.this, "Failed to fetch booking details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Booking> call, Throwable t) {
                Toast.makeText(ConfirmBookingActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmBooking() {
        api.confirmBooking(authToken, bookingId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ConfirmBookingActivity.this, "Booking Confirmed Successfully!", Toast.LENGTH_LONG).show();
                    btnConfirmBooking.setEnabled(false);
                    btnConfirmBooking.setText("Confirmed");
                    tvConfirmStatus.setText("Status: confirmed");
                } else {
                    Toast.makeText(ConfirmBookingActivity.this, "Confirmation Failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ConfirmBookingActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
