$ErrorActionPreference = "SilentlyContinue"

$logDir = Join-Path $PSScriptRoot "logs"
$servicePorts = @(8080, 8761, 8081, 8082, 8083, 8085, 8086, 8087)

Get-ChildItem -Path $logDir -Filter "*.pid" | ForEach-Object {
    $pidValue = Get-Content $_.FullName | Select-Object -First 1
    if ($pidValue) {
        taskkill /PID $pidValue /T /F | Out-Null
        Write-Host "Stopped process $pidValue"
    }
    Remove-Item -LiteralPath $_.FullName -Force
}

Get-NetTCPConnection -LocalPort $servicePorts -State Listen | ForEach-Object {
    Stop-Process -Id $_.OwningProcess -Force
    Write-Host "Stopped process $($_.OwningProcess) on port $($_.LocalPort)"
}
