/*
 * File: ChargingStationRepository.cs
 * Project: EV Charging Station Booking System
 * Description: Repository implementation for Charging Station operations
 * Author: EV Charging System
 * Date: September 27, 2025
 */

using EVChargingBackend.Config;
using EVChargingBackend.Models;
using MongoDB.Driver;

namespace EVChargingBackend.Repositories
{
    /// <summary>
    /// Repository implementation for Charging Station data operations
    /// </summary>
    public class ChargingStationRepository : IChargingStationRepository
    {
        private readonly IMongoCollection<ChargingStation> _stations;

        /// <summary>
        /// Initializes charging station repository with MongoDB context
        /// </summary>
        /// <param name="context">MongoDB database context</param>
        public ChargingStationRepository(MongoDbContext context)
        {
            _stations = context.ChargingStations;
        }

        /// <summary>
        /// Creates a new charging station
        /// </summary>
        /// <param name="station">Charging station to create</param>
        /// <returns>Created charging station</returns>
        public async Task<ChargingStation> CreateAsync(ChargingStation station)
        {
            station.CreatedAt = DateTime.UtcNow;
            station.UpdatedAt = DateTime.UtcNow;
            station.AvailableSlots = station.TotalSlots; // Initially all slots are available
            await _stations.InsertOneAsync(station);
            return station;
        }

        /// <summary>
        /// Gets charging station by ID
        /// </summary>
        /// <param name="id">Station ID</param>
        /// <returns>Charging station if found, null otherwise</returns>
        public async Task<ChargingStation?> GetByIdAsync(string id)
        {
            return await _stations.Find(s => s.Id == id).FirstOrDefaultAsync();
        }

        /// <summary>
        /// Gets all charging stations
        /// </summary>
        /// <returns>List of all charging stations</returns>
        public async Task<List<ChargingStation>> GetAllAsync()
        {
            return await _stations.Find(_ => true).ToListAsync();
        }

        /// <summary>
        /// Gets active charging stations only
        /// </summary>
        /// <returns>List of active charging stations</returns>
        public async Task<List<ChargingStation>> GetActiveStationsAsync()
        {
            return await _stations.Find(s => s.Status == StationStatus.Active).ToListAsync();
        }

        /// <summary>
        /// Updates charging station information
        /// </summary>
        /// <param name="station">Station with updated information</param>
        /// <returns>Updated charging station</returns>
        public async Task<ChargingStation> UpdateAsync(ChargingStation station)
        {
            station.UpdatedAt = DateTime.UtcNow;
            await _stations.ReplaceOneAsync(s => s.Id == station.Id, station);
            return station;
        }

        /// <summary>
        /// Updates available slots for a station
        /// </summary>
        /// <param name="stationId">Station ID</param>
        /// <param name="slotsChange">Change in available slots (positive or negative)</param>
        /// <returns>Success status</returns>
        public async Task<bool> UpdateAvailableSlotsAsync(string stationId, int slotsChange)
        {
            var update = Builders<ChargingStation>.Update
                .Inc(s => s.AvailableSlots, slotsChange)
                .Set(s => s.UpdatedAt, DateTime.UtcNow);

            var result = await _stations.UpdateOneAsync(s => s.Id == stationId, update);
            return result.ModifiedCount > 0;
        }

        /// <summary>
        /// Deletes a charging station
        /// </summary>
        /// <param name="id">Station ID</param>
        /// <returns>Success status</returns>
        public async Task<bool> DeleteAsync(string id)
        {
            var result = await _stations.DeleteOneAsync(s => s.Id == id);
            return result.DeletedCount > 0;
        }
    }
}