/*
 * File: IBookingRepository.cs
 * Project: EV Charging Station Booking System
 * Description: Interface for Booking repository operations
 * Author: EV Charging System
 * Date: September 27, 2025
 */

using EVChargingBackend.Models;

namespace EVChargingBackend.Repositories
{
    /// <summary>
    /// Interface defining booking repository operations
    /// </summary>
    public interface IBookingRepository
    {
        /// <summary>
        /// Creates a new booking
        /// </summary>
        /// <param name="booking">Booking to create</param>
        /// <returns>Created booking</returns>
        Task<Booking> CreateAsync(Booking booking);

        /// <summary>
        /// Gets booking by ID
        /// </summary>
        /// <param name="id">Booking ID</param>
        /// <returns>Booking if found, null otherwise</returns>
        Task<Booking?> GetByIdAsync(string id);

        /// <summary>
        /// Gets all bookings for a specific user
        /// </summary>
        /// <param name="ownerNic">Owner NIC</param>
        /// <returns>List of user bookings</returns>
        Task<List<Booking>> GetByOwnerAsync(string ownerNic);

        /// <summary>
        /// Gets all bookings for a specific station
        /// </summary>
        /// <param name="stationId">Station ID</param>
        /// <returns>List of station bookings</returns>
        Task<List<Booking>> GetByStationAsync(string stationId);

        /// <summary>
        /// Gets active bookings for a station within a time range
        /// </summary>
        /// <param name="stationId">Station ID</param>
        /// <param name="startTime">Start time</param>
        /// <param name="endTime">End time</param>
        /// <returns>List of overlapping bookings</returns>
        Task<List<Booking>> GetOverlappingBookingsAsync(string stationId, DateTime startTime, DateTime endTime);

        /// <summary>
        /// Gets all bookings
        /// </summary>
        /// <returns>List of all bookings</returns>
        Task<List<Booking>> GetAllAsync();

        /// <summary>
        /// Updates booking information
        /// </summary>
        /// <param name="booking">Booking with updated information</param>
        /// <returns>Updated booking</returns>
        Task<Booking> UpdateAsync(Booking booking);

        /// <summary>
        /// Confirms a booking (operator action)
        /// </summary>
        /// <param name="bookingId">Booking ID</param>
        /// <returns>Success status</returns>
        Task<bool> ConfirmBookingAsync(string bookingId);

        /// <summary>
        /// Cancels a booking
        /// </summary>
        /// <param name="bookingId">Booking ID</param>
        /// <returns>Success status</returns>
        Task<bool> CancelBookingAsync(string bookingId);

        /// <summary>
        /// Gets active bookings for a station (for slot management)
        /// </summary>
        /// <param name="stationId">Station ID</param>
        /// <returns>Count of active bookings</returns>
        Task<long> GetActiveBookingCountAsync(string stationId);

        /// <summary>
        /// Gets upcoming confirmed bookings within a time range (for reminders)
        /// </summary>
        /// <param name="fromTime">Start time range</param>
        /// <param name="toTime">End time range</param>
        /// <returns>List of upcoming confirmed bookings</returns>
        Task<List<Booking>> GetUpcomingConfirmedBookingsAsync(DateTime fromTime, DateTime toTime);
    }
}