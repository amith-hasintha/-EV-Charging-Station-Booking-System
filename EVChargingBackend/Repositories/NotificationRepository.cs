/*
 * File: NotificationRepository.cs
 * Project: EV Charging Station Booking System
 * Description: Repository implementation for notification operations
 * Author: EV Charging System
 * Date: October 7, 2025
 */

using EVChargingBackend.Config;
using EVChargingBackend.Models;
using Microsoft.Extensions.Options;
using MongoDB.Driver;

namespace EVChargingBackend.Repositories
{
    /// <summary>
    /// Repository implementation for notification operations
    /// </summary>
    public class NotificationRepository : INotificationRepository
    {
        private readonly IMongoCollection<Notification> _notifications;
        private readonly ILogger<NotificationRepository> _logger;

        /// <summary>
        /// Initializes notification repository
        /// </summary>
        /// <param name="context">MongoDB context</param>
        /// <param name="logger">Logger</param>
        public NotificationRepository(MongoDbContext context, ILogger<NotificationRepository> logger)
        {
            _notifications = context.Notifications;
            _logger = logger;
        }

        /// <summary>
        /// Creates a new notification
        /// </summary>
        /// <param name="notification">Notification to create</param>
        /// <returns>Created notification</returns>
        public async Task<Notification> CreateAsync(Notification notification)
        {
            await _notifications.InsertOneAsync(notification);
            _logger.LogInformation("Created notification {NotificationId} for user {RecipientNIC}", notification.Id, notification.RecipientNIC);
            return notification;
        }

        /// <summary>
        /// Creates multiple notifications at once
        /// </summary>
        /// <param name="notifications">List of notifications to create</param>
        /// <returns>List of created notifications</returns>
        public async Task<List<Notification>> CreateManyAsync(List<Notification> notifications)
        {
            if (notifications.Any())
            {
                await _notifications.InsertManyAsync(notifications);
                _logger.LogInformation("Created {Count} notifications", notifications.Count);
            }
            return notifications;
        }

        /// <summary>
        /// Gets notification by ID
        /// </summary>
        /// <param name="id">Notification ID</param>
        /// <returns>Notification if found</returns>
        public async Task<Notification?> GetByIdAsync(string id)
        {
            return await _notifications.Find(n => n.Id == id).FirstOrDefaultAsync();
        }

        /// <summary>
        /// Gets all notifications for a specific user
        /// </summary>
        /// <param name="recipientNIC">User NIC</param>
        /// <param name="includeRead">Include read notifications</param>
        /// <param name="limit">Maximum number of notifications</param>
        /// <param name="offset">Number of notifications to skip</param>
        /// <returns>List of notifications</returns>
        public async Task<List<Notification>> GetByRecipientAsync(string recipientNIC, bool includeRead = true, int limit = 50, int offset = 0)
        {
            var filter = Builders<Notification>.Filter.Eq(n => n.RecipientNIC, recipientNIC);
            
            if (!includeRead)
            {
                filter = filter & Builders<Notification>.Filter.Eq(n => n.IsRead, false);
            }

            return await _notifications
                .Find(filter)
                .SortByDescending(n => n.CreatedAt)
                .Skip(offset)
                .Limit(limit)
                .ToListAsync();
        }

        /// <summary>
        /// Gets unread notifications for a specific user
        /// </summary>
        /// <param name="recipientNIC">User NIC</param>
        /// <returns>List of unread notifications</returns>
        public async Task<List<Notification>> GetUnreadByRecipientAsync(string recipientNIC)
        {
            var filter = Builders<Notification>.Filter.Eq(n => n.RecipientNIC, recipientNIC) &
                        Builders<Notification>.Filter.Eq(n => n.IsRead, false);

            return await _notifications
                .Find(filter)
                .SortByDescending(n => n.CreatedAt)
                .ToListAsync();
        }

        /// <summary>
        /// Gets notifications by type for a specific user
        /// </summary>
        /// <param name="recipientNIC">User NIC</param>
        /// <param name="type">Notification type</param>
        /// <param name="limit">Maximum number of notifications</param>
        /// <returns>List of notifications</returns>
        public async Task<List<Notification>> GetByRecipientAndTypeAsync(string recipientNIC, NotificationType type, int limit = 20)
        {
            var filter = Builders<Notification>.Filter.Eq(n => n.RecipientNIC, recipientNIC) &
                        Builders<Notification>.Filter.Eq(n => n.Type, type);

            return await _notifications
                .Find(filter)
                .SortByDescending(n => n.CreatedAt)
                .Limit(limit)
                .ToListAsync();
        }

        /// <summary>
        /// Gets notifications related to a specific entity
        /// </summary>
        /// <param name="entityId">Related entity ID</param>
        /// <param name="entityType">Related entity type</param>
        /// <returns>List of notifications</returns>
        public async Task<List<Notification>> GetByRelatedEntityAsync(string entityId, string entityType)
        {
            var filter = Builders<Notification>.Filter.Eq(n => n.RelatedEntityId, entityId) &
                        Builders<Notification>.Filter.Eq(n => n.RelatedEntityType, entityType);

            return await _notifications
                .Find(filter)
                .SortByDescending(n => n.CreatedAt)
                .ToListAsync();
        }

        /// <summary>
        /// Marks a notification as read
        /// </summary>
        /// <param name="id">Notification ID</param>
        /// <returns>Success status</returns>
        public async Task<bool> MarkAsReadAsync(string id)
        {
            var update = Builders<Notification>.Update
                .Set(n => n.IsRead, true)
                .Set(n => n.ReadAt, DateTime.UtcNow);

            var result = await _notifications.UpdateOneAsync(n => n.Id == id, update);
            
            if (result.ModifiedCount > 0)
            {
                _logger.LogInformation("Marked notification {NotificationId} as read", id);
            }

            return result.ModifiedCount > 0;
        }

        /// <summary>
        /// Marks multiple notifications as read
        /// </summary>
        /// <param name="ids">List of notification IDs</param>
        /// <returns>Number of notifications marked as read</returns>
        public async Task<int> MarkMultipleAsReadAsync(List<string> ids)
        {
            var filter = Builders<Notification>.Filter.In(n => n.Id, ids);
            var update = Builders<Notification>.Update
                .Set(n => n.IsRead, true)
                .Set(n => n.ReadAt, DateTime.UtcNow);

            var result = await _notifications.UpdateManyAsync(filter, update);
            
            _logger.LogInformation("Marked {Count} notifications as read", result.ModifiedCount);
            
            return (int)result.ModifiedCount;
        }

        /// <summary>
        /// Marks all notifications for a user as read
        /// </summary>
        /// <param name="recipientNIC">User NIC</param>
        /// <returns>Number of notifications marked as read</returns>
        public async Task<int> MarkAllAsReadAsync(string recipientNIC)
        {
            var filter = Builders<Notification>.Filter.Eq(n => n.RecipientNIC, recipientNIC) &
                        Builders<Notification>.Filter.Eq(n => n.IsRead, false);
            
            var update = Builders<Notification>.Update
                .Set(n => n.IsRead, true)
                .Set(n => n.ReadAt, DateTime.UtcNow);

            var result = await _notifications.UpdateManyAsync(filter, update);
            
            _logger.LogInformation("Marked all {Count} notifications as read for user {RecipientNIC}", result.ModifiedCount, recipientNIC);
            
            return (int)result.ModifiedCount;
        }

        /// <summary>
        /// Marks a notification as delivered
        /// </summary>
        /// <param name="id">Notification ID</param>
        /// <returns>Success status</returns>
        public async Task<bool> MarkAsDeliveredAsync(string id)
        {
            var update = Builders<Notification>.Update
                .Set(n => n.IsDelivered, true)
                .Set(n => n.DeliveredAt, DateTime.UtcNow);

            var result = await _notifications.UpdateOneAsync(n => n.Id == id, update);
            
            return result.ModifiedCount > 0;
        }

        /// <summary>
        /// Deletes a notification
        /// </summary>
        /// <param name="id">Notification ID</param>
        /// <returns>Success status</returns>
        public async Task<bool> DeleteAsync(string id)
        {
            var result = await _notifications.DeleteOneAsync(n => n.Id == id);
            
            if (result.DeletedCount > 0)
            {
                _logger.LogInformation("Deleted notification {NotificationId}", id);
            }

            return result.DeletedCount > 0;
        }

        /// <summary>
        /// Deletes expired notifications
        /// </summary>
        /// <returns>Number of deleted notifications</returns>
        public async Task<int> DeleteExpiredNotificationsAsync()
        {
            var filter = Builders<Notification>.Filter.Lt(n => n.ExpiresAt, DateTime.UtcNow);
            var result = await _notifications.DeleteManyAsync(filter);
            
            _logger.LogInformation("Deleted {Count} expired notifications", result.DeletedCount);
            
            return (int)result.DeletedCount;
        }

        /// <summary>
        /// Gets notification count for a user
        /// </summary>
        /// <param name="recipientNIC">User NIC</param>
        /// <param name="includeRead">Include read notifications in count</param>
        /// <returns>Notification count</returns>
        public async Task<int> GetNotificationCountAsync(string recipientNIC, bool includeRead = true)
        {
            var filter = Builders<Notification>.Filter.Eq(n => n.RecipientNIC, recipientNIC);
            
            if (!includeRead)
            {
                filter = filter & Builders<Notification>.Filter.Eq(n => n.IsRead, false);
            }

            return (int)await _notifications.CountDocumentsAsync(filter);
        }

        /// <summary>
        /// Gets unread notification count for a user
        /// </summary>
        /// <param name="recipientNIC">User NIC</param>
        /// <returns>Unread notification count</returns>
        public async Task<int> GetUnreadCountAsync(string recipientNIC)
        {
            var filter = Builders<Notification>.Filter.Eq(n => n.RecipientNIC, recipientNIC) &
                        Builders<Notification>.Filter.Eq(n => n.IsRead, false);

            return (int)await _notifications.CountDocumentsAsync(filter);
        }
    }
}