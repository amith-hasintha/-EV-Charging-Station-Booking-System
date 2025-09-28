/*
 * File: ChargingStationDTOs.cs
 * Project: EV Charging Station Booking System
 * Description: Data Transfer Objects for Charging Station operations
 * Author: EV Charging System
 * Date: September 27, 2025
 */

using EVChargingBackend.Models;
using System.ComponentModel.DataAnnotations;

namespace EVChargingBackend.DTOs
{
    /// <summary>
    /// DTO for creating a new charging station
    /// </summary>
    public class CreateStationDto
    {
        [Required]
        public string Name { get; set; } = string.Empty;

        [Required]
        public string Location { get; set; } = string.Empty;

        [Required]
        public StationType Type { get; set; }

        [Required]
        [Range(1, 50)]
        public int TotalSlots { get; set; }

        [Required]
        [Range(0.01, 1000.00)]
        public decimal PricePerHour { get; set; }
    }

    /// <summary>
    /// DTO for updating charging station information
    /// </summary>
    public class UpdateStationDto
    {
        public string? Name { get; set; }
        public string? Location { get; set; }
        public StationType? Type { get; set; }
        public int? TotalSlots { get; set; }
        public decimal? PricePerHour { get; set; }
        public StationStatus? Status { get; set; }
    }

    /// <summary>
    /// DTO for charging station response
    /// </summary>
    public class StationResponseDto
    {
        public string Id { get; set; } = string.Empty;
        public string Name { get; set; } = string.Empty;
        public string Location { get; set; } = string.Empty;
        public StationType Type { get; set; }
        public int TotalSlots { get; set; }
        public int AvailableSlots { get; set; }
        public StationStatus Status { get; set; }
        public decimal PricePerHour { get; set; }
        public DateTime CreatedAt { get; set; }
        public DateTime UpdatedAt { get; set; }
    }
}