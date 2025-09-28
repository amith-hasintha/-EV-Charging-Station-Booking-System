/*
 * File: Booking.cs
 * Project: EV Charging Station Booking System
 * Description: Booking model with QR code generation and business rules validation
 * Author: EV Charging System
 * Date: September 27, 2025
 */

using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace EVChargingBackend.Models
{
    /// <summary>
    /// Represents a charging station booking with QR code and time restrictions
    /// </summary>
    public class Booking
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string? Id { get; set; }

        [BsonElement("ownerNIC")]
        public string OwnerNIC { get; set; } = string.Empty;

        [BsonElement("stationId")]
        public string StationId { get; set; } = string.Empty;

        [BsonElement("startTime")]
        public DateTime StartTime { get; set; }

        [BsonElement("endTime")]
        public DateTime EndTime { get; set; }

        [BsonElement("status")]
        public BookingStatus Status { get; set; } = BookingStatus.Active;

        [BsonElement("qrCode")]
        public string QRCode { get; set; } = string.Empty;

        [BsonElement("totalAmount")]
        public decimal TotalAmount { get; set; }

        [BsonElement("createdAt")]
        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

        [BsonElement("updatedAt")]
        public DateTime UpdatedAt { get; set; } = DateTime.UtcNow;

        [BsonElement("confirmedAt")]
        public DateTime? ConfirmedAt { get; set; }

        [BsonElement("cancelledAt")]
        public DateTime? CancelledAt { get; set; }
    }

    /// <summary>
    /// Status enumeration for bookings
    /// </summary>
    public enum BookingStatus
    {
        Active = 0,
        Confirmed = 1,
        Completed = 2,
        Cancelled = 3,
        NoShow = 4
    }
}