package com.example.evcharging.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.evcharging.R;
import com.example.evcharging.activities.LoginActivity;
import com.example.evcharging.api.ApiClient;
import com.example.evcharging.api.ApiService;
import com.example.evcharging.models.Booking;
import com.example.evcharging.models.Station;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateBookingFragment extends Fragment {

    // --- UI and Data Fields ---
    Spinner spinnerStation;
    EditText etStartTime, etEndTime;
    Button btnCreateBooking;
    ApiService apiService;
    String authToken;

    private List<Station> stationList = new ArrayList<>();
    private Station selectedStation;

    public CreateBookingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_booking, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            authToken = getArguments().getString("token");
        }

        if (TextUtils.isEmpty(authToken)) {
            Toast.makeText(getContext(), "Authentication error. Please login again.", Toast.LENGTH_LONG).show();
            navigateToLogin();
            return;
        }

        spinnerStation = view.findViewById(R.id.spinnerStation);
        etStartTime = view.findViewById(R.id.etStartTime);
        etEndTime = view.findViewById(R.id.etEndTime);
        btnCreateBooking = view.findViewById(R.id.btnCreateBooking);

        apiService = ApiClient.getApiService();

        etStartTime.setOnClickListener(v -> showDateTimePicker(etStartTime));
        etEndTime.setOnClickListener(v -> showDateTimePicker(etEndTime));
        btnCreateBooking.setOnClickListener(v -> createBooking());

        fetchStations();
    }

    // --- *** THIS METHOD CONTAINS THE FIX *** ---
    private void showDateTimePicker(final EditText editTextToSet) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // --- Stage 1: Show Date Picker Dialog ---
        // FIX: Use requireActivity() instead of getContext() for more reliability.
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireActivity(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    calendar.set(Calendar.YEAR, selectedYear);
                    calendar.set(Calendar.MONTH, selectedMonth);
                    calendar.set(Calendar.DAY_OF_MONTH, selectedDay);

                    int hour = calendar.get(Calendar.HOUR_OF_DAY);
                    int minute = calendar.get(Calendar.MINUTE);

                    // --- Stage 2: Show Time Picker Dialog after a date is chosen ---
                    // FIX: Also use requireActivity() here.
                    TimePickerDialog timePickerDialog = new TimePickerDialog(requireActivity(),
                            (timeView, selectedHour, selectedMinute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                                calendar.set(Calendar.MINUTE, selectedMinute);
                                calendar.set(Calendar.SECOND, 0);

                                // --- Stage 3: Format the date and set it to the EditText ---
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
                                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                                String formattedDate = sdf.format(calendar.getTime());

                                editTextToSet.setText(formattedDate);
                            }, hour, minute, true); // true for 24-hour view
                    timePickerDialog.show();

                }, year, month, day);

        // Prevent picking a date in the past.
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void fetchStations() {
        if (authToken == null) return;

        apiService.getAllStations(authToken).enqueue(new Callback<List<Station>>() {
            @Override
            public void onResponse(Call<List<Station>> call, Response<List<Station>> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null) { // Check if fragment is still added
                    stationList.clear();
                    stationList.addAll(response.body());

                    List<String> stationDisplayNames = new ArrayList<>();
                    for (Station station : stationList) {
                        stationDisplayNames.add(station.name + " (" + station.location + ")");
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, stationDisplayNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerStation.setAdapter(adapter);

                    spinnerStation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedStation = stationList.get(position);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            selectedStation = null;
                        }
                    });
                } else if(isAdded()) {
                    Toast.makeText(getContext(), "Failed to load stations", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Station>> call, Throwable t) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Network error fetching stations: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // In CreateBookingFragment.java

    private void createBooking() {
        if (selectedStation == null) {
            Toast.makeText(getContext(), "Please select a station", Toast.LENGTH_SHORT).show();
            return;
        }

        String startTime = etStartTime.getText().toString().trim();
        String endTime = etEndTime.getText().toString().trim();

        if (TextUtils.isEmpty(startTime) || TextUtils.isEmpty(endTime)) {
            Toast.makeText(getContext(), "Please select a start and end time", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- THIS IS THE FIX ---
        // Create a Map to hold the request body instead of a Booking object
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("stationId", selectedStation.id);
        requestBody.put("startTime", startTime);
        requestBody.put("endTime", endTime);

        // Call the API with the new request body
        apiService.createBooking(authToken, requestBody).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getContext(), "Booking created successfully!", Toast.LENGTH_LONG).show();
                    // Go back to the previous screen (likely the dashboard)
                    if (getParentFragmentManager() != null) {
                        getParentFragmentManager().popBackStack();
                    }
                } else if (isAdded()) {
                    String errorBody = "unknown error";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        // Ignore
                    }
                    String errorMessage = "Failed to create booking. Code: " + response.code() + ". " + errorBody;
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    private void navigateToLogin() {
        if (getActivity() == null) return;
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }
}
