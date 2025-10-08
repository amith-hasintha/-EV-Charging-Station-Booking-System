# EV Charging Station Booking System - Backend API

## Overview
This is a complete ASP.NET Core 8 Web API backend for an EV Charging Station Booking System with MongoDB database and JWT authentication.

## Features
- **User Management** with role-based authorization (Backoffice, Station Operator, EV Owner)
- **Charging Station Management** with AC/DC types and slot management
- **Booking Management** with business rules and QR code generation
- **Notification System** with real-time notifications for booking confirmations, cancellations, and reminders
- **JWT Authentication & Authorization**
- **MongoDB Integration** with repository pattern
- **Swagger Documentation** with JWT support
- **Centralized Error Handling**
- **Environment Configuration** with .env file
- **Sample Data Seeding**
- **Background Services** for automated notification reminders and cleanup

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

### 3. Configure Environment (SECURE) 🔒
**IMPORTANT**: For security, all sensitive configuration is stored in environment variables.

1. **Copy the environment template:**
   ```bash
   cp .env.example .env
   ```

2. **Edit the `.env` file with your actual values:**
   ```bash
   MONGODB_CONNECTION_STRING=mongodb+srv://username:password@cluster.mongodb.net/EVChargingDB?retryWrites=true&w=majority&appName=YourApp
   MONGODB_DATABASE_NAME=EVChargingDB
   JWT_KEY=your-super-secret-jwt-key-here-make-it-long-and-random
   JWT_ISSUER=EVChargingAPI
   JWT_AUDIENCE=EVChargingClients
   JWT_EXPIRY_MINUTES=60
   ```

3. **Security Features:**
   - ✅ **No credentials in source code** - All sensitive data in `.env` file
   - ✅ **`.env` file ignored by Git** - Never committed to repository
   - ✅ **Environment variable fallback** - Production-ready configuration
   - ✅ **Template provided** - `.env.example` for easy setup

**⚠️ NEVER commit the `.env` file to version control!**

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
- `POST /api/bookings/{id}/cancel-by-operator` - Cancel booking with reason (Operator only)

### Notifications 🔔
- `POST /api/notifications` - Create notification (Backoffice/Operator only)
- `POST /api/notifications/bulk` - Create bulk notifications (Backoffice only)
- `GET /api/notifications/my-notifications` - Get current user's notifications
  - Query params: `includeRead=true&limit=50&offset=0`
- `GET /api/notifications/unread` - Get unread notifications for current user
- `GET /api/notifications/by-type/{type}` - Get notifications by type (BookingConfirmation, BookingCancellation, BookingReminder, etc.)
- `GET /api/notifications/{id}` - Get notification by ID
- `GET /api/notifications/summary` - Get notification summary/statistics
- `POST /api/notifications/mark-read` - Mark specific notifications as read
- `POST /api/notifications/mark-all-read` - Mark all notifications as read
- `DELETE /api/notifications/{id}` - Delete notification
- `POST /api/notifications/cleanup-expired` - Cleanup expired notifications (Backoffice only)

#### Notification Types
- **BookingConfirmation** - Sent when operator confirms a booking
- **BookingCancellation** - Sent when booking is cancelled (by user or operator)
- **BookingReminder** - Automatically sent 2 hours before booking starts
- **StationUpdate** - Station status or information updates
- **SystemAlert** - System-wide alerts and maintenance notices
- **PaymentConfirmation** - Payment processing confirmations

#### Notification Priority Levels
- **Critical** - Urgent system alerts requiring immediate attention
- **High** - Important notifications like booking confirmations
- **Normal** - Regular notifications and reminders (default)
- **Low** - Informational notifications

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

### Notifications
- **Automatic Notifications**: System automatically sends notifications for:
  - Booking confirmations (when operator confirms)
  - Booking cancellations (user or operator initiated)
  - Booking reminders (2 hours before start time)
- **User Isolation**: Users can only view/manage their own notifications
- **Expiration**: Notifications can have expiration dates for automatic cleanup
- **Background Processing**: Automated reminder system runs every 30 minutes
- **Cleanup**: Expired notifications cleaned up every 6 hours
- **Metadata Support**: Rich context data for notifications (station names, times, etc.)
- **Priority System**: Critical, High, Normal, Low priority levels
- **Bulk Operations**: Support for bulk notification creation and management

## Testing with Swagger

1. Start the application (`dotnet run`)
2. Navigate to `http://localhost:5082`
3. Use the login endpoint with sample credentials
4. Copy the JWT token from the response
5. Click "Authorize" button in Swagger UI
6. Enter: `Bearer <your-jwt-token>`
7. Test the endpoints based on your role

## Notification System Workflow 🔔

### Automatic Notification Scenarios

#### 1. Booking Confirmation Flow
```
EV Owner creates booking → Booking status: Active
    ↓
Station Operator calls: POST /api/bookings/{id}/confirm
    ↓
System automatically:
  - Updates booking status to Confirmed
  - Creates confirmation notification for EV Owner
  - Notification includes: station name, booking times, QR code reference
    ↓
EV Owner receives notification via: GET /api/notifications/my-notifications
```

#### 2. Booking Cancellation Flow
```
Option A - User Cancellation:
EV Owner calls: POST /api/bookings/{id}/cancel
    ↓
System automatically sends cancellation notification

Option B - Operator Cancellation:
Operator calls: POST /api/bookings/{id}/cancel-by-operator
    ↓
System automatically sends cancellation notification with reason
```

#### 3. Automatic Reminder System
```
Background Service (runs every 30 minutes):
    ↓
Checks for confirmed bookings starting within 2 hours
    ↓
Sends reminder notifications to EV Owners
    ↓
Prevents duplicate reminders for same booking
```

### Testing Notification Flow

1. **Create an EV Owner account** and login
2. **Create a booking** using EV Owner credentials
3. **Login as Station Operator** 
4. **Confirm the booking**: `POST /api/bookings/{id}/confirm`
5. **Login back as EV Owner**
6. **Check notifications**: `GET /api/notifications/my-notifications`
7. **Verify notification received** with booking confirmation details

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

### 3. Confirm Booking (Station Operator)
```bash
POST /api/bookings/{bookingId}/confirm
Authorization: Bearer <operator-jwt-token>
# This automatically sends confirmation notification to EV Owner
```

### 4. Get My Notifications (EV Owner)
```bash
GET /api/notifications/my-notifications?includeRead=true&limit=20
Authorization: Bearer <ev-owner-jwt-token>
# Response includes booking confirmation notifications
```

### 5. Get Unread Notifications
```bash
GET /api/notifications/unread
Authorization: Bearer <jwt-token>
# Returns only unread notifications
```

### 6. Mark Notifications as Read
```bash
POST /api/notifications/mark-read
Authorization: Bearer <jwt-token>
{
  "notificationIds": ["notification-id-1", "notification-id-2"]
}
```

### 7. Cancel Booking with Operator Reason
```bash
POST /api/bookings/{bookingId}/cancel-by-operator
Authorization: Bearer <operator-jwt-token>
{
  "reason": "Station maintenance required"
}
# This automatically sends cancellation notification to EV Owner
```

### 8. Create Custom Notification (Operator)
```bash
POST /api/notifications
Authorization: Bearer <operator-jwt-token>
{
  "recipientNIC": "123456789V",
  "title": "Station Maintenance Alert",
  "message": "Your preferred charging station will be under maintenance tomorrow",
  "type": "StationUpdate",
  "priority": "High"
}
```

## Database Collections
- **Users**: User accounts with roles and authentication data
- **ChargingStations**: Charging station information and availability
- **Bookings**: Booking records with QR codes and status tracking
- **Notifications**: User notifications with types, priorities, and read status

## Security Features 🔒
- **JWT-based authentication** with configurable expiry
- **Role-based authorization** (Backoffice, Station Operator, EV Owner)
- **Password hashing with BCrypt** for secure credential storage
- **Environment variable configuration** - No credentials in source code
- **Secure credential management** - All sensitive data in `.env` file
- **Git security** - `.env` file automatically ignored by version control
- **Production-ready secrets** - Environment variable fallback support
- **CORS enabled** for secure frontend integration
- **Input validation** and sanitization throughout the API
- **Centralized error handling** prevents information leakage

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