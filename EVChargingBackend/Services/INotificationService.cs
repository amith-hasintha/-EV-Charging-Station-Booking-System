/*
 * File: INotificationService.cs
 * Project: EV Charging Station Booking System
 * Description: Interface for notification service operations
 * Author: EV Charging System
 * Date: October 7, 2025
 */

using EVChargingBackend.DTOs;
using EVChargingBackend.Models;

namespace EVChargingBackend.Services
{
    /// <summary>
    /// Interface for notification service operations
    /// </summary>
    public interface INotificationService
    {
        /// <summary>
        /// Creates a notification
        /// </summary>
        /// <param name="createDto">Notification creation data</param>
        /// <returns>Created notification</returns>
        Task<NotificationResponseDto> CreateNotificationAsync(CreateNotificationDto createDto);

        /// <summary>
        /// Creates bulk notifications for multiple recipients
        /// </summary>
        /// <param name="bulkDto">Bulk notification data</param>
        /// <returns>List of created notifications</returns>
        Task<List<NotificationResponseDto>> CreateBulkNotificationAsync(BulkNotificationDto bulkDto);

        /// <summary>
        /// Creates a booking confirmation notification
        /// </summary>
        /// <param name="recipientNIC">Recipient's NIC</param>
        /// <param name="bookingId">Booking ID</param>
        /// <param name="stationName">Charging station name</param>
        /// <param name="startTime">Booking start time</param>
        /// <param name="endTime">Booking end time</param>
        /// <returns>Created notification</returns>
        Task<NotificationResponseDto> CreateBookingConfirmationNotificationAsync(
            string recipientNIC, string bookingId, string stationName, DateTime startTime, DateTime endTime);

        /// <summary>
        /// Creates a booking cancellation notification
        /// </summary>
        /// <param name="recipientNIC">Recipient's NIC</param>
        /// <param name="bookingId">Booking ID</param>
        /// <param name="stationName">Charging station name</param>
        /// <param name="reason">Cancellation reason</param>
        /// <returns>Created notification</returns>
        Task<NotificationResponseDto> CreateBookingCancellationNotificationAsync(
            string recipientNIC, string bookingId, string stationName, string reason = "");

        /// <summary>
        /// Creates a booking reminder notification
        /// </summary>
        /// <param name="recipientNIC">Recipient's NIC</param>
        /// <param name="bookingId">Booking ID</param>
        /// <param name="stationName">Charging station name</param>
        /// <param name="startTime">Booking start time</param>
        /// <returns>Created notification</returns>
        Task<NotificationResponseDto> CreateBookingReminderNotificationAsync(
            string recipientNIC, string bookingId, string stationName, DateTime startTime);

        /// <summary>
        /// Gets all notifications for a user
        /// </summary>
        /// <param name="recipientNIC">User NIC</param>
        /// <param name="includeRead">Include read notifications</param>
        /// <param name="limit">Maximum number of notifications</param>
        /// <param name="offset">Number of notifications to skip</param>
        /// <returns>List of notifications</returns>
        Task<List<NotificationResponseDto>> GetUserNotificationsAsync(
            string recipientNIC, bool includeRead = true, int limit = 50, int offset = 0);

        /// <summary>
        /// Gets unread notifications for a user
        /// </summary>
        /// <param name="recipientNIC">User NIC</param>
        /// <returns>List of unread notifications</returns>
        Task<List<NotificationResponseDto>> GetUnreadNotificationsAsync(string recipientNIC);

        /// <summary>
        /// Gets notifications by type for a user
        /// </summary>
        /// <param name="recipientNIC">User NIC</param>
        /// <param name="type">Notification type</param>
        /// <param name="limit">Maximum number of notifications</param>
        /// <returns>List of notifications</returns>
        Task<List<NotificationResponseDto>> GetNotificationsByTypeAsync(
            string recipientNIC, NotificationType type, int limit = 20);

        /// <summary>
        /// Gets notification by ID
        /// </summary>
        /// <param name="id">Notification ID</param>
        /// <returns>Notification if found</returns>
        Task<NotificationResponseDto> GetNotificationByIdAsync(string id);

        /// <summary>
        /// Marks notifications as read
        /// </summary>
        /// <param name="notificationIds">List of notification IDs</param>
        /// <param name="userNIC">User NIC for validation</param>
        /// <returns>Number of notifications marked as read</returns>
        Task<int> MarkNotificationsAsReadAsync(List<string> notificationIds, string userNIC);

        /// <summary>
        /// Marks all notifications for a user as read
        /// </summary>
        /// <param name="recipientNIC">User NIC</param>
        /// <returns>Number of notifications marked as read</returns>
        Task<int> MarkAllAsReadAsync(string recipientNIC);

        /// <summary>
        /// Deletes a notification (soft delete by setting expiry)
        /// </summary>
        /// <param name="id">Notification ID</param>
        /// <param name="userNIC">User NIC for validation</param>
        /// <returns>Success status</returns>
        Task<bool> DeleteNotificationAsync(string id, string userNIC);

        /// <summary>
        /// Gets notification summary for a user
        /// </summary>
        /// <param name="recipientNIC">User NIC</param>
        /// <returns>Notification summary</returns>
        Task<NotificationSummaryDto> GetNotificationSummaryAsync(string recipientNIC);

        /// <summary>
        /// Cleans up expired notifications
        /// </summary>
        /// <returns>Number of deleted notifications</returns>
        Task<int> CleanupExpiredNotificationsAsync();
    }
}