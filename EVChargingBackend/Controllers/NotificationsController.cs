/*
 * File: NotificationsController.cs
 * Project: EV Charging Station Booking System
 * Description: Controller for notification management operations
 * Author: EV Charging System
 * Date: October 7, 2025
 */

using EVChargingBackend.DTOs;
using EVChargingBackend.Models;
using EVChargingBackend.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using System.Security.Claims;

namespace EVChargingBackend.Controllers
{
    /// <summary>
    /// Controller for notification management operations
    /// </summary>
    [ApiController]
    [Route("api/[controller]")]
    [Authorize]
    public class NotificationsController : ControllerBase
    {
        private readonly INotificationService _notificationService;
        private readonly ILogger<NotificationsController> _logger;

        /// <summary>
        /// Initializes notifications controller with dependencies
        /// </summary>
        /// <param name="notificationService">Notification service</param>
        /// <param name="logger">Logger</param>
        public NotificationsController(
            INotificationService notificationService,
            ILogger<NotificationsController> logger)
        {
            _notificationService = notificationService;
            _logger = logger;
        }

        /// <summary>
        /// Creates a notification (Backoffice and Station Operators only)
        /// </summary>
        /// <param name="createDto">Notification creation data</param>
        /// <returns>Created notification</returns>
        [HttpPost]
        [Authorize(Roles = $"{nameof(UserRole.Backoffice)},{nameof(UserRole.StationOperator)}")]
        public async Task<ActionResult<NotificationResponseDto>> CreateNotification([FromBody] CreateNotificationDto createDto)
        {
            _logger.LogInformation("Creating notification for user {RecipientNIC}", createDto.RecipientNIC);

            var notification = await _notificationService.CreateNotificationAsync(createDto);

            return CreatedAtAction(nameof(GetNotification), new { id = notification.Id }, notification);
        }

        /// <summary>
        /// Creates bulk notifications (Backoffice only)
        /// </summary>
        /// <param name="bulkDto">Bulk notification data</param>
        /// <returns>List of created notifications</returns>
        [HttpPost("bulk")]
        [Authorize(Roles = nameof(UserRole.Backoffice))]
        public async Task<ActionResult<List<NotificationResponseDto>>> CreateBulkNotification([FromBody] BulkNotificationDto bulkDto)
        {
            _logger.LogInformation("Creating bulk notifications for {Count} recipients", bulkDto.RecipientNICs.Count);

            var notifications = await _notificationService.CreateBulkNotificationAsync(bulkDto);

            return Ok(notifications);
        }

        /// <summary>
        /// Gets current user's notifications
        /// </summary>
        /// <param name="includeRead">Include read notifications</param>
        /// <param name="limit">Maximum number of notifications</param>
        /// <param name="offset">Number of notifications to skip</param>
        /// <returns>List of user's notifications</returns>
        [HttpGet("my-notifications")]
        public async Task<ActionResult<List<NotificationResponseDto>>> GetMyNotifications(
            [FromQuery] bool includeRead = true,
            [FromQuery] int limit = 50,
            [FromQuery] int offset = 0)
        {
            var userNic = User.FindFirst("nic")?.Value;

            if (string.IsNullOrEmpty(userNic))
            {
                return BadRequest("Invalid user token - NIC not found");
            }

            _logger.LogInformation("Fetching notifications for user: {UserNIC}", userNic);

            var notifications = await _notificationService.GetUserNotificationsAsync(
                userNic, includeRead, limit, offset);

            return Ok(notifications);
        }

        /// <summary>
        /// Gets current user's unread notifications
        /// </summary>
        /// <returns>List of unread notifications</returns>
        [HttpGet("unread")]
        public async Task<ActionResult<List<NotificationResponseDto>>> GetUnreadNotifications()
        {
            var userNic = User.FindFirst("nic")?.Value;

            if (string.IsNullOrEmpty(userNic))
            {
                return BadRequest("Invalid user token - NIC not found");
            }

            _logger.LogInformation("Fetching unread notifications for user: {UserNIC}", userNic);

            var notifications = await _notificationService.GetUnreadNotificationsAsync(userNic);

            return Ok(notifications);
        }

        /// <summary>
        /// Gets notifications by type for current user
        /// </summary>
        /// <param name="type">Notification type</param>
        /// <param name="limit">Maximum number of notifications</param>
        /// <returns>List of notifications by type</returns>
        [HttpGet("by-type/{type}")]
        public async Task<ActionResult<List<NotificationResponseDto>>> GetNotificationsByType(
            NotificationType type,
            [FromQuery] int limit = 20)
        {
            var userNic = User.FindFirst("nic")?.Value;

            if (string.IsNullOrEmpty(userNic))
            {
                return BadRequest("Invalid user token - NIC not found");
            }

            _logger.LogInformation("Fetching {Type} notifications for user: {UserNIC}", type, userNic);

            var notifications = await _notificationService.GetNotificationsByTypeAsync(
                userNic, type, limit);

            return Ok(notifications);
        }

        /// <summary>
        /// Gets notification by ID
        /// </summary>
        /// <param name="id">Notification ID</param>
        /// <returns>Notification details</returns>
        [HttpGet("{id}")]
        public async Task<ActionResult<NotificationResponseDto>> GetNotification(string id)
        {
            var userNic = User.FindFirst("nic")?.Value;
            var userRole = User.FindFirst(ClaimTypes.Role)?.Value;

            if (string.IsNullOrEmpty(userNic))
            {
                return BadRequest("Invalid user token - NIC not found");
            }

            _logger.LogInformation("Fetching notification: {NotificationId}", id);

            var notification = await _notificationService.GetNotificationByIdAsync(id);

            // Users can only view their own notifications (except backoffice)
            if (userRole != nameof(UserRole.Backoffice) && notification.RecipientNIC != userNic)
            {
                return Forbid("You can only view your own notifications");
            }

            return Ok(notification);
        }

        /// <summary>
        /// Marks notifications as read
        /// </summary>
        /// <param name="markReadDto">Notification IDs to mark as read</param>
        /// <returns>Success status with count</returns>
        [HttpPost("mark-read")]
        public async Task<ActionResult> MarkNotificationsAsRead([FromBody] MarkNotificationReadDto markReadDto)
        {
            var userNic = User.FindFirst("nic")?.Value;

            if (string.IsNullOrEmpty(userNic))
            {
                return BadRequest("Invalid user token - NIC not found");
            }

            _logger.LogInformation("Marking {Count} notifications as read for user: {UserNIC}", 
                markReadDto.NotificationIds.Count, userNic);

            var markedCount = await _notificationService.MarkNotificationsAsReadAsync(
                markReadDto.NotificationIds, userNic);

            return Ok(new { message = $"{markedCount} notifications marked as read", count = markedCount });
        }

        /// <summary>
        /// Marks all notifications as read for current user
        /// </summary>
        /// <returns>Success status with count</returns>
        [HttpPost("mark-all-read")]
        public async Task<ActionResult> MarkAllNotificationsAsRead()
        {
            var userNic = User.FindFirst("nic")?.Value;

            if (string.IsNullOrEmpty(userNic))
            {
                return BadRequest("Invalid user token - NIC not found");
            }

            _logger.LogInformation("Marking all notifications as read for user: {UserNIC}", userNic);

            var markedCount = await _notificationService.MarkAllAsReadAsync(userNic);

            return Ok(new { message = $"All {markedCount} notifications marked as read", count = markedCount });
        }

        /// <summary>
        /// Deletes a notification
        /// </summary>
        /// <param name="id">Notification ID</param>
        /// <returns>Success status</returns>
        [HttpDelete("{id}")]
        public async Task<ActionResult> DeleteNotification(string id)
        {
            var userNic = User.FindFirst("nic")?.Value;

            if (string.IsNullOrEmpty(userNic))
            {
                return BadRequest("Invalid user token - NIC not found");
            }

            _logger.LogInformation("Deleting notification: {NotificationId} for user: {UserNIC}", id, userNic);

            var result = await _notificationService.DeleteNotificationAsync(id, userNic);

            if (!result)
            {
                return NotFound("Notification not found");
            }

            return Ok(new { message = "Notification deleted successfully" });
        }

        /// <summary>
        /// Gets notification summary for current user
        /// </summary>
        /// <returns>Notification summary</returns>
        [HttpGet("summary")]
        public async Task<ActionResult<NotificationSummaryDto>> GetNotificationSummary()
        {
            var userNic = User.FindFirst("nic")?.Value;

            if (string.IsNullOrEmpty(userNic))
            {
                return BadRequest("Invalid user token - NIC not found");
            }

            _logger.LogInformation("Fetching notification summary for user: {UserNIC}", userNic);

            var summary = await _notificationService.GetNotificationSummaryAsync(userNic);

            return Ok(summary);
        }

        /// <summary>
        /// Cleans up expired notifications (Backoffice only)
        /// </summary>
        /// <returns>Number of deleted notifications</returns>
        [HttpPost("cleanup-expired")]
        [Authorize(Roles = nameof(UserRole.Backoffice))]
        public async Task<ActionResult> CleanupExpiredNotifications()
        {
            _logger.LogInformation("Cleaning up expired notifications");

            var deletedCount = await _notificationService.CleanupExpiredNotificationsAsync();

            return Ok(new { message = $"Cleaned up {deletedCount} expired notifications", count = deletedCount });
        }
    }
}