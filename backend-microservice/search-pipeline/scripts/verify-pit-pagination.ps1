param(
    [string]$ElasticsearchUrl = "http://localhost:9200",
    [string]$CatalogUrl = "http://localhost:8083",
    [string]$Index = "products"
)

$ErrorActionPreference = "Stop"
$prefix = "pit-verify-$([Guid]::NewGuid().ToString('N').Substring(0, 10))"
$ids = 1..5 | ForEach-Object { "$prefix-$_" }
$activeCursor = $null

function Put-TestProduct([string]$Id, [long]$CreatedAt) {
    $document = [ordered]@{
        id = $Id
        code = $Id
        sellerId = "pit-verify-seller"
        name = "$prefix snapshot product"
        description = "Temporary PIT verification document"
        status = "ACTIVE"
        categoryId = "pit-verify-category"
        categoryName = "PIT Verification"
        categorySlug = "pit-verification"
        imageUrl = $null
        ratingAverage = 0
        ratingCount = 0
        createdAt = $CreatedAt
        attributes = @()
        variants = @()
    }
    $json = $document | ConvertTo-Json -Depth 10
    Invoke-RestMethod -Method Put -Uri "$ElasticsearchUrl/$Index/_doc/${Id}?refresh=true" `
        -ContentType "application/json; charset=utf-8" -Body ([Text.Encoding]::UTF8.GetBytes($json)) | Out-Null
}

try {
    $baseCreatedAt = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()
    1..4 | ForEach-Object { Put-TestProduct -Id $ids[$_ - 1] -CreatedAt ($baseCreatedAt + $_) }

    $collected = @()
    $first = Invoke-RestMethod "$CatalogUrl/api/v1/permitall/products/search?categoryId=pit-verify-category&sort=newest&page=0&size=1"
    $collected += $first.items[0].id
    $activeCursor = $first.nextCursor
    if ([string]::IsNullOrWhiteSpace($activeCursor)) {
        throw "First PIT page did not return a cursor (total=$($first.totalElements), items=$($first.items.Count), totalPages=$($first.totalPages))."
    }

    # Mutate the live index after the PIT snapshot: delete a document that has not
    # been paged yet and add a newer matching document.
    Invoke-RestMethod -Method Delete -Uri "$ElasticsearchUrl/$Index/_doc/$($ids[0])?refresh=true" | Out-Null
    Put-TestProduct -Id $ids[4] -CreatedAt ($baseCreatedAt + 100)

    foreach ($page in 1..3) {
        $encodedCursor = [Uri]::EscapeDataString($activeCursor)
        $response = Invoke-RestMethod "$CatalogUrl/api/v1/permitall/products/search?categoryId=pit-verify-category&sort=newest&page=$page&size=1&cursor=$encodedCursor"
        $collected += $response.items[0].id
        $activeCursor = $response.nextCursor
    }

    $unique = @($collected | Select-Object -Unique)
    if ($collected.Count -ne 4 -or $unique.Count -ne 4) {
        throw "PIT pagination returned missing/duplicate documents: $($collected -join ',')"
    }
    if ($collected -notcontains $ids[0]) {
        throw "PIT snapshot lost the document deleted after page 1."
    }
    if ($collected -contains $ids[4]) {
        throw "PIT snapshot included a document added after page 1."
    }
    if ($null -ne $activeCursor) { throw "Last PIT page should not return nextCursor." }

    [ordered]@{
        pages = 4
        uniqueDocuments = $unique.Count
        retainedDeletedDocument = $true
        excludedNewDocument = $true
        pitClosedAtLastPage = $true
        ids = $collected
    } | ConvertTo-Json
} finally {
    if (-not [string]::IsNullOrWhiteSpace($activeCursor)) {
        try {
            $encodedCursor = [Uri]::EscapeDataString($activeCursor)
            Invoke-RestMethod -Method Delete -Uri "$CatalogUrl/api/v1/permitall/products/search/pit?cursor=$encodedCursor" | Out-Null
        } catch { Write-Warning "Could not explicitly close verification PIT: $($_.Exception.Message)" }
    }
    foreach ($id in $ids) {
        try { Invoke-RestMethod -Method Delete -Uri "$ElasticsearchUrl/$Index/_doc/$id" | Out-Null } catch {
            if ($_.Exception.Response.StatusCode.value__ -ne 404) { Write-Warning $_.Exception.Message }
        }
    }
    Invoke-RestMethod -Method Post -Uri "$ElasticsearchUrl/$Index/_refresh" | Out-Null
}
