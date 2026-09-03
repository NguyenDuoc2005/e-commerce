param([Parameter(Mandatory = $true)][string]$Label)

$ErrorActionPreference = "Stop"
$base = "http://localhost:8080"
$suffix = $Label.ToLowerInvariant()
$attributeId = "41000000-0000-0000-0000-000000000003"

function Assert-True { param([bool]$Condition, [string]$Message); if (-not $Condition) { throw "ASSERT: $Message" } }
function Invoke-Api {
    param([string]$Method, [string]$Uri, [hashtable]$Headers = @{}, [object]$Body = $null)
    $p = @{ Method = $Method; Uri = $Uri; Headers = $Headers; TimeoutSec = 15 }
    if ($null -ne $Body) { $p.ContentType = "application/json"; $p.Body = ConvertTo-Json -InputObject $Body -Depth 12 }
    Invoke-RestMethod @p
}
function Get-Code {
    param([string]$Method, [string]$Uri, [hashtable]$Headers = @{}, [object]$Body = $null)
    try {
        $p = @{ Method = $Method; Uri = $Uri; Headers = $Headers; TimeoutSec = 15; UseBasicParsing = $true }
        if ($null -ne $Body) { $p.ContentType = "application/json"; $p.Body = ConvertTo-Json -InputObject $Body -Depth 12 }
        [int](Invoke-WebRequest @p).StatusCode
    } catch { [int]$_.Exception.Response.StatusCode }
}
function Login { param([string]$Email,[string]$Password); (Invoke-Api POST "$base/api/v1/auth/login" @{} @{email=$Email;password=$Password}).data }

$admin = (Invoke-Api POST "$base/api/v1/auth/login-admin" @{} @{email="admin@ecommerce.local";password="Admin@123"}).data
$seller = Login "e2e.approve.reg2@ecommerce.local" "Buyer123!"
$sellerB = Login "customer2@ecommerce.local" "Admin@123"
$adminHeaders = @{Authorization="Bearer $($admin.accessToken)"}
$sellerHeaders = @{Authorization="Bearer $($seller.accessToken)"}
$sellerBHeaders = @{Authorization="Bearer $($sellerB.accessToken)"}

$root = Invoke-Api POST "$base/api/v1/admin/categories" $adminHeaders @{
    name="Catalog Root $Label";code="ROOT-$Label";slug="catalog-root-$suffix";displayOrder=90
}
$leaf = Invoke-Api POST "$base/api/v1/admin/categories" $adminHeaders @{
    name="Catalog Leaf $Label";code="LEAF-$Label";slug="catalog-leaf-$suffix";parentId=$root.id;displayOrder=1
}
Invoke-Api PUT "$base/api/v1/admin/categories/$($leaf.id)/attribute-suggestions" $adminHeaders @(
    @{definitionId=$attributeId;required=$true;filterable=$true;displayOrder=0}
) | Out-Null

$simpleProduct = @{
    categoryId=$root.id;code="PARENT-$Label";name="Invalid Parent $Label";description="must fail"
    productImages=@(@{url="https://example.invalid/parent.png";displayOrder=0;status="ACTIVE"})
    attributes=@();variantAxes=@();variants=@(@{sku="PARENT-$Label";salePrice=100000;quantity=5;defaultVariant=$true;status="ACTIVE";selectionValueKeys=@()})
}
Assert-True ((Get-Code POST "$base/api/v1/seller/products" $sellerHeaders $simpleProduct) -eq 400) "non-leaf category rejected"

$valid = @{
    categoryId=$leaf.id;code="E2E-$Label";name="E2E Matrix $Label";description="two-axis E2E product"
    productImages=@(
        @{url="https://example.invalid/$suffix-main.png";displayOrder=0;status="ACTIVE"},
        @{url="https://example.invalid/$suffix-alt.png";displayOrder=1;status="ACTIVE"}
    )
    attributes=@(@{definitionId=$attributeId;dataType="TEXT";valueText="Cotton $Label";displayOrder=0})
    variantAxes=@(
        @{clientKey="color";name="Mau";displayOrder=1;values=@(
            @{clientKey="red";value="Do";displayOrder=0},@{clientKey="blue";value="Xanh";displayOrder=1}
        )},
        @{clientKey="size";name="Kich thuoc";displayOrder=2;values=@(
            @{clientKey="s";value="S";displayOrder=0},@{clientKey="m";value="M";displayOrder=1}
        )}
    )
    variants=@(
        @{sku="$Label-RED-S";salePrice=101000;quantity=10;defaultVariant=$false;status="ACTIVE";selectionValueKeys=@("red","s")},
        @{sku="$Label-RED-M";salePrice=102000;quantity=11;defaultVariant=$false;status="ACTIVE";selectionValueKeys=@("red","m")},
        @{sku="$Label-BLUE-S";salePrice=103000;quantity=12;defaultVariant=$false;status="ACTIVE";selectionValueKeys=@("blue","s")},
        @{sku="$Label-BLUE-M";salePrice=104000;quantity=13;defaultVariant=$false;status="ACTIVE";selectionValueKeys=@("blue","m")}
    )
}

$tooManyAxes = $valid | ConvertTo-Json -Depth 12 | ConvertFrom-Json
$tooManyAxes.code = "AXIS-$Label"; $tooManyAxes.name = "Invalid Axis $Label"
$tooManyAxes.variantAxes += @{clientKey="material";name="Chat lieu";displayOrder=3;values=@(@{clientKey="cotton";value="Cotton";displayOrder=0})}
Assert-True ((Get-Code POST "$base/api/v1/seller/products" $sellerHeaders $tooManyAxes) -eq 400) "more than two axes rejected"

$duplicate = $valid | ConvertTo-Json -Depth 12 | ConvertFrom-Json
$duplicate.code = "DUP-$Label"; $duplicate.name = "Invalid Combination $Label"
$duplicate.variants += @{sku="$Label-DUP";salePrice=105000;quantity=1;defaultVariant=$false;status="ACTIVE";selectionValueKeys=@("red","s")}
Assert-True ((Get-Code POST "$base/api/v1/seller/products" $sellerHeaders $duplicate) -eq 400) "duplicate combination rejected"

$product = Invoke-Api POST "$base/api/v1/seller/products" $sellerHeaders $valid
Assert-True (@($product.variants).Count -eq 4 -and @($product.variantAxes).Count -eq 2 -and @($product.productImages).Count -eq 2) "product aggregate"
Assert-True ((Get-Code GET "$base/api/v1/seller/products/$($product.id)" $sellerBHeaders) -eq 403) "seller ownership detail"
Assert-True ((Get-Code PUT "$base/api/v1/seller/products/$($product.id)" $sellerBHeaders $valid) -eq 403) "seller ownership update"

$filters = [Uri]::EscapeDataString((@{$attributeId=@{values=@("Cotton $Label")}} | ConvertTo-Json -Compress))
$public = Invoke-Api GET "$base/api/v1/permitall/products?q=E2E%20Matrix%20$Label&categoryId=$($leaf.id)&minPrice=100000&maxPrice=102000&attributeFilters=$filters&sort=price_asc"
Assert-True (@($public.content).Count -eq 1 -and $public.content[0].id -eq $product.id) "public keyword/category/price/attribute/sort"
Assert-True ((Get-Code GET "$base/api/v1/permitall/products/$($product.id)") -eq 200) "active public detail"
Invoke-Api PUT "$base/api/v1/seller/products/$($product.id)/status" $sellerHeaders @{status="INACTIVE"} | Out-Null
Assert-True ((Get-Code GET "$base/api/v1/permitall/products/$($product.id)") -eq 404) "inactive product hidden"
Invoke-Api PUT "$base/api/v1/seller/products/$($product.id)/status" $sellerHeaders @{status="ACTIVE"} | Out-Null

[pscustomobject]@{label=$Label;rootCategoryId=$root.id;leafCategoryId=$leaf.id;productId=$product.id;variantIds=@($product.variants.id);result="PASS"} | ConvertTo-Json -Depth 4
