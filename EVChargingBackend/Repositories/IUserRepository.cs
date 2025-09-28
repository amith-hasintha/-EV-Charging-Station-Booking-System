/*
 * File: IUserRepository.cs
 * Project: EV Charging Station Booking System
 * Description: Interface for User repository operations
 * Author: EV Charging System
 * Date: September 27, 2025
 */

using EVChargingBackend.Models;

namespace EVChargingBackend.Repositories
{
    /// <summary>
    /// Interface defining user repository operations
    /// </summary>
    public interface IUserRepository
    {
        /// <summary>
        /// Creates a new user in the database
        /// </summary>
        /// <param name="user">User entity to create</param>
        /// <returns>Created user</returns>
        Task<User> CreateAsync(User user);

        /// <summary>
        /// Gets user by email address
        /// </summary>
        /// <param name="email">Email address</param>
        /// <returns>User if found, null otherwise</returns>
        Task<User?> GetByEmailAsync(string email);

        /// <summary>
        /// Gets user by NIC (National Identity Card)
        /// </summary>
        /// <param name="nic">NIC number</param>
        /// <returns>User if found, null otherwise</returns>
        Task<User?> GetByNicAsync(string nic);

        /// <summary>
        /// Gets user by ID
        /// </summary>
        /// <param name="id">User ID</param>
        /// <returns>User if found, null otherwise</returns>
        Task<User?> GetByIdAsync(string id);

        /// <summary>
        /// Gets all users
        /// </summary>
        /// <returns>List of all users</returns>
        Task<List<User>> GetAllAsync();

        /// <summary>
        /// Updates user information
        /// </summary>
        /// <param name="user">User entity with updated information</param>
        /// <returns>Updated user</returns>
        Task<User> UpdateAsync(User user);

        /// <summary>
        /// Activates or deactivates a user
        /// </summary>
        /// <param name="id">User ID</param>
        /// <param name="isActive">Active status</param>
        /// <returns>Success status</returns>
        Task<bool> SetActiveStatusAsync(string id, bool isActive);

        /// <summary>
        /// Deletes a user from database
        /// </summary>
        /// <param name="id">User ID</param>
        /// <returns>Success status</returns>
        Task<bool> DeleteAsync(string id);
    }
}