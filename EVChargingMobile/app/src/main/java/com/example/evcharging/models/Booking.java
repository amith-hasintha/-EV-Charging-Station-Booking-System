package com.example.evcharging.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

// This class is ONLY for the local Room database.
@Entity(tableName = "bookings") // This annotation fixes the "no such table" error.
public class Booking {

    @PrimaryKey
    @NonNull
    public String id; // This annotation fixes the "must have @PrimaryKey" error.

    public String ownerNIC;
    public String stationId;
    public String startTime;
    public String endTime;
    public int status;
    public String qrCode;
    public double totalAmount;
    public String createdAt;

    // A default constructor is required by Room.
    public Booking() {
        this.id = ""; // Initialize to a non-null value
    }

    // Optional: A constructor for creating instances manually.
    public Booking(@NonNull String id, String ownerNIC, String stationId, String startTime, String endTime, int status) {
        this.id = id;
        this.ownerNIC = ownerNIC;
        this.stationId = stationId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }
}
