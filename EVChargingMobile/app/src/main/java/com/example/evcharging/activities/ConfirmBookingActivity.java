package com.example.evcharging.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.evcharging.R;
import com.example.evcharging.api.ApiClient;
import com.example.evcharging.api.ApiService;
import com.example.evcharging.models.BookingApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfirmBookingActivity extends AppCompatActivity {

    TextView tvConfirmBookingId, tvConfirmStationId, tvConfirmStatus;
    Button btnConfirmBooking;
    ApiService api;
    String authToken, bookingId;

    /**
     * Called when the activity is first created.
     * Initializes UI components, retrieves intent extras, and fetches booking details.
     */
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

    /**
     * Fetches booking details from the backend using the booking ID and auth token.
     * Updates UI fields based on the response and sets button state based on booking status.
     */

    private void fetchBookingDetails() {
        api.getBookingById(authToken, bookingId).enqueue(new Callback<BookingApi>() {
            @Override
            public void onResponse(Call<BookingApi> call, Response<BookingApi> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BookingApi bookingApi = response.body();
                    tvConfirmBookingId.setText("Booking ID: " + bookingApi.id);
                    tvConfirmStationId.setText("Station ID: " + bookingApi.stationId);

                    // --- THIS IS THE FIX ---
                    // Backend Enum: Active=0, Confirmed=1
                    int status = bookingApi.status;
                    String statusText = "UNKNOWN";

                    // Set status text and color based on the numeric status
                    switch (status) {
                        case 0: // Active
                            statusText = "ACTIVE";
                            tvConfirmStatus.setTextColor(ContextCompat.getColor(ConfirmBookingActivity.this, R.color.orange_soda));
                            break;
                        case 1: // Confirmed
                            statusText = "CONFIRMED";
                            tvConfirmStatus.setTextColor(ContextCompat.getColor(ConfirmBookingActivity.this, R.color.emerald_green));
                            break;
                        case 3: // Cancelled
                            statusText = "CANCELLED";
                            tvConfirmStatus.setTextColor(ContextCompat.getColor(ConfirmBookingActivity.this, R.color.red_error));
                            break;
                        default:
                            tvConfirmStatus.setTextColor(android.graphics.Color.GRAY);
                            break;
                    }
                    tvConfirmStatus.setText("Status: " + statusText);


                    // Only allow confirmation if the booking has been approved by an operator (status 1)
                    if (status == 1) { // <-- Compare integers, not strings
                        btnConfirmBooking.setEnabled(true);
                    } else {
                        btnConfirmBooking.setEnabled(false);
                        btnConfirmBooking.setText("Cannot Confirm (Status: " + statusText + ")");
                    }

                } else {
                    Toast.makeText(ConfirmBookingActivity.this, "Failed to fetch booking details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BookingApi> call, Throwable t) {
                Toast.makeText(ConfirmBookingActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }


    /**
     * Confirms the booking by calling the backend API.
     * Updates the UI based on the success or failure of the confirmation request.
     */
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
