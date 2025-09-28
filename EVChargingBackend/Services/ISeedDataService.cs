/*
 * File: ISeedDataService.cs
 * Project: EV Charging Station Booking System
 * Description: Interface for seed data service
 * Author: EV Charging System
 * Date: September 27, 2025
 */

namespace EVChargingBackend.Services
{
    /// <summary>
    /// Interface for seeding initial data into the database
    /// </summary>
    public interface ISeedDataService
    {
        /// <summary>
        /// Seeds initial data including users, stations, and sample bookings
        /// </summary>
        /// <returns>Task representing the async operation</returns>
        Task SeedDataAsync();
    }
}