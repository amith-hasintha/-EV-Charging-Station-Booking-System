/*
 * File: NotificationService.cs
 * Project: EV Charging Station Booking System
 * Description: Service implementation for notification operations
 * Author: EV Charging System
 * Date: October 7, 2025
 */

using EVChargingBackend.DTOs;
using EVChargingBackend.Models;
using EVChargingBackend.Repositories;

namespace EVChargingBackend.Services
{
    /// <summary>
    /// Service implementation for notification operations
    /// </summary>
    public class NotificationService : INotificationService
    {
        private readonly INotificationRepository _notificationRepository;
        private readonly ILogger<NotificationService> _logger;

        /// <summary>
        /// Initializes notification service with dependencies
        /// </summary>
        /// <param name="notificationRepository">Notification repository</param>
        /// <param name="logger">Logger</param>
        public NotificationService(
            INotificationRepository notificationRepository,
            ILogger<NotificationService> logger)
        {
            _notificationRepository = notificationRepository;
            _logger = logger;
        }

        /// <summary>
        /// Creates a notification
        /// </summary>
        /// <param name="createDto">Notification creation data</param>
        /// <returns>Created notification</returns>
        public async Task<NotificationResponseDto> CreateNotificationAsync(CreateNotificationDto createDto)
        {
            var notification = new Notification
            {
                RecipientNIC = createDto.RecipientNIC,
                Title = createDto.Title,
                Message = createDto.Message,
                Type = createDto.Type,
                RelatedEntityId = createDto.RelatedEntityId,
                RelatedEntityType = createDto.RelatedEntityType,
                Priority = createDto.Priority,
                ExpiresAt = createDto.ExpiresAt,
                Metadata = createDto.Metadata
            };

            var createdNotification = await _notificationRepository.CreateAsync(notification);
            
            _logger.LogInformation("Created notification {NotificationId} for user {RecipientNIC}", 
                createdNotification.Id, createDto.RecipientNIC);

            return MapToResponseDto(createdNotification);
        }

        /// <summary>
        /// Creates bulk notifications for multiple recipients
        /// </summary>
        /// <param name="bulkDto">Bulk notification data</param>
        /// <returns>List of created notifications</returns>
        public async Task<List<NotificationResponseDto>> CreateBulkNotificationAsync(BulkNotificationDto bulkDto)
        {
            var notifications = bulkDto.RecipientNICs.Select(nic => new Notification
            {
                RecipientNIC = nic,
                Title = bulkDto.Title,
                Message = bulkDto.Message,
                Type = bulkDto.Type,
                Priority = bulkDto.Priority,
                ExpiresAt = bulkDto.ExpiresAt,
                Metadata = bulkDto.Metadata
            }).ToList();

            var createdNotifications = await _notificationRepository.CreateManyAsync(notifications);
            
            _logger.LogInformation("Created {Count} bulk notifications", createdNotifications.Count);

            return createdNotifications.Select(MapToResponseDto).ToList();
        }

        /// <summary>
        /// Creates a booking confirmation notification
        /// </summary>
        /// <param name="recipientNIC">Recipient's NIC</param>
        /// <param name="bookingId">Booking ID</param>
        /// <param name="stationName">Charging station name</param>
        /// <param name="startTime">Booking start time</param>
        /// <param name="endTime">Booking end time</param>
        /// <returns>Created notification</returns>
        public async Task<NotificationResponseDto> CreateBookingConfirmationNotificationAsync(
            string recipientNIC, string bookingId, string stationName, DateTime startTime, DateTime endTime)
        {
            var title = "Booking Confirmed";
            var message = $"Your booking at {stationName} has been confirmed for {startTime:yyyy-MM-dd HH:mm} - {endTime:yyyy-MM-dd HH:mm}. Your charging session is ready!";

            var createDto = new CreateNotificationDto
            {
                RecipientNIC = recipientNIC,
                Title = title,
                Message = message,
                Type = NotificationType.BookingConfirmation,
                RelatedEntityId = bookingId,
                RelatedEntityType = nameof(Booking),
                Priority = NotificationPriority.High,
                Metadata = new Dictionary<string, object>
                {
                    ["stationName"] = stationName,
                    ["startTime"] = startTime,
                    ["endTime"] = endTime,
                    ["bookingId"] = bookingId
                }
            };

            return await CreateNotificationAsync(createDto);
        }

        /// <summary>
        /// Creates a booking cancellation notification
        /// </summary>
        /// <param name="recipientNIC">Recipient's NIC</param>
        /// <param name="bookingId">Booking ID</param>
        /// <param name="stationName">Charging station name</param>
        /// <param name="reason">Cancellation reason</param>
        /// <returns>Created notification</returns>
        public async Task<NotificationResponseDto> CreateBookingCancellationNotificationAsync(
            string recipientNIC, string bookingId, string stationName, string reason = "")
        {
            var title = "Booking Cancelled";
            var message = $"Your booking at {stationName} has been cancelled";
            
            if (!string.IsNullOrEmpty(reason))
            {
                message += $". Reason: {reason}";
            }
            
            message += ". You can make a new booking anytime.";

            var createDto = new CreateNotificationDto
            {
                RecipientNIC = recipientNIC,
                Title = title,
                Message = message,
                Type = NotificationType.BookingCancellation,
                RelatedEntityId = bookingId,
                RelatedEntityType = nameof(Booking),
                Priority = NotificationPriority.High,
                Metadata = new Dictionary<string, object>
                {
                    ["stationName"] = stationName,
                    ["bookingId"] = bookingId,
                    ["reason"] = reason ?? ""
                }
            };

            return await CreateNotificationAsync(createDto);
        }

        /// <summary>
        /// Creates a booking reminder notification
        /// </summary>
        /// <param name="recipientNIC">Recipient's NIC</param>
        /// <param name="bookingId">Booking ID</param>
        /// <param name="stationName">Charging station name</param>
        /// <param name="startTime">Booking start time</param>
        /// <returns>Created notification</returns>
        public async Task<NotificationResponseDto> CreateBookingReminderNotificationAsync(
            string recipientNIC, string bookingId, string stationName, DateTime startTime)
        {
            var title = "Booking Reminder";
            var message = $"Reminder: Your charging session at {stationName} starts at {startTime:yyyy-MM-dd HH:mm}. Don't forget to arrive on time!";

            var createDto = new CreateNotificationDto
            {
                RecipientNIC = recipientNIC,
                Title = title,
                Message = message,
                Type = NotificationType.BookingReminder,
                RelatedEntityId = bookingId,
                RelatedEntityType = nameof(Booking),
                Priority = NotificationPriority.Normal,
                ExpiresAt = startTime.AddHours(2), // Reminder expires 2 hours after start time
                Metadata = new Dictionary<string, object>
                {
                    ["stationName"] = stationName,
                    ["startTime"] = startTime,
                    ["bookingId"] = bookingId
                }
            };

            return await CreateNotificationAsync(createDto);
        }

        /// <summary>
        /// Gets all notifications for a user
        /// </summary>
        /// <param name="recipientNIC">User NIC</param>
        /// <param name="includeRead">Include read notifications</param>
        /// <param name="limit">Maximum number of notifications</param>
        /// <param name="offset">Number of notifications to skip</param>
        /// <returns>List of notifications</returns>
        public async Task<List<NotificationResponseDto>> GetUserNotificationsAsync(
            string recipientNIC, bool includeRead = true, int limit = 50, int offset = 0)
        {
            var notifications = await _notificationRepository.GetByRecipientAsync(
                recipientNIC, includeRead, limit, offset);

            return notifications.Select(MapToResponseDto).ToList();
        }

        /// <summary>
        /// Gets unread notifications for a user
        /// </summary>
        /// <param name="recipientNIC">User NIC</param>
        /// <returns>List of unread notifications</returns>
        public async Task<List<NotificationResponseDto>> GetUnreadNotificationsAsync(string recipientNIC)
        {
            var notifications = await _notificationRepository.GetUnreadByRecipientAsync(recipientNIC);
            return notifications.Select(MapToResponseDto).ToList();
        }

        /// <summary>
        /// Gets notifications by type for a user
        /// </summary>
        /// <param name="recipientNIC">User NIC</param>
        /// <param name="type">Notification type</param>
        /// <param name="limit">Maximum number of notifications</param>
        /// <returns>List of notifications</returns>
        public async Task<List<NotificationResponseDto>> GetNotificationsByTypeAsync(
            string recipientNIC, NotificationType type, int limit = 20)
        {
            var notifications = await _notificationRepository.GetByRecipientAndTypeAsync(
                recipientNIC, type, limit);

            return notifications.Select(MapToResponseDto).ToList();
        }

        /// <summary>
        /// Gets notification by ID
        /// </summary>
        /// <param name="id">Notification ID</param>
        /// <returns>Notification if found</returns>
        public async Task<NotificationResponseDto> GetNotificationByIdAsync(string id)
        {
            var notification = await _notificationRepository.GetByIdAsync(id);
            
            if (notification == null)
            {
                throw new KeyNotFoundException("Notification not found");
            }

            return MapToResponseDto(notification);
        }

        /// <summary>
        /// Marks notifications as read
        /// </summary>
        /// <param name="notificationIds">List of notification IDs</param>
        /// <param name="userNIC">User NIC for validation</param>
        /// <returns>Number of notifications marked as read</returns>
        public async Task<int> MarkNotificationsAsReadAsync(List<string> notificationIds, string userNIC)
        {
            // Validate that all notifications belong to the user
            foreach (var id in notificationIds)
            {
                var notification = await _notificationRepository.GetByIdAsync(id);
                if (notification == null || notification.RecipientNIC != userNIC)
                {
                    throw new UnauthorizedAccessException($"Notification {id} not found or access denied");
                }
            }

            var markedCount = await _notificationRepository.MarkMultipleAsReadAsync(notificationIds);
            
            _logger.LogInformation("Marked {Count} notifications as read for user {UserNIC}", 
                markedCount, userNIC);

            return markedCount;
        }

        /// <summary>
        /// Marks all notifications for a user as read
        /// </summary>
        /// <param name="recipientNIC">User NIC</param>
        /// <returns>Number of notifications marked as read</returns>
        public async Task<int> MarkAllAsReadAsync(string recipientNIC)
        {
            var markedCount = await _notificationRepository.MarkAllAsReadAsync(recipientNIC);
            
            _logger.LogInformation("Marked all {Count} notifications as read for user {RecipientNIC}", 
                markedCount, recipientNIC);

            return markedCount;
        }

        /// <summary>
        /// Deletes a notification (soft delete by setting expiry)
        /// </summary>
        /// <param name="id">Notification ID</param>
        /// <param name="userNIC">User NIC for validation</param>
        /// <returns>Success status</returns>
        public async Task<bool> DeleteNotificationAsync(string id, string userNIC)
        {
            var notification = await _notificationRepository.GetByIdAsync(id);
            
            if (notification == null || notification.RecipientNIC != userNIC)
            {
                throw new UnauthorizedAccessException("Notification not found or access denied");
            }

            var result = await _notificationRepository.DeleteAsync(id);
            
            if (result)
            {
                _logger.LogInformation("Deleted notification {NotificationId} for user {UserNIC}", id, userNIC);
            }

            return result;
        }

        /// <summary>
        /// Gets notification summary for a user
        /// </summary>
        /// <param name="recipientNIC">User NIC</param>
        /// <returns>Notification summary</returns>
        public async Task<NotificationSummaryDto> GetNotificationSummaryAsync(string recipientNIC)
        {
            var totalCount = await _notificationRepository.GetNotificationCountAsync(recipientNIC, true);
            var unreadCount = await _notificationRepository.GetUnreadCountAsync(recipientNIC);
            
            var unreadNotifications = await _notificationRepository.GetUnreadByRecipientAsync(recipientNIC);
            var highPriorityCount = unreadNotifications.Count(n => n.Priority == NotificationPriority.High);
            var criticalCount = unreadNotifications.Count(n => n.Priority == NotificationPriority.Critical);
            
            var latestNotification = unreadNotifications.OrderByDescending(n => n.CreatedAt).FirstOrDefault();

            return new NotificationSummaryDto
            {
                TotalNotifications = totalCount,
                UnreadNotifications = unreadCount,
                HighPriorityNotifications = highPriorityCount,
                CriticalNotifications = criticalCount,
                LastNotificationTime = latestNotification?.CreatedAt
            };
        }

        /// <summary>
        /// Cleans up expired notifications
        /// </summary>
        /// <returns>Number of deleted notifications</returns>
        public async Task<int> CleanupExpiredNotificationsAsync()
        {
            var deletedCount = await _notificationRepository.DeleteExpiredNotificationsAsync();
            
            _logger.LogInformation("Cleaned up {Count} expired notifications", deletedCount);

            return deletedCount;
        }

        /// <summary>
        /// Maps notification entity to response DTO
        /// </summary>
        /// <param name="notification">Notification entity</param>
        /// <returns>Notification response DTO</returns>
        private static NotificationResponseDto MapToResponseDto(Notification notification)
        {
            return new NotificationResponseDto
            {
                Id = notification.Id ?? "",
                RecipientNIC = notification.RecipientNIC,
                Title = notification.Title,
                Message = notification.Message,
                Type = notification.Type,
                RelatedEntityId = notification.RelatedEntityId,
                RelatedEntityType = notification.RelatedEntityType,
                IsRead = notification.IsRead,
                IsDelivered = notification.IsDelivered,
                Priority = notification.Priority,
                CreatedAt = notification.CreatedAt,
                ReadAt = notification.ReadAt,
                DeliveredAt = notification.DeliveredAt,
                ExpiresAt = notification.ExpiresAt,
                Metadata = notification.Metadata
            };
        }
    }
}