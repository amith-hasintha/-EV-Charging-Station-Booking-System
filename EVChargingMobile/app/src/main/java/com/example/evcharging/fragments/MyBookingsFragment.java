package com.example.evcharging.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evcharging.R;
import com.example.evcharging.adapters.BookingAdapter;
import com.example.evcharging.api.ApiClient;
import com.example.evcharging.api.ApiService;
import com.example.evcharging.models.BookingApi;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyBookingsFragment extends Fragment {

    private RecyclerView rvMyBookings;
    private BookingAdapter bookingAdapter;
    private List<BookingApi> bookingsList = new ArrayList<>();
    private ApiService apiService;
    private String authToken;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Use the simple layout file we created for the fragment
        return inflater.inflate(R.layout.fragment_my_bookings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get the token passed from the parent activity
        if (getArguments() != null) {
            authToken = getArguments().getString("token");
        }

        apiService = ApiClient.getApiService();
        rvMyBookings = view.findViewById(R.id.rvMyBookings);

        setupRecyclerView();

        // Check for token after setup
        if (TextUtils.isEmpty(authToken)) {
            Toast.makeText(getContext(), "Authentication Error. Please log in again.", Toast.LENGTH_LONG).show();
            // Optionally, navigate to login
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Fetch data every time the fragment becomes visible.
        // This ensures the booking list is always fresh.
        if (!TextUtils.isEmpty(authToken)) {
            fetchMyBookings();
        }
    }

    private void setupRecyclerView() {
        rvMyBookings.setLayoutManager(new LinearLayoutManager(getContext()));
        // *** THE FIX: Initialize the adapter with all required arguments ***
        bookingAdapter = new BookingAdapter(bookingsList, apiService, authToken);
        rvMyBookings.setAdapter(bookingAdapter);
    }

    private void fetchMyBookings() {
        // This method correctly calls the /api/bookings/my-bookings endpoint
        apiService.getMyBookings(authToken).enqueue(new Callback<List<BookingApi>>() {
            @Override
            public void onResponse(Call<List<BookingApi>> call, Response<List<BookingApi>> response) {
                // Check if the fragment is still attached to the activity
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    bookingsList.clear();
                    bookingsList.addAll(response.body());
                    bookingAdapter.notifyDataSetChanged();
                } else if (isAdded()) {
                    Toast.makeText(getContext(), "Failed to load bookings. Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<BookingApi>> call, Throwable t) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Network error while fetching bookings: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
