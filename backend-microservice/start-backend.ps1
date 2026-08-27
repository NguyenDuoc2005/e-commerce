param(
    [string]$RootPassword = "12345678",
    [int]$DockerStartupTimeoutSeconds = 180,
    [int]$ServiceStartupTimeoutSeconds = 240
)

$ErrorActionPreference = "Stop"

$composeFile = Join-Path $PSScriptRoot "docker-compose.yml"
$stopScript = Join-Path $PSScriptRoot "stop-all.ps1"
$runScript = Join-Path $PSScriptRoot "run-all.ps1"
$initDatabasesScript = Join-Path $PSScriptRoot "init-databases.ps1"
$logDir = Join-Path $PSScriptRoot "logs"

foreach ($requiredPath in @($composeFile, $stopScript, $runScript, $initDatabasesScript)) {
    if (-not (Test-Path -LiteralPath $requiredPath)) {
        throw "Required project file not found: $requiredPath"
    }
}

function Assert-NativeCommandSucceeded {
    param([Parameter(Mandatory = $true)][string]$Action)

    if ($LASTEXITCODE -ne 0) {
        throw "$Action failed with exit code $LASTEXITCODE."
    }
}

function Test-DockerReady {
    $previousErrorActionPreference = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    try {
        docker info *> $null
        return ($LASTEXITCODE -eq 0)
    } finally {
        $ErrorActionPreference = $previousErrorActionPreference
    }
}

function Wait-TcpPort {
    param(
        [Parameter(Mandatory = $true)][int]$Port,
        [Parameter(Mandatory = $true)][datetime]$Deadline
    )

    do {
        if (Test-NetConnection 127.0.0.1 -Port $Port -InformationLevel Quiet -WarningAction SilentlyContinue) {
            return
        }
        Start-Sleep -Seconds 2
    } while ((Get-Date) -lt $Deadline)

    throw "Infrastructure port $Port did not become ready before the timeout."
}

if (-not (Get-Command docker.exe -ErrorAction SilentlyContinue)) {
    throw "Docker CLI was not found. Install Docker Desktop and reopen PowerShell."
}

if (-not (Test-DockerReady)) {
    $dockerDesktop = "C:\Program Files\Docker\Docker\Docker Desktop.exe"
    if (-not (Test-Path -LiteralPath $dockerDesktop)) {
        throw "Docker Desktop is not running and was not found at: $dockerDesktop"
    }

    Write-Host "Starting Docker Desktop..."
    Start-Process -FilePath $dockerDesktop -WindowStyle Hidden
    $dockerDeadline = (Get-Date).AddSeconds($DockerStartupTimeoutSeconds)
    do {
        Start-Sleep -Seconds 5
        if (Test-DockerReady) {
            break
        }
    } while ((Get-Date) -lt $dockerDeadline)

    if (-not (Test-DockerReady)) {
        throw "Docker Desktop did not become ready within $DockerStartupTimeoutSeconds seconds."
    }
}

Write-Host "Starting backend infrastructure (MySQL, Kafka, Elasticsearch)..."
docker compose -f $composeFile up -d mysql kafka elasticsearch
Assert-NativeCommandSucceeded "Starting backend infrastructure"

$infrastructureDeadline = (Get-Date).AddSeconds($DockerStartupTimeoutSeconds)
$mysqlReady = $false
do {
    $previousErrorActionPreference = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    try {
        docker compose -f $composeFile exec -T mysql mysqladmin ping -uroot "-p$RootPassword" --silent *> $null
        $mysqlReady = ($LASTEXITCODE -eq 0)
    } finally {
        $ErrorActionPreference = $previousErrorActionPreference
    }
    if ($mysqlReady) {
        break
    }
    Start-Sleep -Seconds 2
} while ((Get-Date) -lt $infrastructureDeadline)

if (-not $mysqlReady) {
    throw "MySQL did not become ready within $DockerStartupTimeoutSeconds seconds."
}
Wait-TcpPort -Port 9092 -Deadline $infrastructureDeadline
Wait-TcpPort -Port 9200 -Deadline $infrastructureDeadline

# Non-destructive: only creates project databases that do not exist yet.
& $initDatabasesScript -RootPassword $RootPassword -UseDocker
if (-not $?) {
    throw "Database initialization failed."
}

$databaseCountQuery = "SELECT COUNT(*) FROM information_schema.SCHEMATA WHERE SCHEMA_NAME IN ('ecommerce_auth','ecommerce_user','ecommerce_catalog','ecommerce_promotion','ecommerce_cart','ecommerce_order','ecommerce_seller','ecommerce_payout');"
$databaseCount = docker compose -f $composeFile exec -T -e "MYSQL_PWD=$RootPassword" mysql mysql -uroot -N -e $databaseCountQuery
Assert-NativeCommandSucceeded "Verifying project databases"
if ([int]$databaseCount -ne 8) {
    throw "Expected 8 project databases but found $databaseCount."
}

# Keep the existing demo data while bringing the promotion schema up to the
# Flash Sale contract. This is idempotent and runs before the JVMs compete for
# resources during parallel startup.
$campaignTypeColumnQuery = "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='ecommerce_promotion' AND TABLE_NAME='promotion_campaign' AND COLUMN_NAME='campaign_type';"
$campaignTypeColumnCount = docker compose -f $composeFile exec -T -e "MYSQL_PWD=$RootPassword" mysql mysql -uroot -N -e $campaignTypeColumnQuery
Assert-NativeCommandSucceeded "Checking the Flash Sale schema"
if ([int]$campaignTypeColumnCount -eq 0) {
    Write-Host "Applying the non-destructive Flash Sale schema update..." -ForegroundColor Yellow
    docker compose -f $composeFile exec -T -e "MYSQL_PWD=$RootPassword" mysql mysql -uroot -e "ALTER TABLE ecommerce_promotion.promotion_campaign ADD COLUMN campaign_type VARCHAR(32) NOT NULL DEFAULT 'STANDARD' AFTER seller_id;"
    Assert-NativeCommandSucceeded "Adding promotion_campaign.campaign_type"
}

Write-Host "Stopping previous backend processes..."
& $stopScript

Write-Host "Building and starting all backend services..."
& $runScript -DbHost "localhost" -DbPort 3307 -DbPassword $RootPassword -WithNotification

$healthEndpoints = [ordered]@{
    discovery    = "http://127.0.0.1:8761/actuator/health"
    auth         = "http://127.0.0.1:8081/actuator/health"
    user         = "http://127.0.0.1:8082/actuator/health"
    catalog      = "http://127.0.0.1:8083/actuator/health"
    promotion    = "http://127.0.0.1:8085/actuator/health"
    order        = "http://127.0.0.1:8086/actuator/health"
    cart         = "http://127.0.0.1:8087/actuator/health"
    notification = "http://127.0.0.1:8088/actuator/health"
    seller       = "http://127.0.0.1:8089/actuator/health"
    payout       = "http://127.0.0.1:8091/actuator/health"
    gateway      = "http://127.0.0.1:8080/actuator/health"
}

$serviceDeadline = (Get-Date).AddSeconds($ServiceStartupTimeoutSeconds)
$pending = [System.Collections.Generic.HashSet[string]]::new([string[]]$healthEndpoints.Keys)
while ($pending.Count -gt 0 -and (Get-Date) -lt $serviceDeadline) {
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
    Write-Host ""
    Write-Host "Services that failed health checks: $($pending -join ', ')" -ForegroundColor Red
    foreach ($name in @($pending)) {
        $logFile = Join-Path $logDir "$name-service.out.log"
        if ($name -eq "discovery") { $logFile = Join-Path $logDir "discovery-server.out.log" }
        if ($name -eq "gateway") { $logFile = Join-Path $logDir "api-gateway.out.log" }
        if (Test-Path -LiteralPath $logFile) {
            Write-Host "--- Last lines from $logFile ---" -ForegroundColor Yellow
            Get-Content -LiteralPath $logFile -Tail 40
        }
    }
    throw "Backend startup failed health verification."
}

$expectedRegistrations = @(
    "API-GATEWAY",
    "AUTH-SERVICE",
    "USER-SERVICE",
    "CATALOG-SERVICE",
    "PROMOTION-SERVICE",
    "ORDER-SERVICE",
    "CART-SERVICE",
    "NOTIFICATION-SERVICE",
    "SELLER-SERVICE",
    "PAYOUT-SERVICE"
)
$registrationDeadline = (Get-Date).AddSeconds(90)
$missingRegistrations = $expectedRegistrations
do {
    try {
        $eurekaResponse = Invoke-RestMethod -Uri "http://127.0.0.1:8761/eureka/apps" -Headers @{ Accept = "application/json" } -TimeoutSec 5
        $registeredNames = @($eurekaResponse.applications.application | Where-Object {
            @($_.instance | Where-Object { $_.status -eq "UP" }).Count -gt 0
        } | ForEach-Object { $_.name })
        $missingRegistrations = @($expectedRegistrations | Where-Object { $_ -notin $registeredNames })
    } catch {
        $missingRegistrations = $expectedRegistrations
    }
    if ($missingRegistrations.Count -gt 0) {
        Start-Sleep -Seconds 2
    }
} while ($missingRegistrations.Count -gt 0 -and (Get-Date) -lt $registrationDeadline)

if ($missingRegistrations.Count -gt 0) {
    throw "Services missing from Eureka: $($missingRegistrations -join ', ')"
}

# Eureka clients refresh their local registry every 30 seconds by default. Waiting
# here prevents the first request through the gateway from racing that refresh.
Write-Host "Eureka registry is complete. Waiting for service-discovery caches..."
Start-Sleep -Seconds 35

Write-Host ""
Write-Host "BACKEND READY: all 11 services are healthy." -ForegroundColor Green
Write-Host "Gateway: http://localhost:8080"
Write-Host "Eureka:  http://localhost:8761"
Write-Host "Logs:    $logDir"
Write-Host "Stop:    powershell -ExecutionPolicy Bypass -File backend-microservice\stop-all.ps1"
