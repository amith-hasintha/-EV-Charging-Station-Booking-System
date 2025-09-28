/*
 * File: ApiService.java
 * Purpose: Retrofit interface defining backend endpoints.
 */
package com.example.evcharging.api;

import com.example.evcharging.models.Booking; // Updated import
import com.example.evcharging.models.Station; // Updated import
import com.example.evcharging.models.User;    // Updated import
// import com.example.evcharging.models.CreateBookingRequest; // Consider if Booking model is complex

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
// import retrofit2.http.Query; // Keep if any other endpoint uses it, remove if not.

public interface ApiService {

    @POST("api/auth/register") // Updated path
    retrofit2.Call<Map<String, Object>> register(@Body User user);

    @POST("api/auth/login") // Updated path
    retrofit2.Call<Map<String, Object>> login(@Body Map<String, String> body);

    // Replaced nearbyStations with getActiveStations as per spec
    // If nearbyStations with lat/lng is still needed, it would be a separate API endpoint
    @GET("api/chargingstations/active")
    retrofit2.Call<List<Station>> getActiveStations(@Header("Authorization") String authToken);

    @POST("api/bookings") // Updated path
    Call<Map<String, Object>> createBooking(@Header("Authorization") String authToken, @Body Booking booking); // Or a specific CreateBookingRequest model

    @GET("api/bookings/my-bookings") // Updated path
    retrofit2.Call<List<Booking>> getMyBookings(@Header("Authorization") String authToken);

    @POST("api/users/deactivate")
    retrofit2.Call<Map<String, Object>> deactivateAccount(@Header("Authorization") String authToken);
}
