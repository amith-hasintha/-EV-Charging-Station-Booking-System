package com.example.evcharging.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evcharging.R;
import com.example.evcharging.adapters.OperatorBookingAdapter;
import com.example.evcharging.api.ApiClient;
import com.example.evcharging.api.ApiService;
import com.example.evcharging.models.BookingApi;
import com.example.evcharging.models.CancellationReason; // <-- Import the new model

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OperatorBookingsFragment extends Fragment implements OperatorBookingAdapter.BookingListener {

    private static final String TAG = "OperatorBookingsFrag";
    private static final String ARG_TOKEN = "ARG_TOKEN";
    private static final String ARG_STATION_ID = "ARG_STATION_ID";

    private RecyclerView rvOperatorBookings;
    private OperatorBookingAdapter adapter;
    private final List<BookingApi> bookingList = new ArrayList<>();
    private ApiService apiService;
    private String authToken;
    private String stationId;

    private ProgressBar progressBar;
    private TextView tvNoBookings;

    public static OperatorBookingsFragment newInstance(String token, String stationId) {
        OperatorBookingsFragment fragment = new OperatorBookingsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TOKEN, token);
        args.putString(ARG_STATION_ID, stationId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            authToken = getArguments().getString(ARG_TOKEN);
            stationId = getArguments().getString(ARG_STATION_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_operator_bookings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        apiService = ApiClient.getApiService();

        progressBar = view.findViewById(R.id.progressBar);
        tvNoBookings = view.findViewById(R.id.tvNoBookings);
        rvOperatorBookings = view.findViewById(R.id.rvOperatorBookings);
        setupRecyclerView();

        if (TextUtils.isEmpty(stationId)) {
            Log.e(TAG, "Station ID is missing! Cannot fetch bookings.");
            progressBar.setVisibility(View.GONE);
            tvNoBookings.setText("Operator station ID not found.");
            tvNoBookings.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!TextUtils.isEmpty(authToken) && !TextUtils.isEmpty(stationId)) {
            fetchBookingsForStation();
        }
    }

    private void setupRecyclerView() {
        rvOperatorBookings.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new OperatorBookingAdapter(getContext(), bookingList, this);
        rvOperatorBookings.setAdapter(adapter);
    }

    private void fetchBookingsForStation() {
        // This method remains unchanged
        progressBar.setVisibility(View.VISIBLE);
        tvNoBookings.setVisibility(View.GONE);
        rvOperatorBookings.setVisibility(View.GONE);

        apiService.getStationBookings(authToken, stationId).enqueue(new Callback<List<BookingApi>>() {
            @Override
            public void onResponse(@NonNull Call<List<BookingApi>> call, @NonNull Response<List<BookingApi>> response) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<BookingApi> bookings = response.body();
                    if (bookings.isEmpty()) {
                        tvNoBookings.setText("No bookings found for this station.");
                        tvNoBookings.setVisibility(View.VISIBLE);
                    } else {
                        rvOperatorBookings.setVisibility(View.VISIBLE);
                        adapter.updateData(bookings);
                    }
                } else {
                    tvNoBookings.setText("Failed to load bookings. Code: " + response.code());
                    tvNoBookings.setVisibility(View.VISIBLE);
                    Log.e(TAG, "API Error on getStationBookings. Code: " + response.code() + " Message: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<BookingApi>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                tvNoBookings.setText("Network Error. Please try again.");
                tvNoBookings.setVisibility(View.VISIBLE);
                Log.e(TAG, "Network failure on getStationBookings: ", t);
            }
        });
    }

    @Override
    public void onConfirm(String bookingId) {
        // This method remains unchanged
        apiService.confirmBooking(authToken, bookingId).enqueue(createActionCallback("confirmed"));
    }

    // --- START: NEW onCancelByOperator and showCancelDialog methods ---
    @Override
    public void onCancelByOperator(String bookingId) {
        showCancelDialog(bookingId);
    }

    private void showCancelDialog(final String bookingId) {
        if (getContext() == null) return;

        final EditText reasonInput = new EditText(getContext());
        reasonInput.setHint("Enter reason for cancellation");
        reasonInput.setPadding(40, 40, 40, 40);

        new AlertDialog.Builder(getContext())
                .setTitle("Cancel Booking")
                .setMessage("Please provide a reason for cancelling this booking.")
                .setView(reasonInput)
                .setPositiveButton("Submit", (dialog, which) -> {
                    String reason = reasonInput.getText().toString().trim();
                    if (TextUtils.isEmpty(reason)) {
                        Toast.makeText(getContext(), "Reason cannot be empty.", Toast.LENGTH_SHORT).show();
                    } else {
                        CancellationReason cancellationReason = new CancellationReason(reason);
                        apiService.cancelBookingByOperator(authToken, bookingId, cancellationReason)
                                .enqueue(createActionCallback("cancelled"));
                    }
                })
                .setNegativeButton("Back", null)
                .show();
    }
    // --- END: NEW METHODS ---

    private Callback<Void> createActionCallback(String action) {
        // This method remains unchanged
        return new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (isAdded() && response.isSuccessful()) {
                    Toast.makeText(getContext(), "Booking successfully " + action + ".", Toast.LENGTH_SHORT).show();
                    fetchBookingsForStation(); // Refresh the list
                } else if (isAdded()) {
                    Toast.makeText(getContext(), "Action failed. Code: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Network Error. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        };
    }
}
