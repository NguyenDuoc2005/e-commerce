param(
    [Parameter(Mandatory = $true)]
    [ValidateSet("CLEAN1", "CLEAN2")]
    [string]$Label
)

# Backward-compatible entry point. The full runner resolves IDs from the
# current canonical seed and response data instead of historical UUIDs.
& (Join-Path $PSScriptRoot 'e2e-full-regression.ps1') -Label $Label
exit $LASTEXITCODE

$ErrorActionPreference = "Stop"
$base = "http://localhost:8080"
$alphaSellerId = "275949a6-8b28-494c-a75d-2acd45106aca"
$productId = "b9b99405-3c86-4dc0-a9ad-904c293eb5e9"
$redM = "fa4db936-9443-487b-bbe6-37f260b888b6"
$redS = "c188c554-0c71-41c9-9556-c141cc1debcc"
$charger = "38000000-0000-0000-0000-000000000011"

function Assert-True {
    param([bool]$Condition, [string]$Message)
    if (-not $Condition) { throw "ASSERT: $Message" }
}

function Invoke-Api {
    param(
        [string]$Method,
        [string]$Uri,
        [hashtable]$Headers = @{},
        [object]$Body = $null
    )
    $parameters = @{
        Method = $Method
        Uri = $Uri
        Headers = $Headers
        TimeoutSec = 15
    }
    if ($null -ne $Body) {
        $parameters.ContentType = "application/json"
        $parameters.Body = if ($Body -is [string]) { $Body } else { $Body | ConvertTo-Json -Depth 8 }
    }
    Invoke-RestMethod @parameters
}

function Get-HttpCode {
    param([string]$Method, [string]$Uri, [hashtable]$Headers = @{}, [object]$Body = $null)
    try {
        $parameters = @{ Method = $Method; Uri = $Uri; Headers = $Headers; TimeoutSec = 15; UseBasicParsing = $true }
        if ($null -ne $Body) {
            $parameters.ContentType = "application/json"
            $parameters.Body = if ($Body -is [string]) { $Body } else { $Body | ConvertTo-Json -Depth 8 }
        }
        return [int](Invoke-WebRequest @parameters).StatusCode
    } catch {
        return [int]$_.Exception.Response.StatusCode
    }
}

function Login-User {
    param([string]$Email, [string]$Password)
    (Invoke-Api POST "$base/api/v1/auth/login" @{} @{ email = $Email; password = $Password }).data
}

$buyerData = Login-User "e2e.buyer2@ecommerce.local" "Buyer123"
$sellerData = Login-User "e2e.buyer1@ecommerce.local" "Buyer123"
$seller2Data = Login-User "customer2@ecommerce.local" "Admin@123"
$adminData = (Invoke-Api POST "$base/api/v1/auth/login-admin" @{} @{
    email = "admin@ecommerce.local"; password = "Admin@123"
}).data
$buyerHeaders = @{ Authorization = "Bearer $($buyerData.accessToken)" }
$sellerHeaders = @{ Authorization = "Bearer $($sellerData.accessToken)" }
$seller2Headers = @{ Authorization = "Bearer $($seller2Data.accessToken)" }
$adminHeaders = @{ Authorization = "Bearer $($adminData.accessToken)" }

# 3.1-3.2: refresh, duplicate registration, and immediate suspension enforcement.
$refresh = Invoke-Api POST "$base/api/v1/auth/refresh" @{} @{ refreshToken = $buyerData.refreshToken }
Assert-True ([bool]$refresh.data.accessToken) "refresh token"
$duplicateCode = Get-HttpCode POST "$base/api/v1/sellers/register-shop" $sellerHeaders @{
    shopName = "Duplicate $Label"; sellerSlug = "duplicate-$($Label.ToLower())"
    contactEmail = "e2e.buyer1@ecommerce.local"; contactPhone = "0399999001"; pickupAddress = "E2E"
}
Assert-True ($duplicateCode -in 400, 409) "duplicate shop registration must be blocked"
Invoke-Api POST "$base/api/v1/admin/sellers/$alphaSellerId/suspend" $adminHeaders @{ reason = "$Label live token" } | Out-Null
Assert-True ((Get-HttpCode GET "$base/api/v1/seller/profile" $sellerHeaders) -eq 403) "suspended seller token"
Invoke-Api POST "$base/api/v1/admin/sellers/$alphaSellerId/reopen" $adminHeaders | Out-Null
$sellerData = Login-User "e2e.buyer1@ecommerce.local" "Buyer123"
$sellerHeaders = @{ Authorization = "Bearer $($sellerData.accessToken)" }

# 3.3-3.5: catalog ownership/public filtering and authoritative cart snapshot.
Invoke-Api PUT "$base/api/v1/seller/products/$productId/status" $sellerHeaders @{ status = "ACTIVE" } | Out-Null
$product = Invoke-Api GET "$base/api/v1/seller/products/$productId" $sellerHeaders
Assert-True (@($product.variants).Count -eq 4) "product aggregate must retain four variants"
Assert-True ((Get-HttpCode GET "$base/api/v1/seller/products/$productId" $seller2Headers) -eq 403) "cross-seller product access"
$publicProducts = Invoke-Api GET "$base/api/v1/permitall/products?keyword=E2E%20Alpha&categoryId=10e83d7c-4c08-4c3a-bc37-985167c42490&minPrice=100000&maxPrice=130000"
Assert-True ($null -ne $publicProducts) "public catalog filters"
Invoke-Api POST "$base/api/v1/buyer/cart" $buyerHeaders @{ idSPCT = $redS; price = "1"; quantity = "1" } | Out-Null
$cart = Invoke-Api GET "$base/api/v1/buyer/cart" $buyerHeaders
$cartItem = @($cart.data.items) | Where-Object productVariantId -eq $redS | Select-Object -First 1
Assert-True ($cartItem.price -eq 111000) "cart must ignore client price"
Invoke-Api PUT "$base/api/v1/buyer/cart/$($cartItem.id)" $buyerHeaders | Out-Null

# 3.6-3.7: invalid input contract, multi-seller checkout, and full state machine.
$invalidCode = Get-HttpCode POST "$base/api/orders/create" $buyerHeaders @{
    hoTen = "Bad"; soDienThoai = "0399999002"; address = "E2E"
    email = "e2e.buyer2@ecommerce.local"; hinhThucThanhToan = "COD"
    product = @(@{ id = "00000000-0000-0000-0000-000000000099"; quantity = 1 })
}
Assert-True ($invalidCode -eq 400) "unknown variant must return 400"
$order = Invoke-Api POST "$base/api/orders/create" $buyerHeaders @{
    hoTen = "Clean Buyer"; soDienThoai = "0399999002"; address = "Clean Address"
    email = "e2e.buyer2@ecommerce.local"; hinhThucThanhToan = "COD"
    tongTien = 1; phiShip = 999999; giamGia = 999999; tongCong = 1
    Customer = "00000000-0000-0000-0000-000000000001"
    product = @(@{ id = $redM; quantity = 1 }, @{ id = $charger; quantity = 1 })
}
Assert-True ($order.customer_id -eq "417ef8eb-8959-472a-b424-90de7d87ceb4") "checkout customer authority"
Assert-True ($order.total_amount -eq 605000 -and $order.total_after_discount -eq 635000) "checkout totals authority"
$subA = @((Invoke-Api GET "$base/api/v1/seller/orders?q=$($order.code)" $sellerHeaders))[0]
$subB = @((Invoke-Api GET "$base/api/v1/seller/orders?q=$($order.code)" $seller2Headers))[0]
Assert-True ($subA.order_id -eq $order.id -and $subB.order_id -eq $order.id) "multi-seller split"
foreach ($action in @("confirm", "ready-to-ship", "shipping", "complete")) {
    Invoke-Api POST "$base/api/v1/seller/orders/$($subA.id)/$action" $sellerHeaders | Out-Null
    Invoke-Api POST "$base/api/v1/seller/orders/$($subB.id)/$action" $seller2Headers | Out-Null
}
$grouped = Invoke-Api GET "$base/api/v1/buyer/orders/grouped" $buyerHeaders
$root = @($grouped.data) | Where-Object id -eq $order.id | Select-Object -First 1
Assert-True ($root.orderStatus -eq 4 -or $root.order_status -eq 4) "root status aggregation"

# 3.8-3.10: promotion reads, review/chat/follow, dispute, and effective moderation action.
Assert-True ($null -ne (Invoke-Api GET "$base/api/v1/seller/vouchers" $sellerHeaders)) "seller vouchers"
Assert-True ($null -ne (Invoke-Api GET "$base/api/v1/seller/promotions" $sellerHeaders)) "seller campaigns"
Assert-True ($null -ne (Invoke-Api GET "$base/api/v1/permitall/flash-sales")) "public flash sales"
$review = (Invoke-Api POST "$base/api/v1/buyer/reviews" $buyerHeaders @{
    orderSellerId = $subA.id; productDetailId = $redM; productRating = 5; shopRating = 5
    comment = "$Label review"; imageUrls = @()
}).data
Assert-True ([bool]$review.id) "purchased review"
Invoke-Api POST "$base/api/v1/buyer/shops/$alphaSellerId/follow" $buyerHeaders | Out-Null
Invoke-Api DELETE "$base/api/v1/buyer/shops/$alphaSellerId/follow" $buyerHeaders | Out-Null
$conversation = Invoke-Api POST "$base/api/v1/buyer/chat/conversations" $buyerHeaders @{ sellerId = $alphaSellerId }
Invoke-Api POST "$base/api/v1/buyer/chat/conversations/$($conversation.id)/messages" $buyerHeaders @{ content = "$Label ping" } | Out-Null
Invoke-Api POST "$base/api/v1/seller/chat/conversations/$($conversation.id)/read" $sellerHeaders | Out-Null
$dispute = Invoke-Api POST "$base/api/v1/buyer/disputes" $buyerHeaders @{
    orderSellerId = $subA.id; disputeType = "OTHER"; reason = "$Label regression"; requestedAmount = 1000
}
Invoke-Api POST "$base/api/v1/seller/disputes/$($dispute.id)/respond" $sellerHeaders @{ message = "$Label response" } | Out-Null
Invoke-Api POST "$base/api/v1/admin/disputes/$($dispute.id)/take-review" $adminHeaders | Out-Null
$resolvedDispute = Invoke-Api POST "$base/api/v1/admin/disputes/$($dispute.id)/resolve" $adminHeaders @{
    decision = "REJECT_BUYER"; note = "$Label clean reject"
}
Assert-True ($resolvedDispute.status -eq "RESOLVED_REJECT_BUYER") "dispute workflow"
$report = Invoke-Api POST "$base/api/v1/buyer/reports" $buyerHeaders @{
    targetType = "REVIEW"; targetId = $review.id; reasonCode = "FAKE_REVIEW"; description = "$Label report"
}
Invoke-Api POST "$base/api/v1/admin/reports/$($report.id)/review" $adminHeaders | Out-Null
$resolvedReport = Invoke-Api POST "$base/api/v1/admin/reports/$($report.id)/resolve" $adminHeaders @{
    actionTaken = "REVIEW_HIDDEN"; note = "$Label hide"
}
Assert-True ($resolvedReport.status -eq "ACTION_TAKEN") "report workflow"

# 3.11 and risks 6/8: admin CRUD/statistics, secret redaction, and downstream boundary.
$banner = (Invoke-Api POST "$base/api/v1/admin/banners" $adminHeaders @{
    title = "$Label Banner"; imageUrl = "https://example.invalid/$($Label.ToLower()).png"
    position = "HOME_TOP"; active = $true; sortOrder = 10
}).data
$updatedBanner = (Invoke-Api PUT "$base/api/v1/admin/banners/$($banner.id)" $adminHeaders @{
    title = "$Label Banner Updated"; imageUrl = "https://example.invalid/$($Label.ToLower())-2.png"
    position = "HOME_TOP"; active = $true; sortOrder = 9
}).data
Invoke-Api DELETE "$base/api/v1/admin/banners/$($banner.id)" $adminHeaders | Out-Null
$customer = (Invoke-Api GET "$base/api/v1/admin/khach-hang/183f0bb6-736f-4674-816f-9287cbc5e699" $adminHeaders).data
$staff = (Invoke-Api GET "$base/api/v1/admin/nhan-vien/47dcc60d-4d5e-4cd0-abc9-272baee0390f" $adminHeaders).data
Assert-True ($null -eq $customer.password -and $null -eq $staff.password) "password hashes must be redacted"
$dashboard = Invoke-Api GET "$base/api/v1/admin/thong-ke/marketplace-dashboard" $adminHeaders
Assert-True ($dashboard.gmv -gt 0) "marketplace statistics"
Assert-True ((Get-HttpCode GET "http://localhost:8082/api/v1/admin/khach-hang" @{ "X-User-Id" = "fake" }) -eq 401) "direct admin boundary"
Assert-True ((Get-HttpCode GET "http://localhost:8083/internal/catalog/products/$productId") -eq 401) "direct internal boundary"
Assert-True ((Get-HttpCode POST "http://localhost:8088/api/v1/notifications/email" @{} @{}) -eq 401) "direct notification boundary"
Assert-True ((Get-HttpCode GET "$base/catalog-service/internal/catalog/products/$productId") -eq 404) "gateway discovery disabled"

[pscustomobject]@{
    pass = $Label
    orderCode = $order.code
    orderId = $order.id
    rootStatus = 4
    reviewId = $review.id
    dispute = $resolvedDispute.status
    report = $resolvedReport.actionTaken
    bannerCrud = $updatedBanner.title
    security = "direct=401, discovery=404"
    result = "CLEAN"
} | ConvertTo-Json
