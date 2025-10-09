package com.example.evcharging.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.evcharging.R;
import com.example.evcharging.api.ApiClient;
import com.example.evcharging.api.ApiService;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private ApiService api;

    public static final String PREFS_NAME = "EV_CHARGING_PREFS";
    public static final String AUTH_TOKEN_KEY = "AUTH_TOKEN_KEY";
    public static final String STATION_ID_KEY = "STATION_ID_KEY"; // Key for saving stationId

    /**
     * Called when the activity is first created.
     * Initializes UI components, API service, and button click listeners.
     */
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

    /**
     * Collects user input for email and password.
     * Validates input and triggers API login if fields are not empty.
     */
    private void doLogin() {
        String email = etEmail.getText().toString().trim();
        String pwd = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pwd)) {
            Toast.makeText(this, "Enter Email and Password", Toast.LENGTH_SHORT).show();
            return;
        }
        loginViaApi(email, pwd);
    }

    /**
     * Calls the backend API to perform login using provided credentials.
     * Handles response by saving auth token and navigating to the correct dashboard based on user role.
     */
    private void loginViaApi(String email, String password) {
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        api.login(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> responseBody = response.body();
                    String authToken = (String) responseBody.get("token");
                    int role = -1;
                    String stationId = null;

                    if (responseBody.containsKey("user") && responseBody.get("user") instanceof Map) {
                        Map<String, Object> userMap = (Map<String, Object>) responseBody.get("user");
                        Object roleObj = userMap.get("role");
                        if (roleObj instanceof Number) {
                            role = ((Number) roleObj).intValue();
                        }

                        // If it's an operator (role 1), get their stationId
                        if (role == 1 && userMap.containsKey("stationId")) {
                            stationId = (String) userMap.get("stationId");
                        }
                    }

                    if (authToken != null) {
                        String fullToken = "Bearer " + authToken;
                        saveAuthData(fullToken, stationId); // Save both token and stationId
                        Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                        navigateToDashboardByRole(fullToken, stationId, role);
                    } else {
                        Toast.makeText(LoginActivity.this, "Login failed: Token is missing.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Login failed: Invalid credentials.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Saves the authentication token and stationId (if available) in SharedPreferences.
     */
    private void saveAuthData(String token, String stationId) {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(AUTH_TOKEN_KEY, token);
        if (stationId != null) {
            editor.putString(STATION_ID_KEY, stationId);
        } else {
            editor.remove(STATION_ID_KEY); // Clear stationId if user is not an operator
        }
        editor.apply();
    }

    /**
     * Navigates the user to the correct dashboard based on their role.
     * Operators (role 1) go to OperatorDashboard, others go to DashboardActivity.
     */
    private void navigateToDashboardByRole(String authToken, String stationId, int role) {
        Intent intent;
        if (role == 1) { // Role 1 is Operator
            intent = new Intent(LoginActivity.this, OperatorDashboardActivity.class);
            intent.putExtra("stationId", stationId); // Pass stationId to Operator Dashboard
        } else { // Role 2 (or any other) is EV Owner
            intent = new Intent(LoginActivity.this, DashboardActivity.class);
        }
        intent.putExtra("token", authToken);
        startActivity(intent);
        finish();
    }
}
