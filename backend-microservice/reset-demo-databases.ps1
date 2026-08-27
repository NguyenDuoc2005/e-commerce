param(
    [string]$RootPassword = "12345678",
    [string]$DbHost = "localhost",
    [int]$DbPort = 3306,
    [switch]$UseDocker,
    [switch]$Force
)

$ErrorActionPreference = "Stop"

# Windows PowerShell 5.1 otherwise encodes text piped to native commands as
# ASCII, replacing Vietnamese characters with '?'. Keep SQL imports UTF-8.
$OutputEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new($false)

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

$catalogSchemaFile = Join-Path $PSScriptRoot "catalog-service\src\main\resources\db\migration\manual\p1_product_domain_reset.sql"
$catalogSeedFile = Join-Path $PSScriptRoot "catalog-service\src\main\resources\db\migration\manual\p1_product_domain_seed.sql"
foreach ($requiredFile in @($catalogSchemaFile, $catalogSeedFile)) {
    if (-not (Test-Path -LiteralPath $requiredFile)) {
        throw "Required catalog SQL file not found: $requiredFile"
    }
}

function Assert-NativeCommandSucceeded {
    param([Parameter(Mandatory = $true)][string]$Action)

    if ($LASTEXITCODE -ne 0) {
        throw "$Action failed with exit code $LASTEXITCODE."
    }
}

$resetCatalogSql = "DROP DATABASE IF EXISTS ``ecommerce_catalog``; CREATE DATABASE ``ecommerce_catalog`` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

if ($UseDocker) {
    $composeFile = Join-Path $PSScriptRoot "docker-compose.yml"
    docker compose -f $composeFile up -d mysql
    Assert-NativeCommandSucceeded "Starting Docker MySQL"
    docker compose -f $composeFile exec -T mysql mysql -uroot "-p$RootPassword" -e $dropCreateSql
    Assert-NativeCommandSucceeded "Recreating demo databases"
    Get-Content -Raw -Encoding UTF8 -Path $seedFile | docker compose -f $composeFile exec -T mysql mysql --default-character-set=utf8mb4 -uroot "-p$RootPassword"
    Assert-NativeCommandSucceeded "Loading the shared demo seed"

    # The shared seed still contains the historical catalog layout. Replace only this
    # freshly-created database with the canonical P1 schema that catalog-service validates.
    docker compose -f $composeFile exec -T mysql mysql -uroot "-p$RootPassword" -e $resetCatalogSql
    Assert-NativeCommandSucceeded "Recreating the catalog database"
    Get-Content -Raw -Encoding UTF8 -Path $catalogSchemaFile | docker compose -f $composeFile exec -T mysql mysql --default-character-set=utf8mb4 -uroot "-p$RootPassword" ecommerce_catalog
    Assert-NativeCommandSucceeded "Loading the canonical catalog schema"
    Get-Content -Raw -Encoding UTF8 -Path $catalogSeedFile | docker compose -f $composeFile exec -T mysql mysql --default-character-set=utf8mb4 -uroot "-p$RootPassword" ecommerce_catalog
    Assert-NativeCommandSucceeded "Loading the canonical catalog seed"
} else {
    mysql -h $DbHost -P $DbPort -uroot "-p$RootPassword" -e $dropCreateSql
    Assert-NativeCommandSucceeded "Recreating demo databases"
    Get-Content -Raw -Encoding UTF8 -Path $seedFile | mysql --default-character-set=utf8mb4 -h $DbHost -P $DbPort -uroot "-p$RootPassword"
    Assert-NativeCommandSucceeded "Loading the shared demo seed"

    mysql -h $DbHost -P $DbPort -uroot "-p$RootPassword" -e $resetCatalogSql
    Assert-NativeCommandSucceeded "Recreating the catalog database"
    Get-Content -Raw -Encoding UTF8 -Path $catalogSchemaFile | mysql --default-character-set=utf8mb4 -h $DbHost -P $DbPort -uroot "-p$RootPassword" ecommerce_catalog
    Assert-NativeCommandSucceeded "Loading the canonical catalog schema"
    Get-Content -Raw -Encoding UTF8 -Path $catalogSeedFile | mysql --default-character-set=utf8mb4 -h $DbHost -P $DbPort -uroot "-p$RootPassword" ecommerce_catalog
    Assert-NativeCommandSucceeded "Loading the canonical catalog seed"
}

Write-Host "Reset and seeded demo databases: $($databases -join ', ')"
Write-Host "Demo accounts:"
Write-Host "  Admin:    admin@ecommerce.local / Admin@123"
Write-Host "  Staff:    staff@ecommerce.local / Admin@123"
Write-Host "  Seller 1 (approved): customer1@ecommerce.local / Admin@123"
Write-Host "  Seller 2 (approved): customer2@ecommerce.local / Admin@123"
Write-Host "  Seller 3 (pending):  customer3@ecommerce.local / Admin@123"
