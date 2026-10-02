param(
    [string]$ConnectUrl = "http://localhost:8084",
    [string]$ElasticsearchUrl = "http://localhost:9200",
    [string]$TargetIndex = "products_v3",
    [switch]$MigrateLegacyProductsIndex
)

$ErrorActionPreference = "Stop"
$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$targetIndex = $TargetIndex
$searchAlias = "products"
$mappingFile = if ($targetIndex -eq "products_v3") { "products-v3-index-mapping.json" } else { "products-index-mapping.json" }

function Test-ElasticsearchPath {
    param([string]$Path)
    try {
        Invoke-WebRequest -Method Head -Uri "$ElasticsearchUrl/$Path" -UseBasicParsing | Out-Null
        return $true
    } catch {
        if ($_.Exception.Response.StatusCode.value__ -eq 404) {
            return $false
        }
        throw
    }
}

try {
    Invoke-RestMethod -Method Get -Uri "$ConnectUrl/connectors" | Out-Null
} catch {
    throw "Kafka Connect is not ready at $ConnectUrl. Start kafka and kafka-connect before deploying connectors."
}

$debezium = Get-Content -Raw -Path (Join-Path $root "connectors\debezium-outbox-products.json")
$debeziumDefinition = $debezium | ConvertFrom-Json
$debeziumConfig = $debeziumDefinition.config
$debeziumValidationConfig = $debeziumConfig | ConvertTo-Json -Depth 20 | ConvertFrom-Json
$debeziumValidationConfig | Add-Member -MemberType NoteProperty -Name "name" -Value $debeziumDefinition.name -Force
$debeziumValidation = Invoke-RestMethod `
    -Method Put `
    -Uri "$ConnectUrl/connector-plugins/io.debezium.connector.mysql.MySqlConnector/config/validate" `
    -ContentType "application/json" `
    -Body ($debeziumValidationConfig | ConvertTo-Json -Depth 20)
if ($debeziumValidation.error_count -gt 0) {
    $errors = $debeziumValidation.configs.value.errors | Where-Object { $_ } | Select-Object -Unique
    throw "Debezium connector configuration is invalid: $($errors -join '; ')"
}

$sink = Get-Content -Raw -Path (Join-Path $root "connectors\elasticsearch-products-sink.json")
$sinkDefinition = $sink | ConvertFrom-Json
$sinkConfig = $sinkDefinition.config
$sinkValidationConfig = $sinkConfig | ConvertTo-Json -Depth 20 | ConvertFrom-Json
$sinkValidationConfig | Add-Member -MemberType NoteProperty -Name "name" -Value $sinkDefinition.name -Force
$sinkValidation = Invoke-RestMethod `
    -Method Put `
    -Uri "$ConnectUrl/connector-plugins/io.confluent.connect.elasticsearch.ElasticsearchSinkConnector/config/validate" `
    -ContentType "application/json" `
    -Body ($sinkValidationConfig | ConvertTo-Json -Depth 20)
if ($sinkValidation.error_count -gt 0) {
    $errors = $sinkValidation.configs.value.errors | Where-Object { $_ } | Select-Object -Unique
    throw "Elasticsearch sink connector configuration is invalid: $($errors -join '; ')"
}

Write-Host "Ensuring $targetIndex index and $searchAlias alias..."
$mapping = Get-Content -Raw -Path (Join-Path $root "elasticsearch\$mappingFile")
if (-not (Test-ElasticsearchPath $targetIndex)) {
    Invoke-RestMethod -Method Put -Uri "$ElasticsearchUrl/$targetIndex" -ContentType "application/json" -Body $mapping | Out-Null
} else {
    # Adding fields is a backward-compatible mapping update. Existing documents
    # receive the new field when catalog re-enqueues ProductUpdated events.
    $mappingDefinition = $mapping | ConvertFrom-Json
    $mappingUpdate = $mappingDefinition.mappings | ConvertTo-Json -Depth 30
    Invoke-RestMethod -Method Put -Uri "$ElasticsearchUrl/$targetIndex/_mapping" `
        -ContentType "application/json" -Body $mappingUpdate | Out-Null
}

$activeMapping = Invoke-RestMethod -Method Get -Uri "$ElasticsearchUrl/$targetIndex/_mapping"
$attributesMapping = $activeMapping.$targetIndex.mappings.properties.attributes
if ($attributesMapping.type -ne "nested") {
    throw "Index $targetIndex does not have the required nested attributes mapping. Create a new versioned index before deploying connectors."
}
$variantsMapping = $activeMapping.$targetIndex.mappings.properties.variants
if ($variantsMapping.type -ne "nested" -or $variantsMapping.properties.selections.type -ne "nested") {
    throw "Index $targetIndex does not have the required nested variants/selections mapping."
}

try {
    $aliasState = Invoke-RestMethod -Method Get -Uri "$ElasticsearchUrl/_alias/$searchAlias"
} catch {
    if ($_.Exception.Response.StatusCode.value__ -ne 404) {
        throw
    }
    $aliasState = $null
}

$legacyConcreteIndex = (Test-ElasticsearchPath $searchAlias) -and $null -eq $aliasState
if ($legacyConcreteIndex) {
    if (-not $MigrateLegacyProductsIndex) {
        throw "Legacy concrete index '$searchAlias' blocks the versioned alias. Re-run with -MigrateLegacyProductsIndex after confirming it is a derived, rebuildable search index."
    }

    Write-Host "Migrating legacy concrete index $searchAlias to $targetIndex..."
    $reindexBody = @{
        source = @{
            index = $searchAlias
            _source = @{ excludes = @("_class") }
        }
        dest = @{ index = $targetIndex }
    } | ConvertTo-Json -Depth 5
    $reindexResult = Invoke-RestMethod -Method Post -Uri "$ElasticsearchUrl/_reindex?wait_for_completion=true" -ContentType "application/json" -Body $reindexBody
    if ($reindexResult.failures.Count -gt 0) {
        throw "Legacy product reindex reported failures; the legacy index was not removed."
    }
    Invoke-RestMethod -Method Post -Uri "$ElasticsearchUrl/$targetIndex/_refresh" | Out-Null
    $legacyCount = (Invoke-RestMethod -Method Get -Uri "$ElasticsearchUrl/$searchAlias/_count").count
    $targetCount = (Invoke-RestMethod -Method Get -Uri "$ElasticsearchUrl/$targetIndex/_count").count
    if ($targetCount -lt $legacyCount) {
        throw "Target index count $targetCount is smaller than legacy count $legacyCount; the legacy index was not removed."
    }
    Invoke-RestMethod -Method Delete -Uri "$ElasticsearchUrl/$searchAlias" | Out-Null
    $aliasState = $null
}

if ($null -eq $aliasState) {
    $aliasBody = @{ actions = @(@{ add = @{ index = $targetIndex; alias = $searchAlias; is_write_index = $true } }) } | ConvertTo-Json -Depth 5
    Invoke-RestMethod -Method Post -Uri "$ElasticsearchUrl/_aliases" -ContentType "application/json" -Body $aliasBody | Out-Null
} elseif ($null -eq $aliasState.PSObject.Properties[$targetIndex]) {
    $currentIndices = $aliasState.PSObject.Properties.Name -join ", "
    throw "Alias $searchAlias currently points to $currentIndices. Refusing to switch traffic to $targetIndex without rebuild and consistency checks. Run reindex-products-v3.ps1."
}

Write-Host "Deploying Debezium outbox source connector..."
Invoke-RestMethod -Method Put -Uri "$ConnectUrl/connectors/catalog-products-outbox-source/config" -ContentType "application/json" -Body ($debeziumConfig | ConvertTo-Json -Depth 20) | Out-Null

Write-Host "Deploying Elasticsearch sink connector..."
Invoke-RestMethod -Method Put -Uri "$ConnectUrl/connectors/products-elasticsearch-sink/config" -ContentType "application/json" -Body ($sinkConfig | ConvertTo-Json -Depth 20) | Out-Null

Write-Host "Connectors:"
Invoke-RestMethod -Method Get -Uri "$ConnectUrl/connectors"
