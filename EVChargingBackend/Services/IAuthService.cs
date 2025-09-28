/*
 * File: IAuthService.cs
 * Project: EV Charging Station Booking System
 * Description: Interface for authentication services
 * Author: EV Charging System
 * Date: September 27, 2025
 */

using EVChargingBackend.DTOs;

namespace EVChargingBackend.Services
{
    /// <summary>
    /// Interface defining authentication service operations
    /// </summary>
    public interface IAuthService
    {
        /// <summary>
        /// Registers a new user in the system
        /// </summary>
        /// <param name="registerDto">User registration data</param>
        /// <returns>Created user response</returns>
        Task<UserResponseDto> RegisterAsync(RegisterUserDto registerDto);

        /// <summary>
        /// Authenticates user login credentials
        /// </summary>
        /// <param name="loginDto">Login credentials</param>
        /// <returns>Login response with JWT token</returns>
        Task<LoginResponseDto> LoginAsync(LoginDto loginDto);

        /// <summary>
        /// Generates JWT token for authenticated user
        /// </summary>
        /// <param name="user">User data for token generation</param>
        /// <returns>JWT token string</returns>
        string GenerateJwtToken(UserResponseDto user);
    }
}