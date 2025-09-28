/*
 * File: ChargingStationService.cs
 * Project: EV Charging Station Booking System
 * Description: Charging station management service implementation
 * Author: EV Charging System
 * Date: September 27, 2025
 */

using EVChargingBackend.DTOs;
using EVChargingBackend.Models;
using EVChargingBackend.Repositories;

namespace EVChargingBackend.Services
{
    /// <summary>
    /// Service implementation for charging station management operations
    /// </summary>
    public class ChargingStationService : IChargingStationService
    {
        private readonly IChargingStationRepository _stationRepository;
        private readonly IBookingRepository _bookingRepository;
        private readonly ILogger<ChargingStationService> _logger;

        /// <summary>
        /// Initializes charging station service with dependencies
        /// </summary>
        /// <param name="stationRepository">Station repository for data operations</param>
        /// <param name="bookingRepository">Booking repository for validation</param>
        /// <param name="logger">Logger for service operations</param>
        public ChargingStationService(
            IChargingStationRepository stationRepository,
            IBookingRepository bookingRepository,
            ILogger<ChargingStationService> logger)
        {
            _stationRepository = stationRepository;
            _bookingRepository = bookingRepository;
            _logger = logger;
        }

        /// <summary>
        /// Creates a new charging station
        /// </summary>
        /// <param name="createDto">Station creation data</param>
        /// <returns>Created station</returns>
        public async Task<StationResponseDto> CreateStationAsync(CreateStationDto createDto)
        {
            var station = new ChargingStation
            {
                Name = createDto.Name,
                Location = createDto.Location,
                Type = createDto.Type,
                TotalSlots = createDto.TotalSlots,
                AvailableSlots = createDto.TotalSlots,
                PricePerHour = createDto.PricePerHour,
                Status = StationStatus.Active
            };

            var createdStation = await _stationRepository.CreateAsync(station);
            
            _logger.LogInformation("Charging station created: {StationId}", createdStation.Id);
            
            return MapToStationResponseDto(createdStation);
        }

        /// <summary>
        /// Gets all charging stations
        /// </summary>
        /// <returns>List of all stations</returns>
        public async Task<List<StationResponseDto>> GetAllStationsAsync()
        {
            var stations = await _stationRepository.GetAllAsync();
            return stations.Select(MapToStationResponseDto).ToList();
        }

        /// <summary>
        /// Gets active charging stations only
        /// </summary>
        /// <returns>List of active stations</returns>
        public async Task<List<StationResponseDto>> GetActiveStationsAsync()
        {
            var stations = await _stationRepository.GetActiveStationsAsync();
            return stations.Select(MapToStationResponseDto).ToList();
        }

        /// <summary>
        /// Gets charging station by ID
        /// </summary>
        /// <param name="id">Station ID</param>
        /// <returns>Station if found</returns>
        public async Task<StationResponseDto> GetStationByIdAsync(string id)
        {
            var station = await _stationRepository.GetByIdAsync(id);
            if (station == null)
            {
                throw new KeyNotFoundException("Charging station not found");
            }

            return MapToStationResponseDto(station);
        }

        /// <summary>
        /// Updates charging station information
        /// </summary>
        /// <param name="id">Station ID</param>
        /// <param name="updateDto">Updated station data</param>
        /// <returns>Updated station</returns>
        public async Task<StationResponseDto> UpdateStationAsync(string id, UpdateStationDto updateDto)
        {
            var station = await _stationRepository.GetByIdAsync(id);
            if (station == null)
            {
                throw new KeyNotFoundException("Charging station not found");
            }

            // Update only provided fields
            if (!string.IsNullOrEmpty(updateDto.Name))
                station.Name = updateDto.Name;

            if (!string.IsNullOrEmpty(updateDto.Location))
                station.Location = updateDto.Location;

            if (updateDto.Type.HasValue)
                station.Type = updateDto.Type.Value;

            if (updateDto.TotalSlots.HasValue)
            {
                var slotsDifference = updateDto.TotalSlots.Value - station.TotalSlots;
                station.TotalSlots = updateDto.TotalSlots.Value;
                station.AvailableSlots += slotsDifference; // Adjust available slots
                
                // Ensure available slots don't go below 0
                if (station.AvailableSlots < 0)
                    station.AvailableSlots = 0;
            }

            if (updateDto.PricePerHour.HasValue)
                station.PricePerHour = updateDto.PricePerHour.Value;

            if (updateDto.Status.HasValue)
                station.Status = updateDto.Status.Value;

            var updatedStation = await _stationRepository.UpdateAsync(station);
            
            _logger.LogInformation("Charging station updated: {StationId}", id);
            
            return MapToStationResponseDto(updatedStation);
        }

        /// <summary>
        /// Deactivates a charging station (with validation)
        /// </summary>
        /// <param name="id">Station ID</param>
        /// <returns>Success status</returns>
        public async Task<bool> DeactivateStationAsync(string id)
        {
            var station = await _stationRepository.GetByIdAsync(id);
            if (station == null)
            {
                throw new KeyNotFoundException("Charging station not found");
            }

            // Check for active bookings
            var activeBookings = await _bookingRepository.GetActiveBookingCountAsync(id);
            if (activeBookings > 0)
            {
                throw new ArgumentException("Cannot deactivate station with active bookings");
            }

            station.Status = StationStatus.Inactive;
            await _stationRepository.UpdateAsync(station);
            
            _logger.LogInformation("Charging station deactivated: {StationId}", id);
            
            return true;
        }

        /// <summary>
        /// Deletes a charging station
        /// </summary>
        /// <param name="id">Station ID</param>
        /// <returns>Success status</returns>
        public async Task<bool> DeleteStationAsync(string id)
        {
            var station = await _stationRepository.GetByIdAsync(id);
            if (station == null)
            {
                throw new KeyNotFoundException("Charging station not found");
            }

            // Check for any bookings (not just active ones)
            var allBookings = await _bookingRepository.GetByStationAsync(id);
            if (allBookings.Any())
            {
                throw new ArgumentException("Cannot delete station with existing bookings");
            }

            var result = await _stationRepository.DeleteAsync(id);
            
            if (result)
            {
                _logger.LogInformation("Charging station deleted: {StationId}", id);
            }

            return result;
        }

        /// <summary>
        /// Maps ChargingStation entity to StationResponseDto
        /// </summary>
        /// <param name="station">Charging station entity</param>
        /// <returns>Station response DTO</returns>
        private static StationResponseDto MapToStationResponseDto(ChargingStation station)
        {
            return new StationResponseDto
            {
                Id = station.Id ?? string.Empty,
                Name = station.Name,
                Location = station.Location,
                Type = station.Type,
                TotalSlots = station.TotalSlots,
                AvailableSlots = station.AvailableSlots,
                Status = station.Status,
                PricePerHour = station.PricePerHour,
                CreatedAt = station.CreatedAt,
                UpdatedAt = station.UpdatedAt
            };
        }
    }
}