param(
    [string]$ConnectUrl = "http://localhost:8084",
    [string]$ElasticsearchUrl = "http://localhost:9200"
)

$ErrorActionPreference = "Stop"
$root = Resolve-Path (Join-Path $PSScriptRoot "..")

Write-Host "Creating products_v1 index and products alias if needed..."
$mapping = Get-Content -Raw -Path (Join-Path $root "elasticsearch\products-index-mapping.json")
try {
    Invoke-RestMethod -Method Put -Uri "$ElasticsearchUrl/products_v1" -ContentType "application/json" -Body $mapping | Out-Null
} catch {
    if ($_.Exception.Response.StatusCode.value__ -ne 400) {
        throw
    }
}

$aliasBody = '{"actions":[{"add":{"index":"products_v1","alias":"products"}}]}'
try {
    Invoke-RestMethod -Method Post -Uri "$ElasticsearchUrl/_aliases" -ContentType "application/json" -Body $aliasBody | Out-Null
} catch {
    if ($_.Exception.Response.StatusCode.value__ -ne 400) {
        throw
    }
}

Write-Host "Deploying Debezium outbox source connector..."
$debezium = Get-Content -Raw -Path (Join-Path $root "connectors\debezium-outbox-products.json")
Invoke-RestMethod -Method Put -Uri "$ConnectUrl/connectors/catalog-products-outbox-source/config" -ContentType "application/json" -Body (($debezium | ConvertFrom-Json).config | ConvertTo-Json -Depth 20) | Out-Null

Write-Host "Deploying Elasticsearch sink connector..."
$sink = Get-Content -Raw -Path (Join-Path $root "connectors\elasticsearch-products-sink.json")
Invoke-RestMethod -Method Put -Uri "$ConnectUrl/connectors/products-elasticsearch-sink/config" -ContentType "application/json" -Body (($sink | ConvertFrom-Json).config | ConvertTo-Json -Depth 20) | Out-Null

Write-Host "Connectors:"
Invoke-RestMethod -Method Get -Uri "$ConnectUrl/connectors"
