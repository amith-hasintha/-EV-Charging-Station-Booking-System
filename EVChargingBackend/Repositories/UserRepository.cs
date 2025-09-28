/*
 * File: UserRepository.cs
 * Project: EV Charging Station Booking System
 * Description: Repository implementation for User operations
 * Author: EV Charging System
 * Date: September 27, 2025
 */

using EVChargingBackend.Config;
using EVChargingBackend.Models;
using MongoDB.Driver;

namespace EVChargingBackend.Repositories
{
    /// <summary>
    /// Repository implementation for User data operations
    /// </summary>
    public class UserRepository : IUserRepository
    {
        private readonly IMongoCollection<User> _users;

        /// <summary>
        /// Initializes user repository with MongoDB context
        /// </summary>
        /// <param name="context">MongoDB database context</param>
        public UserRepository(MongoDbContext context)
        {
            _users = context.Users;
        }

        /// <summary>
        /// Creates a new user in the database
        /// </summary>
        /// <param name="user">User entity to create</param>
        /// <returns>Created user</returns>
        public async Task<User> CreateAsync(User user)
        {
            user.CreatedAt = DateTime.UtcNow;
            user.UpdatedAt = DateTime.UtcNow;
            await _users.InsertOneAsync(user);
            return user;
        }

        /// <summary>
        /// Gets user by email address
        /// </summary>
        /// <param name="email">Email address</param>
        /// <returns>User if found, null otherwise</returns>
        public async Task<User?> GetByEmailAsync(string email)
        {
            return await _users.Find(u => u.Email == email).FirstOrDefaultAsync();
        }

        /// <summary>
        /// Gets user by NIC (National Identity Card)
        /// </summary>
        /// <param name="nic">NIC number</param>
        /// <returns>User if found, null otherwise</returns>
        public async Task<User?> GetByNicAsync(string nic)
        {
            return await _users.Find(u => u.NIC == nic).FirstOrDefaultAsync();
        }

        /// <summary>
        /// Gets user by ID
        /// </summary>
        /// <param name="id">User ID</param>
        /// <returns>User if found, null otherwise</returns>
        public async Task<User?> GetByIdAsync(string id)
        {
            return await _users.Find(u => u.Id == id).FirstOrDefaultAsync();
        }

        /// <summary>
        /// Gets all users
        /// </summary>
        /// <returns>List of all users</returns>
        public async Task<List<User>> GetAllAsync()
        {
            return await _users.Find(_ => true).ToListAsync();
        }

        /// <summary>
        /// Updates user information
        /// </summary>
        /// <param name="user">User entity with updated information</param>
        /// <returns>Updated user</returns>
        public async Task<User> UpdateAsync(User user)
        {
            user.UpdatedAt = DateTime.UtcNow;
            await _users.ReplaceOneAsync(u => u.Id == user.Id, user);
            return user;
        }

        /// <summary>
        /// Activates or deactivates a user
        /// </summary>
        /// <param name="id">User ID</param>
        /// <param name="isActive">Active status</param>
        /// <returns>Success status</returns>
        public async Task<bool> SetActiveStatusAsync(string id, bool isActive)
        {
            var update = Builders<User>.Update
                .Set(u => u.IsActive, isActive)
                .Set(u => u.UpdatedAt, DateTime.UtcNow);
            
            var result = await _users.UpdateOneAsync(u => u.Id == id, update);
            return result.ModifiedCount > 0;
        }

        /// <summary>
        /// Deletes a user from database
        /// </summary>
        /// <param name="id">User ID</param>
        /// <returns>Success status</returns>
        public async Task<bool> DeleteAsync(string id)
        {
            var result = await _users.DeleteOneAsync(u => u.Id == id);
            return result.DeletedCount > 0;
        }
    }
}