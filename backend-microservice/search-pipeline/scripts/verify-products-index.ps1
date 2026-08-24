param(
    [string]$ElasticsearchUrl = "http://localhost:9200",
    [string]$Index = "products"
)

$ErrorActionPreference = "Stop"

function Invoke-SearchCount {
    param([hashtable]$Query)
    $body = @{ query = $Query } | ConvertTo-Json -Depth 30
    return (Invoke-RestMethod -Method Post -Uri "$ElasticsearchUrl/$Index/_search" `
        -ContentType "application/json" -Body $body).hits.total.value
}

function New-SelectionQuery {
    param([string]$ValueId)
    return @{
        nested = @{
            path = "variants.selections"
            query = @{ term = @{ "variants.selections.valueId" = $ValueId } }
        }
    }
}

$mapping = (Invoke-RestMethod -Method Get -Uri "$ElasticsearchUrl/$Index/_mapping").PSObject.Properties.Value.mappings
if ($mapping.dynamic -ne "strict" `
        -or $mapping.properties.attributes.type -ne "nested" `
        -or $mapping.properties.variants.type -ne "nested" `
        -or $mapping.properties.variants.properties.selections.type -ne "nested") {
    throw "Product index mapping is not the strict nested target contract."
}
if ($null -ne $mapping.properties.PSObject.Properties["brand"] `
        -or $null -ne $mapping.properties.PSObject.Properties["brandId"]) {
    throw "Legacy brand fields still exist at product document top level."
}

$descriptiveMulti = Invoke-SearchCount @{
    bool = @{ filter = @(
        @{ nested = @{ path = "attributes"; query = @{ bool = @{ filter = @(
            @{ term = @{ "attributes.definitionId" = "41000000-0000-0000-0000-000000000001" } },
            @{ term = @{ "attributes.optionIds" = "42000000-0000-0000-0000-000000000001" } }
        ) } } } },
        @{ nested = @{ path = "attributes"; query = @{ bool = @{ filter = @(
            @{ term = @{ "attributes.definitionId" = "41000000-0000-0000-0000-000000000005" } },
            @{ term = @{ "attributes.valueNumber" = 8 } }
        ) } } } }
    ) }
}

$crossVariant = Invoke-SearchCount @{
    nested = @{ path = "variants"; query = @{ bool = @{ filter = @(
        (New-SelectionQuery "44000000-0000-0000-0000-000000000002"),
        (New-SelectionQuery "44000000-0000-0000-0000-000000000003"),
        @{ term = @{ "variants.status" = "ACTIVE" } }
    ) } } }
}

$sameVariant = Invoke-SearchCount @{
    nested = @{ path = "variants"; query = @{ bool = @{ filter = @(
        (New-SelectionQuery "44000000-0000-0000-0000-000000000001"),
        (New-SelectionQuery "44000000-0000-0000-0000-000000000005"),
        @{ term = @{ "variants.status" = "ACTIVE" } }
    ) } } }
}

$categoryAndPrice = Invoke-SearchCount @{
    bool = @{ filter = @(
        @{ term = @{ categoryId = "32000000-0000-0000-0000-000000000103" } },
        @{ nested = @{ path = "variants"; query = @{ bool = @{ filter = @(
            @{ term = @{ "variants.status" = "ACTIVE" } },
            @{ range = @{ "variants.salePrice" = @{ gte = 1200000; lte = 1250000 } } }
        ) } } } }
    ) }
}

$result = [ordered]@{
    descriptiveMulti = $descriptiveMulti
    crossVariantMustBeZero = $crossVariant
    validSameVariant = $sameVariant
    categoryActivePrice = $categoryAndPrice
}

if ($descriptiveMulti -ne 1 -or $crossVariant -ne 0 -or $sameVariant -ne 1 -or $categoryAndPrice -ne 1) {
    throw "Product index smoke query failed: $($result | ConvertTo-Json -Compress)"
}

$result | ConvertTo-Json
