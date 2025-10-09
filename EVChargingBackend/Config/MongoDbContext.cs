/*
 * File: MongoDbContext.cs
 * Project: EV Charging Station Booking System
 * Description: MongoDB context for database operations
 * Author: EV Charging System
 * Date: September 27, 2025
 */

using EVChargingBackend.Models;
using Microsoft.Extensions.Options;
using MongoDB.Driver;

namespace EVChargingBackend.Config
{
    /// <summary>
    /// MongoDB database context providing access to collections
    /// </summary>
    public class MongoDbContext
    {
        private readonly IMongoDatabase _database;

        /// <summary>
        /// Initializes MongoDB context with connection settings
        /// </summary>
        /// <param name="settings">MongoDB configuration settings</param>
        public MongoDbContext(IOptions<MongoDbSettings> settings)
        {
            var client = new MongoClient(settings.Value.ConnectionString);
            _database = client.GetDatabase(settings.Value.DatabaseName);
        }

        /// <summary>
        /// Gets the Users collection
        /// </summary>
        public IMongoCollection<User> Users => _database.GetCollection<User>("Users");

        /// <summary>
        /// Gets the ChargingStations collection
        /// </summary>
        public IMongoCollection<ChargingStation> ChargingStations => _database.GetCollection<ChargingStation>("ChargingStations");

        /// <summary>
        /// Gets the Bookings collection
        /// </summary>
        public IMongoCollection<Booking> Bookings => _database.GetCollection<Booking>("Bookings");

        /// <summary>
        /// Gets the Notifications collection
        /// </summary>
        public IMongoCollection<Notification> Notifications => _database.GetCollection<Notification>("Notifications");

        /// <summary>
        /// Gets the MongoDB database instance
        /// </summary>
        public IMongoDatabase Database => _database;
    }
}