param(
    [string]$RootPassword = "12345678",
    [switch]$UseDocker,
    [string[]]$Databases = @(
        "ecommerce_auth",
        "ecommerce_user",
        "ecommerce_catalog",
        "ecommerce_promotion",
        "ecommerce_cart",
        "ecommerce_order",
        "ecommerce_inventory"
    )
)

$ErrorActionPreference = "Stop"

$sql = ($Databases | ForEach-Object {
    "CREATE DATABASE IF NOT EXISTS ``$_`` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
}) -join " "

if ($UseDocker) {
    $composeFile = Join-Path $PSScriptRoot "docker-compose.yml"
    docker compose -f $composeFile up -d mysql
    docker compose -f $composeFile exec -T mysql mysql -uroot "-p$RootPassword" -e $sql
} else {
    mysql -uroot "-p$RootPassword" -e $sql
}

Write-Host "Ensured databases: $($Databases -join ', ')"
