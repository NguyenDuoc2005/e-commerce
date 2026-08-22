param(
    [string]$RootPassword = "12345678",
    [string]$DbHost = "localhost",
    [int]$DbPort = 3306,
    [switch]$UseDocker,
    [switch]$Force
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

if (-not $Force) {
    Write-Host "This will DROP and recreate: $($databases -join ', ')" -ForegroundColor Yellow
    $answer = Read-Host "Type RESET to continue"
    if ($answer -ne "RESET") {
        Write-Host "Cancelled."
        exit 0
    }
}

$dropCreateSql = ($databases | ForEach-Object {
    "DROP DATABASE IF EXISTS ``$_``; CREATE DATABASE ``$_`` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
}) -join " "

$seedFile = Join-Path $PSScriptRoot "seed-demo-data.sql"
if (-not (Test-Path $seedFile)) {
    throw "Seed file not found: $seedFile"
}

if ($UseDocker) {
    $composeFile = Join-Path $PSScriptRoot "docker-compose.yml"
    docker compose -f $composeFile up -d mysql
    docker compose -f $composeFile exec -T mysql mysql -uroot "-p$RootPassword" -e $dropCreateSql
    Get-Content -Raw -Path $seedFile | docker compose -f $composeFile exec -T mysql mysql -uroot "-p$RootPassword"
} else {
    mysql -h $DbHost -P $DbPort -uroot "-p$RootPassword" -e $dropCreateSql
    Get-Content -Raw -Path $seedFile | mysql -h $DbHost -P $DbPort -uroot "-p$RootPassword"
}

Write-Host "Reset and seeded demo databases: $($databases -join ', ')"
Write-Host "Demo accounts:"
Write-Host "  Admin:    admin@ecommerce.local / Admin@123"
Write-Host "  Staff:    staff@ecommerce.local / Admin@123"
Write-Host "  Seller 1 (approved): customer1@ecommerce.local / Admin@123"
Write-Host "  Seller 2 (approved): customer2@ecommerce.local / Admin@123"
Write-Host "  Seller 3 (pending):  customer3@ecommerce.local / Admin@123"
