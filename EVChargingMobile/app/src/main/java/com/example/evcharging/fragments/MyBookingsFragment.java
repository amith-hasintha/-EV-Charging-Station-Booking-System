package com.example.evcharging.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    private static final String TAG = "MyBookingsFragment";
    private static final String ARG_TOKEN = "ARG_TOKEN";

    private RecyclerView rvMyBookings;
    private BookingAdapter adapter;
    private final List<BookingApi> bookingList = new ArrayList<>();
    private ApiService apiService;
    private String authToken;

    public static MyBookingsFragment newInstance(String token) {
        MyBookingsFragment fragment = new MyBookingsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TOKEN, token);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            authToken = getArguments().getString(ARG_TOKEN);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_bookings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiService = ApiClient.getApiService();
        rvMyBookings = view.findViewById(R.id.rvMyBookings);
        setupRecyclerView();
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchMyBookings();
    }

    private void setupRecyclerView() {
        rvMyBookings.setLayoutManager(new LinearLayoutManager(getContext()));
        // --- START: THIS IS THE FIX ---
        // The constructor only takes 3 arguments. The 4th one has been removed.
        adapter = new BookingAdapter(bookingList, apiService, authToken);
        // --- END: FIX ---
        rvMyBookings.setAdapter(adapter);
    }

    private void fetchMyBookings() {
        if (authToken == null) return;
        apiService.getMyBookings(authToken).enqueue(new Callback<List<BookingApi>>() {
            @Override
            public void onResponse(@NonNull Call<List<BookingApi>> call, @NonNull Response<List<BookingApi>> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    bookingList.clear();
                    bookingList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<BookingApi>> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to fetch bookings: " + t.getMessage());
            }
        });
    }
}
