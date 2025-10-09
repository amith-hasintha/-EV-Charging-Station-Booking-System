package com.example.evcharging.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "users")
public class User {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    public String nic; // National ID Card - used as primary key

    @ColumnInfo(name = "first_name")
    public String firstName;

    @ColumnInfo(name = "last_name")
    public String lastName;

    @ColumnInfo(name = "email")
    public String email;

    @ColumnInfo(name = "password")
    public String password; // Note: Storing plain text passwords is not secure.

    @ColumnInfo(name = "role")
    public int role;

    @SerializedName("phoneNumber")
    public String phoneNumber;

    // --- ADD THIS LINE ---
    @SerializedName("stationId")
    public String stationId; // This will hold the operator's assigned station ID


    @ColumnInfo(name = "is_active")
    public boolean active;

    // Room requires a public, no-argument constructor
    public User() {}

    // You can keep other constructors for convenience
    public User(@NonNull String nic, String firstName, String lastName, String email, String password, int role, String phoneNumber) {
        this.nic = nic;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.role = role;
        this.phoneNumber = phoneNumber;
        this.active = true;
    }
}
