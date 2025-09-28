/*
 * File: IBookingService.cs
 * Project: EV Charging Station Booking System
 * Description: Interface for booking management services
 * Author: EV Charging System
 * Date: September 27, 2025
 */

using EVChargingBackend.DTOs;

namespace EVChargingBackend.Services
{
    /// <summary>
    /// Interface defining booking management service operations
    /// </summary>
    public interface IBookingService
    {
        /// <summary>
        /// Creates a new booking with business rule validation
        /// </summary>
        /// <param name="createDto">Booking creation data</param>
        /// <param name="ownerNic">Owner NIC from JWT token</param>
        /// <returns>Created booking</returns>
        Task<BookingResponseDto> CreateBookingAsync(CreateBookingDto createDto, string ownerNic);

        /// <summary>
        /// Gets all bookings (backoffice and operators)
        /// </summary>
        /// <returns>List of all bookings</returns>
        Task<List<BookingResponseDto>> GetAllBookingsAsync();

        /// <summary>
        /// Gets bookings for specific user
        /// </summary>
        /// <param name="ownerNic">Owner NIC</param>
        /// <returns>List of user bookings</returns>
        Task<List<BookingResponseDto>> GetUserBookingsAsync(string ownerNic);

        /// <summary>
        /// Gets bookings for specific station (operators)
        /// </summary>
        /// <param name="stationId">Station ID</param>
        /// <returns>List of station bookings</returns>
        Task<List<BookingResponseDto>> GetStationBookingsAsync(string stationId);

        /// <summary>
        /// Gets booking by ID
        /// </summary>
        /// <param name="id">Booking ID</param>
        /// <returns>Booking if found</returns>
        Task<BookingResponseDto> GetBookingByIdAsync(string id);

        /// <summary>
        /// Updates booking with business rule validation
        /// </summary>
        /// <param name="id">Booking ID</param>
        /// <param name="updateDto">Updated booking data</param>
        /// <param name="ownerNic">Owner NIC from JWT token</param>
        /// <returns>Updated booking</returns>
        Task<BookingResponseDto> UpdateBookingAsync(string id, UpdateBookingDto updateDto, string ownerNic);

        /// <summary>
        /// Confirms booking (station operator action)
        /// </summary>
        /// <param name="bookingId">Booking ID</param>
        /// <returns>Success status</returns>
        Task<bool> ConfirmBookingAsync(string bookingId);

        /// <summary>
        /// Cancels booking with time validation
        /// </summary>
        /// <param name="id">Booking ID</param>
        /// <param name="ownerNic">Owner NIC from JWT token</param>
        /// <returns>Success status</returns>
        Task<bool> CancelBookingAsync(string id, string ownerNic);
    }
}