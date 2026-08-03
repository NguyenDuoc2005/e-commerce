$ErrorActionPreference = "SilentlyContinue"

$logDir = Join-Path $PSScriptRoot "logs"

Get-ChildItem -Path $logDir -Filter "*.pid" | ForEach-Object {
    $pidValue = Get-Content $_.FullName | Select-Object -First 1
    if ($pidValue) {
        Stop-Process -Id ([int]$pidValue) -Force
        Write-Host "Stopped process $pidValue"
    }
    Remove-Item -LiteralPath $_.FullName -Force
}
