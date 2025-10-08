/*
 * File: INotificationRepository.cs
 * Project: EV Charging Station Booking System
 * Description: Interface for notification repository operations
 * Author: EV Charging System
 * Date: October 7, 2025
 */

using EVChargingBackend.Models;

namespace EVChargingBackend.Repositories
{
    /// <summary>
    /// Interface for notification repository operations
    /// </summary>
    public interface INotificationRepository
    {
        /// <summary>
        /// Creates a new notification
        /// </summary>
        /// <param name="notification">Notification to create</param>
        /// <returns>Created notification</returns>
        Task<Notification> CreateAsync(Notification notification);

        /// <summary>
        /// Creates multiple notifications at once
        /// </summary>
        /// <param name="notifications">List of notifications to create</param>
        /// <returns>List of created notifications</returns>
        Task<List<Notification>> CreateManyAsync(List<Notification> notifications);

        /// <summary>
        /// Gets notification by ID
        /// </summary>
        /// <param name="id">Notification ID</param>
        /// <returns>Notification if found</returns>
        Task<Notification?> GetByIdAsync(string id);

        /// <summary>
        /// Gets all notifications for a specific user
        /// </summary>
        /// <param name="recipientNIC">User NIC</param>
        /// <param name="includeRead">Include read notifications</param>
        /// <param name="limit">Maximum number of notifications</param>
        /// <param name="offset">Number of notifications to skip</param>
        /// <returns>List of notifications</returns>
        Task<List<Notification>> GetByRecipientAsync(string recipientNIC, bool includeRead = true, int limit = 50, int offset = 0);

        /// <summary>
        /// Gets unread notifications for a specific user
        /// </summary>
        /// <param name="recipientNIC">User NIC</param>
        /// <returns>List of unread notifications</returns>
        Task<List<Notification>> GetUnreadByRecipientAsync(string recipientNIC);

        /// <summary>
        /// Gets notifications by type for a specific user
        /// </summary>
        /// <param name="recipientNIC">User NIC</param>
        /// <param name="type">Notification type</param>
        /// <param name="limit">Maximum number of notifications</param>
        /// <returns>List of notifications</returns>
        Task<List<Notification>> GetByRecipientAndTypeAsync(string recipientNIC, NotificationType type, int limit = 20);

        /// <summary>
        /// Gets notifications related to a specific entity
        /// </summary>
        /// <param name="entityId">Related entity ID</param>
        /// <param name="entityType">Related entity type</param>
        /// <returns>List of notifications</returns>
        Task<List<Notification>> GetByRelatedEntityAsync(string entityId, string entityType);

        /// <summary>
        /// Marks a notification as read
        /// </summary>
        /// <param name="id">Notification ID</param>
        /// <returns>Success status</returns>
        Task<bool> MarkAsReadAsync(string id);

        /// <summary>
        /// Marks multiple notifications as read
        /// </summary>
        /// <param name="ids">List of notification IDs</param>
        /// <returns>Number of notifications marked as read</returns>
        Task<int> MarkMultipleAsReadAsync(List<string> ids);

        /// <summary>
        /// Marks all notifications for a user as read
        /// </summary>
        /// <param name="recipientNIC">User NIC</param>
        /// <returns>Number of notifications marked as read</returns>
        Task<int> MarkAllAsReadAsync(string recipientNIC);

        /// <summary>
        /// Marks a notification as delivered
        /// </summary>
        /// <param name="id">Notification ID</param>
        /// <returns>Success status</returns>
        Task<bool> MarkAsDeliveredAsync(string id);

        /// <summary>
        /// Deletes a notification
        /// </summary>
        /// <param name="id">Notification ID</param>
        /// <returns>Success status</returns>
        Task<bool> DeleteAsync(string id);

        /// <summary>
        /// Deletes expired notifications
        /// </summary>
        /// <returns>Number of deleted notifications</returns>
        Task<int> DeleteExpiredNotificationsAsync();

        /// <summary>
        /// Gets notification count for a user
        /// </summary>
        /// <param name="recipientNIC">User NIC</param>
        /// <param name="includeRead">Include read notifications in count</param>
        /// <returns>Notification count</returns>
        Task<int> GetNotificationCountAsync(string recipientNIC, bool includeRead = true);

        /// <summary>
        /// Gets unread notification count for a user
        /// </summary>
        /// <param name="recipientNIC">User NIC</param>
        /// <returns>Unread notification count</returns>
        Task<int> GetUnreadCountAsync(string recipientNIC);
    }
}