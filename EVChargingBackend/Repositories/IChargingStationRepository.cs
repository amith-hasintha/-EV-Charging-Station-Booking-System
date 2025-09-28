/*
 * File: IChargingStationRepository.cs
 * Project: EV Charging Station Booking System
 * Description: Interface for Charging Station repository operations
 * Author: EV Charging System
 * Date: September 27, 2025
 */

using EVChargingBackend.Models;

namespace EVChargingBackend.Repositories
{
    /// <summary>
    /// Interface defining charging station repository operations
    /// </summary>
    public interface IChargingStationRepository
    {
        /// <summary>
        /// Creates a new charging station
        /// </summary>
        /// <param name="station">Charging station to create</param>
        /// <returns>Created charging station</returns>
        Task<ChargingStation> CreateAsync(ChargingStation station);

        /// <summary>
        /// Gets charging station by ID
        /// </summary>
        /// <param name="id">Station ID</param>
        /// <returns>Charging station if found, null otherwise</returns>
        Task<ChargingStation?> GetByIdAsync(string id);

        /// <summary>
        /// Gets all charging stations
        /// </summary>
        /// <returns>List of all charging stations</returns>
        Task<List<ChargingStation>> GetAllAsync();

        /// <summary>
        /// Gets active charging stations only
        /// </summary>
        /// <returns>List of active charging stations</returns>
        Task<List<ChargingStation>> GetActiveStationsAsync();

        /// <summary>
        /// Updates charging station information
        /// </summary>
        /// <param name="station">Station with updated information</param>
        /// <returns>Updated charging station</returns>
        Task<ChargingStation> UpdateAsync(ChargingStation station);

        /// <summary>
        /// Updates available slots for a station
        /// </summary>
        /// <param name="stationId">Station ID</param>
        /// <param name="slotsChange">Change in available slots (positive or negative)</param>
        /// <returns>Success status</returns>
        Task<bool> UpdateAvailableSlotsAsync(string stationId, int slotsChange);

        /// <summary>
        /// Deletes a charging station
        /// </summary>
        /// <param name="id">Station ID</param>
        /// <returns>Success status</returns>
        Task<bool> DeleteAsync(string id);
    }
}