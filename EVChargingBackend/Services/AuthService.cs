/*
 * File: AuthService.cs
 * Project: EV Charging Station Booking System
 * Description: Authentication service implementation with JWT token generation
 * Author: EV Charging System
 * Date: September 27, 2025
 */

using EVChargingBackend.Config;
using EVChargingBackend.DTOs;
using EVChargingBackend.Models;
using EVChargingBackend.Repositories;
using Microsoft.Extensions.Options;
using Microsoft.IdentityModel.Tokens;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;

namespace EVChargingBackend.Services
{
    /// <summary>
    /// Service implementation for user authentication and JWT token management
    /// </summary>
    public class AuthService : IAuthService
    {
        private readonly IUserRepository _userRepository;
        private readonly JwtSettings _jwtSettings;
        private readonly ILogger<AuthService> _logger;

        /// <summary>
        /// Initializes authentication service with dependencies
        /// </summary>
        /// <param name="userRepository">User repository for data operations</param>
        /// <param name="jwtSettings">JWT configuration settings</param>
        /// <param name="logger">Logger for service operations</param>
        public AuthService(
            IUserRepository userRepository,
            IOptions<JwtSettings> jwtSettings,
            ILogger<AuthService> logger)
        {
            _userRepository = userRepository;
            _jwtSettings = jwtSettings.Value;
            _logger = logger;
        }

        /// <summary>
        /// Registers a new user in the system
        /// </summary>
        /// <param name="registerDto">User registration data</param>
        /// <returns>Created user response</returns>
        public async Task<UserResponseDto> RegisterAsync(RegisterUserDto registerDto)
        {
            // Check if user already exists by email
            var existingUserByEmail = await _userRepository.GetByEmailAsync(registerDto.Email);
            if (existingUserByEmail != null)
            {
                throw new ArgumentException("User with this email already exists");
            }

            // Check if user already exists by NIC
            var existingUserByNic = await _userRepository.GetByNicAsync(registerDto.NIC);
            if (existingUserByNic != null)
            {
                throw new ArgumentException("User with this NIC already exists");
            }

            // Hash password (in production, use proper password hashing like BCrypt)
            var hashedPassword = BCrypt.Net.BCrypt.HashPassword(registerDto.Password);

            // Create user entity
            var user = new User
            {
                NIC = registerDto.NIC,
                FirstName = registerDto.FirstName,
                LastName = registerDto.LastName,
                Email = registerDto.Email,
                Password = hashedPassword,
                Role = registerDto.Role,
                StationId = (int)registerDto.Role == 1 ? registerDto.StationId : null,
                PhoneNumber = registerDto.PhoneNumber,
                IsActive = true
            };

            // Save user to database
            var createdUser = await _userRepository.CreateAsync(user);
            
            _logger.LogInformation("User registered successfully with email: {Email}", registerDto.Email);

            // Return user response DTO
            return MapToUserResponseDto(createdUser);
        }

        /// <summary>
        /// Authenticates user login credentials
        /// </summary>
        /// <param name="loginDto">Login credentials</param>
        /// <returns>Login response with JWT token</returns>
        public async Task<LoginResponseDto> LoginAsync(LoginDto loginDto)
        {
            // Get user by email
            var user = await _userRepository.GetByEmailAsync(loginDto.Email);
            if (user == null)
            {
                throw new UnauthorizedAccessException("Invalid email or password");
            }

            // Check if user is active
            if (!user.IsActive)
            {
                throw new UnauthorizedAccessException("User account is deactivated");
            }

            // Verify password
            if (!BCrypt.Net.BCrypt.Verify(loginDto.Password, user.Password))
            {
                throw new UnauthorizedAccessException("Invalid email or password");
            }

            var userResponse = MapToUserResponseDto(user);
            var token = GenerateJwtToken(userResponse);

            _logger.LogInformation("User logged in successfully: {Email}", loginDto.Email);

            if (user.Role == UserRole.StationOperator)
            {
                userResponse.StationId = user.StationId;
            }

            return new LoginResponseDto
            {
                Token = token,
                User = userResponse
            };
        }

        /// <summary>
        /// Generates JWT token for authenticated user
        /// </summary>
        /// <param name="user">User data for token generation</param>
        /// <returns>JWT token string</returns>
        public string GenerateJwtToken(UserResponseDto user)
        {
            var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(_jwtSettings.Key));
            var credentials = new SigningCredentials(key, SecurityAlgorithms.HmacSha256);

            var claims = new[]
            {
                new Claim(ClaimTypes.NameIdentifier, user.Id),
                new Claim(ClaimTypes.Name, user.Email),
                new Claim(ClaimTypes.Role, user.Role.ToString()),
                new Claim("nic", user.NIC),
                new Claim("firstName", user.FirstName),
                new Claim("lastName", user.LastName)
            };

            var token = new JwtSecurityToken(
                issuer: _jwtSettings.Issuer,
                audience: _jwtSettings.Audience,
                claims: claims,
                expires: DateTime.UtcNow.AddMinutes(_jwtSettings.ExpiryInMinutes),
                signingCredentials: credentials
            );

            return new JwtSecurityTokenHandler().WriteToken(token);
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
                StationId = user.StationId,
                IsActive = user.IsActive,
                PhoneNumber = user.PhoneNumber,
                CreatedAt = user.CreatedAt
            };
        }
    }
}