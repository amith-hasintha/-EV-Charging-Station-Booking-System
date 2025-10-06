package com.example.evcharging.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.evcharging.models.Booking;
import java.util.List;

@Dao
public interface BookingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Booking booking);

    // Corrected the query to use the column names 'user_id' and 'start_time'
    @Query("SELECT * FROM bookings WHERE user_id = :userId ORDER BY start_time DESC")
    List<Booking> getBookingsByUserId(String userId);

    @Query("DELETE FROM bookings")
    void deleteAll();
}
