# EV Charging API - IIS Deployment Script (PowerShell)
# Run this script as Administrator for best results

param(
    [string]$PublishPath = "C:\inetpub\wwwroot\EVChargingAPI",
    [string]$SiteName = "EV Charging API",
    [string]$AppPoolName = "EVChargingAPI",
    [int]$Port = 5000
)

Write-Host "=========================================" -ForegroundColor Green
Write-Host "EV Charging API - IIS Deployment Script" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Green
Write-Host ""

$ProjectPath = $PSScriptRoot
Write-Host "Project Path: $ProjectPath" -ForegroundColor Yellow
Write-Host "Publish Path: $PublishPath" -ForegroundColor Yellow
Write-Host ""

try {
    # Step 1: Clean and Build
    Write-Host "[1/6] Cleaning previous build..." -ForegroundColor Cyan
    & dotnet clean -c Release
    if ($LASTEXITCODE -ne 0) { throw "Failed to clean project" }

    Write-Host "[2/6] Restoring packages..." -ForegroundColor Cyan
    & dotnet restore
    if ($LASTEXITCODE -ne 0) { throw "Failed to restore packages" }

    # Step 2: Publish
    Write-Host "[3/6] Publishing application..." -ForegroundColor Cyan
    & dotnet publish -c Release -o $PublishPath --no-restore
    if ($LASTEXITCODE -ne 0) { throw "Failed to publish application" }

    # Step 3: Copy environment file
    Write-Host "[4/6] Copying environment file..." -ForegroundColor Cyan
    if (Test-Path ".env") {
        Copy-Item ".env" "$PublishPath\.env" -Force
        Write-Host "Environment file copied successfully" -ForegroundColor Green
    } else {
        Write-Host "WARNING: .env file not found" -ForegroundColor Yellow
    }

    # Step 4: Configure IIS (if running as admin)
    $isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole] "Administrator")
    
    if ($isAdmin) {
        Write-Host "[5/6] Configuring IIS..." -ForegroundColor Cyan
        
        # Import WebAdministration module
        Import-Module WebAdministration -ErrorAction SilentlyContinue
        
        if (Get-Module -Name WebAdministration) {
            # Create Application Pool
            if (Get-WebAppPool -Name $AppPoolName -ErrorAction SilentlyContinue) {
                Write-Host "Application Pool '$AppPoolName' already exists" -ForegroundColor Yellow
            } else {
                New-WebAppPool -Name $AppPoolName
                Set-WebAppPool -Name $AppPoolName -processModel.identityType ApplicationPoolIdentity
                Set-WebAppPool -Name $AppPoolName -managedRuntimeVersion ""
                Write-Host "Application Pool '$AppPoolName' created" -ForegroundColor Green
            }
            
            # Create Website
            if (Get-WebSite -Name $SiteName -ErrorAction SilentlyContinue) {
                Write-Host "Website '$SiteName' already exists - updating..." -ForegroundColor Yellow
                Set-WebSite -Name $SiteName -PhysicalPath $PublishPath
            } else {
                New-WebSite -Name $SiteName -Port $Port -PhysicalPath $PublishPath -ApplicationPool $AppPoolName
                Write-Host "Website '$SiteName' created" -ForegroundColor Green
            }
            
            # Start the site
            Start-WebSite -Name $SiteName
            Write-Host "Website started" -ForegroundColor Green
        } else {
            Write-Host "IIS WebAdministration module not available - configure manually" -ForegroundColor Yellow
        }
    } else {
        Write-Host "[5/6] Skipping IIS configuration (requires Administrator privileges)" -ForegroundColor Yellow
    }

    # Step 5: Configure Firewall
    Write-Host "[6/6] Configuring Windows Firewall..." -ForegroundColor Cyan
    if ($isAdmin) {
        $ruleName = "EV Charging API Port $Port"
        $existingRule = Get-NetFirewallRule -DisplayName $ruleName -ErrorAction SilentlyContinue
        
        if (-not $existingRule) {
            New-NetFirewallRule -DisplayName $ruleName -Direction Inbound -Protocol TCP -LocalPort $Port -Action Allow
            Write-Host "Firewall rule created for port $Port" -ForegroundColor Green
        } else {
            Write-Host "Firewall rule already exists" -ForegroundColor Yellow
        }
    } else {
        Write-Host "Run as Administrator to configure firewall automatically" -ForegroundColor Yellow
    }

    Write-Host ""
    Write-Host "=========================================" -ForegroundColor Green
    Write-Host "Deployment completed successfully!" -ForegroundColor Green
    Write-Host "=========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Application URL: http://localhost:$Port" -ForegroundColor Cyan
    Write-Host "Swagger UI: http://localhost:$Port/" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Manual steps (if not running as Administrator):" -ForegroundColor Yellow
    Write-Host "1. Open IIS Manager (inetmgr)" -ForegroundColor White
    Write-Host "2. Create Application Pool: $AppPoolName (No Managed Code)" -ForegroundColor White
    Write-Host "3. Create Website: $SiteName -> $PublishPath" -ForegroundColor White
    Write-Host "4. Allow port $Port in Windows Firewall" -ForegroundColor White

} catch {
    Write-Host ""
    Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Deployment failed!" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Press any key to continue..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")