/*
 * File: UsersController.cs
 * Project: EV Charging Station Booking System
 * Description: User management controller with role-based authorization
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
    /// Controller for user management operations with role-based access control
    /// </summary>
    [ApiController]
    [Route("api/[controller]")]
    [Authorize]
    public class UsersController : ControllerBase
    {
        private readonly IUserService _userService;
        private readonly ILogger<UsersController> _logger;

        /// <summary>
        /// Initializes users controller with dependencies
        /// </summary>
        /// <param name="userService">User service for business operations</param>
        /// <param name="logger">Logger for controller operations</param>
        public UsersController(IUserService userService, ILogger<UsersController> logger)
        {
            _userService = userService;
            _logger = logger;
        }

        /// <summary>
        /// Gets all users (backoffice only)
        /// </summary>
        /// <returns>List of all users</returns>
        [HttpGet]
        [Authorize(Roles = nameof(UserRole.Backoffice))]
        public async Task<ActionResult<List<UserResponseDto>>> GetAllUsers()
        {
            _logger.LogInformation("Fetching all users");
            
            var users = await _userService.GetAllUsersAsync();
            
            return Ok(users);
        }

        /// <summary>
        /// Gets user by ID
        /// </summary>
        /// <param name="id">User ID</param>
        /// <returns>User information</returns>
        [HttpGet("{id}")]
        public async Task<ActionResult<UserResponseDto>> GetUser(string id)
        {
            var currentUserId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            var currentUserRole = User.FindFirst(ClaimTypes.Role)?.Value;
            
            // Users can only view their own profile unless they are backoffice
            if (currentUserRole != nameof(UserRole.Backoffice) && currentUserId != id)
            {
                return Forbid("You can only view your own profile");
            }

            _logger.LogInformation("Fetching user: {UserId}", id);
            
            var user = await _userService.GetUserByIdAsync(id);
            
            return Ok(user);
        }

        /// <summary>
        /// Updates user information
        /// </summary>
        /// <param name="id">User ID</param>
        /// <param name="updateDto">Updated user data</param>
        /// <returns>Updated user information</returns>
        [HttpPut("{id}")]
        public async Task<ActionResult<UserResponseDto>> UpdateUser(string id, [FromBody] UpdateUserDto updateDto)
        {
            var currentUserId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            var currentUserRole = User.FindFirst(ClaimTypes.Role)?.Value;
            
            // Users can only update their own profile unless they are backoffice
            if (currentUserRole != nameof(UserRole.Backoffice) && currentUserId != id)
            {
                return Forbid("You can only update your own profile");
            }

            _logger.LogInformation("Updating user: {UserId}", id);
            
            var user = await _userService.UpdateUserAsync(id, updateDto);
            
            return Ok(user);
        }

        /// <summary>
        /// Activates or deactivates a user (backoffice only)
        /// </summary>
        /// <param name="id">User ID</param>
        /// <param name="isActive">Active status</param>
        /// <returns>Success status</returns>
        [HttpPatch("{id}/status")]
        [Authorize(Roles = nameof(UserRole.Backoffice))]
        public async Task<ActionResult> SetUserActiveStatus(string id, [FromBody] bool isActive)
        {
            _logger.LogInformation("Setting user {UserId} active status to {Status}", id, isActive);
            
            var result = await _userService.SetUserActiveStatusAsync(id, isActive);
            
            if (!result)
            {
                return NotFound("User not found");
            }
            
            return Ok(new { message = $"User {(isActive ? "activated" : "deactivated")} successfully" });
        }

        /// <summary>
        /// Deactivates current user (EV Owner self-deactivation)
        /// </summary>
        /// <returns>Success status</returns>
        [HttpPost("deactivate")]
        [Authorize(Roles = nameof(UserRole.EVOwner))]
        public async Task<ActionResult> DeactivateCurrentUser()
        {
            var currentUserId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            
            if (string.IsNullOrEmpty(currentUserId))
            {
                return BadRequest("Invalid user token");
            }

            _logger.LogInformation("User self-deactivation: {UserId}", currentUserId);
            
            var result = await _userService.DeactivateUserAsync(currentUserId);
            
            if (!result)
            {
                return NotFound("User not found");
            }
            
            return Ok(new { message = "Account deactivated successfully" });
        }

        /// <summary>
        /// Deletes a user (backoffice only)
        /// </summary>
        /// <param name="id">User ID</param>
        /// <returns>Success status</returns>
        [HttpDelete("{id}")]
        [Authorize(Roles = nameof(UserRole.Backoffice))]
        public async Task<ActionResult> DeleteUser(string id)
        {
            _logger.LogInformation("Deleting user: {UserId}", id);
            
            var result = await _userService.DeleteUserAsync(id);
            
            if (!result)
            {
                return NotFound("User not found");
            }
            
            return Ok(new { message = "User deleted successfully" });
        }
    }
}