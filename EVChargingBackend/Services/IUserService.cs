/*
 * File: IUserService.cs
 * Project: EV Charging Station Booking System
 * Description: Interface for user management services
 * Author: EV Charging System
 * Date: September 27, 2025
 */

using EVChargingBackend.DTOs;

namespace EVChargingBackend.Services
{
    /// <summary>
    /// Interface defining user management service operations
    /// </summary>
    public interface IUserService
    {
        /// <summary>
        /// Gets all users (backoffice only)
        /// </summary>
        /// <returns>List of all users</returns>
        Task<List<UserResponseDto>> GetAllUsersAsync();

        /// <summary>
        /// Gets user by ID
        /// </summary>
        /// <param name="id">User ID</param>
        /// <returns>User if found</returns>
        Task<UserResponseDto> GetUserByIdAsync(string id);

        /// <summary>
        /// Updates user information
        /// </summary>
        /// <param name="id">User ID</param>
        /// <param name="updateDto">Updated user data</param>
        /// <returns>Updated user</returns>
        Task<UserResponseDto> UpdateUserAsync(string id, UpdateUserDto updateDto);

        /// <summary>
        /// Activates or deactivates a user (backoffice only)
        /// </summary>
        /// <param name="id">User ID</param>
        /// <param name="isActive">Active status</param>
        /// <returns>Success status</returns>
        Task<bool> SetUserActiveStatusAsync(string id, bool isActive);

        /// <summary>
        /// Deactivates current user (EV Owner self-deactivation)
        /// </summary>
        /// <param name="id">User ID</param>
        /// <returns>Success status</returns>
        Task<bool> DeactivateUserAsync(string id);

        /// <summary>
        /// Deletes a user (backoffice only)
        /// </summary>
        /// <param name="id">User ID</param>
        /// <returns>Success status</returns>
        Task<bool> DeleteUserAsync(string id);
    }
}