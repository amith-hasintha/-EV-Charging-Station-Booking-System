/*
 * File: IChargingStationService.cs
 * Project: EV Charging Station Booking System
 * Description: Interface for charging station management services
 * Author: EV Charging System
 * Date: September 27, 2025
 */

using EVChargingBackend.DTOs;

namespace EVChargingBackend.Services
{
    /// <summary>
    /// Interface defining charging station management service operations
    /// </summary>
    public interface IChargingStationService
    {
        /// <summary>
        /// Creates a new charging station
        /// </summary>
        /// <param name="createDto">Station creation data</param>
        /// <returns>Created station</returns>
        Task<StationResponseDto> CreateStationAsync(CreateStationDto createDto);

        /// <summary>
        /// Gets all charging stations
        /// </summary>
        /// <returns>List of all stations</returns>
        Task<List<StationResponseDto>> GetAllStationsAsync();

        /// <summary>
        /// Gets active charging stations only
        /// </summary>
        /// <returns>List of active stations</returns>
        Task<List<StationResponseDto>> GetActiveStationsAsync();

        /// <summary>
        /// Gets charging station by ID
        /// </summary>
        /// <param name="id">Station ID</param>
        /// <returns>Station if found</returns>
        Task<StationResponseDto> GetStationByIdAsync(string id);

        /// <summary>
        /// Updates charging station information
        /// </summary>
        /// <param name="id">Station ID</param>
        /// <param name="updateDto">Updated station data</param>
        /// <returns>Updated station</returns>
        Task<StationResponseDto> UpdateStationAsync(string id, UpdateStationDto updateDto);

        /// <summary>
        /// Deactivates a charging station (with validation)
        /// </summary>
        /// <param name="id">Station ID</param>
        /// <returns>Success status</returns>
        Task<bool> DeactivateStationAsync(string id);

        /// <summary>
        /// Deletes a charging station
        /// </summary>
        /// <param name="id">Station ID</param>
        /// <returns>Success status</returns>
        Task<bool> DeleteStationAsync(string id);
    }
}