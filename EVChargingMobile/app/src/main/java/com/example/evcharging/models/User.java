/*
 * Author: YourName
 * File: User.java
 * Purpose: Model for user (EV Owner / Operator)
 */

package com.example.evcharging.models;

public class User {
    public String nic; // primary key
    public String firstName; // Changed from 'name'
    public String lastName;  // Added
    public String email;
    public String password; // Added for registration
    public int role;       // Changed from String to int
    public String phoneNumber; // Changed from 'phone'
    public boolean active;

    public User() {}

    // Constructor for registration
    public User(String nic, String firstName, String lastName, String email, String password, int role, String phoneNumber) {
        this.nic = nic;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password; // Should generally be handled securely, not stored long-term as plain text
        this.role = role;
        this.phoneNumber = phoneNumber;
        this.active = true; // Default to active on creation, or as per API response
    }

    // Optional: Constructor for user data received from server (without password)
    public User(String nic, String firstName, String lastName, String email, int role, String phoneNumber, boolean active) {
        this.nic = nic;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
        this.phoneNumber = phoneNumber;
        this.active = active;
    }

    // Getters and setters can be added if preferred over public fields
}
