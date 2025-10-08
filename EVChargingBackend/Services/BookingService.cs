/*
 * File: BookingService.cs
 * Project: EV Charging Station Booking System
 * Description: Booking management service implementation with business rules
 * Author: EV Charging System
 * Date: September 27, 2025
 */

using EVChargingBackend.DTOs;
using EVChargingBackend.Models;
using EVChargingBackend.Repositories;

namespace EVChargingBackend.Services
{
    /// <summary>
    /// Service implementation for booking management operations
    /// </summary>
    public class BookingService : IBookingService
    {
        private readonly IBookingRepository _bookingRepository;
        private readonly IChargingStationRepository _stationRepository;
        private readonly INotificationService _notificationService;
        private readonly ILogger<BookingService> _logger;

        /// <summary>
        /// Initializes booking service with dependencies
        /// </summary>
        /// <param name="bookingRepository">Booking repository for data operations</param>
        /// <param name="stationRepository">Station repository for validation</param>
        /// <param name="notificationService">Notification service for sending notifications</param>
        /// <param name="logger">Logger for service operations</param>
        public BookingService(
            IBookingRepository bookingRepository,
            IChargingStationRepository stationRepository,
            INotificationService notificationService,
            ILogger<BookingService> logger)
        {
            _bookingRepository = bookingRepository;
            _stationRepository = stationRepository;
            _notificationService = notificationService;
            _logger = logger;
        }

        /// <summary>
        /// Creates a new booking with business rule validation
        /// </summary>
        /// <param name="createDto">Booking creation data</param>
        /// <param name="ownerNic">Owner NIC from JWT token</param>
        /// <returns>Created booking</returns>
        public async Task<BookingResponseDto> CreateBookingAsync(CreateBookingDto createDto, string ownerNic)
        {
            // Validate time constraints
            ValidateBookingTimes(createDto.StartTime, createDto.EndTime);

            // Get and validate station
            var station = await _stationRepository.GetByIdAsync(createDto.StationId);
            if (station == null)
            {
                throw new KeyNotFoundException("Charging station not found");
            }

            if (station.Status != StationStatus.Active)
            {
                throw new ArgumentException("Charging station is not active");
            }

            if (station.AvailableSlots <= 0)
            {
                throw new ArgumentException("No available slots at this charging station");
            }

            // Check for overlapping bookings
            var overlappingBookings = await _bookingRepository.GetOverlappingBookingsAsync(
                createDto.StationId, createDto.StartTime, createDto.EndTime);

            if (overlappingBookings.Count >= station.TotalSlots)
            {
                throw new ArgumentException("No available slots for the requested time period");
            }

            // Calculate total amount
            var duration = createDto.EndTime - createDto.StartTime;
            var totalAmount = (decimal)duration.TotalHours * station.PricePerHour;

            // Create booking
            var booking = new Booking
            {
                OwnerNIC = ownerNic,
                StationId = createDto.StationId,
                StartTime = createDto.StartTime,
                EndTime = createDto.EndTime,
                Status = BookingStatus.Active,
                QRCode = GenerateQRCode(),
                TotalAmount = totalAmount
            };

            var createdBooking = await _bookingRepository.CreateAsync(booking);

            // Update available slots
            await _stationRepository.UpdateAvailableSlotsAsync(createDto.StationId, -1);
            
            _logger.LogInformation("Booking created: {BookingId} for station {StationId}", createdBooking.Id, createDto.StationId);

            return await MapToBookingResponseDto(createdBooking);
        }

        /// <summary>
        /// Gets all bookings (backoffice and operators)
        /// </summary>
        /// <returns>List of all bookings</returns>
        public async Task<List<BookingResponseDto>> GetAllBookingsAsync()
        {
            var bookings = await _bookingRepository.GetAllAsync();
            var bookingDtos = new List<BookingResponseDto>();

            foreach (var booking in bookings)
            {
                bookingDtos.Add(await MapToBookingResponseDto(booking));
            }

            return bookingDtos;
        }

        /// <summary>
        /// Gets bookings for specific user
        /// </summary>
        /// <param name="ownerNic">Owner NIC</param>
        /// <returns>List of user bookings</returns>
        public async Task<List<BookingResponseDto>> GetUserBookingsAsync(string ownerNic)
        {
            var bookings = await _bookingRepository.GetByOwnerAsync(ownerNic);
            var bookingDtos = new List<BookingResponseDto>();

            foreach (var booking in bookings)
            {
                bookingDtos.Add(await MapToBookingResponseDto(booking));
            }

            return bookingDtos;
        }

        /// <summary>
        /// Gets bookings for specific station (operators)
        /// </summary>
        /// <param name="stationId">Station ID</param>
        /// <returns>List of station bookings</returns>
        public async Task<List<BookingResponseDto>> GetStationBookingsAsync(string stationId)
        {
            var bookings = await _bookingRepository.GetByStationAsync(stationId);
            var bookingDtos = new List<BookingResponseDto>();

            foreach (var booking in bookings)
            {
                bookingDtos.Add(await MapToBookingResponseDto(booking));
            }

            return bookingDtos;
        }

        /// <summary>
        /// Gets booking by ID
        /// </summary>
        /// <param name="id">Booking ID</param>
        /// <returns>Booking if found</returns>
        public async Task<BookingResponseDto> GetBookingByIdAsync(string id)
        {
            var booking = await _bookingRepository.GetByIdAsync(id);
            if (booking == null)
            {
                throw new KeyNotFoundException("Booking not found");
            }

            return await MapToBookingResponseDto(booking);
        }

        /// <summary>
        /// Updates booking with business rule validation
        /// </summary>
        /// <param name="id">Booking ID</param>
        /// <param name="updateDto">Updated booking data</param>
        /// <param name="ownerNic">Owner NIC from JWT token</param>
        /// <returns>Updated booking</returns>
        public async Task<BookingResponseDto> UpdateBookingAsync(string id, UpdateBookingDto updateDto, string ownerNic)
        {
            var booking = await _bookingRepository.GetByIdAsync(id);
            if (booking == null)
            {
                throw new KeyNotFoundException("Booking not found");
            }

            // Only the booking owner can update their booking
            if (booking.OwnerNIC != ownerNic)
            {
                throw new UnauthorizedAccessException("You can only update your own bookings");
            }

            // Check if booking can be modified (12 hours before start time)
            if (DateTime.UtcNow.AddHours(12) > booking.StartTime)
            {
                throw new ArgumentException("Cannot modify booking within 12 hours of start time");
            }

            // Only active bookings can be updated
            if (booking.Status != BookingStatus.Active)
            {
                throw new ArgumentException("Only active bookings can be updated");
            }

            var hasTimeChanges = false;

            // Update time if provided
            if (updateDto.StartTime.HasValue || updateDto.EndTime.HasValue)
            {
                var newStartTime = updateDto.StartTime ?? booking.StartTime;
                var newEndTime = updateDto.EndTime ?? booking.EndTime;

                ValidateBookingTimes(newStartTime, newEndTime);

                // Check for overlapping bookings (exclude current booking)
                var overlappingBookings = await _bookingRepository.GetOverlappingBookingsAsync(
                    booking.StationId, newStartTime, newEndTime);

                var conflictingBookings = overlappingBookings.Where(b => b.Id != id).ToList();
                var station = await _stationRepository.GetByIdAsync(booking.StationId);

                if (conflictingBookings.Count >= station!.TotalSlots)
                {
                    throw new ArgumentException("No available slots for the requested time period");
                }

                booking.StartTime = newStartTime;
                booking.EndTime = newEndTime;
                hasTimeChanges = true;
            }

            // Update status if provided
            if (updateDto.Status.HasValue)
            {
                booking.Status = updateDto.Status.Value;
            }

            // Recalculate total amount if time changed
            if (hasTimeChanges)
            {
                var station = await _stationRepository.GetByIdAsync(booking.StationId);
                var duration = booking.EndTime - booking.StartTime;
                booking.TotalAmount = (decimal)duration.TotalHours * station!.PricePerHour;
            }

            var updatedBooking = await _bookingRepository.UpdateAsync(booking);
            
            _logger.LogInformation("Booking updated: {BookingId}", id);

            return await MapToBookingResponseDto(updatedBooking);
        }

        /// <summary>
        /// Confirms booking (station operator action)
        /// </summary>
        /// <param name="bookingId">Booking ID</param>
        /// <returns>Success status</returns>
        public async Task<bool> ConfirmBookingAsync(string bookingId)
        {
            var booking = await _bookingRepository.GetByIdAsync(bookingId);
            if (booking == null)
            {
                throw new KeyNotFoundException("Booking not found");
            }

            if (booking.Status != BookingStatus.Active)
            {
                throw new ArgumentException("Only active bookings can be confirmed");
            }

            var result = await _bookingRepository.ConfirmBookingAsync(bookingId);
            
            if (result)
            {
                _logger.LogInformation("Booking confirmed: {BookingId}", bookingId);
                
                // Send confirmation notification to the EV owner
                try
                {
                    var station = await _stationRepository.GetByIdAsync(booking.StationId);
                    var stationName = station?.Name ?? "Charging Station";
                    
                    await _notificationService.CreateBookingConfirmationNotificationAsync(
                        booking.OwnerNIC,
                        bookingId,
                        stationName,
                        booking.StartTime,
                        booking.EndTime
                    );
                    
                    _logger.LogInformation("Confirmation notification sent for booking: {BookingId}", bookingId);
                }
                catch (Exception ex)
                {
                    _logger.LogError(ex, "Failed to send confirmation notification for booking: {BookingId}", bookingId);
                    // Don't fail the booking confirmation if notification fails
                }
            }

            return result;
        }

        /// <summary>
        /// Cancels booking with time validation
        /// </summary>
        /// <param name="id">Booking ID</param>
        /// <param name="ownerNic">Owner NIC from JWT token</param>
        /// <returns>Success status</returns>
        public async Task<bool> CancelBookingAsync(string id, string ownerNic)
        {
            var booking = await _bookingRepository.GetByIdAsync(id);
            if (booking == null)
            {
                throw new KeyNotFoundException("Booking not found");
            }

            // Only the booking owner can cancel their booking
            if (booking.OwnerNIC != ownerNic)
            {
                throw new UnauthorizedAccessException("You can only cancel your own bookings");
            }

            // Check if booking can be cancelled (12 hours before start time)
            if (DateTime.UtcNow.AddHours(12) > booking.StartTime)
            {
                throw new ArgumentException("Cannot cancel booking within 12 hours of start time");
            }

            // Only active or confirmed bookings can be cancelled
            if (booking.Status != BookingStatus.Active && booking.Status != BookingStatus.Confirmed)
            {
                throw new ArgumentException("Only active or confirmed bookings can be cancelled");
            }

            var result = await _bookingRepository.CancelBookingAsync(id);
            
            if (result)
            {
                // Return the slot to available pool
                await _stationRepository.UpdateAvailableSlotsAsync(booking.StationId, 1);
                _logger.LogInformation("Booking cancelled: {BookingId}", id);
                
                // Send cancellation notification to the EV owner
                try
                {
                    var station = await _stationRepository.GetByIdAsync(booking.StationId);
                    var stationName = station?.Name ?? "Charging Station";
                    
                    await _notificationService.CreateBookingCancellationNotificationAsync(
                        booking.OwnerNIC,
                        id,
                        stationName,
                        "Cancelled by user"
                    );
                    
                    _logger.LogInformation("Cancellation notification sent for booking: {BookingId}", id);
                }
                catch (Exception ex)
                {
                    _logger.LogError(ex, "Failed to send cancellation notification for booking: {BookingId}", id);
                    // Don't fail the booking cancellation if notification fails
                }
            }

            return result;
        }

        /// <summary>
        /// Cancels booking by operator (station operator or backoffice)
        /// </summary>
        /// <param name="bookingId">Booking ID</param>
        /// <param name="reason">Cancellation reason</param>
        /// <returns>Success status</returns>
        public async Task<bool> CancelBookingByOperatorAsync(string bookingId, string reason = "")
        {
            var booking = await _bookingRepository.GetByIdAsync(bookingId);
            if (booking == null)
            {
                throw new KeyNotFoundException("Booking not found");
            }

            // Only active or confirmed bookings can be cancelled
            if (booking.Status != BookingStatus.Active && booking.Status != BookingStatus.Confirmed)
            {
                throw new ArgumentException("Only active or confirmed bookings can be cancelled");
            }

            var result = await _bookingRepository.CancelBookingAsync(bookingId);
            
            if (result)
            {
                // Return the slot to available pool
                await _stationRepository.UpdateAvailableSlotsAsync(booking.StationId, 1);
                _logger.LogInformation("Booking cancelled by operator: {BookingId}, Reason: {Reason}", bookingId, reason);
                
                // Send cancellation notification to the EV owner
                try
                {
                    var station = await _stationRepository.GetByIdAsync(booking.StationId);
                    var stationName = station?.Name ?? "Charging Station";
                    
                    var fullReason = string.IsNullOrEmpty(reason) ? "Cancelled by station operator" : $"Cancelled by station operator - {reason}";
                    
                    await _notificationService.CreateBookingCancellationNotificationAsync(
                        booking.OwnerNIC,
                        bookingId,
                        stationName,
                        fullReason
                    );
                    
                    _logger.LogInformation("Cancellation notification sent for booking: {BookingId}", bookingId);
                }
                catch (Exception ex)
                {
                    _logger.LogError(ex, "Failed to send cancellation notification for booking: {BookingId}", bookingId);
                    // Don't fail the booking cancellation if notification fails
                }
            }

            return result;
        }

        /// <summary>
        /// Validates booking time constraints
        /// </summary>
        /// <param name="startTime">Booking start time</param>
        /// <param name="endTime">Booking end time</param>
        private static void ValidateBookingTimes(DateTime startTime, DateTime endTime)
        {
            // Ensure start time is in the future
            if (startTime <= DateTime.UtcNow)
            {
                throw new ArgumentException("Booking start time must be in the future");
            }

            // Ensure end time is after start time
            if (endTime <= startTime)
            {
                throw new ArgumentException("Booking end time must be after start time");
            }

            // Maximum 7 days in advance
            if (startTime > DateTime.UtcNow.AddDays(7))
            {
                throw new ArgumentException("Bookings can only be made up to 7 days in advance");
            }

            // Minimum booking duration (1 hour)
            if ((endTime - startTime).TotalHours < 1)
            {
                throw new ArgumentException("Minimum booking duration is 1 hour");
            }

            // Maximum booking duration (24 hours)
            if ((endTime - startTime).TotalHours > 24)
            {
                throw new ArgumentException("Maximum booking duration is 24 hours");
            }
        }

        /// <summary>
        /// Generates a unique QR code for the booking
        /// </summary>
        /// <returns>QR code string</returns>
        private static string GenerateQRCode()
        {
            return Guid.NewGuid().ToString("N").ToUpper();
        }

        /// <summary>
        /// Maps Booking entity to BookingResponseDto with station details
        /// </summary>
        /// <param name="booking">Booking entity</param>
        /// <returns>Booking response DTO</returns>
        private async Task<BookingResponseDto> MapToBookingResponseDto(Booking booking)
        {
            var station = await _stationRepository.GetByIdAsync(booking.StationId);
            
            return new BookingResponseDto
            {
                Id = booking.Id ?? string.Empty,
                OwnerNIC = booking.OwnerNIC,
                StationId = booking.StationId,
                StationName = station?.Name ?? "Unknown Station",
                StationLocation = station?.Location ?? "Unknown Location",
                StartTime = booking.StartTime,
                EndTime = booking.EndTime,
                Status = booking.Status,
                QRCode = booking.QRCode,
                TotalAmount = booking.TotalAmount,
                CreatedAt = booking.CreatedAt,
                ConfirmedAt = booking.ConfirmedAt,
                CancelledAt = booking.CancelledAt
            };
        }
    }
}