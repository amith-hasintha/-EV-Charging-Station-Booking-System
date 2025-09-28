/*
 * File: BookingDTOs.cs
 * Project: EV Charging Station Booking System
 * Description: Data Transfer Objects for Booking operations
 * Author: EV Charging System
 * Date: September 27, 2025
 */

using EVChargingBackend.Models;
using System.ComponentModel.DataAnnotations;

namespace EVChargingBackend.DTOs
{
    /// <summary>
    /// DTO for creating a new booking
    /// </summary>
    public class CreateBookingDto
    {
        [Required]
        public string StationId { get; set; } = string.Empty;

        [Required]
        public DateTime StartTime { get; set; }

        [Required]
        public DateTime EndTime { get; set; }
    }

    /// <summary>
    /// DTO for updating booking information
    /// </summary>
    public class UpdateBookingDto
    {
        public DateTime? StartTime { get; set; }
        public DateTime? EndTime { get; set; }
        public BookingStatus? Status { get; set; }
    }

    /// <summary>
    /// DTO for booking response with station details
    /// </summary>
    public class BookingResponseDto
    {
        public string Id { get; set; } = string.Empty;
        public string OwnerNIC { get; set; } = string.Empty;
        public string StationId { get; set; } = string.Empty;
        public string StationName { get; set; } = string.Empty;
        public string StationLocation { get; set; } = string.Empty;
        public DateTime StartTime { get; set; }
        public DateTime EndTime { get; set; }
        public BookingStatus Status { get; set; }
        public string QRCode { get; set; } = string.Empty;
        public decimal TotalAmount { get; set; }
        public DateTime CreatedAt { get; set; }
        public DateTime? ConfirmedAt { get; set; }
        public DateTime? CancelledAt { get; set; }
    }

    /// <summary>
    /// DTO for booking confirmation by station operator
    /// </summary>
    public class ConfirmBookingDto
    {
        [Required]
        public string BookingId { get; set; } = string.Empty;
    }
}