/*
 * File: ChargingStationsController.cs
 * Project: EV Charging Station Booking System
 * Description: Charging station management controller
 * Author: EV Charging System
 * Date: September 27, 2025
 */

using EVChargingBackend.DTOs;
using EVChargingBackend.Models;
using EVChargingBackend.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace EVChargingBackend.Controllers
{
    /// <summary>
    /// Controller for charging station management operations
    /// </summary>
    [ApiController]
    [Route("api/[controller]")]
    [Authorize]
    public class ChargingStationsController : ControllerBase
    {
        private readonly IChargingStationService _stationService;
        private readonly ILogger<ChargingStationsController> _logger;

        /// <summary>
        /// Initializes charging stations controller with dependencies
        /// </summary>
        /// <param name="stationService">Charging station service</param>
        /// <param name="logger">Logger for controller operations</param>
        public ChargingStationsController(IChargingStationService stationService, ILogger<ChargingStationsController> logger)
        {
            _stationService = stationService;
            _logger = logger;
        }

        /// <summary>
        /// Creates a new charging station (backoffice only)
        /// </summary>
        /// <param name="createDto">Station creation data</param>
        /// <returns>Created station information</returns>
        [HttpPost]
        [Authorize(Roles = nameof(UserRole.Backoffice))]
        public async Task<ActionResult<StationResponseDto>> CreateStation([FromBody] CreateStationDto createDto)
        {
            _logger.LogInformation("Creating new charging station: {Name}", createDto.Name);
            
            var station = await _stationService.CreateStationAsync(createDto);
            
            return CreatedAtAction(nameof(GetStation), new { id = station.Id }, station);
        }

        /// <summary>
        /// Gets all charging stations
        /// </summary>
        /// <returns>List of all charging stations</returns>
        [HttpGet]
        public async Task<ActionResult<List<StationResponseDto>>> GetAllStations()
        {
            _logger.LogInformation("Fetching all charging stations");
            
            var stations = await _stationService.GetAllStationsAsync();
            
            return Ok(stations);
        }

        /// <summary>
        /// Gets active charging stations only
        /// </summary>
        /// <returns>List of active charging stations</returns>
        [HttpGet("active")]
        public async Task<ActionResult<List<StationResponseDto>>> GetActiveStations()
        {
            _logger.LogInformation("Fetching active charging stations");
            
            var stations = await _stationService.GetActiveStationsAsync();
            
            return Ok(stations);
        }

        /// <summary>
        /// Gets charging station by ID
        /// </summary>
        /// <param name="id">Station ID</param>
        /// <returns>Station information</returns>
        [HttpGet("{id}")]
        public async Task<ActionResult<StationResponseDto>> GetStation(string id)
        {
            _logger.LogInformation("Fetching charging station: {StationId}", id);
            
            var station = await _stationService.GetStationByIdAsync(id);
            
            return Ok(station);
        }

        /// <summary>
        /// Updates charging station information (backoffice only)
        /// </summary>
        /// <param name="id">Station ID</param>
        /// <param name="updateDto">Updated station data</param>
        /// <returns>Updated station information</returns>
        [HttpPut("{id}")]
        [Authorize(Roles = nameof(UserRole.Backoffice))]
        public async Task<ActionResult<StationResponseDto>> UpdateStation(string id, [FromBody] UpdateStationDto updateDto)
        {
            _logger.LogInformation("Updating charging station: {StationId}", id);
            
            var station = await _stationService.UpdateStationAsync(id, updateDto);
            
            return Ok(station);
        }

        /// <summary>
        /// Deactivates a charging station (backoffice only)
        /// </summary>
        /// <param name="id">Station ID</param>
        /// <returns>Success status</returns>
        [HttpPost("{id}/deactivate")]
        [Authorize(Roles = nameof(UserRole.Backoffice))]
        public async Task<ActionResult> DeactivateStation(string id)
        {
            _logger.LogInformation("Deactivating charging station: {StationId}", id);
            
            var result = await _stationService.DeactivateStationAsync(id);
            
            if (!result)
            {
                return NotFound("Charging station not found");
            }
            
            return Ok(new { message = "Charging station deactivated successfully" });
        }

        /// <summary>
        /// Deletes a charging station (backoffice only)
        /// </summary>
        /// <param name="id">Station ID</param>
        /// <returns>Success status</returns>
        [HttpDelete("{id}")]
        [Authorize(Roles = nameof(UserRole.Backoffice))]
        public async Task<ActionResult> DeleteStation(string id)
        {
            _logger.LogInformation("Deleting charging station: {StationId}", id);
            
            var result = await _stationService.DeleteStationAsync(id);
            
            if (!result)
            {
                return NotFound("Charging station not found");
            }
            
            return Ok(new { message = "Charging station deleted successfully" });
        }
    }
}