package com.example.evcharging.db;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.evcharging.dao.BookingDao;
import com.example.evcharging.dao.UserDao;
import com.example.evcharging.models.Booking;
import com.example.evcharging.models.BookingApi;
import com.example.evcharging.models.User;

// Add your entities to the entities array
@Database(entities = {User.class, Booking.class}, version = 2, exportSchema = false) // <-- ADD Booking.class AND INCREMENT version
public abstract class AppDatabase extends RoomDatabase {

    // Define your DAOs here
    public abstract UserDao userDao();
    public abstract BookingDao bookingDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "ev_charging_db")
                            // Add migrations here if you change the schema in the future
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
