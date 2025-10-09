package com.example.evcharging.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.evcharging.R;
import com.example.evcharging.adapters.BookingAdapter;
import com.example.evcharging.api.ApiClient;
import com.example.evcharging.api.ApiService;
import com.example.evcharging.models.BookingApi;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "DashboardFragment";
    private static final String ARG_TOKEN = "ARG_TOKEN"; // Key for argument

    private GoogleMap mMap;
    private ApiService apiService;
    private String authToken;
    private BookingAdapter bookingAdapter;
    private final List<BookingApi> bookingList = new ArrayList<>();
    private TextView tvNoUpcomingBookings;
    private RecyclerView rvBookings;

    public static DashboardFragment newInstance(String token) {
        DashboardFragment fragment = new DashboardFragment();
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
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiService = ApiClient.getApiService();
        tvNoUpcomingBookings = view.findViewById(R.id.tvNoUpcomingBookings);
        rvBookings = view.findViewById(R.id.rvBookings);

        setupRecyclerView();

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchBookings();
    }

    private void setupRecyclerView() {
        rvBookings.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        // --- START: THIS IS THE FIX ---
        // The constructor only takes 3 arguments. The 4th one has been removed.
        bookingAdapter = new BookingAdapter(bookingList, apiService, authToken);
        // --- END: FIX ---
        rvBookings.setAdapter(bookingAdapter);
    }

    private void fetchBookings() {
        if (authToken == null) return;
        apiService.getMyBookings(authToken).enqueue(new Callback<List<BookingApi>>() {
            @Override
            public void onResponse(@NonNull Call<List<BookingApi>> call, @NonNull Response<List<BookingApi>> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    List<BookingApi> bookings = response.body();
                    bookingList.clear();
                    bookingList.addAll(bookings);
                    bookingAdapter.notifyDataSetChanged();

                    if (bookings.isEmpty()) {
                        rvBookings.setVisibility(View.GONE);
                        tvNoUpcomingBookings.setVisibility(View.VISIBLE);
                    } else {
                        rvBookings.setVisibility(View.VISIBLE);
                        tvNoUpcomingBookings.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<BookingApi>> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to fetch bookings: " + t.getMessage());
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        LatLng sriLanka = new LatLng(7.8731, 80.7718);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sriLanka, 8));
        if (getContext() != null && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
    }
}
