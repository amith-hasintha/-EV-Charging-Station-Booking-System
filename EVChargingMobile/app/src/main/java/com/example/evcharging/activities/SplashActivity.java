package com.example.evcharging.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.evcharging.R;
import com.example.evcharging.api.ApiClient;
import com.example.evcharging.api.ApiService;
import com.example.evcharging.models.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashActivity extends AppCompatActivity {

    /**
     * Called when the activity is first created.
     * Displays the splash screen for a short duration and then checks user authentication status.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Wait a moment to show the splash screen, then check the user's status.
        new Handler(Looper.getMainLooper()).postDelayed(this::checkUserStatus, 1500);
    }

    /**
     * Checks if the user has a saved authentication token.
     * If no token exists, navigates to login.
     * If a token exists, fetches the user profile to determine the role.
     */
    private void checkUserStatus() {
        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        String authToken = prefs.getString(LoginActivity.AUTH_TOKEN_KEY, null);

        if (TextUtils.isEmpty(authToken)) {
            // No token saved, user must log in.
            navigateToLogin();
        } else {
            // Token found, verify it and get user role.
            fetchProfileAndNavigate(authToken);
        }
    }

    /**
     * Fetches the user's profile from the backend to verify the token and retrieve the user role.
     * The authentication token of the user.
     */
    private void fetchProfileAndNavigate(String authToken) {
        ApiService apiService = ApiClient.getApiService();
        apiService.getMyProfile(authToken).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    // Successfully fetched profile, now navigate based on the role.
                    navigateToDashboardByRole(authToken, user.role);
                } else {
                    // Token might be expired or invalid. Clear it and go to login.
                    Toast.makeText(SplashActivity.this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
                    clearTokenAndNavigateToLogin();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                // Network error. Can't verify token, so go to login.
                Toast.makeText(SplashActivity.this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
                navigateToLogin();
            }
        });
    }

    private void navigateToDashboardByRole(String authToken, int role) {
        Intent intent;
        if (role == 1) {
            // Role 1 is Operator.
            intent = new Intent(SplashActivity.this, OperatorDashboardActivity.class);
        } else {
            // Role 2 (or any other) is EV Owner.
            intent = new Intent(SplashActivity.this, DashboardActivity.class);
        }
        intent.putExtra("token", authToken);
        startActivity(intent);
        finish(); // Close SplashActivity.
    }

    private void navigateToLogin() {
        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        finish(); // Close SplashActivity.
    }

    private void clearTokenAndNavigateToLogin() {
        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(LoginActivity.AUTH_TOKEN_KEY).apply();
        navigateToLogin();
    }
}
