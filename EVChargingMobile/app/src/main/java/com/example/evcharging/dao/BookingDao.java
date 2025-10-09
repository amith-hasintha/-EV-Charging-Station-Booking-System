package com.example.evcharging.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.evcharging.models.Booking;

import java.util.List;

@Dao
public interface BookingDao {

    // This now works because Booking is a valid @Entity
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Booking booking);

    // This now works because we are querying the 'bookings' table for the Booking entity
    @Query("SELECT * FROM bookings WHERE ownerNIC = :nic")
    List<Booking> getBookingsByUserNic(String nic);

    // This now works because it queries the correct 'bookings' table
    @Query("DELETE FROM bookings")
    void deleteAll();

    // REMOVED: All methods that were trying to use or return 'BookingApi'.
    // The DAO should not know about the API model.
}
