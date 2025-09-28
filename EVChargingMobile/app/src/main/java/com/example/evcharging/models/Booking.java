/*
 * File: Booking.java
 * Purpose: Booking model
 */
package com.example.evcharging.models;

public class Booking {
    public String id; // Booking ID, typically assigned by the server
    public String stationId; // ID of the charging station
    public String startTime; // ISO format string e.g., "2025-09-28T10:00:00Z"
    public String endTime;   // ISO format string e.g., "2025-09-28T12:00:00Z"
    public String status;    // e.g., "pending", "approved", "cancelled", "completed"
    public String nic;       // User's NIC, may be present in responses or for client-side use

    public Booking() {
        // Default constructor for Gson
    }

    // Constructor for creating a new booking request (sent to API)
    public Booking(String stationId, String startTime, String endTime) {
        this.stationId = stationId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Full constructor (typically for data received from server)
    public Booking(String id, String stationId, String startTime, String endTime, String status, String nic) {
        this.id = id;
        this.stationId = stationId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.nic = nic;
    }

    // Consider adding getters and setters if you prefer them over public fields
}
