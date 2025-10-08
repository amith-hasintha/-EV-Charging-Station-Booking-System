/*
 * File: UserDTOs.cs
 * Project: EV Charging Station Booking System
 * Description: Data Transfer Objects for User operations
 * Author: EV Charging System
 * Date: September 27, 2025
 */

using EVChargingBackend.Models;
using System.ComponentModel.DataAnnotations;

namespace EVChargingBackend.DTOs
{
    /// <summary>
    /// DTO for user registration
    /// </summary>
    public class RegisterUserDto
    {
        [Required]
        public string NIC { get; set; } = string.Empty;

        [Required]
        public string FirstName { get; set; } = string.Empty;

        [Required]
        public string LastName { get; set; } = string.Empty;

        [Required]
        [EmailAddress]
        public string Email { get; set; } = string.Empty;

        [Required]
        [MinLength(6)]
        public string Password { get; set; } = string.Empty;

        [Required]
        public UserRole Role { get; set; }

        public string? StationId { get; set; } 

        public string? PhoneNumber { get; set; }
    }

    /// <summary>
    /// DTO for user login
    /// </summary>
    public class LoginDto
    {
        [Required]
        public string Email { get; set; } = string.Empty;

        [Required]
        public string Password { get; set; } = string.Empty;
    }

    /// <summary>
    /// DTO for updating user information
    /// </summary>
    public class UpdateUserDto
    {
        public string? FirstName { get; set; }
        public string? LastName { get; set; }
        public string? PhoneNumber { get; set; }
        public string? Email { get; set; }
    }

    /// <summary>
    /// DTO for user response (excludes sensitive data)
    /// </summary>
    public class UserResponseDto
    {
        public string Id { get; set; } = string.Empty;
        public string NIC { get; set; } = string.Empty;
        public string FirstName { get; set; } = string.Empty;
        public string LastName { get; set; } = string.Empty;
        public string Email { get; set; } = string.Empty;
        public UserRole Role { get; set; }
        public bool IsActive { get; set; }
        public string? PhoneNumber { get; set; }
        public DateTime CreatedAt { get; set; }

        public string? StationId { get; set; }
    }

    /// <summary>
    /// DTO for login response with token
    /// </summary>
    public class LoginResponseDto
    {
        public string Token { get; set; } = string.Empty;
        public UserResponseDto User { get; set; } = null!;
    }
}