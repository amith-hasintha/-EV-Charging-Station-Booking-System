package com.example.evcharging.api;

import com.example.evcharging.models.BookingApi;
import com.example.evcharging.models.CancellationReason;
import com.example.evcharging.models.Station;
import com.example.evcharging.models.User;
import com.example.evcharging.models.Notification;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

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
    @GET("api/chargingstations/active")
    Call<List<Station>> getActiveStations(@Header("Authorization") String token);

    @GET("api/chargingstations")
    Call<List<Station>> getAllStations(@Header("Authorization") String token);


    // --- EV Owner Bookings ---
    @GET("api/bookings/my-bookings")
    Call<List<BookingApi>> getMyBookings(@Header("Authorization") String token);

    @POST("api/bookings")
    Call<Map<String, Object>> createBooking(@Header("Authorization") String token, @Body Map<String, String> body);

    @POST("api/bookings/{id}/cancel")
    Call<Void> cancelBooking(@Header("Authorization") String token, @Path("id") String bookingId);


    // --- Operator/Admin Bookings ---
    @GET("api/bookings")
    Call<List<BookingApi>> getAllBookings(@Header("Authorization") String token);

    @GET("api/bookings/{id}")
    Call<BookingApi> getBookingById(@Header("Authorization") String token, @Path("id") String bookingId);

    @GET("api/bookings/station/{stationId}")
    Call<List<BookingApi>> getStationBookings(@Header("Authorization") String token, @Path("stationId") String stationId);

    @POST("api/bookings/{id}/finalize")
    Call<Void> finalizeBooking(@Header("Authorization") String token, @Path("id") String bookingId);

    // --- START: CORRECTED OPERATOR ACTIONS ---
    // The old "approveBooking" and "rejectBooking" methods have been completely removed.

    @POST("api/bookings/{id}/confirm")
    Call<Void> confirmBooking(@Header("Authorization") String token, @Path("id") String bookingId);

    @POST("api/bookings/{id}/cancel-by-operator")
    Call<Void> cancelBookingByOperator(
            @Header("Authorization") String token,
            @Path("id") String bookingId,
            @Body CancellationReason reason
    );
    // --- END: CORRECTED OPERATOR ACTIONS ---


    // --- Notifications ---
    @GET("api/notifications/my-notifications")
    Call<List<Notification>> getMyNotifications(@Header("Authorization") String token);

    @GET("api/notifications/my-notifications")
    Call<List<Notification>> getMyNotificationsWithQuery(
            @Header("Authorization") String token,
            @Query("includeRead") boolean includeRead,
            @Query("limit") int limit
    );
}
