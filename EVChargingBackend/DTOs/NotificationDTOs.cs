/*
 * File: NotificationDTOs.cs
 * Project: EV Charging Station Booking System
 * Description: Data Transfer Objects for notification operations
 * Author: EV Charging System
 * Date: October 7, 2025
 */

using EVChargingBackend.Models;
using System.ComponentModel.DataAnnotations;

namespace EVChargingBackend.DTOs
{
    /// <summary>
    /// DTO for creating notifications
    /// </summary>
    public class CreateNotificationDto
    {
        [Required]
        public string RecipientNIC { get; set; } = string.Empty;

        [Required]
        [StringLength(200)]
        public string Title { get; set; } = string.Empty;

        [Required]
        [StringLength(1000)]
        public string Message { get; set; } = string.Empty;

        [Required]
        public NotificationType Type { get; set; }

        public string? RelatedEntityId { get; set; }

        public string? RelatedEntityType { get; set; }

        public NotificationPriority Priority { get; set; } = NotificationPriority.Normal;

        public DateTime? ExpiresAt { get; set; }

        public Dictionary<string, object>? Metadata { get; set; }
    }

    /// <summary>
    /// DTO for notification responses
    /// </summary>
    public class NotificationResponseDto
    {
        public string Id { get; set; } = string.Empty;
        public string RecipientNIC { get; set; } = string.Empty;
        public string Title { get; set; } = string.Empty;
        public string Message { get; set; } = string.Empty;
        public NotificationType Type { get; set; }
        public string? RelatedEntityId { get; set; }
        public string? RelatedEntityType { get; set; }
        public bool IsRead { get; set; }
        public bool IsDelivered { get; set; }
        public NotificationPriority Priority { get; set; }
        public DateTime CreatedAt { get; set; }
        public DateTime? ReadAt { get; set; }
        public DateTime? DeliveredAt { get; set; }
        public DateTime? ExpiresAt { get; set; }
        public Dictionary<string, object>? Metadata { get; set; }
    }

    /// <summary>
    /// DTO for updating notification read status
    /// </summary>
    public class MarkNotificationReadDto
    {
        [Required]
        public List<string> NotificationIds { get; set; } = new List<string>();
    }

    /// <summary>
    /// DTO for notification summary/statistics
    /// </summary>
    public class NotificationSummaryDto
    {
        public int TotalNotifications { get; set; }
        public int UnreadNotifications { get; set; }
        public int HighPriorityNotifications { get; set; }
        public int CriticalNotifications { get; set; }
        public DateTime? LastNotificationTime { get; set; }
    }

    /// <summary>
    /// DTO for bulk notification creation
    /// </summary>
    public class BulkNotificationDto
    {
        [Required]
        public List<string> RecipientNICs { get; set; } = new List<string>();

        [Required]
        [StringLength(200)]
        public string Title { get; set; } = string.Empty;

        [Required]
        [StringLength(1000)]
        public string Message { get; set; } = string.Empty;

        [Required]
        public NotificationType Type { get; set; }

        public NotificationPriority Priority { get; set; } = NotificationPriority.Normal;

        public DateTime? ExpiresAt { get; set; }

        public Dictionary<string, object>? Metadata { get; set; }
    }
}