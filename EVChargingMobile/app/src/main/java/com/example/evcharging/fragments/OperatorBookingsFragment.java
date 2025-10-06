package com.example.evcharging.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.evcharging.models.Booking;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Implement the adapter's listener interface
public class OperatorBookingsFragment extends Fragment implements OperatorBookingAdapter.BookingListener {

    private RecyclerView rvOperatorBookings;
    private TextView tvNoBookings;
    private ProgressBar progressBar;
    private OperatorBookingAdapter adapter;
    private ApiService apiService;
    private String authToken;
    private final List<Booking> bookingList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_operator_bookings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            authToken = getArguments().getString("token");
        }

        apiService = ApiClient.getApiService();
        rvOperatorBookings = view.findViewById(R.id.rvOperatorBookings);
        tvNoBookings = view.findViewById(R.id.tvNoBookings);
        progressBar = view.findViewById(R.id.progressBar);

        setupRecyclerView();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Fetch bookings when the fragment is resumed
        if (authToken != null) {
            fetchAllBookings();
        }
    }

    private void setupRecyclerView() {
        rvOperatorBookings.setLayoutManager(new LinearLayoutManager(getContext()));
        // Pass 'this' as the listener
        adapter = new OperatorBookingAdapter(getContext(), bookingList, this);
        rvOperatorBookings.setAdapter(adapter);
    }

    private void fetchAllBookings() {
        progressBar.setVisibility(View.VISIBLE);
        rvOperatorBookings.setVisibility(View.GONE);
        tvNoBookings.setVisibility(View.GONE);

        apiService.getAllBookings(authToken).enqueue(new Callback<List<Booking>>() {
            @Override
            public void onResponse(@NonNull Call<List<Booking>> call, @NonNull Response<List<Booking>> response) {
                if (!isAdded()) return; // Check if fragment is still attached
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<Booking> bookings = response.body();
                    if (bookings.isEmpty()) {
                        tvNoBookings.setText("No bookings found.");
                        tvNoBookings.setVisibility(View.VISIBLE);
                    } else {
                        rvOperatorBookings.setVisibility(View.VISIBLE);
                        adapter.updateData(bookings);
                    }
                } else {
                    tvNoBookings.setText("Failed to load bookings.");
                    tvNoBookings.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Booking>> call, @NonNull Throwable t) {
                if (!isAdded()) return; // Check if fragment is still attached
                progressBar.setVisibility(View.GONE);
                tvNoBookings.setText("Network Error. Please try again.");
                tvNoBookings.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onApprove(String bookingId) {
        progressBar.setVisibility(View.VISIBLE);
        apiService.approveBooking(authToken, bookingId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Booking Approved", Toast.LENGTH_SHORT).show();
                    fetchAllBookings(); // Refresh list
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Failed to approve booking", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onReject(String bookingId) {
        progressBar.setVisibility(View.VISIBLE);
        apiService.rejectBooking(authToken, bookingId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Booking Rejected", Toast.LENGTH_SHORT).show();
                    fetchAllBookings(); // Refresh list
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Failed to reject booking", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
