/*
 * File: LoginActivity.java
 * Purpose: Login screen for users (EV Owners)
 */
package com.example.evcharging.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context; // Added for SharedPreferences
import android.content.SharedPreferences; // Added
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.content.Intent;
import android.text.TextUtils; // Added for checking empty strings
import android.widget.Toast;

import com.example.evcharging.R; // Updated import
import com.example.evcharging.api.ApiClient; // Updated import
import com.example.evcharging.api.ApiService; // Updated import

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    // Renamed etNic to etEmail to reflect API change
    EditText etEmail, etPassword;
    Button btnLogin;
    TextView tvRegister;
    ApiService api;

    // SharedPreferences constants
    public static final String PREFS_NAME = "EV_CHARGING_PREFS";
    public static final String AUTH_TOKEN_KEY = "AUTH_TOKEN_KEY";
    public static final String USER_NIC_KEY = "USER_NIC_KEY";
    public static final String USER_EMAIL_KEY = "USER_EMAIL_KEY";


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views - updated IDs
        etEmail = findViewById(R.id.etEmail); // Changed from etNic and R.id.etNic
        etPassword = findViewById(R.id.etPassword); // Changed from R.id.etPassword
        btnLogin = findViewById(R.id.btnLogin); // Changed from R.id.btnLogin
        tvRegister = findViewById(R.id.tvRegister); // Changed from R.id.tvRegister
        api = ApiClient.getApiService();

        // Login button click -> call backend
        btnLogin.setOnClickListener(v -> doLogin());

        // Register click -> open register activity
        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    /**
     * Perform login by sending email and password to server
     */
    private void doLogin() {
        String email = etEmail.getText().toString().trim();
        String pwd = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pwd)) {
            Toast.makeText(this, "Enter Email and Password", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> body = new HashMap<>();
        body.put("email", email); // API expects "email"
        body.put("password", pwd);

        // Call API
        Call<Map<String, Object>> call = api.login(body);
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> responseBody = response.body();
                    String authToken = null;
                    String userNic = null;
                    String userEmail = null;

                    if (responseBody.containsKey("token")) {
                        authToken = (String) responseBody.get("token");
                    }

                    // Attempt to extract user details, structure might vary
                    if (responseBody.containsKey("user") && responseBody.get("user") instanceof Map) {
                        Map<String, Object> userMap = (Map<String, Object>) responseBody.get("user");
                        if (userMap.containsKey("nic")) {
                            userNic = (String) userMap.get("nic");
                        }
                        if (userMap.containsKey("email")) {
                            userEmail = (String) userMap.get("email");
                        }
                        // You could extract other user details here if needed (firstName, lastName, etc.)
                    } else if (responseBody.containsKey("nic")) { // Fallback if nic is top-level
                        userNic = (String) responseBody.get("nic");
                    }


                    if (!TextUtils.isEmpty(authToken)) {
                        // Save token and user NIC to SharedPreferences
                        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString(AUTH_TOKEN_KEY, "Bearer " + authToken); // Store with "Bearer " prefix
                        if (userNic != null) {
                            editor.putString(USER_NIC_KEY, userNic);
                        }
                         if (userEmail != null) {
                            editor.putString(USER_EMAIL_KEY, userEmail);
                        }
                        editor.apply();

                        Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(LoginActivity.this, DashboardActivity.class);
                        // Pass NIC to DashboardActivity, it will retrieve token from SharedPreferences
                        if (userNic != null) {
                            i.putExtra("nic", userNic);
                        } else if (userEmail !=null){
                             i.putExtra("email", userEmail); // Fallback to email if NIC not present
                        }
                        startActivity(i);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Login failed: Token not found in response", Toast.LENGTH_LONG).show();
                    }

                } else {
                    String errorMessage = "Login failed";
                    if (response.errorBody() != null) {
                        try {
                            // You might want to parse a more specific error message from the errorBody
                            errorMessage += ": " + response.code() + " " + response.message();
                        } catch (Exception e) {
                            // Ignore
                        }
                    }
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
