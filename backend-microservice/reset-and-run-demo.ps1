param(
    [string]$RootPassword = "12345678",
    [int]$StartupTimeoutSeconds = 180,
    [switch]$UseLocalMySql
)

$ErrorActionPreference = "Stop"

$databases = @(
    "ecommerce_auth",
    "ecommerce_user",
    "ecommerce_catalog",
    "ecommerce_promotion",
    "ecommerce_cart",
    "ecommerce_order",
    "ecommerce_seller",
    "ecommerce_payout"
)
if ($databases.Count -ne 8 -or @($databases | Where-Object { $_ -notmatch '^ecommerce_[a-z]+$' }).Count -gt 0) {
    throw "Safety check failed: unexpected database target list."
}

$stopScript = Join-Path $PSScriptRoot "stop-all.ps1"
$resetScript = Join-Path $PSScriptRoot "reset-demo-databases.ps1"
$runScript = Join-Path $PSScriptRoot "run-all.ps1"
foreach ($requiredScript in @($stopScript, $resetScript, $runScript)) {
    if (-not (Test-Path -LiteralPath $requiredScript)) {
        throw "Required script not found: $requiredScript"
    }
}

$dbPort = if ($UseLocalMySql) { 3306 } else { 3307 }
Write-Host "Reset target: $($databases -join ', ')" -ForegroundColor Yellow
Write-Host "Database endpoint: localhost:$dbPort"
Write-Host "Stopping backend services before database reset..."
& $stopScript

Write-Host "Dropping, recreating and seeding project databases..." -ForegroundColor Yellow
if ($UseLocalMySql) {
    & $resetScript -RootPassword $RootPassword -DbHost "127.0.0.1" -DbPort $dbPort -Force
} else {
    & $resetScript -RootPassword $RootPassword -UseDocker -Force
}
if ($LASTEXITCODE -ne 0) {
    throw "Database reset failed with exit code $LASTEXITCODE"
}

Write-Host "Building and starting the complete backend stack..."
& $runScript -DbHost "localhost" -DbPort $dbPort -DbPassword $RootPassword -WithNotification
if ($LASTEXITCODE -ne 0) {
    throw "Backend startup failed with exit code $LASTEXITCODE"
}

$healthEndpoints = [ordered]@{
    discovery   = "http://127.0.0.1:8761/actuator/health"
    auth        = "http://127.0.0.1:8081/actuator/health"
    user        = "http://127.0.0.1:8082/actuator/health"
    catalog     = "http://127.0.0.1:8083/actuator/health"
    promotion   = "http://127.0.0.1:8085/actuator/health"
    order       = "http://127.0.0.1:8086/actuator/health"
    cart        = "http://127.0.0.1:8087/actuator/health"
    notification = "http://127.0.0.1:8088/actuator/health"
    seller      = "http://127.0.0.1:8089/actuator/health"
    payout      = "http://127.0.0.1:8091/actuator/health"
    gateway     = "http://127.0.0.1:8080/actuator/health"
}
$deadline = (Get-Date).AddSeconds($StartupTimeoutSeconds)
$pending = [System.Collections.Generic.HashSet[string]]::new([string[]]$healthEndpoints.Keys)
while ($pending.Count -gt 0 -and (Get-Date) -lt $deadline) {
    foreach ($name in @($pending)) {
        try {
            $health = Invoke-RestMethod -Uri $healthEndpoints[$name] -TimeoutSec 2
            if ($health.status -eq "UP") {
                [void]$pending.Remove($name)
                Write-Host "Healthy: $name"
            }
        } catch {}
    }
    if ($pending.Count -gt 0) {
        Start-Sleep -Seconds 2
    }
}
if ($pending.Count -gt 0) {
    throw "Services did not become healthy: $($pending -join ', ')"
}

$loginBody = @{ email = "admin@ecommerce.local"; password = "Admin@123" } | ConvertTo-Json
$smokeDeadline = (Get-Date).AddSeconds(60)
$lastSmokeError = $null
$disputes = @()
$disputeCount = 0
while ((Get-Date) -lt $smokeDeadline) {
    try {
        $login = Invoke-RestMethod -Method Post -Uri "http://127.0.0.1:8080/api/v1/auth/login-admin" -ContentType "application/json" -Body $loginBody -TimeoutSec 15
        $token = $login.data.accessToken
        if ([string]::IsNullOrWhiteSpace($token)) {
            throw "Admin login did not return an access token."
        }

        $headers = @{ Authorization = "Bearer $token" }
        $disputes = Invoke-RestMethod -Method Get -Uri "http://127.0.0.1:8080/api/v1/admin/disputes" -Headers $headers -TimeoutSec 15
        $disputeCount = if ($null -eq $disputes) { 0 } elseif ($disputes -is [System.Array]) { $disputes.Length } else { 1 }
        if ($disputeCount -lt 4) {
            throw "Expected at least 4 seeded disputes, found $disputeCount."
        }
        break
    } catch {
        $lastSmokeError = $_.Exception.Message
        Start-Sleep -Seconds 3
    }
}
if ($disputeCount -lt 4) {
    throw "Admin dispute smoke test failed: $lastSmokeError"
}

Write-Host ""
Write-Host "RESET COMPLETE" -ForegroundColor Green
Write-Host "All 8 ecommerce databases were recreated and the backend is healthy."
Write-Host "Seeded disputes: $disputeCount"
Write-Host "Frontend: http://localhost:6688"
Write-Host "Gateway:  http://localhost:8080"
Write-Host "Admin:    admin@ecommerce.local / Admin@123"
Write-Host "Buyer 1:  customer1@ecommerce.local / Admin@123"
Write-Host "Buyer 2:  customer2@ecommerce.local / Admin@123"
