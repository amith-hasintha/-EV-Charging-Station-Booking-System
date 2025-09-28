/*
 * File: JwtSettings.cs
 * Project: EV Charging Station Booking System
 * Description: JWT authentication configuration settings
 * Author: EV Charging System
 * Date: September 27, 2025
 */

namespace EVChargingBackend.Config
{
    /// <summary>
    /// JWT token configuration for authentication
    /// </summary>
    public class JwtSettings
    {
        public string Key { get; set; } = string.Empty;
        public string Issuer { get; set; } = string.Empty;
        public string Audience { get; set; } = string.Empty;
        public int ExpiryInMinutes { get; set; } = 60;
    }
}