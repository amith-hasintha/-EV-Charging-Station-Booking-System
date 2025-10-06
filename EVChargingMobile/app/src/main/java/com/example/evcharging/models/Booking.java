package com.example.evcharging.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "bookings",
        foreignKeys = @ForeignKey(entity = User.class,
                                  parentColumns = "id",
                                  childColumns = "user_id",
                                  onDelete = ForeignKey.CASCADE),
        indices = {@Index("user_id")})
public class Booking {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "booking_id")
    public String id;

    @ColumnInfo(name = "station_id")
    public String stationId;

    @ColumnInfo(name = "start_time")
    public String startTime;

    @ColumnInfo(name = "end_time")
    public String endTime;

    @ColumnInfo(name = "status")
    public String status;

    @ColumnInfo(name = "user_id")
    public String userId;

    // This constructor is used by Room
    public Booking() {}

    // This constructor is used by BookingActivity.java
    @Ignore // We use @Ignore to tell Room to not use this constructor
    public Booking(String stationId, String startTime, String endTime) {
        this.stationId = stationId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // This full constructor can be used for creating objects from API responses or other parts of the app
    public Booking(@NonNull String id, String stationId, String startTime, String endTime, String status, String userId) {
        this.id = id;
        this.stationId = stationId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.userId = userId;
    }
}
