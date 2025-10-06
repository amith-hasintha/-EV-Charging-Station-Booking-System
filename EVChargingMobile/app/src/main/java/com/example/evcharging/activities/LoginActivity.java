package com.example.evcharging.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.evcharging.R;
import com.example.evcharging.api.ApiClient;
import com.example.evcharging.api.ApiService;
import com.google.gson.Gson; // Import Gson for easier debugging

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin;
    TextView tvRegister;
    ApiService api;

    public static final String PREFS_NAME = "EV_CHARGING_PREFS";
    public static final String AUTH_TOKEN_KEY = "AUTH_TOKEN_KEY";
    private static final String TAG = "LoginActivity"; // For logging

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        api = ApiClient.getApiService();

        btnLogin.setOnClickListener(v -> doLogin());
        tvRegister.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
    }

    private void doLogin() {
        String email = etEmail.getText().toString().trim();
        String pwd = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pwd)) {
            Toast.makeText(this, "Enter Email and Password", Toast.LENGTH_SHORT).show();
            return;
        }
        loginViaApi(email, pwd);
    }

    private void loginViaApi(String email, String password) {
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        api.login(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> responseBody = response.body();

                    // For debugging: print the entire response
                    Log.d(TAG, "Login Response: " + new Gson().toJson(responseBody));

                    String authToken = (String) responseBody.get("token");
                    int role = -1; // Default to an invalid role

                    // --- THIS IS THE FINAL, CRITICAL FIX ---
                    // Check if the "user" object exists in the response and is a Map
                    if (responseBody.containsKey("user") && responseBody.get("user") instanceof Map) {
                        // Get the nested user map
                        Map<String, Object> userMap = (Map<String, Object>) responseBody.get("user");

                        // Now, get the role from inside the user map
                        Object roleObj = userMap.get("role");
                        if (roleObj instanceof Number) {
                            role = ((Number) roleObj).intValue();
                        }
                    }
                    // --- END OF FIX ---

                    Log.d(TAG, "Extracted Role: " + role); // Log the role we found

                    if (authToken != null) {
                        String fullToken = "Bearer " + authToken;
                        saveAuthToken(fullToken);
                        Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                        navigateToDashboardByRole(fullToken, role);
                    } else {
                        Toast.makeText(LoginActivity.this, "Login failed: Token is missing.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.e(TAG, "Login failed: " + response.code() + " " + response.message());
                    Toast.makeText(LoginActivity.this, "Login failed: Invalid credentials.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e(TAG, "Network error: " + t.getMessage(), t);
                Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveAuthToken(String token) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(AUTH_TOKEN_KEY, token).apply();
    }

    private void navigateToDashboardByRole(String authToken, int role) {
        Intent intent;
        if (role == 1) {
            // Role 1 is Operator -> Go to OperatorDashboardActivity
            intent = new Intent(LoginActivity.this, OperatorDashboardActivity.class);
        } else {
            // Role 2 (or any other role) is EV Owner -> Go to DashboardActivity
            intent = new Intent(LoginActivity.this, DashboardActivity.class);
        }
        intent.putExtra("token", authToken);
        startActivity(intent);
        finish(); // Finish LoginActivity so the user can't go back
    }
}
