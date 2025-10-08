/*
 * File: Notification.cs
 * Project: EV Charging Station Booking System
 * Description: Notification model for system notifications
 * Author: EV Charging System
 * Date: October 7, 2025
 */

using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace EVChargingBackend.Models
{
    /// <summary>
    /// Represents a notification in the system
    /// </summary>
    public class Notification
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string? Id { get; set; }

        [BsonElement("recipientNIC")]
        public string RecipientNIC { get; set; } = string.Empty;

        [BsonElement("title")]
        public string Title { get; set; } = string.Empty;

        [BsonElement("message")]
        public string Message { get; set; } = string.Empty;

        [BsonElement("type")]
        public NotificationType Type { get; set; }

        [BsonElement("relatedEntityId")]
        public string? RelatedEntityId { get; set; }

        [BsonElement("relatedEntityType")]
        public string? RelatedEntityType { get; set; }

        [BsonElement("isRead")]
        public bool IsRead { get; set; } = false;

        [BsonElement("isDelivered")]
        public bool IsDelivered { get; set; } = false;

        [BsonElement("priority")]
        public NotificationPriority Priority { get; set; } = NotificationPriority.Normal;

        [BsonElement("createdAt")]
        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

        [BsonElement("readAt")]
        public DateTime? ReadAt { get; set; }

        [BsonElement("deliveredAt")]
        public DateTime? DeliveredAt { get; set; }

        [BsonElement("expiresAt")]
        public DateTime? ExpiresAt { get; set; }

        [BsonElement("metadata")]
        public Dictionary<string, object>? Metadata { get; set; }
    }

    /// <summary>
    /// Types of notifications in the system
    /// </summary>
    public enum NotificationType
    {
        BookingConfirmation = 0,
        BookingCancellation = 1,
        BookingReminder = 2,
        StationUpdate = 3,
        SystemAlert = 4,
        PaymentConfirmation = 5,
        BookingExpired = 6
    }

    /// <summary>
    /// Priority levels for notifications
    /// </summary>
    public enum NotificationPriority
    {
        Low = 0,
        Normal = 1,
        High = 2,
        Critical = 3
    }
}