/*
 * File: AuthController.cs
 * Project: EV Charging Station Booking System
 * Description: Authentication controller for login and registration
 * Author: EV Charging System
 * Date: September 27, 2025
 */

using EVChargingBackend.DTOs;
using EVChargingBackend.Services;
using Microsoft.AspNetCore.Mvc;

namespace EVChargingBackend.Controllers
{
    /// <summary>
    /// Controller for user authentication operations (login and registration)
    /// </summary>
    [ApiController]
    [Route("api/[controller]")]
    public class AuthController : ControllerBase
    {
        private readonly IAuthService _authService;
        private readonly ILogger<AuthController> _logger;

        /// <summary>
        /// Initializes authentication controller with dependencies
        /// </summary>
        /// <param name="authService">Authentication service</param>
        /// <param name="logger">Logger for controller operations</param>
        public AuthController(IAuthService authService, ILogger<AuthController> logger)
        {
            _authService = authService;
            _logger = logger;
        }

        /// <summary>
        /// Registers a new user in the system
        /// </summary>
        /// <param name="registerDto">User registration data</param>
        /// <returns>Created user information</returns>
        [HttpPost("register")]
        public async Task<ActionResult<UserResponseDto>> Register([FromBody] RegisterUserDto registerDto)
        {
            _logger.LogInformation("Registration attempt for email: {Email}", registerDto.Email);
            
            var user = await _authService.RegisterAsync(registerDto);
            
            return CreatedAtAction(nameof(Register), new { id = user.Id }, user);
        }

        /// <summary>
        /// Authenticates user login and returns JWT token
        /// </summary>
        /// <param name="loginDto">Login credentials</param>
        /// <returns>Login response with JWT token</returns>
        [HttpPost("login")]
        public async Task<ActionResult<LoginResponseDto>> Login([FromBody] LoginDto loginDto)
        {
            _logger.LogInformation("Login attempt for email: {Email}", loginDto.Email);
            
            var response = await _authService.LoginAsync(loginDto);
            
            return Ok(response);
        }
    }
}