package com.example.evcharging.fragments;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evcharging.R;
import com.example.evcharging.activities.LoginActivity;
import com.example.evcharging.adapters.DashboardBookingAdapter;
import com.example.evcharging.api.ApiClient;
import com.example.evcharging.api.ApiService;
import com.example.evcharging.models.BookingApi;
import com.example.evcharging.models.Station;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardFragment extends Fragment implements OnMapReadyCallback {

    private TextView tvPending, tvApproved, tvNearby, tvNoUpcomingBookings;
    private RecyclerView rvBookings;
    private DashboardBookingAdapter adapter; // The variable is correctly named 'adapter'
    private List<BookingApi> bookingsList = new ArrayList<>();
    private ApiService apiService;
    private String authToken;

    private GoogleMap gMap;
    private List<Station> stationList = new ArrayList<>();

    public DashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            authToken = getArguments().getString("token");
        }

        tvPending = view.findViewById(R.id.tvPending);
        tvApproved = view.findViewById(R.id.tvApproved);
        tvNearby = view.findViewById(R.id.tvNearby);
        rvBookings = view.findViewById(R.id.rvBookings);
        tvNoUpcomingBookings = view.findViewById(R.id.tvNoUpcomingBookings);

        apiService = ApiClient.getApiService();

        setupRecyclerView();

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        if (TextUtils.isEmpty(authToken)) {
            Toast.makeText(getContext(), "Authentication error. Please login again.", Toast.LENGTH_LONG).show();
            navigateToLogin();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!TextUtils.isEmpty(authToken)) {
            fetchMyBookings();
            fetchActiveStations();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;
        LatLng defaultLocation = new LatLng(6.9271, 79.8612); // Colombo
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f));
        if (!stationList.isEmpty()) {
            updateMapMarkers();
        }
    }

    private void setupRecyclerView() {
        rvBookings.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        adapter = new DashboardBookingAdapter(bookingsList);
        rvBookings.setAdapter(adapter);
    }

    private void fetchMyBookings() {
        apiService.getMyBookings(authToken).enqueue(new Callback<List<BookingApi>>() {
            @Override
            public void onResponse(@NonNull Call<List<BookingApi>> call, @NonNull Response<List<BookingApi>> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    List<BookingApi> allBookingApis = response.body();

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            updateDashboardSummary(allBookingApis); // Renamed for clarity
                            updateUpcomingBookings(allBookingApis); // Renamed for clarity
                        });
                    }
                } else if (isAdded()) {
                    handleApiError("load your bookings", response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<BookingApi>> call, @NonNull Throwable t) {
                if (isAdded()) Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchActiveStations() {
        apiService.getActiveStations(authToken).enqueue(new Callback<List<Station>>() {
            @Override
            public void onResponse(@NonNull Call<List<Station>> call, @NonNull Response<List<Station>> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    stationList.clear();
                    stationList.addAll(response.body());

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            tvNearby.setText(String.format(Locale.getDefault(), "%d\nNearby", stationList.size()));
                            updateMapMarkers();
                        });
                    }
                } else if(isAdded()) {
                    handleApiError("load nearby stations", response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Station>> call, @NonNull Throwable t) {
                if (isAdded()) Toast.makeText(getContext(), "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateMapMarkers() {
        if (gMap == null || stationList.isEmpty()) {
            return;
        }
        gMap.clear();
        for (Station station : stationList) {
            LatLng location = new LatLng(station.latitude, station.longitude);
            gMap.addMarker(new MarkerOptions().position(location).title(station.name));
        }
        if (!stationList.isEmpty()) {
            LatLng firstStation = new LatLng(stationList.get(0).latitude, stationList.get(0).longitude);
            gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(firstStation, 13f));
        }
    }

    private void updateDashboardSummary(List<BookingApi> allBookingApis) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Backend Enum: Active=0, Confirmed=1
            long pending = allBookingApis.stream().filter(b -> b.status == 0).count(); // Use numeric status
            long approved = allBookingApis.stream().filter(b -> b.status == 1).count(); // Use numeric status
            tvPending.setText(String.format(Locale.getDefault(), "%d\nPending", pending));
            tvApproved.setText(String.format(Locale.getDefault(), "%d\nApproved", approved));
        }
    }

    private void updateUpcomingBookings(List<BookingApi> allBookingApis) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            List<BookingApi> upcoming = new ArrayList<>();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Filter for bookings that are either Active (0) or Confirmed (1)
                upcoming = allBookingApis.stream()
                        .filter(b -> b.status == 0 || b.status == 1) // Compare integers
                        .sorted(Comparator.comparing(b -> ZonedDateTime.parse(b.startTime)))
                        .collect(Collectors.toList());
            }

            if (upcoming.isEmpty()) {
                rvBookings.setVisibility(View.GONE);
                tvNoUpcomingBookings.setVisibility(View.VISIBLE);
            } else {
                rvBookings.setVisibility(View.VISIBLE);
                tvNoUpcomingBookings.setVisibility(View.GONE);
                // --- THIS IS THE FIX ---
                // Use the correct method name 'updateBookings' and the correct variable 'adapter'
                adapter.updateBookings(upcoming);
            }
        }
    }

    private void handleApiError(String action, int code) {
        if (getContext() == null) return;
        String errorMsg = "Failed to " + action;
        if (code == 401) {
            errorMsg += ". Your session has expired.";
            navigateToLogin();
        } else {
            errorMsg += ". Error code: " + code;
        }
        Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
    }

    private void navigateToLogin() {
        if (getActivity() == null) return;
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }
}
