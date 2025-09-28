/*
 * File: RegisterActivity.java
 * Purpose: Register new EV owner via API
 */
package com.example.evcharging.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils; // For checking empty strings
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
// import android.content.Intent; // If you want to navigate to LoginActivity automatically

import com.example.evcharging.R; // Updated import
import com.example.evcharging.api.ApiClient; // Updated import
import com.example.evcharging.api.ApiService; // Updated import
import com.example.evcharging.models.User; // Updated import

import java.util.Map; // For API response

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    // Updated EditText fields to match User model and API requirements
    EditText etNic, etFirstName, etLastName, etEmail, etPassword, etPhoneNumber;
    Button btnRegister;
    ApiService api;

    // Note: AppDatabase and SQLite related code has been removed for direct API registration.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Make sure these IDs match your activity_register.xml layout
        etNic = findViewById(R.id.etNic);
        etFirstName = findViewById(R.id.etFirstName); // Assuming you rename/add etFirstName
        etLastName = findViewById(R.id.etLastName);   // Assuming you add etLastName
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword); // Assuming you add etPassword
        etPhoneNumber = findViewById(R.id.etPhoneNumber); // Assuming you rename/use etPhoneNumber

        btnRegister = findViewById(R.id.btnRegister);
        api = ApiClient.getApiService();

        btnRegister.setOnClickListener(v -> registerUser());
    }

    /**
     * Register user by calling the server API
     */
    private void registerUser() {
        String nic = etNic.getText().toString().trim();
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();

        // Basic validation
        if (TextUtils.isEmpty(nic) || TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) ||
            TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Role for EV Owner is 2 as per API spec
        int role = 2;

        User userToRegister = new User(nic, firstName, lastName, email, password, role, phoneNumber);

        Call<Map<String, Object>> call = api.register(userToRegister);
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Assuming the response body might contain a success message or user data
                    Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_LONG).show();
                    // Optional: Navigate to LoginActivity
                    // Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    // startActivity(intent);
                    finish(); // Close RegisterActivity and return to previous (likely LoginActivity)
                } else {
                    String errorMessage = "Registration failed";
                    if (response.errorBody() != null) {
                        try {
                            // TODO: Parse the error body for a more specific message from the API
                            // String errorJson = response.errorBody().string();
                            // Gson gson = new Gson();
                            // Map<String, String> errorMap = gson.fromJson(errorJson, Map.class);
                            // if (errorMap != null && errorMap.containsKey("message")) {
                            //     errorMessage += ": " + errorMap.get("message");
                            // } else {
                                 errorMessage += ": " + response.code() + " " + response.message();
                            // }
                        } catch (Exception e) {
                            errorMessage += ". Error parsing error response.";
                        }
                    } else if (response.code() > 0) {
                         errorMessage += ": " + response.code() + " " + response.message();
                    }
                    Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
