/*
 * File: BookingRepository.cs
 * Project: EV Charging Station Booking System
 * Description: Repository implementation for Booking operations
 * Author: EV Charging System
 * Date: September 27, 2025
 */

using EVChargingBackend.Config;
using EVChargingBackend.Models;
using MongoDB.Driver;

namespace EVChargingBackend.Repositories
{
    /// <summary>
    /// Repository implementation for Booking data operations
    /// </summary>
    public class BookingRepository : IBookingRepository
    {
        private readonly IMongoCollection<Booking> _bookings;

        /// <summary>
        /// Initializes booking repository with MongoDB context
        /// </summary>
        /// <param name="context">MongoDB database context</param>
        public BookingRepository(MongoDbContext context)
        {
            _bookings = context.Bookings;
        }

        /// <summary>
        /// Creates a new booking
        /// </summary>
        /// <param name="booking">Booking to create</param>
        /// <returns>Created booking</returns>
        public async Task<Booking> CreateAsync(Booking booking)
        {
            booking.CreatedAt = DateTime.UtcNow;
            booking.UpdatedAt = DateTime.UtcNow;
            await _bookings.InsertOneAsync(booking);
            return booking;
        }

        /// <summary>
        /// Gets booking by ID
        /// </summary>
        /// <param name="id">Booking ID</param>
        /// <returns>Booking if found, null otherwise</returns>
        public async Task<Booking?> GetByIdAsync(string id)
        {
            return await _bookings.Find(b => b.Id == id).FirstOrDefaultAsync();
        }

        /// <summary>
        /// Gets all bookings for a specific user
        /// </summary>
        /// <param name="ownerNic">Owner NIC</param>
        /// <returns>List of user bookings</returns>
        public async Task<List<Booking>> GetByOwnerAsync(string ownerNic)
        {
            return await _bookings.Find(b => b.OwnerNIC == ownerNic)
                .SortByDescending(b => b.CreatedAt)
                .ToListAsync();
        }

        /// <summary>
        /// Gets all bookings for a specific station
        /// </summary>
        /// <param name="stationId">Station ID</param>
        /// <returns>List of station bookings</returns>
        public async Task<List<Booking>> GetByStationAsync(string stationId)
        {
            return await _bookings.Find(b => b.StationId == stationId)
                .SortByDescending(b => b.StartTime)
                .ToListAsync();
        }

        /// <summary>
        /// Gets active bookings for a station within a time range
        /// </summary>
        /// <param name="stationId">Station ID</param>
        /// <param name="startTime">Start time</param>
        /// <param name="endTime">End time</param>
        /// <returns>List of overlapping bookings</returns>
        public async Task<List<Booking>> GetOverlappingBookingsAsync(string stationId, DateTime startTime, DateTime endTime)
        {
            var filter = Builders<Booking>.Filter.And(
                Builders<Booking>.Filter.Eq(b => b.StationId, stationId),
                Builders<Booking>.Filter.In(b => b.Status, new[] { BookingStatus.Active, BookingStatus.Confirmed }),
                Builders<Booking>.Filter.Or(
                    // New booking starts during existing booking
                    Builders<Booking>.Filter.And(
                        Builders<Booking>.Filter.Lte(b => b.StartTime, startTime),
                        Builders<Booking>.Filter.Gt(b => b.EndTime, startTime)
                    ),
                    // New booking ends during existing booking
                    Builders<Booking>.Filter.And(
                        Builders<Booking>.Filter.Lt(b => b.StartTime, endTime),
                        Builders<Booking>.Filter.Gte(b => b.EndTime, endTime)
                    ),
                    // New booking completely contains existing booking
                    Builders<Booking>.Filter.And(
                        Builders<Booking>.Filter.Gte(b => b.StartTime, startTime),
                        Builders<Booking>.Filter.Lte(b => b.EndTime, endTime)
                    )
                )
            );

            return await _bookings.Find(filter).ToListAsync();
        }

        /// <summary>
        /// Gets all bookings
        /// </summary>
        /// <returns>List of all bookings</returns>
        public async Task<List<Booking>> GetAllAsync()
        {
            return await _bookings.Find(_ => true)
                .SortByDescending(b => b.CreatedAt)
                .ToListAsync();
        }

        /// <summary>
        /// Updates booking information
        /// </summary>
        /// <param name="booking">Booking with updated information</param>
        /// <returns>Updated booking</returns>
        public async Task<Booking> UpdateAsync(Booking booking)
        {
            booking.UpdatedAt = DateTime.UtcNow;
            await _bookings.ReplaceOneAsync(b => b.Id == booking.Id, booking);
            return booking;
        }

        /// <summary>
        /// Confirms a booking (operator action)
        /// </summary>
        /// <param name="bookingId">Booking ID</param>
        /// <returns>Success status</returns>
        public async Task<bool> ConfirmBookingAsync(string bookingId)
        {
            var update = Builders<Booking>.Update
                .Set(b => b.Status, BookingStatus.Confirmed)
                .Set(b => b.ConfirmedAt, DateTime.UtcNow)
                .Set(b => b.UpdatedAt, DateTime.UtcNow);

            var result = await _bookings.UpdateOneAsync(b => b.Id == bookingId, update);
            return result.ModifiedCount > 0;
        }

        /// <summary>
        /// Cancels a booking
        /// </summary>
        /// <param name="bookingId">Booking ID</param>
        /// <returns>Success status</returns>
        public async Task<bool> CancelBookingAsync(string bookingId)
        {
            var update = Builders<Booking>.Update
                .Set(b => b.Status, BookingStatus.Cancelled)
                .Set(b => b.CancelledAt, DateTime.UtcNow)
                .Set(b => b.UpdatedAt, DateTime.UtcNow);

            var result = await _bookings.UpdateOneAsync(b => b.Id == bookingId, update);
            return result.ModifiedCount > 0;
        }

        /// <summary>
        /// Gets active bookings for a station (for slot management)
        /// </summary>
        /// <param name="stationId">Station ID</param>
        /// <returns>Count of active bookings</returns>
        public async Task<long> GetActiveBookingCountAsync(string stationId)
        {
            var filter = Builders<Booking>.Filter.And(
                Builders<Booking>.Filter.Eq(b => b.StationId, stationId),
                Builders<Booking>.Filter.In(b => b.Status, new[] { BookingStatus.Active, BookingStatus.Confirmed })
            );

            return await _bookings.CountDocumentsAsync(filter);
        }

        /// <summary>
        /// Gets upcoming confirmed bookings within a time range (for reminders)
        /// </summary>
        /// <param name="fromTime">Start time range</param>
        /// <param name="toTime">End time range</param>
        /// <returns>List of upcoming confirmed bookings</returns>
        public async Task<List<Booking>> GetUpcomingConfirmedBookingsAsync(DateTime fromTime, DateTime toTime)
        {
            var filter = Builders<Booking>.Filter.And(
                Builders<Booking>.Filter.Eq(b => b.Status, BookingStatus.Confirmed),
                Builders<Booking>.Filter.Gte(b => b.StartTime, fromTime),
                Builders<Booking>.Filter.Lte(b => b.StartTime, toTime)
            );

            return await _bookings.Find(filter).ToListAsync();
        }
    }
}