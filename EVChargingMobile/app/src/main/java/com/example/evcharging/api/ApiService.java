package com.example.evcharging.api;

import com.example.evcharging.models.Booking;
import com.example.evcharging.models.Station;
import com.example.evcharging.models.User;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {

    // --- Authentication ---
    @POST("api/auth/login")
    Call<Map<String, Object>> login(@Body Map<String, String> body);

    @POST("api/auth/register")
    Call<Map<String, Object>> register(@Body User user);

    // --- User Profile ---
    @GET("api/users/me")
    Call<User> getMyProfile(@Header("Authorization") String token);

    @PUT("api/users/me")
    Call<User> updateMyProfile(@Header("Authorization") String token, @Body User user);

    @POST("api/users/deactivate")
    Call<Void> deactivateMyAccount(@Header("Authorization") String token);


    // --- Stations ---
    // THE CRITICAL FIX: Reverted back to "chargingstations" as per your API documentation.
    @GET("api/chargingstations/active")
    Call<List<Station>> getActiveStations(@Header("Authorization") String token);

    // THE CRITICAL FIX: Reverted back to "chargingstations" as per your API documentation.
    @GET("api/chargingstations")
    Call<List<Station>> getAllStations(@Header("Authorization") String token);


    // --- EV Owner Bookings ---
    // This path is correct as per your docs.
    @GET("api/bookings/my-bookings")
    Call<List<Booking>> getMyBookings(@Header("Authorization") String token);

    @POST("api/bookings")
    Call<Map<String, Object>> createBooking(@Header("Authorization") String token, @Body Map<String, String> body);

    @POST("api/bookings/{id}/cancel")
    Call<Void> cancelBooking(@Header("Authorization") String token, @Path("id") String bookingId);


    // --- Operator/Admin Bookings ---
    @GET("api/bookings")
    Call<List<Booking>> getAllBookings(@Header("Authorization") String token);

    @GET("api/bookings/{id}")
    Call<Booking> getBookingById(@Header("Authorization") String token, @Path("id") String bookingId);

    @POST("api/bookings/{id}/confirm")
    Call<Void> confirmBooking(@Header("Authorization") String token, @Path("id") String bookingId);

    @POST("api/bookings/{id}/approve")
    Call<Void> approveBooking(@Header("Authorization") String token, @Path("id") String bookingId);

    @POST("api/bookings/{id}/reject")
    Call<Void> rejectBooking(@Header("Authorization") String token, @Path("id") String bookingId);

    @POST("api/bookings/{id}/finalize")
    Call<Void> finalizeBooking(@Header("Authorization") String token, @Path("id") String bookingId);
}
