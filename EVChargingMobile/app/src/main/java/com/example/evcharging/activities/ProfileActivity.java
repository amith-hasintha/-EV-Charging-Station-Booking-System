package com.example.evcharging.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.evcharging.R;
import com.example.evcharging.api.ApiClient;
import com.example.evcharging.api.ApiService;
import com.example.evcharging.models.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    EditText etProfileFirstName, etProfileLastName, etProfilePhone;
    Button btnUpdateProfile, btnDeactivate;
    ApiService api;
    String authToken;

    /**
     * Called when the activity is first created.
     * Initializes UI components, API service, authentication token,
     * and sets up click listeners for profile update and deactivation.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        etProfileFirstName = findViewById(R.id.etProfileFirstName);
        etProfileLastName = findViewById(R.id.etProfileLastName);
        etProfilePhone = findViewById(R.id.etProfilePhone);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);
        btnDeactivate = findViewById(R.id.btnDeactivate);

        api = ApiClient.getApiService();
        authToken = getIntent().getStringExtra("token");

        fetchProfile();

        btnUpdateProfile.setOnClickListener(v -> updateProfile());
        btnDeactivate.setOnClickListener(v -> showDeactivationConfirmDialog());
    }

    /**
     * Fetches the current user's profile from the backend API.
     * Populates the UI fields with the retrieved data.
     */
    private void fetchProfile() {
        api.getMyProfile(authToken).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    etProfileFirstName.setText(user.firstName);
                    etProfileLastName.setText(user.lastName);
                    etProfilePhone.setText(user.phoneNumber);
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Updates the user's profile with the values entered in the UI.
     * Sends the updated profile to the backend API.
     */
    private void updateProfile() {
        String firstName = etProfileFirstName.getText().toString();
        String lastName = etProfileLastName.getText().toString();
        String phone = etProfilePhone.getText().toString();

        User userToUpdate = new User();
        userToUpdate.firstName = firstName;
        userToUpdate.lastName = lastName;
        userToUpdate.phoneNumber = phone;

        api.updateMyProfile(authToken, userToUpdate).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ProfileActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Displays a confirmation dialog to the user before deactivating their account.
     */
    private void showDeactivationConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Deactivate Account")
                .setMessage("Are you sure? This action cannot be undone from the app.")
                .setPositiveButton("Deactivate", (dialog, which) -> deactivateAccount())
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Deactivates the user's account via the backend API.
     * Clears stored authentication data and navigates back to the login screen upon success.
     */
    private void deactivateAccount() {
        api.deactivateMyAccount(authToken).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "Account deactivated. Logging out.", Toast.LENGTH_LONG).show();
                    // Clear token and navigate to login
                    SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE);
                    prefs.edit().clear().apply();

                    Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(ProfileActivity.this, "Deactivation failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
