/*
 * File: Station.java
 * Purpose: Model for Charging Station
 */
package com.example.evcharging.models;

public class Station {
    public String id;
    public String name;
    public String location;

    // --- START: ADD THESE TWO FIELDS ---
    public double latitude;
    public double longitude;
    // --- END: ADD THESE TWO FIELDS ---

    public int type; // e.g., 1 for Type 1 AC, 2 for CCS, 3 for CHAdeMO
    public int totalSlots;
    public double pricePerHour;
    public int availableSlots; // Number of currently available slots
    public String status; // e.g., "active", "maintenance", "coming_soon"

    public Station() {
        // Default constructor for Gson
    }

    // --- UPDATE THE CONSTRUCTOR TO INCLUDE LATITUDE AND LONGITUDE ---
    public Station(String id, String name, String location, double latitude, double longitude, int type, int totalSlots, double pricePerHour, int availableSlots, String status) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.latitude = latitude;     // Add this line
        this.longitude = longitude;   // Add this line
        this.type = type;
        this.totalSlots = totalSlots;
        this.pricePerHour = pricePerHour;
        this.availableSlots = availableSlots;
        this.status = status;
    }

    // No getters and setters needed since fields are public
}
