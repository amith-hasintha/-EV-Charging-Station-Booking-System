/*
 * File: NotificationBackgroundService.cs
 * Project: EV Charging Station Booking System
 * Description: Background service for handling notification reminders and cleanup
 * Author: EV Charging System
 * Date: October 7, 2025
 */

using EVChargingBackend.Models;
using EVChargingBackend.Repositories;
using EVChargingBackend.Services;

namespace EVChargingBackend.Services
{
    /// <summary>
    /// Background service for notification reminders and cleanup
    /// </summary>
    public class NotificationBackgroundService : BackgroundService
    {
        private readonly IServiceProvider _serviceProvider;
        private readonly ILogger<NotificationBackgroundService> _logger;
        private readonly TimeSpan _reminderCheckInterval = TimeSpan.FromMinutes(30); // Check every 30 minutes
        private readonly TimeSpan _cleanupInterval = TimeSpan.FromHours(6); // Cleanup every 6 hours
        private DateTime _lastCleanup = DateTime.UtcNow;

        /// <summary>
        /// Initializes notification background service
        /// </summary>
        /// <param name="serviceProvider">Service provider for dependency injection</param>
        /// <param name="logger">Logger</param>
        public NotificationBackgroundService(
            IServiceProvider serviceProvider,
            ILogger<NotificationBackgroundService> logger)
        {
            _serviceProvider = serviceProvider;
            _logger = logger;
        }

        /// <summary>
        /// Main execution loop for background service
        /// </summary>
        /// <param name="stoppingToken">Cancellation token</param>
        protected override async Task ExecuteAsync(CancellationToken stoppingToken)
        {
            _logger.LogInformation("Notification background service started");

            while (!stoppingToken.IsCancellationRequested)
            {
                try
                {
                    using var scope = _serviceProvider.CreateScope();
                    
                    // Send booking reminders
                    await SendBookingRemindersAsync(scope.ServiceProvider);

                    // Cleanup expired notifications if it's time
                    if (DateTime.UtcNow - _lastCleanup >= _cleanupInterval)
                    {
                        await CleanupExpiredNotificationsAsync(scope.ServiceProvider);
                        _lastCleanup = DateTime.UtcNow;
                    }
                }
                catch (Exception ex)
                {
                    _logger.LogError(ex, "Error in notification background service");
                }

                await Task.Delay(_reminderCheckInterval, stoppingToken);
            }

            _logger.LogInformation("Notification background service stopped");
        }

        /// <summary>
        /// Sends booking reminder notifications
        /// </summary>
        /// <param name="serviceProvider">Service provider</param>
        private async Task SendBookingRemindersAsync(IServiceProvider serviceProvider)
        {
            try
            {
                var bookingRepository = serviceProvider.GetRequiredService<IBookingRepository>();
                var stationRepository = serviceProvider.GetRequiredService<IChargingStationRepository>();
                var notificationService = serviceProvider.GetRequiredService<INotificationService>();
                var notificationRepository = serviceProvider.GetRequiredService<INotificationRepository>();

                var currentTime = DateTime.UtcNow;
                var reminderWindow = currentTime.AddHours(2); // Send reminders 2 hours before

                // Get confirmed bookings starting within the next 2 hours
                var upcomingBookings = await bookingRepository.GetUpcomingConfirmedBookingsAsync(
                    currentTime, reminderWindow);

                _logger.LogDebug("Found {Count} upcoming bookings for reminders", upcomingBookings.Count);

                foreach (var booking in upcomingBookings)
                {
                    try
                    {
                        // Check if reminder notification already sent for this booking
                        var existingReminders = await notificationRepository.GetByRelatedEntityAsync(
                            booking.Id!, nameof(Booking));

                        var reminderExists = existingReminders.Any(n => 
                            n.Type == NotificationType.BookingReminder && 
                            n.RecipientNIC == booking.OwnerNIC);

                        if (reminderExists)
                        {
                            continue; // Reminder already sent
                        }

                        // Get station details
                        var station = await stationRepository.GetByIdAsync(booking.StationId);
                        var stationName = station?.Name ?? "Charging Station";

                        // Send reminder notification
                        await notificationService.CreateBookingReminderNotificationAsync(
                            booking.OwnerNIC,
                            booking.Id!,
                            stationName,
                            booking.StartTime
                        );

                        _logger.LogInformation("Sent booking reminder for booking {BookingId} to user {OwnerNIC}", 
                            booking.Id, booking.OwnerNIC);
                    }
                    catch (Exception ex)
                    {
                        _logger.LogError(ex, "Failed to send reminder for booking {BookingId}", booking.Id);
                    }
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error in SendBookingRemindersAsync");
            }
        }

        /// <summary>
        /// Cleans up expired notifications
        /// </summary>
        /// <param name="serviceProvider">Service provider</param>
        private async Task CleanupExpiredNotificationsAsync(IServiceProvider serviceProvider)
        {
            try
            {
                var notificationService = serviceProvider.GetRequiredService<INotificationService>();

                var deletedCount = await notificationService.CleanupExpiredNotificationsAsync();

                if (deletedCount > 0)
                {
                    _logger.LogInformation("Cleaned up {Count} expired notifications", deletedCount);
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error in CleanupExpiredNotificationsAsync");
            }
        }
    }
}