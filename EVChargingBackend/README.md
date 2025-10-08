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

#### Option A: Development (dotnet run)
```bash
dotnet run
```
The API will be available at: `http://localhost:5082`

#### Option B: IIS Express (Visual Studio)
```bash
dotnet run --launch-profile "IIS Express"
```
The API will be available at: `http://localhost:5000`

#### Option C: IIS Production Hosting (See IIS Deployment section below)

Swagger UI will be accessible at the root path of any of the above URLs.

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

---

## 🚀 IIS Deployment Guide

### Prerequisites for IIS Hosting
- Windows 10/11 or Windows Server with IIS enabled
- .NET 8 Runtime and ASP.NET Core Hosting Bundle
- Administrator access to configure IIS

### Step 1: Install Required Components

#### 1.1 Enable IIS on Windows
**Windows 10/11:**
1. Open **Control Panel** → **Programs** → **Turn Windows features on or off**
2. Check **Internet Information Services**
3. Expand **IIS** → **World Wide Web Services** → **Application Development Features**
4. Ensure these are checked:
   - ✅ **ASP.NET Core Hosting Bundle** (or install separately)
   - ✅ **IIS Management Console**
   - ✅ **Common HTTP Features**

#### 1.2 Install ASP.NET Core Hosting Bundle
1. Download from: https://dotnet.microsoft.com/en-us/download/dotnet/8.0
2. Look for **"ASP.NET Core Runtime 8.0.x - Windows Hosting Bundle"**
3. Run the installer as Administrator
4. **Restart IIS** after installation: `iisreset` in Admin Command Prompt

### Step 2: Publish the Application

#### 2.1 Build for Production
```bash
# Navigate to the project directory
cd "C:\Users\Amith\Desktop\EAD asnmnt\-EV-Charging-Station-Booking-System\EVChargingBackend"

# Publish the application
dotnet publish -c Release -o "C:\inetpub\wwwroot\EVChargingAPI"
```

#### 2.2 Verify Published Files
The publish folder should contain:
- `EVChargingBackend.dll`
- `web.config` (auto-generated or using our custom one)
- `wwwroot/` folder
- All dependencies in the same directory

### Step 3: Configure IIS

#### 3.1 Open IIS Manager
1. Press **Win + R**, type `inetmgr`, press Enter
2. Or search for **"Internet Information Services (IIS) Manager"**

#### 3.2 Create Application Pool
1. Right-click **"Application Pools"** → **"Add Application Pool..."**
2. **Name**: `EVChargingAPI`
3. **.NET CLR Version**: **"No Managed Code"** (Important!)
4. **Managed Pipeline Mode**: `Integrated`
5. Click **OK**

#### 3.3 Configure Application Pool Settings
1. Right-click the **EVChargingAPI** pool → **Advanced Settings...**
2. Set **Identity** to `ApplicationPoolIdentity` (default)
3. Set **Start Mode** to `AlwaysRunning` (optional, for better performance)
4. Click **OK**

#### 3.4 Create Website/Application
**Option A: Create New Website:**
1. Right-click **"Sites"** → **"Add Website..."**
2. **Site Name**: `EV Charging API`
3. **Application Pool**: Select `EVChargingAPI`
4. **Physical Path**: `C:\inetpub\wwwroot\EVChargingAPI`
5. **Binding**: 
   - Type: `http`
   - IP Address: `All Unassigned`
   - Port: `5000`
   - Host Name: (leave blank or use `localhost`)
6. Click **OK**

**Option B: Create Application under Default Web Site:**
1. Expand **"Sites"** → **"Default Web Site"**
2. Right-click **"Default Web Site"** → **"Add Application..."**
3. **Alias**: `evcharging`
4. **Application Pool**: Select `EVChargingAPI`
5. **Physical Path**: `C:\inetpub\wwwroot\EVChargingAPI`
6. Click **OK**

### Step 4: Configure Windows Firewall

#### 4.1 Allow Inbound Traffic
```powershell
# Run as Administrator in PowerShell
New-NetFirewallRule -DisplayName "EV Charging API" -Direction Inbound -Protocol TCP -LocalPort 5000 -Action Allow
```

**Or via Windows Firewall GUI:**
1. Open **Windows Defender Firewall** → **Advanced Settings**
2. Click **"Inbound Rules"** → **"New Rule..."**
3. Select **Port** → **TCP** → **Specific Local Ports**: `5000`
4. Select **Allow the connection**
5. Apply to all profiles, name it "EV Charging API"

### Step 5: Set Environment Variables (Production)

#### 5.1 Create Production Environment File
Create `.env` in the publish directory with production values:
```bash
# Production .env file in C:\inetpub\wwwroot\EVChargingAPI\.env
MONGODB_CONNECTION_STRING=your-production-mongodb-connection
MONGODB_DATABASE_NAME=EVChargingDB
JWT_KEY=your-production-jwt-secret-key
JWT_ISSUER=EVChargingAPI
JWT_AUDIENCE=EVChargingClients
JWT_EXPIRY_MINUTES=60
ASPNETCORE_ENVIRONMENT=Production
```

#### 5.2 Set IIS Environment Variables (Alternative)
1. In **IIS Manager**, click on your site/application
2. Double-click **"Configuration Editor"**
3. Section: `system.webServer/aspNetCore`
4. Click on **"environmentVariables"** → **"..."**
5. Add each environment variable

### Step 6: Test the Deployment

#### 6.1 Start the Site
1. In **IIS Manager**, select your site
2. Click **"Start"** in the Actions panel (if not already started)

#### 6.2 Test API Endpoints
Open browser and navigate to:
- **Swagger UI**: `http://localhost:5000` (if new website)
- **Or**: `http://localhost/evcharging` (if application under Default Web Site)

#### 6.3 Verify API Functionality
Test these endpoints:
```bash
# Health check
GET http://localhost:5000/api/chargingstations/active

# Authentication
POST http://localhost:5000/api/auth/login
{
  "email": "admin@evcharging.com",
  "password": "Admin123!"
}
```

### Step 7: Troubleshooting

#### 7.1 Common Issues and Solutions

**"HTTP Error 500.31 - Failed to load ASP.NET Core runtime"**
- Solution: Install ASP.NET Core Hosting Bundle and restart IIS

**"HTTP Error 403.14 - Forbidden"**
- Solution: Ensure Application Pool is set to "No Managed Code"

**"Database connection issues"**
- Solution: Verify `.env` file exists and MongoDB connection string is correct

**"JWT Token issues"**
- Solution: Check JWT_KEY in environment variables

#### 7.2 Check Logs
**Event Viewer Logs:**
1. Open **Event Viewer** → **Windows Logs** → **Application**
2. Look for ASP.NET Core related errors

**IIS Logs:**
- Location: `C:\inetpub\logs\LogFiles\W3SVC1\`

**Application Logs:**
- Check the `logs` folder in your publish directory (if configured in `web.config`)

#### 7.3 Useful Commands
```powershell
# Restart IIS (run as Administrator)
iisreset

# Check .NET Core runtime
dotnet --info

# Test if port is listening
netstat -an | findstr :5000

# Check application pool status
%windir%\system32\inetsrv\appcmd list apppool
```

### Step 8: Production Considerations

#### 8.1 SSL/HTTPS Configuration
For production, configure HTTPS:
1. Obtain SSL certificate
2. Bind certificate to IIS site
3. Update firewall rules for port 443
4. Update environment variables accordingly

#### 8.2 Performance Optimization
- Enable **Output Caching** in IIS
- Configure **Compression**
- Set up **Application Initialization**
- Monitor performance counters

#### 8.3 Security Hardening
- Remove unused IIS modules
- Configure proper file permissions
- Set up regular security updates
- Configure proper logging and monitoring

### 📊 Quick Reference URLs
After successful deployment:
- **Swagger UI**: `http://localhost:5000/`
- **API Base**: `http://localhost:5000/api/`
- **Health Check**: `http://localhost:5000/api/chargingstations/active`

Your EV Charging Station Booking API is now ready for production use on IIS! 🎉