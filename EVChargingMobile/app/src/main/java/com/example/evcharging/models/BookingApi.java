package com.example.evcharging.models;

import androidx.annotation.NonNull;

// Note: I am removing the Room annotations (@Entity, @PrimaryKey, etc.)
// because the fields from the API now differ from what you might store locally.
// If you need Room persistence, a separate "local" model is recommended.
public class BookingApi {

    @NonNull
    public String id;

    // This field now correctly matches the backend's "ownerNIC"
    public String ownerNIC;

    public String stationId;
    public String startTime;
    public String endTime;

    // This field will now correctly receive the numeric status from the API
    public int status;

    public String qrCode;
    public double totalAmount; // Using double for decimal values
    public String createdAt;
    public String updatedAt;
    public String confirmedAt;
    public String cancelledAt;

    // A default constructor is good practice for libraries like Gson
    public BookingApi() {}

    // A full constructor for convenience
    public BookingApi(@NonNull String id, String ownerNIC, String stationId, String startTime, String endTime, int status) {
        this.id = id;
        this.ownerNIC = ownerNIC;
        this.stationId = stationId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }
}
