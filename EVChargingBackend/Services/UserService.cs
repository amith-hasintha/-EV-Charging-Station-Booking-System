/*
 * File: UserService.cs
 * Project: EV Charging Station Booking System
 * Description: User management service implementation
 * Author: EV Charging System
 * Date: September 27, 2025
 */

using EVChargingBackend.DTOs;
using EVChargingBackend.Models;
using EVChargingBackend.Repositories;

namespace EVChargingBackend.Services
{
    /// <summary>
    /// Service implementation for user management operations
    /// </summary>
    public class UserService : IUserService
    {
        private readonly IUserRepository _userRepository;
        private readonly ILogger<UserService> _logger;

        /// <summary>
        /// Initializes user service with dependencies
        /// </summary>
        /// <param name="userRepository">User repository for data operations</param>
        /// <param name="logger">Logger for service operations</param>
        public UserService(IUserRepository userRepository, ILogger<UserService> logger)
        {
            _userRepository = userRepository;
            _logger = logger;
        }

        /// <summary>
        /// Gets all users (backoffice only)
        /// </summary>
        /// <returns>List of all users</returns>
        public async Task<List<UserResponseDto>> GetAllUsersAsync()
        {
            var users = await _userRepository.GetAllAsync();
            return users.Select(MapToUserResponseDto).ToList();
        }

        /// <summary>
        /// Gets user by ID
        /// </summary>
        /// <param name="id">User ID</param>
        /// <returns>User if found</returns>
        public async Task<UserResponseDto> GetUserByIdAsync(string id)
        {
            var user = await _userRepository.GetByIdAsync(id);
            if (user == null)
            {
                throw new KeyNotFoundException("User not found");
            }

            return MapToUserResponseDto(user);
        }

        /// <summary>
        /// Updates user information
        /// </summary>
        /// <param name="id">User ID</param>
        /// <param name="updateDto">Updated user data</param>
        /// <returns>Updated user</returns>
        public async Task<UserResponseDto> UpdateUserAsync(string id, UpdateUserDto updateDto)
        {
            var user = await _userRepository.GetByIdAsync(id);
            if (user == null)
            {
                throw new KeyNotFoundException("User not found");
            }

            // Update only provided fields
            if (!string.IsNullOrEmpty(updateDto.FirstName))
                user.FirstName = updateDto.FirstName;
            
            if (!string.IsNullOrEmpty(updateDto.LastName))
                user.LastName = updateDto.LastName;
            
            if (!string.IsNullOrEmpty(updateDto.Email))
            {
                // Check if new email is already in use
                var existingUser = await _userRepository.GetByEmailAsync(updateDto.Email);
                if (existingUser != null && existingUser.Id != id)
                {
                    throw new ArgumentException("Email is already in use");
                }
                user.Email = updateDto.Email;
            }
            
            if (updateDto.PhoneNumber != null)
                user.PhoneNumber = updateDto.PhoneNumber;

            var updatedUser = await _userRepository.UpdateAsync(user);
            
            _logger.LogInformation("User updated successfully: {UserId}", id);
            
            return MapToUserResponseDto(updatedUser);
        }

        /// <summary>
        /// Activates or deactivates a user (backoffice only)
        /// </summary>
        /// <param name="id">User ID</param>
        /// <param name="isActive">Active status</param>
        /// <returns>Success status</returns>
        public async Task<bool> SetUserActiveStatusAsync(string id, bool isActive)
        {
            var user = await _userRepository.GetByIdAsync(id);
            if (user == null)
            {
                throw new KeyNotFoundException("User not found");
            }

            var result = await _userRepository.SetActiveStatusAsync(id, isActive);
            
            if (result)
            {
                _logger.LogInformation("User {UserId} status changed to {Status}", id, isActive ? "Active" : "Inactive");
            }

            return result;
        }

        /// <summary>
        /// Deactivates current user (EV Owner self-deactivation)
        /// </summary>
        /// <param name="id">User ID</param>
        /// <returns>Success status</returns>
        public async Task<bool> DeactivateUserAsync(string id)
        {
            var user = await _userRepository.GetByIdAsync(id);
            if (user == null)
            {
                throw new KeyNotFoundException("User not found");
            }

            // Only EV Owners can deactivate themselves
            if (user.Role != UserRole.EVOwner)
            {
                throw new UnauthorizedAccessException("Only EV Owners can deactivate themselves");
            }

            var result = await _userRepository.SetActiveStatusAsync(id, false);
            
            if (result)
            {
                _logger.LogInformation("EV Owner {UserId} self-deactivated", id);
            }

            return result;
        }

        /// <summary>
        /// Deletes a user (backoffice only)
        /// </summary>
        /// <param name="id">User ID</param>
        /// <returns>Success status</returns>
        public async Task<bool> DeleteUserAsync(string id)
        {
            var user = await _userRepository.GetByIdAsync(id);
            if (user == null)
            {
                throw new KeyNotFoundException("User not found");
            }

            var result = await _userRepository.DeleteAsync(id);
            
            if (result)
            {
                _logger.LogInformation("User deleted: {UserId}", id);
            }

            return result;
        }

        /// <summary>
        /// Maps User entity to UserResponseDto
        /// </summary>
        /// <param name="user">User entity</param>
        /// <returns>User response DTO</returns>
        private static UserResponseDto MapToUserResponseDto(User user)
        {
            return new UserResponseDto
            {
                Id = user.Id ?? string.Empty,
                NIC = user.NIC,
                FirstName = user.FirstName,
                LastName = user.LastName,
                Email = user.Email,
                Role = user.Role,
                IsActive = user.IsActive,
                PhoneNumber = user.PhoneNumber,
                CreatedAt = user.CreatedAt
            };
        }
    }
}