/*
 * File: BookingActivity.java
 * Purpose: Booking creation screen
 */
package com.example.evcharging.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.evcharging.R; // Updated import
import com.example.evcharging.api.ApiClient;
import com.example.evcharging.api.ApiService;
import com.example.evcharging.models.Booking;

import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingActivity extends AppCompatActivity {

    EditText etStationId, etStartTime, etEndTime;
    Button btnCreateBooking;
    ApiService api;
    String authToken;

    // SharedPreferences constants (same as LoginActivity)
    public static final String PREFS_NAME = "EV_CHARGING_PREFS";
    public static final String AUTH_TOKEN_KEY = "AUTH_TOKEN_KEY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking); // Ensure this layout file exists and has the views

        // Retrieve SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        authToken = prefs.getString(AUTH_TOKEN_KEY, null);

        if (TextUtils.isEmpty(authToken)) {
            Toast.makeText(this, "Authentication error. Please login again.", Toast.LENGTH_LONG).show();
            // Optionally, redirect to LoginActivity
            // startActivity(new Intent(BookingActivity.this, LoginActivity.class));
            finish(); // Close activity if not authenticated
            return;
        }

        etStationId = findViewById(R.id.etStationId); // Assume this ID exists in R.layout.activity_booking
        etStartTime = findViewById(R.id.etStartTime); // Assume this ID exists
        etEndTime = findViewById(R.id.etEndTime);     // Assume this ID exists
        btnCreateBooking = findViewById(R.id.btnCreateBooking); // Assume this ID exists

        api = ApiClient.getApiService();

        btnCreateBooking.setOnClickListener(v -> createBooking());
    }

    private void createBooking() {
        String stationId = etStationId.getText().toString().trim();
        String startTime = etStartTime.getText().toString().trim(); // Expects ISO 8601 format: "YYYY-MM-DDTHH:MM:SSZ"
        String endTime = etEndTime.getText().toString().trim();     // Expects ISO 8601 format: "YYYY-MM-DDTHH:MM:SSZ"

        if (TextUtils.isEmpty(stationId) || TextUtils.isEmpty(startTime) || TextUtils.isEmpty(endTime)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Add more robust validation for startTime and endTime formats and logic (e.g., endTime > startTime)

        Booking newBooking = new Booking(stationId, startTime, endTime);

        Call<Map<String, Object>> call = api.createBooking(authToken, newBooking);
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Assuming the response body might contain the created booking details or a success message
                    Toast.makeText(BookingActivity.this, "Booking created successfully!", Toast.LENGTH_LONG).show();
                    finish(); // Close activity and return to the previous screen (e.g., Dashboard)
                } else {
                    String errorMessage = "Failed to create booking";
                    if (response.code() == 401) {
                        errorMessage += ". Session expired. Please login again.";
                        // TODO: Redirect to LoginActivity
                    } else if (response.errorBody() != null) {
                        try {
                            // TODO: Parse the error body for a more specific message from the API
                            // String errorJson = response.errorBody().string();
                            // Gson gson = new Gson();
                            // Map<String, String> errorMap = gson.fromJson(errorJson, Map.class);
                            // if (errorMap != null && errorMap.containsKey("message")) {
                            //     errorMessage += ": " + errorMap.get("message");
                            // } else {
                                 errorMessage += ": " + response.code() + " " + response.message();
                            // }
                        } catch (Exception e) {
                            errorMessage += ". Error parsing error response.";
                        }
                    } else if (response.code() > 0) {
                        errorMessage += ": " + response.code() + " " + response.message();
                    }
                    Toast.makeText(BookingActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(BookingActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
