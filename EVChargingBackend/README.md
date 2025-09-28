# EV Charging Station Booking System - Backend API

## Overview
This is a complete ASP.NET Core 8 Web API backend for an EV Charging Station Booking System with MongoDB database and JWT authentication.

## Features
- **User Management** with role-based authorization (Backoffice, Station Operator, EV Owner)
- **Charging Station Management** with AC/DC types and slot management
- **Booking Management** with business rules and QR code generation
- **JWT Authentication & Authorization**
- **MongoDB Integration** with repository pattern
- **Swagger Documentation** with JWT support
- **Centralized Error Handling**
- **Environment Configuration** with .env file
- **Sample Data Seeding**

## Architecture
- **Controllers**: Handle HTTP requests and responses
- **Services**: Business logic and validation
- **Repositories**: Data access layer
- **Models**: Entity definitions
- **DTOs**: Data transfer objects
- **Config**: Configuration classes
- **Middleware**: Global error handling

## Prerequisites
- .NET 8 SDK
- MongoDB (connection string in .env file)
- Visual Studio or VS Code

## Setup Instructions

### 1. Clone and Navigate
```bash
cd EVChargingBackend
```

### 2. Install Dependencies
```bash
dotnet restore
```

### 3. Configure Environment
The `.env` file is already configured with your MongoDB connection string:
```
MONGODB_CONNECTION_STRING=mongodb+srv://Amith:Amith123@agroprolk.6cgbg.mongodb.net/EVChargingDB?retryWrites=true&w=majority&appName=AgroProLK
MONGODB_DATABASE_NAME=EVChargingDB
JWT_KEY=5f8f8c9e-4d3b-4c2a-9f7b-2e5f8f8c9e4d
JWT_ISSUER=EVChargingAPI
JWT_AUDIENCE=EVChargingClients
JWT_EXPIRY_MINUTES=60
```

### 4. Build the Project
```bash
dotnet build
```

### 5. Run the Application
```bash
dotnet run
```

The API will be available at: `http://localhost:5082`
Swagger UI will be accessible at: `http://localhost:5082` (root path)

## Sample Users (Auto-seeded)
The application automatically seeds sample data on first run:

### Backoffice User
- Email: `admin@evcharging.com`
- Password: `Admin123!`
- Role: Backoffice

### Station Operator
- Email: `operator@evcharging.com`
- Password: `Operator123!`
- Role: StationOperator

### EV Owners
- Email: `john.doe@example.com`, Password: `John123!`
- Email: `jane.smith@example.com`, Password: `Jane123!`

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - User login

### Users Management
- `GET /api/users` - Get all users (Backoffice only)
- `GET /api/users/{id}` - Get user by ID
- `PUT /api/users/{id}` - Update user
- `PATCH /api/users/{id}/status` - Activate/deactivate user (Backoffice only)
- `POST /api/users/deactivate` - Self-deactivate (EV Owner only)
- `DELETE /api/users/{id}` - Delete user (Backoffice only)

### Charging Stations
- `POST /api/chargingstations` - Create station (Backoffice only)
- `GET /api/chargingstations` - Get all stations
- `GET /api/chargingstations/active` - Get active stations only
- `GET /api/chargingstations/{id}` - Get station by ID
- `PUT /api/chargingstations/{id}` - Update station (Backoffice only)
- `POST /api/chargingstations/{id}/deactivate` - Deactivate station (Backoffice only)
- `DELETE /api/chargingstations/{id}` - Delete station (Backoffice only)

### Bookings
- `POST /api/bookings` - Create booking (EV Owner only)
- `GET /api/bookings` - Get all bookings (Backoffice/Operator only)
- `GET /api/bookings/my-bookings` - Get current user's bookings (EV Owner only)
- `GET /api/bookings/station/{stationId}` - Get station bookings (Operator only)
- `GET /api/bookings/{id}` - Get booking by ID
- `PUT /api/bookings/{id}` - Update booking (EV Owner only, own bookings)
- `POST /api/bookings/{id}/confirm` - Confirm booking (Operator only)
- `POST /api/bookings/{id}/cancel` - Cancel booking (EV Owner only, own bookings)

## Business Rules

### Bookings
- Maximum 7 days in advance
- Updates/cancellations must be ≥12 hours before booking
- Minimum duration: 1 hour
- Maximum duration: 24 hours
- QR code generated for each booking
- Automatic slot management

### Users
- NIC is unique identifier for EV Owners
- EV Owners can self-deactivate (only backoffice can reactivate)
- Role-based access control throughout

### Charging Stations
- Cannot deactivate stations with active bookings
- Automatic available slot calculation
- AC/DC type support

## Testing with Swagger

1. Start the application (`dotnet run`)
2. Navigate to `http://localhost:5082`
3. Use the login endpoint with sample credentials
4. Copy the JWT token from the response
5. Click "Authorize" button in Swagger UI
6. Enter: `Bearer <your-jwt-token>`
7. Test the endpoints based on your role

## Sample API Usage

### 1. Login
```bash
POST /api/auth/login
{
  "email": "admin@evcharging.com",
  "password": "Admin123!"
}
```

### 2. Create Booking (EV Owner)
```bash
POST /api/bookings
Authorization: Bearer <jwt-token>
{
  "stationId": "<station-id>",
  "startTime": "2025-09-28T10:00:00Z",
  "endTime": "2025-09-28T12:00:00Z"
}
```

## Database Collections
- **Users**: User accounts with roles
- **ChargingStations**: Charging station information
- **Bookings**: Booking records with QR codes

## Security Features
- JWT-based authentication
- Role-based authorization
- Password hashing with BCrypt
- Environment variable configuration
- CORS enabled for frontend integration

## Error Handling
Global exception middleware handles:
- Validation errors (400)
- Not found errors (404)
- Unauthorized access (401)
- Internal server errors (500)

## Logging
Comprehensive logging throughout the application:
- User operations
- Booking creation/updates
- Station management
- Error tracking

## Deployment Notes
- Configured for IIS deployment
- Environment variables for production security
- MongoDB connection secured via .env
- JWT secret key externalized

This system is production-ready with proper architecture, security, and error handling!