$ErrorActionPreference = "Stop"

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$logDir = Join-Path $PSScriptRoot "logs"
$servicePorts = @(8080, 8761, 8081, 8082, 8083, 8085, 8086, 8087, 8088, 8089, 8091)
$serviceModules = @(
    "discovery-server", "auth-service", "user-service", "catalog-service",
    "promotion-service", "cart-service", "order-service", "seller-service",
    "payout-service", "notification-service", "api-gateway"
)

function Test-ProjectServiceProcess {
    param([CimInstance]$Process)

    if (-not $Process -or [string]::IsNullOrWhiteSpace($Process.CommandLine)) {
        return $false
    }
    $commandLine = $Process.CommandLine
    if ($commandLine.IndexOf($repoRoot, [StringComparison]::OrdinalIgnoreCase) -lt 0) {
        return $false
    }
    return $serviceModules | Where-Object { $commandLine -match [regex]::Escape($_) } | Select-Object -First 1
}

$stopped = [System.Collections.Generic.HashSet[int]]::new()
$projectProcesses = Get-CimInstance Win32_Process | Where-Object { Test-ProjectServiceProcess $_ }
foreach ($process in $projectProcesses) {
    Stop-Process -Id $process.ProcessId -Force -ErrorAction SilentlyContinue
    [void]$stopped.Add([int]$process.ProcessId)
    Write-Host "Stopped project service process $($process.ProcessId)"
}

foreach ($listener in @(Get-NetTCPConnection -LocalPort $servicePorts -State Listen -ErrorAction SilentlyContinue)) {
    if ($stopped.Contains([int]$listener.OwningProcess)) {
        continue
    }
    $process = Get-CimInstance Win32_Process -Filter "ProcessId = $($listener.OwningProcess)" -ErrorAction SilentlyContinue
    if (Test-ProjectServiceProcess $process) {
        Stop-Process -Id $listener.OwningProcess -Force -ErrorAction SilentlyContinue
        [void]$stopped.Add([int]$listener.OwningProcess)
        Write-Host "Stopped project service process $($listener.OwningProcess) on port $($listener.LocalPort)"
    }
}

Get-ChildItem -LiteralPath $logDir -Filter "*.pid" -File -ErrorAction SilentlyContinue | ForEach-Object {
    Remove-Item -LiteralPath $_.FullName -Force
}

Write-Host "Stopped $($stopped.Count) backend project process(es)."
