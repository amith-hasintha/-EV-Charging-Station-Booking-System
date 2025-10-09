package com.example.evcharging.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.evcharging.R;
import com.example.evcharging.adapters.NotificationAdapter;
import com.example.evcharging.api.ApiClient;
import com.example.evcharging.api.ApiService;
import com.example.evcharging.models.Notification;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsFragment extends Fragment {

    private static final String TAG = "NotificationsFragment";
    private static final String ARG_TOKEN = "ARG_TOKEN"; // Key for argument bundle

    private RecyclerView rvNotifications;
    private TextView tvNoNotifications;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private NotificationAdapter adapter;

    private ApiService apiService;
    private String authToken;
    private final List<Notification> notificationList = new ArrayList<>();

    // --- THIS IS THE MISSING METHOD THAT FIXES THE BUILD ERROR ---
    public static NotificationsFragment newInstance(String token) {
        NotificationsFragment fragment = new NotificationsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TOKEN, token);
        fragment.setArguments(args);
        return fragment;
    }
    // --- END OF FIX ---

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve arguments here, which is safer for the fragment lifecycle
        if (getArguments() != null) {
            authToken = getArguments().getString(ARG_TOKEN);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiService = ApiClient.getApiService();
        rvNotifications = view.findViewById(R.id.rvNotifications);
        tvNoNotifications = view.findViewById(R.id.tvNoNotifications);
        progressBar = view.findViewById(R.id.progressBar);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        setupRecyclerView();
        setupSwipeRefresh();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (authToken != null) {
            fetchNotifications(true); // Show initial loading progress bar
        }
    }

    private void setupRecyclerView() {
        rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotificationAdapter(notificationList);
        rvNotifications.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> fetchNotifications(false)); // Don't show initial progress bar on refresh
    }

    private void fetchNotifications(boolean showInitialProgress) {
        if (showInitialProgress) {
            progressBar.setVisibility(View.VISIBLE);
        }
        rvNotifications.setVisibility(View.GONE);
        tvNoNotifications.setVisibility(View.GONE);

        apiService.getMyNotifications(authToken).enqueue(new Callback<List<Notification>>() {
            @Override
            public void onResponse(@NonNull Call<List<Notification>> call, @NonNull Response<List<Notification>> response) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<Notification> notifications = response.body();
                    if (notifications.isEmpty()) {
                        tvNoNotifications.setText("You have no notifications.");
                        tvNoNotifications.setVisibility(View.VISIBLE);
                    } else {
                        rvNotifications.setVisibility(View.VISIBLE);
                        adapter.updateData(notifications);
                    }
                } else {
                    String errorText = "Failed to load notifications. Code: " + response.code();
                    tvNoNotifications.setText(errorText);
                    tvNoNotifications.setVisibility(View.VISIBLE);
                    Log.e(TAG, "API Error: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Notification>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                tvNoNotifications.setText("Network error. Please try again.");
                tvNoNotifications.setVisibility(View.VISIBLE);
                Log.e(TAG, "Network Failure: ", t);
            }
        });
    }
}
