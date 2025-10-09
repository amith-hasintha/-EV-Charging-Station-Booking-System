package com.example.evcharging.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.evcharging.R;
import com.example.evcharging.api.ApiClient;
import com.example.evcharging.api.ApiService;
import com.example.evcharging.models.Station;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateBookingFragment extends Fragment {

    private static final String TAG = "CreateBookingFragment";
    private static final String ARG_TOKEN = "ARG_TOKEN";

    private Spinner spinnerStation;
    private EditText etStartTime, etEndTime;
    private ApiService apiService;
    private String authToken;
    private List<Station> stationList = new ArrayList<>();
    private Calendar startCalendar = Calendar.getInstance();
    private Calendar endCalendar = Calendar.getInstance();

    public static CreateBookingFragment newInstance(String token) {
        CreateBookingFragment fragment = new CreateBookingFragment();
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
        // --- START: THIS IS THE FIX ---
        // The layout file is named 'activity_booking.xml', not 'fragment_create_booking.xml'.
        return inflater.inflate(R.layout.activity_booking, container, false);
        // --- END: FIX ---
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiService = ApiClient.getApiService();
        spinnerStation = view.findViewById(R.id.spinnerStation);
        etStartTime = view.findViewById(R.id.etStartTime);
        etEndTime = view.findViewById(R.id.etEndTime);
        Button btnCreateBooking = view.findViewById(R.id.btnCreateBooking);

        etStartTime.setOnClickListener(v -> showDateTimePicker(startCalendar, etStartTime));
        etEndTime.setOnClickListener(v -> showDateTimePicker(endCalendar, etEndTime));
        btnCreateBooking.setOnClickListener(v -> createBooking());

        fetchStations();
    }

    private void fetchStations() {
        if (authToken == null) return;
        apiService.getActiveStations(authToken).enqueue(new Callback<List<Station>>() {
            @Override
            public void onResponse(@NonNull Call<List<Station>> call, @NonNull Response<List<Station>> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    stationList = response.body();
                    List<String> stationNames = new ArrayList<>();
                    for (Station station : stationList) {
                        stationNames.add(station.name);
                    }
                    if (getContext() != null) {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, stationNames);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerStation.setAdapter(adapter);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Station>> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to fetch stations: " + t.getMessage());
            }
        });
    }

    private void showDateTimePicker(final Calendar calendar, final EditText editText) {
        if (getContext() == null) return;
        new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            new TimePickerDialog(getContext(), (timeView, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
                editText.setText(sdf.format(calendar.getTime()));
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void createBooking() {
        if (stationList.isEmpty() || spinnerStation.getSelectedItem() == null) {
            Toast.makeText(getContext(), "Please select a station", Toast.LENGTH_SHORT).show();
            return;
        }

        String stationId = stationList.get(spinnerStation.getSelectedItemPosition()).id;
        String startTime = etStartTime.getText().toString();
        String endTime = etEndTime.getText().toString();

        if (startTime.isEmpty() || endTime.isEmpty()) {
            Toast.makeText(getContext(), "Please select start and end times", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> body = new HashMap<>();
        body.put("stationId", stationId);
        body.put("startTime", startTime);
        body.put("endTime", endTime);

        apiService.createBooking(authToken, body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                if (isAdded() && response.isSuccessful()) {
                    Toast.makeText(getContext(), "Booking created successfully!", Toast.LENGTH_SHORT).show();
                } else if(isAdded()) {
                    Toast.makeText(getContext(), "Failed to create booking: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
