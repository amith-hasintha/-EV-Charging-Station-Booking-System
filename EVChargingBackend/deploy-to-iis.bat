@echo off
echo =====================================
echo EV Charging API - IIS Deployment Script
echo =====================================
echo.

REM Set variables
set PROJECT_PATH=%~dp0
set PUBLISH_PATH=C:\inetpub\wwwroot\EVChargingAPI
set PROJECT_NAME=EVChargingBackend

echo Project Path: %PROJECT_PATH%
echo Publish Path: %PUBLISH_PATH%
echo.

echo [1/4] Cleaning previous build...
dotnet clean -c Release
if %ERRORLEVEL% neq 0 (
    echo ERROR: Failed to clean project
    pause
    exit /b 1
)

echo [2/4] Restoring packages...
dotnet restore
if %ERRORLEVEL% neq 0 (
    echo ERROR: Failed to restore packages
    pause
    exit /b 1
)

echo [3/4] Publishing application...
dotnet publish -c Release -o "%PUBLISH_PATH%" --no-restore
if %ERRORLEVEL% neq 0 (
    echo ERROR: Failed to publish application
    pause
    exit /b 1
)

echo [4/4] Copying environment file...
if exist ".env" (
    copy ".env" "%PUBLISH_PATH%\.env" >nul
    echo Environment file copied successfully
) else (
    echo WARNING: .env file not found - make sure to configure environment variables in IIS
)

echo.
echo =====================================
echo Deployment completed successfully!
echo =====================================
echo.
echo Published to: %PUBLISH_PATH%
echo.
echo Next Steps:
echo 1. Open IIS Manager (inetmgr)
echo 2. Create/Configure Application Pool (No Managed Code)
echo 3. Create Website/Application pointing to: %PUBLISH_PATH%
echo 4. Test at: http://localhost:5000
echo.
echo For detailed instructions, see README.md
echo.
pause