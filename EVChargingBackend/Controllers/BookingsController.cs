/*
 * File: BookingsController.cs
 * Project: EV Charging Station Booking System
 * Description: Booking management controller with role-based operations
 * Author: EV Charging System
 * Date: September 27, 2025
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
    /// Controller for booking management operations with role-based access control
    /// </summary>
    [ApiController]
    [Route("api/[controller]")]
    [Authorize]
    public class BookingsController : ControllerBase
    {
        private readonly IBookingService _bookingService;
        private readonly ILogger<BookingsController> _logger;

        /// <summary>
        /// Initializes bookings controller with dependencies
        /// </summary>
        /// <param name="bookingService">Booking service for business operations</param>
        /// <param name="logger">Logger for controller operations</param>
        public BookingsController(IBookingService bookingService, ILogger<BookingsController> logger)
        {
            _bookingService = bookingService;
            _logger = logger;
        }

        /// <summary>
        /// Creates a new booking (EV Owner only)
        /// </summary>
        /// <param name="createDto">Booking creation data</param>
        /// <returns>Created booking information</returns>
        [HttpPost]
        [Authorize(Roles = nameof(UserRole.EVOwner))]
        public async Task<ActionResult<BookingResponseDto>> CreateBooking([FromBody] CreateBookingDto createDto)
        {
            var ownerNic = User.FindFirst("nic")?.Value;
            
            if (string.IsNullOrEmpty(ownerNic))
            {
                return BadRequest("Invalid user token");
            }

            _logger.LogInformation("Creating booking for station {StationId} by user {OwnerNIC}", createDto.StationId, ownerNic);
            
            var booking = await _bookingService.CreateBookingAsync(createDto, ownerNic);
            
            return CreatedAtAction(nameof(GetBooking), new { id = booking.Id }, booking);
        }

        /// <summary>
        /// Gets all bookings (backoffice and station operators)
        /// </summary>
        /// <returns>List of all bookings</returns>
        [HttpGet]
        [Authorize(Roles = $"{nameof(UserRole.Backoffice)},{nameof(UserRole.StationOperator)}")]
        public async Task<ActionResult<List<BookingResponseDto>>> GetAllBookings()
        {
            _logger.LogInformation("Fetching all bookings");
            
            var bookings = await _bookingService.GetAllBookingsAsync();
            
            return Ok(bookings);
        }

        /// <summary>
        /// Gets current user's bookings (EV Owner only)
        /// </summary>
        /// <returns>List of user's bookings</returns>
        [HttpGet("my-bookings")]
        [Authorize(Roles = nameof(UserRole.EVOwner))]
        public async Task<ActionResult<List<BookingResponseDto>>> GetMyBookings()
        {
            var ownerNic = User.FindFirst("nic")?.Value;
            
            if (string.IsNullOrEmpty(ownerNic))
            {
                return BadRequest("Invalid user token - NIC not found");
            }

            _logger.LogInformation("Fetching bookings for user: {OwnerNIC}", ownerNic);
            
            var bookings = await _bookingService.GetUserBookingsAsync(ownerNic);
            
            return Ok(bookings);
        }

        /// <summary>
        /// Gets bookings for a specific station (station operators)
        /// </summary>
        /// <param name="stationId">Station ID</param>
        /// <returns>List of station bookings</returns>
        [HttpGet("station/{stationId}")]
        [Authorize(Roles = $"{nameof(UserRole.Backoffice)},{nameof(UserRole.StationOperator)}")]
        public async Task<ActionResult<List<BookingResponseDto>>> GetStationBookings(string stationId)
        {
            _logger.LogInformation("Fetching bookings for station: {StationId}", stationId);
            
            var bookings = await _bookingService.GetStationBookingsAsync(stationId);
            
            return Ok(bookings);
        }

        /// <summary>
        /// Gets booking by ID
        /// </summary>
        /// <param name="id">Booking ID</param>
        /// <returns>Booking information</returns>
        [HttpGet("{id}")]
        public async Task<ActionResult<BookingResponseDto>> GetBooking(string id)
        {
            var currentUserRole = User.FindFirst(ClaimTypes.Role)?.Value;
            var ownerNic = User.FindFirst("nic")?.Value;

            _logger.LogInformation("Fetching booking: {BookingId}", id);
            
            var booking = await _bookingService.GetBookingByIdAsync(id);

            // EV Owners can only view their own bookings
            if (currentUserRole == nameof(UserRole.EVOwner) && booking.OwnerNIC != ownerNic)
            {
                return Forbid("You can only view your own bookings");
            }
            
            return Ok(booking);
        }

        /// <summary>
        /// Updates booking information (EV Owner only, own bookings)
        /// </summary>
        /// <param name="id">Booking ID</param>
        /// <param name="updateDto">Updated booking data</param>
        /// <returns>Updated booking information</returns>
        [HttpPut("{id}")]
        [Authorize(Roles = nameof(UserRole.EVOwner))]
        public async Task<ActionResult<BookingResponseDto>> UpdateBooking(string id, [FromBody] UpdateBookingDto updateDto)
        {
            var ownerNic = User.FindFirst("nic")?.Value;
            
            if (string.IsNullOrEmpty(ownerNic))
            {
                return BadRequest("Invalid user token - NIC not found");
            }

            _logger.LogInformation("Updating booking: {BookingId}", id);
            
            var booking = await _bookingService.UpdateBookingAsync(id, updateDto, ownerNic);
            
            return Ok(booking);
        }

        /// <summary>
        /// Confirms booking (station operator action)
        /// </summary>
        /// <param name="id">Booking ID</param>
        /// <returns>Success status</returns>
        [HttpPost("{id}/confirm")]
        [Authorize(Roles = $"{nameof(UserRole.Backoffice)},{nameof(UserRole.StationOperator)}")]
        public async Task<ActionResult> ConfirmBooking(string id)
        {
            _logger.LogInformation("Confirming booking: {BookingId}", id);
            
            var result = await _bookingService.ConfirmBookingAsync(id);
            
            if (!result)
            {
                return NotFound("Booking not found");
            }
            
            return Ok(new { message = "Booking confirmed successfully" });
        }

        /// <summary>
        /// Cancels booking (EV Owner only, own bookings)
        /// </summary>
        /// <param name="id">Booking ID</param>
        /// <returns>Success status</returns>
        [HttpPost("{id}/cancel")]
        [Authorize(Roles = nameof(UserRole.EVOwner))]
        public async Task<ActionResult> CancelBooking(string id)
        {
            var ownerNic = User.FindFirst("nic")?.Value;
            
            if (string.IsNullOrEmpty(ownerNic))
            {
                return BadRequest("Invalid user token - NIC not found");
            }

            _logger.LogInformation("Cancelling booking: {BookingId}", id);
            
            var result = await _bookingService.CancelBookingAsync(id, ownerNic);
            
            if (!result)
            {
                return NotFound("Booking not found");
            }
            
            return Ok(new { message = "Booking cancelled successfully" });
        }
    }
}