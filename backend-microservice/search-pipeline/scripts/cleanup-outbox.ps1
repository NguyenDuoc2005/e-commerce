param(
    [string]$MysqlUser = "root",
    [string]$MysqlPassword = "12345678",
    [string]$Database = "ecommerce_catalog",
    [int]$RetentionDays = 7
)

$ErrorActionPreference = "Stop"
$compose = Resolve-Path (Join-Path $PSScriptRoot "..\..\docker-compose.yml")
$query = "DELETE FROM outbox WHERE created_at < (CURRENT_TIMESTAMP(6) - INTERVAL $RetentionDays DAY);"
docker compose -f $compose exec -T mysql mysql -u$MysqlUser -p$MysqlPassword $Database -e $query
