param(
    [string]$DbHost = "localhost",
    [int]$DbPort = 3306,
    [string]$DbUser = "root",
    [string]$DbPassword = "12345678",
    [switch]$WithNotification
)

$ErrorActionPreference = "Stop"

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$projectDir = Join-Path $repoRoot "backend-microservice"
$gradle = Join-Path $projectDir "gradlew.bat"
$logDir = Join-Path $projectDir "logs"

New-Item -ItemType Directory -Force -Path $logDir | Out-Null

Write-Host "Building common-lib before starting services..."
& $gradle -p $projectDir ":common-lib:jar" "--no-daemon" "--max-workers=1"

function Start-ServiceProcess {
    param(
        [string]$Name,
        [string]$Task,
        [hashtable]$Env = @{}
    )

    $envCommands = @(
        'set "EUREKA_DEFAULT_ZONE=http://localhost:8761/eureka"'
    )

    foreach ($entry in $Env.GetEnumerator()) {
        $escapedValue = [string]$entry.Value -replace '"', '\"'
        $envCommands += "set `"$($entry.Key)=$escapedValue`""
    }

    $outLogFile = Join-Path $logDir "$Name.out.log"
    $errLogFile = Join-Path $logDir "$Name.err.log"
    $pidFile = Join-Path $logDir "$Name.pid"
    $runnerFile = Join-Path $logDir "$Name.run.ps1"
    $cmdFile = Join-Path $logDir "$Name.run.cmd"
    $command = @"
@echo off
cd /d "$repoRoot"
$($envCommands -join "`r`n")
call "$gradle" -p "$projectDir" $Task > "$outLogFile" 2>&1
"@
    Set-Content -Path $cmdFile -Value $command -Encoding ASCII

    $process = Start-Process cmd.exe -WindowStyle Hidden -PassThru -ArgumentList @(
        "/c",
        "`"$cmdFile`""
    )

    Set-Content -Path $errLogFile -Value "" -Encoding UTF8

    Set-Content -Path $pidFile -Value $process.Id
    Write-Host "Started $Name, pid: $($process.Id), logs: $outLogFile / $errLogFile"
}

$jdbcOptions = "createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true"
$authJdbc = "jdbc:mysql://$DbHost`:$DbPort/ecommerce_auth`?$jdbcOptions"
$userJdbc = "jdbc:mysql://$DbHost`:$DbPort/ecommerce_user`?$jdbcOptions"
$catalogJdbc = "jdbc:mysql://$DbHost`:$DbPort/ecommerce_catalog`?$jdbcOptions"
$promotionJdbc = "jdbc:mysql://$DbHost`:$DbPort/ecommerce_promotion`?$jdbcOptions"
$cartJdbc = "jdbc:mysql://$DbHost`:$DbPort/ecommerce_cart`?$jdbcOptions"
$orderJdbc = "jdbc:mysql://$DbHost`:$DbPort/ecommerce_order`?$jdbcOptions"
$sellerJdbc = "jdbc:mysql://$DbHost`:$DbPort/ecommerce_seller`?$jdbcOptions"
$payoutJdbc = "jdbc:mysql://$DbHost`:$DbPort/ecommerce_payout`?$jdbcOptions"
$dbEnv = @{
    AUTH_DATASOURCE_URL = $authJdbc
    AUTH_DATASOURCE_USERNAME = $DbUser
    AUTH_DATASOURCE_PASSWORD = $DbPassword
    USER_DATASOURCE_URL = $userJdbc
    USER_DATASOURCE_USERNAME = $DbUser
    USER_DATASOURCE_PASSWORD = $DbPassword
    CATALOG_DATASOURCE_URL = $catalogJdbc
    CATALOG_DATASOURCE_USERNAME = $DbUser
    CATALOG_DATASOURCE_PASSWORD = $DbPassword
    PROMOTION_DATASOURCE_URL = $promotionJdbc
    PROMOTION_DATASOURCE_USERNAME = $DbUser
    PROMOTION_DATASOURCE_PASSWORD = $DbPassword
    CART_DATASOURCE_URL = $cartJdbc
    CART_DATASOURCE_USERNAME = $DbUser
    CART_DATASOURCE_PASSWORD = $DbPassword
    ORDER_DATASOURCE_URL = $orderJdbc
    ORDER_DATASOURCE_USERNAME = $DbUser
    ORDER_DATASOURCE_PASSWORD = $DbPassword
    SELLER_DATASOURCE_URL = $sellerJdbc
    SELLER_DATASOURCE_USERNAME = $DbUser
    SELLER_DATASOURCE_PASSWORD = $DbPassword
    PAYOUT_DATASOURCE_URL = $payoutJdbc
    PAYOUT_DATASOURCE_USERNAME = $DbUser
    PAYOUT_DATASOURCE_PASSWORD = $DbPassword
}

Start-ServiceProcess "discovery-server" ":discovery-server:bootRun"
Start-Sleep -Seconds 12

Start-ServiceProcess "auth-service" ":auth-service:bootRun" $dbEnv
Start-ServiceProcess "user-service" ":user-service:bootRun" $dbEnv
Start-ServiceProcess "catalog-service" ":catalog-service:bootRun" $dbEnv
Start-ServiceProcess "promotion-service" ":promotion-service:bootRun" $dbEnv
Start-ServiceProcess "cart-service" ":cart-service:bootRun" $dbEnv
Start-ServiceProcess "order-service" ":order-service:bootRun" $dbEnv
Start-ServiceProcess "seller-service" ":seller-service:bootRun" $dbEnv
Start-ServiceProcess "payout-service" ":payout-service:bootRun" $dbEnv

if ($WithNotification) {
    Start-ServiceProcess "notification-service" ":notification-service:bootRun" @{
        KAFKA_BOOTSTRAP_SERVERS = "localhost:9092"
    }
}

Start-Sleep -Seconds 18
Start-ServiceProcess "api-gateway" ":api-gateway:bootRun"

Write-Host ""
Write-Host "Backend microservice startup requested."
Write-Host "Gateway: http://localhost:8080"
Write-Host "Eureka:  http://localhost:8761"
Write-Host "Logs:    $logDir"
Write-Host "Stop command: powershell -ExecutionPolicy Bypass -File backend-microservice\stop-all.ps1"
