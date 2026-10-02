param(
    [string]$ElasticsearchUrl = "http://localhost:9200",
    [string]$Index = "products",
    [string]$ComposeFile = "backend-microservice\docker-compose.yml",
    [string]$MysqlPassword = "12345678"
)

$ErrorActionPreference = "Stop"
$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..\..")
$composePath = Join-Path $repoRoot $ComposeFile

function Invoke-Utf8Json {
    param([string]$Uri, [string]$Body)
    $response = Invoke-WebRequest -Method Post -Uri $Uri -UseBasicParsing `
        -ContentType "application/json; charset=utf-8" -Body ([Text.Encoding]::UTF8.GetBytes($Body))
    $response.RawContentStream.Position = 0
    $reader = [IO.StreamReader]::new($response.RawContentStream, [Text.Encoding]::UTF8)
    try { return ($reader.ReadToEnd() | ConvertFrom-Json) } finally { $reader.Dispose() }
}

$mysqlRows = & docker compose -f $composePath exec -T mysql mysql `
    -uroot "-p$MysqlPassword" -N -B ecommerce_catalog `
    -e "SELECT id, seller_id, category_id, TO_BASE64(name), COALESCE(rating_count,0) FROM product WHERE status=0 ORDER BY id"
if ($LASTEXITCODE -ne 0) { throw "Could not read active products from MySQL." }

$mysql = @{}
foreach ($line in $mysqlRows) {
    if ([string]::IsNullOrWhiteSpace($line)) { continue }
    $parts = $line -split "`t", 5
    $mysql[$parts[0]] = [ordered]@{
        sellerId = $parts[1]
        categoryId = $parts[2]
        name = [Text.Encoding]::UTF8.GetString([Convert]::FromBase64String($parts[3]))
        ratingCount = [long]$parts[4]
    }
}

$documents = @{}
$searchAfter = $null
do {
    $body = [ordered]@{
        size = 1000
        query = @{ term = @{ status = "ACTIVE" } }
        sort = @(@{ id = "asc" })
        _source = @("id", "sellerId", "categoryId", "name", "ratingCount")
    }
    if ($null -ne $searchAfter) { $body.search_after = @($searchAfter) }
    $response = Invoke-Utf8Json -Uri "$ElasticsearchUrl/$Index/_search" `
        -Body ($body | ConvertTo-Json -Depth 10)
    foreach ($hit in $response.hits.hits) { $documents[$hit._source.id] = $hit._source }
    $searchAfter = if ($response.hits.hits.Count -gt 0) { $response.hits.hits[-1].sort[0] } else { $null }
} while ($response.hits.hits.Count -eq 1000)

$missing = @($mysql.Keys | Where-Object { -not $documents.ContainsKey($_) })
$extra = @($documents.Keys | Where-Object { -not $mysql.ContainsKey($_) })
$mismatched = @()
foreach ($id in $mysql.Keys | Where-Object { $documents.ContainsKey($_) }) {
    $left = $mysql[$id]
    $right = $documents[$id]
    if ($left.sellerId -ne $right.sellerId `
            -or $left.categoryId -ne $right.categoryId `
            -or $left.name -ne $right.name `
            -or $left.ratingCount -ne [long]$right.ratingCount) {
        $mismatched += [pscustomobject][ordered]@{
            id = $id
            mysql = [pscustomobject]$left
            elasticsearch = [pscustomobject][ordered]@{
                sellerId = $right.sellerId
                categoryId = $right.categoryId
                name = $right.name
                ratingCount = [long]$right.ratingCount
            }
        }
    }
}

$result = [ordered]@{
    mysqlActive = $mysql.Count
    elasticsearchActive = $documents.Count
    missingIds = @($missing | Select-Object -First 20)
    extraIds = @($extra | Select-Object -First 20)
    mismatchedCoreFields = @($mismatched | Select-Object -First 20)
}
if ($missing.Count -gt 0 -or $extra.Count -gt 0 -or $mismatched.Count -gt 0) {
    throw "MySQL/Elasticsearch consistency check failed: $($result | ConvertTo-Json -Compress)"
}
$result | ConvertTo-Json
