package com.example.evcharging.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.evcharging.R;
import com.example.evcharging.api.ApiClient;
import com.example.evcharging.api.ApiService;
import com.example.evcharging.db.AppDatabase;
import com.example.evcharging.models.User;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    EditText etNic, etFirstName, etLastName, etEmail, etPassword, etPhoneNumber;
    Button btnRegister;
    ApiService api;

    /**
     * Called when the activity is first created.
     * Initializes UI components, API service, and sets click listener for registration button.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etNic = findViewById(R.id.etNic);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        btnRegister = findViewById(R.id.btnRegister);

        api = ApiClient.getApiService();

        btnRegister.setOnClickListener(v -> registerUser());
    }

    /**
     * Handles the user registration process.
     * 1. Validates input fields locally.
     * 2. Sends registration data to the backend API.
     * 3. Stores the registered user in local database upon success.
     */
    private void registerUser() {
        String nic = etNic.getText().toString().trim();
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();

        // --- Client-Side Validation ---
        if (TextUtils.isEmpty(nic) || TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) ||
            TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Added password length check based on backend requirements
        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show();
            return; // Stop the registration process
        }

        // Role for EV Owner is 2 as per API spec
        int role = 2;
        User userToRegister = new User(nic, firstName, lastName, email, password, role, phoneNumber);

        Call<Map<String, Object>> call = api.register(userToRegister);
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    executor.execute(() -> {
                        AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
                        db.userDao().insert(userToRegister);
                    });

                    Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    // You can add more specific error handling here later if needed
                    String errorMessage = "Registration failed: " + response.code() + " " + response.message();
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
