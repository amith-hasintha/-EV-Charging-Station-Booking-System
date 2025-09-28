/*
 * File: MongoDbSettings.cs
 * Project: EV Charging Station Booking System
 * Description: MongoDB configuration settings
 * Author: EV Charging System
 * Date: September 27, 2025
 */

namespace EVChargingBackend.Config
{
    /// <summary>
    /// MongoDB connection and database configuration
    /// </summary>
    public class MongoDbSettings
    {
        public string ConnectionString { get; set; } = string.Empty;
        public string DatabaseName { get; set; } = string.Empty;
    }
}