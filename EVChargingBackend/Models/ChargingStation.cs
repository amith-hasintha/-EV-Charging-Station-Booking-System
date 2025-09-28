/*
 * File: ChargingStation.cs
 * Project: EV Charging Station Booking System
 * Description: Charging station model with AC/DC types and slot management
 * Author: EV Charging System
 * Date: September 27, 2025
 */

using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace EVChargingBackend.Models
{
    /// <summary>
    /// Represents a charging station with its properties and availability
    /// </summary>
    public class ChargingStation
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string? Id { get; set; }

        [BsonElement("name")]
        public string Name { get; set; } = string.Empty;

        [BsonElement("location")]
        public string Location { get; set; } = string.Empty;

        [BsonElement("type")]
        public StationType Type { get; set; }

        [BsonElement("totalSlots")]
        public int TotalSlots { get; set; }

        [BsonElement("availableSlots")]
        public int AvailableSlots { get; set; }

        [BsonElement("status")]
        public StationStatus Status { get; set; } = StationStatus.Active;

        [BsonElement("pricePerHour")]
        public decimal PricePerHour { get; set; }

        [BsonElement("createdAt")]
        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

        [BsonElement("updatedAt")]
        public DateTime UpdatedAt { get; set; } = DateTime.UtcNow;
    }

    /// <summary>
    /// Types of charging stations (AC or DC)
    /// </summary>
    public enum StationType
    {
        AC = 0,
        DC = 1
    }

    /// <summary>
    /// Status of charging stations
    /// </summary>
    public enum StationStatus
    {
        Active = 0,
        Inactive = 1,
        Maintenance = 2
    }
}