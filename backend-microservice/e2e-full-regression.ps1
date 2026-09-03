param(
    [Parameter(Mandatory = $true)]
    [ValidateSet("CLEAN1", "CLEAN2")]
    [string]$Label
)

$ErrorActionPreference = "Stop"
$base = "http://localhost:8080"
$compose = Join-Path $PSScriptRoot "docker-compose.yml"

function Assert-True {
    param([bool]$Condition, [string]$Message)
    if (-not $Condition) { throw "ASSERT: $Message" }
}

function Invoke-Api {
    param([string]$Method, [string]$Uri, [hashtable]$Headers = @{}, [object]$Body = $null)
    $parameters = @{ Method = $Method; Uri = $Uri; Headers = $Headers; TimeoutSec = 30 }
    if ($null -ne $Body) {
        $parameters.ContentType = "application/json"
        $parameters.Body = $Body | ConvertTo-Json -Depth 10
    }
    Invoke-RestMethod @parameters
}

function Get-Code {
    param([string]$Method, [string]$Uri, [hashtable]$Headers = @{}, [object]$Body = $null)
    try {
        $parameters = @{ Method = $Method; Uri = $Uri; Headers = $Headers; TimeoutSec = 35; UseBasicParsing = $true }
        if ($null -ne $Body) {
            $parameters.ContentType = "application/json"
            $parameters.Body = $Body | ConvertTo-Json -Depth 10
        }
        [int](Invoke-WebRequest @parameters).StatusCode
    } catch {
        [int]$_.Exception.Response.StatusCode
    }
}

function Decode-Claims {
    param([string]$Token)
    $segment = $Token.Split('.')[1].Replace('-', '+').Replace('_', '/')
    while ($segment.Length % 4) { $segment += '=' }
    [Text.Encoding]::UTF8.GetString([Convert]::FromBase64String($segment)) | ConvertFrom-Json
}

function Sql-Scalar {
    param([string]$Sql)
    $value = & docker compose -f $compose exec -T -e MYSQL_PWD=12345678 mysql mysql -uroot --batch --skip-column-names -e $Sql
    if ($LASTEXITCODE -ne 0) { throw "mysql failed: $Sql" }
    $value | Select-Object -First 1
}

function Invoke-Flow {
    param([string]$Name, [hashtable]$Arguments)
    $raw = (& (Join-Path $PSScriptRoot $Name) @Arguments | Out-String)
    $jsonStart = $raw.IndexOf('{')
    if ($jsonStart -lt 0) { throw "Flow $Name did not produce JSON: $raw" }
    $result = $raw.Substring($jsonStart) | ConvertFrom-Json
    Assert-True ($result.result -in @('PASS', 'CLEAN')) "$Name failed"
    $result
}

function Stop-ProjectService {
    param([int]$Port, [string]$Module)
    $listener = Get-NetTCPConnection -LocalPort $Port -State Listen
    $process = Get-CimInstance Win32_Process -Filter "ProcessId = $($listener.OwningProcess)"
    Assert-True ($process.CommandLine -like "*e-commerce*$Module*") "unsafe process target for $Module"
    Stop-Process -Id $listener.OwningProcess -Force
    $deadline = (Get-Date).AddSeconds(15)
    while ((Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue) -and (Get-Date) -lt $deadline) {
        Start-Sleep -Milliseconds 300
    }
}

function Start-ProjectService {
    param([int]$Port, [string]$Module)
    $commandFile = Join-Path $PSScriptRoot "logs\$Module.run.cmd"
    Start-Process -FilePath "cmd.exe" -ArgumentList @('/c', "`"$commandFile`"") -WindowStyle Hidden
    $deadline = (Get-Date).AddSeconds(100)
    do {
        Start-Sleep -Seconds 2
        try { $up = (Invoke-RestMethod -Uri "http://127.0.0.1:$Port/actuator/health" -TimeoutSec 2).status -eq 'UP' }
        catch { $up = $false }
    } until ($up -or (Get-Date) -ge $deadline)
    Assert-True $up "$Module did not restart"
}

$healthPorts = @(8080, 8761, 8081, 8082, 8083, 8085, 8086, 8087, 8088, 8089, 8091)
foreach ($port in $healthPorts) {
    Assert-True ((Invoke-RestMethod -Uri "http://127.0.0.1:$port/actuator/health" -TimeoutSec 5).status -eq 'UP') "health $port"
}
$eurekaDeadline = (Get-Date).AddSeconds(60)
do {
    $apps = (Invoke-RestMethod -Uri 'http://127.0.0.1:8761/eureka/apps' -Headers @{ Accept = 'application/json' }).applications.application
    $eurekaReady = @($apps).Count -eq 10 -and @($apps | Where-Object {
        @($_.instance | Where-Object { $_.status -eq 'UP' }).Count -ge 1
    }).Count -eq 10
    if (-not $eurekaReady) { Start-Sleep -Seconds 2 }
} until ($eurekaReady -or (Get-Date) -ge $eurekaDeadline)
Assert-True $eurekaReady "Eureka registration count"

$registration = Invoke-Flow 'e2e-registration-flow.ps1' @{ Label = 'reg2' }

Invoke-Api PUT "$base/api/v1/auth/register" @{} @{
    userName = 'E2E Buyer 2'; email = 'e2e.buyer2@ecommerce.local'; phone = '0399999200'; password = 'Buyer123'
} | Out-Null
$buyer = (Invoke-Api POST "$base/api/v1/auth/login" @{} @{ email = 'e2e.buyer2@ecommerce.local'; password = 'Buyer123' }).data
$approved = (Invoke-Api POST "$base/api/v1/auth/login" @{} @{ email = $registration.approvedEmail; password = 'Buyer123!' }).data
$approvedHeaders = @{ Authorization = "Bearer $($approved.accessToken)" }

Stop-ProjectService 8089 'seller-service'
$degradedLogin = (Invoke-Api POST "$base/api/v1/auth/login" @{} @{ email = 'customer1@ecommerce.local'; password = 'Admin@123' }).data
$degradedClaims = Decode-Claims $degradedLogin.accessToken
Assert-True (@($degradedClaims.roles) -contains 'USERS' -and @($degradedClaims.roles) -notcontains 'SELLER' -and $null -eq $degradedClaims.sellerId) 'degraded buyer login'
Start-ProjectService 8089 'seller-service'

$catalog = Invoke-Flow 'e2e-catalog-flow.ps1' @{ Label = "${Label}CAT" }
$variants = @($catalog.variantIds)
Assert-True ($variants.Count -eq 4) 'catalog variant count'
Assert-True ([int](Sql-Scalar "SELECT COUNT(*) FROM ecommerce_catalog.outbox WHERE aggregate_id='$($catalog.productId)';") -ge 1) 'catalog outbox event'

& docker compose -f $compose stop kafka-connect | Out-Null
$publicWithoutConnect = Invoke-Api GET "$base/api/v1/permitall/products?q=$([uri]::EscapeDataString("E2E Matrix ${Label}CAT"))"
Assert-True (@($publicWithoutConnect.content | Where-Object { $_.id -eq $catalog.productId }).Count -eq 1) 'MySQL public read without Kafka Connect'
& docker compose -f $compose up -d kafka-connect | Out-Null

$cart = Invoke-Flow 'e2e-cart-flow.ps1' @{ ProductId = $catalog.productId; VariantId = $variants[0] }
$checkout = Invoke-Flow 'e2e-checkout-flow.ps1' @{ Label = "${Label}CHECK" }

$compVariant = '38000000-0000-0000-0000-000000000007'
Invoke-Api POST "$base/api/v1/buyer/cart" $approvedHeaders @{ idSPCT = $compVariant; price = 1; quantity = 1 } | Out-Null
$stockBefore = [int](Sql-Scalar "SELECT quantity FROM ecommerce_catalog.product_variant WHERE id='$compVariant';")
$voucherBefore = [int](Sql-Scalar "SELECT quantity FROM ecommerce_promotion.voucher WHERE code='WELCOME10';")
$ordersBefore = [int](Sql-Scalar 'SELECT COUNT(*) FROM ecommerce_order.orders;')
Stop-ProjectService 8087 'cart-service'
$failedCheckoutCode = Get-Code POST "$base/api/orders/create" $approvedHeaders @{
    hoTen = 'Compensation'; soDienThoai = '0399999203'; address = 'E2E'; email = $registration.approvedEmail
    hinhThucThanhToan = 'COD'; maGiamGia = 'WELCOME10'; tongTien = 1; phiShip = 1; giamGia = 1; tongCong = 1
    product = @(@{ id = $compVariant; quantity = 1 })
}
Start-ProjectService 8087 'cart-service'
Assert-True ($failedCheckoutCode -ge 500) 'checkout disruption response'
Assert-True ([int](Sql-Scalar "SELECT quantity FROM ecommerce_catalog.product_variant WHERE id='$compVariant';") -eq $stockBefore) 'checkout stock compensation'
Assert-True ([int](Sql-Scalar "SELECT quantity FROM ecommerce_promotion.voucher WHERE code='WELCOME10';") -eq $voucherBefore) 'checkout voucher compensation'
Assert-True ([int](Sql-Scalar 'SELECT COUNT(*) FROM ecommerce_order.orders;') -eq $ordersBefore) 'checkout order rollback'

$order = Invoke-Flow 'e2e-order-payout-flow.ps1' @{
    Label = "${Label}ORDER"; VariantA = $variants[0]; SellerAId = $registration.approvedSellerId
}
$completedSubOrder = [string](Sql-Scalar "SELECT id FROM ecommerce_order.order_seller WHERE order_id='$($order.completedOrderId)';")
$promotion = Invoke-Flow 'e2e-promotion-flow.ps1' @{
    Label = "${Label}PROMO"; SellerId = $registration.approvedSellerId
    VariantA = $variants[0]; VariantWithdraw = $variants[1]; VariantReject = $variants[2]
}
$social = Invoke-Flow 'e2e-social-flow.ps1' @{
    Label = "${Label}SOCIAL"; SellerId = $registration.approvedSellerId; ProductId = $catalog.productId
    VariantId = $variants[0]; OtherVariant = $variants[1]; OrderSellerId = $completedSubOrder
}
$dispute = Invoke-Flow 'e2e-dispute-flow.ps1' @{
    Label = "${Label}DISPUTE"; SellerId = $registration.approvedSellerId; Variant = $variants[3]
}
$buyerClaims = Decode-Claims $buyer.accessToken
$report = Invoke-Flow 'e2e-report-flow.ps1' @{
    Label = "${Label}REPORT"; ProductId = $catalog.productId; ShopId = $registration.approvedSellerId
    ShopSlug = 'approve-shop-reg2'; ReviewId = $social.reviewId; UserId = $buyerClaims.userId
}
$admin = Invoke-Flow 'e2e-admin-flow.ps1' @{ Label = "${Label}ADMIN" }

$adminLogin = (Invoke-Api POST "$base/api/v1/auth/login-admin" @{} @{ email = 'admin@ecommerce.local'; password = 'Admin@123' }).data
$adminHeaders = @{ Authorization = "Bearer $($adminLogin.accessToken)" }
$banner = (Invoke-Api POST "$base/api/v1/admin/banners" $adminHeaders @{
    title = "$Label Banner"; imageUrl = "https://example.invalid/$($Label.ToLower()).png"
    position = 'HOME_TOP'; active = $true; sortOrder = 10
}).data
$updatedBanner = (Invoke-Api PUT "$base/api/v1/admin/banners/$($banner.id)" $adminHeaders @{
    title = "$Label Banner Updated"; imageUrl = "https://example.invalid/$($Label.ToLower())-2.png"
    position = 'HOME_TOP'; active = $true; sortOrder = 9
}).data
$publicBanners = (Invoke-Api GET "$base/api/v1/permitall/banners?position=HOME_TOP").data
Assert-True ($updatedBanner.title -eq "$Label Banner Updated" -and @($publicBanners.id) -contains $banner.id) 'banner create/update/public'
Invoke-Api DELETE "$base/api/v1/admin/banners/$($banner.id)" $adminHeaders | Out-Null
$publicBannersAfter = (Invoke-Api GET "$base/api/v1/permitall/banners?position=HOME_TOP").data
Assert-True (@($publicBannersAfter.id) -notcontains $banner.id) 'banner delete'

Assert-True ((Get-Code GET 'http://localhost:8082/api/v1/admin/khach-hang' @{ 'X-User-Id' = 'fake' }) -eq 401) 'direct admin boundary'
Assert-True ((Get-Code GET "http://localhost:8083/internal/catalog/products/$($catalog.productId)") -eq 401) 'direct internal boundary'
Assert-True ((Get-Code GET "$base/catalog-service/internal/catalog/products/$($catalog.productId)") -eq 404) 'gateway discovery disabled'
Assert-True ((Get-Code POST "$base/api/v1/notifications/email" @{} @{ to = 'nobody@example.invalid'; subject = 'E2E'; content = 'blocked' }) -eq 401) 'anonymous email blocked'
Assert-True ((Get-Code POST 'http://localhost:8088/api/v1/notifications/email' @{} @{ to = 'nobody@example.invalid'; subject = 'E2E'; content = 'blocked' }) -eq 401) 'direct email blocked'

foreach ($port in $healthPorts) {
    Assert-True ((Invoke-RestMethod -Uri "http://127.0.0.1:$port/actuator/health" -TimeoutSec 5).status -eq 'UP') "final health $port"
}

[pscustomobject]@{
    pass = $Label
    flows = @('3.1-3.2 registration/auth', '3.3-3.4 catalog/search', '3.5 cart', '3.6 checkout/VNPay/compensation',
        '3.7 order/payout', '3.8 promotion', '3.9 social', '3.10 dispute/report', '3.11 admin/banner')
    orderId = $checkout.codOrderId
    completedOrderId = $order.completedOrderId
    disputeId = $dispute.fullRefundDispute
    reportId = $report.productReport
    security = 'direct=401, discovery=404, notification=401'
    result = 'CLEAN'
} | ConvertTo-Json -Depth 5
