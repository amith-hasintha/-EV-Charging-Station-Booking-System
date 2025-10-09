# IIS Setup Verification Script
# Run this script to check if your system is ready for ASP.NET Core deployment

Write-Host "=============================================" -ForegroundColor Green
Write-Host "IIS Setup Verification for ASP.NET Core 8.0" -ForegroundColor Green
Write-Host "=============================================" -ForegroundColor Green
Write-Host ""

$checks = @()

# Check 1: Windows Version
Write-Host "[1] Checking Windows Version..." -ForegroundColor Cyan
$windowsVersion = [Environment]::OSVersion.Version
$checks += @{
    Check = "Windows Version"
    Status = if ($windowsVersion.Major -ge 10) { "✓ PASS" } else { "✗ FAIL" }
    Details = "Windows $($windowsVersion.Major).$($windowsVersion.Minor)"
    Required = "Windows 10+ or Windows Server 2016+"
}

# Check 2: IIS Installation
Write-Host "[2] Checking IIS Installation..." -ForegroundColor Cyan
$iisInstalled = Get-WindowsOptionalFeature -Online -FeatureName IIS-WebServerRole -ErrorAction SilentlyContinue
$checks += @{
    Check = "IIS Web Server"
    Status = if ($iisInstalled -and $iisInstalled.State -eq "Enabled") { "✓ PASS" } else { "✗ FAIL" }
    Details = if ($iisInstalled) { $iisInstalled.State } else { "Not Found" }
    Required = "IIS Web Server Role enabled"
}

# Check 3: .NET 8 Runtime
Write-Host "[3] Checking .NET 8 Runtime..." -ForegroundColor Cyan
try {
    $dotnetVersion = & dotnet --version 2>$null
    $dotnet8Installed = $dotnetVersion -like "8.*"
    $checks += @{
        Check = ".NET 8 SDK/Runtime"
        Status = if ($dotnet8Installed) { "✓ PASS" } else { "⚠ WARNING" }
        Details = if ($dotnetVersion) { "Version $dotnetVersion" } else { "Not Found" }
        Required = ".NET 8.0 or later"
    }
} catch {
    $checks += @{
        Check = ".NET 8 SDK/Runtime"
        Status = "✗ FAIL"
        Details = "Not installed or not in PATH"
        Required = ".NET 8.0 SDK or Runtime"
    }
}

# Check 4: ASP.NET Core Hosting Bundle (Registry Check)
Write-Host "[4] Checking ASP.NET Core Hosting Bundle..." -ForegroundColor Cyan
$hostingBundleKeys = @(
    "HKLM:\SOFTWARE\WOW6432Node\Microsoft\Updates\.NET Core*",
    "HKLM:\SOFTWARE\Microsoft\Updates\.NET Core*"
)

$hostingBundleInstalled = $false
foreach ($keyPath in $hostingBundleKeys) {
    try {
        $keys = Get-ChildItem -Path $keyPath -ErrorAction SilentlyContinue | Where-Object { $_.Name -like "*Hosting Bundle*" -and $_.Name -like "*8.0*" }
        if ($keys) {
            $hostingBundleInstalled = $true
            break
        }
    } catch { }
}

$checks += @{
    Check = "ASP.NET Core Hosting Bundle"
    Status = if ($hostingBundleInstalled) { "✓ PASS" } else { "✗ FAIL" }
    Details = if ($hostingBundleInstalled) { "Installed" } else { "Not Found" }
    Required = "ASP.NET Core 8.0 Hosting Bundle"
}

# Check 5: IIS Management Console
Write-Host "[5] Checking IIS Management Console..." -ForegroundColor Cyan
$iisManagerInstalled = Get-WindowsOptionalFeature -Online -FeatureName IIS-ManagementConsole -ErrorAction SilentlyContinue
$checks += @{
    Check = "IIS Management Console"
    Status = if ($iisManagerInstalled -and $iisManagerInstalled.State -eq "Enabled") { "✓ PASS" } else { "✗ FAIL" }
    Details = if ($iisManagerInstalled) { $iisManagerInstalled.State } else { "Not Found" }
    Required = "IIS Management Console enabled"
}

# Check 6: Administrator Rights
Write-Host "[6] Checking Administrator Rights..." -ForegroundColor Cyan
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole] "Administrator")
$checks += @{
    Check = "Administrator Rights"
    Status = if ($isAdmin) { "✓ PASS" } else { "⚠ WARNING" }
    Details = if ($isAdmin) { "Running as Administrator" } else { "Not running as Administrator" }
    Required = "Administrator rights for IIS configuration"
}

# Check 7: WebAdministration Module
Write-Host "[7] Checking WebAdministration PowerShell Module..." -ForegroundColor Cyan
$webAdminModule = Get-Module -ListAvailable -Name WebAdministration -ErrorAction SilentlyContinue
$checks += @{
    Check = "WebAdministration Module"
    Status = if ($webAdminModule) { "✓ PASS" } else { "✗ FAIL" }
    Details = if ($webAdminModule) { "Available" } else { "Not Available" }
    Required = "WebAdministration PowerShell Module"
}

# Display Results
Write-Host ""
Write-Host "=============================================" -ForegroundColor Green
Write-Host "VERIFICATION RESULTS" -ForegroundColor Green
Write-Host "=============================================" -ForegroundColor Green

foreach ($check in $checks) {
    $color = switch ($check.Status) {
        { $_ -like "*PASS*" } { "Green" }
        { $_ -like "*WARNING*" } { "Yellow" }
        { $_ -like "*FAIL*" } { "Red" }
        default { "White" }
    }
    
    Write-Host ("  {0,-30} {1}" -f $check.Check, $check.Status) -ForegroundColor $color
    Write-Host ("    Details: {0}" -f $check.Details) -ForegroundColor Gray
    if ($check.Status -like "*FAIL*" -or $check.Status -like "*WARNING*") {
        Write-Host ("    Required: {0}" -f $check.Required) -ForegroundColor Gray
    }
    Write-Host ""
}

# Summary and Recommendations
$passCount = ($checks | Where-Object { $_.Status -like "*PASS*" }).Count
$failCount = ($checks | Where-Object { $_.Status -like "*FAIL*" }).Count
$warnCount = ($checks | Where-Object { $_.Status -like "*WARNING*" }).Count

Write-Host "=============================================" -ForegroundColor Green
Write-Host "SUMMARY" -ForegroundColor Green
Write-Host "=============================================" -ForegroundColor Green
Write-Host ("  Passed:   {0}" -f $passCount) -ForegroundColor Green
Write-Host ("  Failed:   {0}" -f $failCount) -ForegroundColor Red
Write-Host ("  Warnings: {0}" -f $warnCount) -ForegroundColor Yellow
Write-Host ""

if ($failCount -eq 0) {
    Write-Host "✓ Your system is ready for IIS deployment!" -ForegroundColor Green
} else {
    Write-Host "⚠ Please address the failed checks before deploying to IIS." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Installation Links:" -ForegroundColor Cyan
    Write-Host "  - ASP.NET Core Hosting Bundle: https://dotnet.microsoft.com/download/dotnet/8.0" -ForegroundColor White
    Write-Host "  - Enable IIS: Control Panel → Programs → Turn Windows features on or off" -ForegroundColor White
}

Write-Host ""
Write-Host "Press any key to continue..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")