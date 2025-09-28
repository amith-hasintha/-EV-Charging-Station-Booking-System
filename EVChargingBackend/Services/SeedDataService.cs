/*
 * File: SeedDataService.cs
 * Project: EV Charging Station Booking System
 * Description: Service for seeding initial data into the database
 * Author: EV Charging System
 * Date: September 27, 2025
 */

using EVChargingBackend.Models;
using EVChargingBackend.Repositories;

namespace EVChargingBackend.Services
{
    /// <summary>
    /// Service implementation for seeding initial data into the database
    /// </summary>
    public class SeedDataService : ISeedDataService
    {
        private readonly IUserRepository _userRepository;
        private readonly IChargingStationRepository _stationRepository;
        private readonly IBookingRepository _bookingRepository;
        private readonly ILogger<SeedDataService> _logger;

        /// <summary>
        /// Initializes seed data service with repositories
        /// </summary>
        /// <param name="userRepository">User repository</param>
        /// <param name="stationRepository">Station repository</param>
        /// <param name="bookingRepository">Booking repository</param>
        /// <param name="logger">Logger for service operations</param>
        public SeedDataService(
            IUserRepository userRepository,
            IChargingStationRepository stationRepository,
            IBookingRepository bookingRepository,
            ILogger<SeedDataService> logger)
        {
            _userRepository = userRepository;
            _stationRepository = stationRepository;
            _bookingRepository = bookingRepository;
            _logger = logger;
        }

        /// <summary>
        /// Seeds initial data including users, stations, and sample bookings
        /// </summary>
        /// <returns>Task representing the async operation</returns>
        public async Task SeedDataAsync()
        {
            try
            {
                // Check if data already exists
                var existingUsers = await _userRepository.GetAllAsync();
                if (existingUsers.Any())
                {
                    _logger.LogInformation("Data already exists, skipping seed");
                    return;
                }

                _logger.LogInformation("Starting database seeding...");

                // Seed Users
                await SeedUsersAsync();
                
                // Seed Charging Stations
                await SeedChargingStationsAsync();
                
                // Seed Sample Bookings
                await SeedBookingsAsync();

                _logger.LogInformation("Database seeding completed successfully");
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error occurred during database seeding");
                throw;
            }
        }

        /// <summary>
        /// Seeds initial user accounts
        /// </summary>
        private async Task SeedUsersAsync()
        {
            var users = new[]
            {
                new User
                {
                    NIC = "123456789V",
                    FirstName = "Admin",
                    LastName = "User",
                    Email = "admin@evcharging.com",
                    Password = BCrypt.Net.BCrypt.HashPassword("Admin123!"),
                    Role = UserRole.Backoffice,
                    PhoneNumber = "+94771234567",
                    IsActive = true
                },
                new User
                {
                    NIC = "987654321V",
                    FirstName = "Station",
                    LastName = "Operator",
                    Email = "operator@evcharging.com",
                    Password = BCrypt.Net.BCrypt.HashPassword("Operator123!"),
                    Role = UserRole.StationOperator,
                    PhoneNumber = "+94777654321",
                    IsActive = true
                },
                new User
                {
                    NIC = "456789123V",
                    FirstName = "John",
                    LastName = "Doe",
                    Email = "john.doe@example.com",
                    Password = BCrypt.Net.BCrypt.HashPassword("John123!"),
                    Role = UserRole.EVOwner,
                    PhoneNumber = "+94774567891",
                    IsActive = true
                },
                new User
                {
                    NIC = "789123456V",
                    FirstName = "Jane",
                    LastName = "Smith",
                    Email = "jane.smith@example.com",
                    Password = BCrypt.Net.BCrypt.HashPassword("Jane123!"),
                    Role = UserRole.EVOwner,
                    PhoneNumber = "+94777891234",
                    IsActive = true
                },
                new User
                {
                    NIC = "321654987V",
                    FirstName = "Bob",
                    LastName = "Wilson",
                    Email = "bob.wilson@example.com",
                    Password = BCrypt.Net.BCrypt.HashPassword("Bob123!"),
                    Role = UserRole.EVOwner,
                    PhoneNumber = "+94773216549",
                    IsActive = false // Deactivated user
                }
            };

            foreach (var user in users)
            {
                await _userRepository.CreateAsync(user);
                _logger.LogInformation("Seeded user: {Email}", user.Email);
            }
        }

        /// <summary>
        /// Seeds initial charging stations
        /// </summary>
        private async Task SeedChargingStationsAsync()
        {
            var stations = new[]
            {
                new ChargingStation
                {
                    Name = "City Center DC Fast Charger",
                    Location = "Colombo City Center, Main Street",
                    Type = StationType.DC,
                    TotalSlots = 4,
                    AvailableSlots = 3,
                    Status = StationStatus.Active,
                    PricePerHour = 500.00m
                },
                new ChargingStation
                {
                    Name = "Shopping Mall AC Charger",
                    Location = "Kandy Shopping Complex, Level B1",
                    Type = StationType.AC,
                    TotalSlots = 8,
                    AvailableSlots = 6,
                    Status = StationStatus.Active,
                    PricePerHour = 300.00m
                },
                new ChargingStation
                {
                    Name = "Highway Rest Stop Charger",
                    Location = "Southern Expressway, Rest Area 1",
                    Type = StationType.DC,
                    TotalSlots = 6,
                    AvailableSlots = 6,
                    Status = StationStatus.Active,
                    PricePerHour = 600.00m
                },
                new ChargingStation
                {
                    Name = "University Campus Charger",
                    Location = "University of Colombo, Parking Area C",
                    Type = StationType.AC,
                    TotalSlots = 12,
                    AvailableSlots = 10,
                    Status = StationStatus.Active,
                    PricePerHour = 250.00m
                },
                new ChargingStation
                {
                    Name = "Business District Charger",
                    Location = "World Trade Center, Underground Parking",
                    Type = StationType.DC,
                    TotalSlots = 3,
                    AvailableSlots = 3,
                    Status = StationStatus.Maintenance,
                    PricePerHour = 550.00m
                }
            };

            foreach (var station in stations)
            {
                await _stationRepository.CreateAsync(station);
                _logger.LogInformation("Seeded charging station: {Name}", station.Name);
            }
        }

        /// <summary>
        /// Seeds sample bookings
        /// </summary>
        private async Task SeedBookingsAsync()
        {
            // Get seeded data for references
            var users = await _userRepository.GetAllAsync();
            var stations = await _stationRepository.GetAllAsync();
            
            var evOwners = users.Where(u => u.Role == UserRole.EVOwner && u.IsActive).ToList();
            var activeStations = stations.Where(s => s.Status == StationStatus.Active).ToList();

            if (!evOwners.Any() || !activeStations.Any())
            {
                _logger.LogWarning("No EV owners or active stations found for booking seeding");
                return;
            }

            var bookings = new[]
            {
                new Booking
                {
                    OwnerNIC = evOwners[0].NIC, // John Doe
                    StationId = activeStations[0].Id!, // City Center DC Fast Charger
                    StartTime = DateTime.UtcNow.AddHours(2),
                    EndTime = DateTime.UtcNow.AddHours(4),
                    Status = BookingStatus.Active,
                    QRCode = Guid.NewGuid().ToString("N").ToUpper(),
                    TotalAmount = 1000.00m
                },
                new Booking
                {
                    OwnerNIC = evOwners[1].NIC, // Jane Smith
                    StationId = activeStations[1].Id!, // Shopping Mall AC Charger
                    StartTime = DateTime.UtcNow.AddDays(1),
                    EndTime = DateTime.UtcNow.AddDays(1).AddHours(3),
                    Status = BookingStatus.Confirmed,
                    QRCode = Guid.NewGuid().ToString("N").ToUpper(),
                    TotalAmount = 900.00m,
                    ConfirmedAt = DateTime.UtcNow.AddMinutes(-30)
                },
                new Booking
                {
                    OwnerNIC = evOwners[0].NIC, // John Doe
                    StationId = activeStations[2].Id!, // Highway Rest Stop Charger
                    StartTime = DateTime.UtcNow.AddDays(-1),
                    EndTime = DateTime.UtcNow.AddDays(-1).AddHours(2),
                    Status = BookingStatus.Completed,
                    QRCode = Guid.NewGuid().ToString("N").ToUpper(),
                    TotalAmount = 1200.00m,
                    ConfirmedAt = DateTime.UtcNow.AddDays(-1).AddMinutes(-30)
                },
                new Booking
                {
                    OwnerNIC = evOwners[1].NIC, // Jane Smith
                    StationId = activeStations[3].Id!, // University Campus Charger
                    StartTime = DateTime.UtcNow.AddHours(6),
                    EndTime = DateTime.UtcNow.AddHours(8),
                    Status = BookingStatus.Cancelled,
                    QRCode = Guid.NewGuid().ToString("N").ToUpper(),
                    TotalAmount = 500.00m,
                    CancelledAt = DateTime.UtcNow.AddMinutes(-15)
                }
            };

            foreach (var booking in bookings)
            {
                await _bookingRepository.CreateAsync(booking);
                _logger.LogInformation("Seeded booking: {BookingId} for station {StationId}", booking.Id, booking.StationId);
            }
        }
    }
}