param(
    [string]$ConnectUrl = "http://localhost:8084",
    [string]$ElasticsearchUrl = "http://localhost:9200",
    [string]$CatalogAdminUrl = "http://localhost:8083/api/v1/admin/product-attributes/reindex",
    [string]$AdminBearerToken = "",
    [string]$GatewayToken = "",
    [string]$TargetIndex = "products_v3",
    [int]$ConsistencyRetries = 12,
    [int]$RetryDelaySeconds = 5,
    [switch]$SkipMysqlRebuild,
    [switch]$RecreateTarget
)

$ErrorActionPreference = "Stop"
$pipelineRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$mappingPath = Join-Path $pipelineRoot "elasticsearch\products-v3-index-mapping.json"
$alias = "products"
$sink = "products-elasticsearch-sink"
$sinkPaused = $false

function Test-EsPath([string]$Path) {
    try { Invoke-WebRequest -Method Head -Uri "$ElasticsearchUrl/$Path" -UseBasicParsing | Out-Null; return $true }
    catch { if ($_.Exception.Response.StatusCode.value__ -eq 404) { return $false }; throw }
}

function Wait-ConnectorState([string]$Expected) {
    for ($attempt = 1; $attempt -le 20; $attempt++) {
        $status = Invoke-RestMethod "$ConnectUrl/connectors/$sink/status"
        $states = @($status.connector.state) + @($status.tasks | ForEach-Object { $_.state })
        if (($states | Where-Object { $_ -ne $Expected }).Count -eq 0) { return }
        Start-Sleep -Seconds 1
    }
    throw "Connector $sink did not reach state $Expected."
}

if (-not (Test-EsPath $alias)) { throw "Search alias/index '$alias' does not exist; deploy the pipeline first." }

if (-not $SkipMysqlRebuild) {
    $headers = @{}
    if (-not [string]::IsNullOrWhiteSpace($AdminBearerToken)) { $headers.Authorization = "Bearer $AdminBearerToken" }
    if (-not [string]::IsNullOrWhiteSpace($GatewayToken)) { $headers."X-Gateway-Token" = $GatewayToken }
    Write-Host "Enqueuing a complete active-product rebuild from MySQL..."
    Invoke-RestMethod -Method Post -Uri $CatalogAdminUrl -Headers $headers | Out-Null
    for ($attempt = 1; $attempt -le $ConsistencyRetries; $attempt++) {
        try {
            & (Join-Path $PSScriptRoot "verify-products-consistency.ps1") -ElasticsearchUrl $ElasticsearchUrl -Index $alias | Out-Null
            break
        } catch {
            if ($attempt -eq $ConsistencyRetries) { throw }
            Start-Sleep -Seconds $RetryDelaySeconds
        }
    }
}

if (Test-EsPath $TargetIndex) {
    $targetAliases = Invoke-RestMethod -Method Get -Uri "$ElasticsearchUrl/$TargetIndex/_alias" -ErrorAction SilentlyContinue
    $alreadyLive = $null -ne $targetAliases.$TargetIndex.aliases.PSObject.Properties[$alias]
    if ($alreadyLive) { throw "$TargetIndex is already serving alias $alias; no migration is needed." }
    $targetCount = (Invoke-RestMethod "$ElasticsearchUrl/$TargetIndex/_count").count
    if ($targetCount -gt 0 -and -not $RecreateTarget) {
        throw "$TargetIndex already contains $targetCount documents. Re-run with -RecreateTarget only if it is a disposable derived index."
    }
    if ($RecreateTarget) { Invoke-RestMethod -Method Delete -Uri "$ElasticsearchUrl/$TargetIndex" | Out-Null }
}
if (-not (Test-EsPath $TargetIndex)) {
    $mapping = Get-Content -Raw -Encoding UTF8 -LiteralPath $mappingPath
    Invoke-RestMethod -Method Put -Uri "$ElasticsearchUrl/$TargetIndex" -ContentType "application/json" -Body $mapping | Out-Null
}

try {
    Write-Host "Pausing $sink so CDC events queue during the atomic migration..."
    Invoke-RestMethod -Method Put -Uri "$ConnectUrl/connectors/$sink/pause" | Out-Null
    $sinkPaused = $true
    Wait-ConnectorState "PAUSED"

    $reindexBody = @{ source = @{ index = $alias }; dest = @{ index = $TargetIndex } } | ConvertTo-Json -Depth 5
    $result = Invoke-RestMethod -Method Post -Uri "$ElasticsearchUrl/_reindex?wait_for_completion=true&refresh=true" `
        -ContentType "application/json" -Body $reindexBody
    if ($result.failures.Count -gt 0) { throw "Reindex returned failures; alias was not changed." }

    & (Join-Path $PSScriptRoot "verify-products-consistency.ps1") -ElasticsearchUrl $ElasticsearchUrl -Index $TargetIndex | Out-Null
    $aliasState = Invoke-RestMethod "$ElasticsearchUrl/_alias/$alias"
    $actions = @($aliasState.PSObject.Properties.Name | ForEach-Object {
        @{ remove = @{ index = $_; alias = $alias } }
    })
    $actions += @{ add = @{ index = $TargetIndex; alias = $alias; is_write_index = $true } }
    $aliasBody = @{ actions = $actions } | ConvertTo-Json -Depth 8
    Invoke-RestMethod -Method Post -Uri "$ElasticsearchUrl/_aliases" -ContentType "application/json" -Body $aliasBody | Out-Null
    Write-Host "Alias $alias now points atomically to $TargetIndex. The previous index was retained for rollback."
} finally {
    if ($sinkPaused) {
        Invoke-RestMethod -Method Put -Uri "$ConnectUrl/connectors/$sink/resume" | Out-Null
        Wait-ConnectorState "RUNNING"
    }
}
