param(
    [string]$DbHost = "localhost",
    [int]$DbPort = 3306,
    [string]$DbUser = "root",
    [string]$DbPassword = "12345678",
    [int]$MaxHeapMb = 320,
    [int]$MaxMetaspaceMb = 192,
    [switch]$WithNotification
)

$ErrorActionPreference = "Stop"

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$projectDir = Join-Path $repoRoot "backend-microservice"
$gradle = Join-Path $projectDir "gradlew.bat"
$logDir = Join-Path $projectDir "logs"

function Get-JavaMajorVersion {
    param([string]$JavaExecutable)
    if (-not (Test-Path -LiteralPath $JavaExecutable)) {
        return 0
    }
    $previousErrorActionPreference = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    try {
        $versionOutput = (& $JavaExecutable -version 2>&1 | Out-String)
    } finally {
        $ErrorActionPreference = $previousErrorActionPreference
    }
    if ($versionOutput -match 'version\s+"(?<major>\d+)(?:\.(?<minor>\d+))?') {
        $major = [int]$Matches.major
        return $(if ($major -eq 1) { [int]$Matches.minor } else { $major })
    }
    return 0
}

$javaCandidates = [System.Collections.Generic.List[string]]::new()
if ($env:JAVA_HOME) {
    $javaCandidates.Add((Join-Path $env:JAVA_HOME "bin\java.exe"))
}
$jdkRoot = Join-Path $env:USERPROFILE ".jdks"
if (Test-Path -LiteralPath $jdkRoot) {
    Get-ChildItem -LiteralPath $jdkRoot -Directory | Sort-Object Name -Descending | ForEach-Object {
        $javaCandidates.Add((Join-Path $_.FullName "bin\java.exe"))
    }
}
$pathJava = Get-Command java.exe -ErrorAction SilentlyContinue
if ($pathJava) {
    $javaCandidates.Add($pathJava.Source)
}
$java = $javaCandidates | Where-Object { (Get-JavaMajorVersion $_) -ge 17 } | Select-Object -First 1
if (-not $java) {
    throw "Java 17 or newer is required. Install a JDK or set JAVA_HOME to a compatible version."
}
$env:JAVA_HOME = Split-Path (Split-Path $java -Parent) -Parent
Write-Host "Using Java $(Get-JavaMajorVersion $java): $java"

New-Item -ItemType Directory -Force -Path $logDir | Out-Null

$modules = @(
    "discovery-server",
    "auth-service",
    "user-service",
    "catalog-service",
    "promotion-service",
    "cart-service",
    "order-service",
    "seller-service",
    "payout-service",
    "api-gateway"
)
if ($WithNotification) {
    $modules += "notification-service"
}

$bootJarTasks = $modules | ForEach-Object { ":${_}:bootJar" }
Write-Host "Building service boot jars sequentially..."
& $gradle -p $projectDir @bootJarTasks "--no-daemon" "--max-workers=1"
if ($LASTEXITCODE -ne 0) {
    throw "Backend bootJar build failed with exit code $LASTEXITCODE"
}

function Start-ServiceProcess {
    param(
        [string]$Name,
        [string]$Module,
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
    $cmdFile = Join-Path $logDir "$Name.run.cmd"
    $jar = Get-ChildItem -Path (Join-Path $projectDir "$Module\build\libs") -Filter "$Module-*.jar" -File |
        Where-Object { $_.Name -notlike "*-plain.jar" } |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1
    if (-not $jar) {
        throw "Cannot find executable jar for $Module"
    }
    $command = @"
@echo off
cd /d "$repoRoot"
$($envCommands -join "`r`n")
"$java" -Xms64m -Xmx$($MaxHeapMb)m -XX:MaxMetaspaceSize=$($MaxMetaspaceMb)m -jar "$($jar.FullName)" > "$outLogFile" 2>&1
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

Start-ServiceProcess "discovery-server" "discovery-server"
Start-Sleep -Seconds 12

Start-ServiceProcess "auth-service" "auth-service" $dbEnv
Start-ServiceProcess "user-service" "user-service" $dbEnv
Start-ServiceProcess "catalog-service" "catalog-service" $dbEnv
Start-ServiceProcess "promotion-service" "promotion-service" $dbEnv
Start-ServiceProcess "cart-service" "cart-service" $dbEnv
Start-ServiceProcess "order-service" "order-service" $dbEnv
Start-ServiceProcess "seller-service" "seller-service" $dbEnv
Start-ServiceProcess "payout-service" "payout-service" $dbEnv

if ($WithNotification) {
    Start-ServiceProcess "notification-service" "notification-service" @{
        KAFKA_BOOTSTRAP_SERVERS = "localhost:9092"
    }
}

Start-Sleep -Seconds 18
Start-ServiceProcess "api-gateway" "api-gateway"

Write-Host ""
Write-Host "Backend microservice startup requested."
Write-Host "Gateway: http://localhost:8080"
Write-Host "Eureka:  http://localhost:8761"
Write-Host "Logs:    $logDir"
Write-Host "Stop command: powershell -ExecutionPolicy Bypass -File backend-microservice\stop-all.ps1"
