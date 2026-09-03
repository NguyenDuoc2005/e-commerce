param(
    [Parameter(Mandatory = $true)]
    [string]$Label
)

$ErrorActionPreference = "Stop"
$base = "http://localhost:8080"
$password = "Buyer123!"
$suffix = $Label.ToLowerInvariant()

function Assert-True {
    param([bool]$Condition, [string]$Message)
    if (-not $Condition) { throw "ASSERT: $Message" }
}

function Invoke-Api {
    param([string]$Method, [string]$Uri, [hashtable]$Headers = @{}, [object]$Body = $null)
    $parameters = @{ Method = $Method; Uri = $Uri; Headers = $Headers; TimeoutSec = 15 }
    if ($null -ne $Body) {
        $parameters.ContentType = "application/json"
        $parameters.Body = $Body | ConvertTo-Json -Depth 8
    }
    Invoke-RestMethod @parameters
}

function Get-HttpCode {
    param([string]$Method, [string]$Uri, [hashtable]$Headers = @{}, [object]$Body = $null)
    try {
        $parameters = @{ Method = $Method; Uri = $Uri; Headers = $Headers; TimeoutSec = 15; UseBasicParsing = $true }
        if ($null -ne $Body) {
            $parameters.ContentType = "application/json"
            $parameters.Body = $Body | ConvertTo-Json -Depth 8
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

function Register-Buyer {
    param([string]$Name, [string]$Email, [string]$Phone)
    Invoke-Api PUT "$base/api/v1/auth/register" @{} @{
        userName = $Name; email = $Email; phone = $Phone; password = $password
    } | Out-Null
    (Invoke-Api POST "$base/api/v1/auth/login" @{} @{ email = $Email; password = $password }).data
}

function Shop-Body {
    param([string]$Name, [string]$Slug, [string]$Identity)
    @{
        shopName = $Name; sellerSlug = $Slug; description = "E2E registration $Label"
        pickupAddress = "E2E Address"; contactPhone = "0399999100"; identityType = "CCCD"
        identityNumber = $Identity; bankName = "E2E Bank"; bankAccountNo = $Identity
        bankAccountHolder = "E2E OWNER"
    }
}

$admin = (Invoke-Api POST "$base/api/v1/auth/login-admin" @{} @{
    email = "admin@ecommerce.local"; password = "Admin@123"
}).data
$adminHeaders = @{ Authorization = "Bearer $($admin.accessToken)" }

$approveEmail = "e2e.approve.$suffix@ecommerce.local"
$rejectEmail = "e2e.reject.$suffix@ecommerce.local"
$approve = Register-Buyer "Approve $Label" $approveEmail ("0381" + (Get-Random -Minimum 100000 -Maximum 999999))
$reject = Register-Buyer "Reject $Label" $rejectEmail ("0382" + (Get-Random -Minimum 100000 -Maximum 999999))

$approveClaims = Decode-Claims $approve.accessToken
Assert-True ($approveClaims.role -eq "USERS" -and @($approveClaims.roles) -notcontains "SELLER") "new buyer JWT"
$refreshed = (Invoke-Api POST "$base/api/v1/auth/refresh" @{} @{ refreshToken = $approve.refreshToken }).data
Assert-True ((Decode-Claims $refreshed.accessToken).tokenType -eq "ACCESS") "refresh access token type"
Assert-True ((Get-HttpCode POST "$base/api/v1/auth/refresh" @{} @{ refreshToken = $approve.accessToken }) -eq 401) "access token cannot refresh"

$approveHeaders = @{ Authorization = "Bearer $($approve.accessToken)" }
$rejectHeaders = @{ Authorization = "Bearer $($reject.accessToken)" }
$approveShop = (Invoke-Api POST "$base/api/v1/sellers/register-shop" $approveHeaders (Shop-Body "Approve Shop $Label" "approve-shop-$suffix" "091$((Get-Random -Minimum 1000000 -Maximum 9999999))")).data
$rejectShop = (Invoke-Api POST "$base/api/v1/sellers/register-shop" $rejectHeaders (Shop-Body "Reject Shop $Label" "reject-shop-$suffix" "092$((Get-Random -Minimum 1000000 -Maximum 9999999))")).data
Assert-True ($approveShop.status -eq "PENDING_APPROVAL" -and $rejectShop.status -eq "PENDING_APPROVAL") "shop starts pending"
$duplicateStatus = Get-HttpCode POST "$base/api/v1/sellers/register-shop" $approveHeaders (Shop-Body "Duplicate $Label" "duplicate-$suffix" "093$((Get-Random -Minimum 1000000 -Maximum 9999999))")
Assert-True ($duplicateStatus -in 400, 409) "duplicate active application"

Invoke-Api POST "$base/api/v1/admin/sellers/$($approveShop.id)/approve" $adminHeaders | Out-Null
Invoke-Api POST "$base/api/v1/admin/sellers/$($rejectShop.id)/reject" $adminHeaders @{ reason = "E2E reject $Label" } | Out-Null

$approvedLogin = (Invoke-Api POST "$base/api/v1/auth/login" @{} @{ email = $approveEmail; password = $password }).data
$rejectedLogin = (Invoke-Api POST "$base/api/v1/auth/login" @{} @{ email = $rejectEmail; password = $password }).data
$approvedClaims = Decode-Claims $approvedLogin.accessToken
$rejectedClaims = Decode-Claims $rejectedLogin.accessToken
Assert-True (@($approvedClaims.roles) -contains "SELLER" -and $approvedClaims.sellerId -eq $approveShop.id) "approved JWT seller context"
Assert-True (@($rejectedClaims.roles) -notcontains "SELLER" -and $null -eq $rejectedClaims.sellerId) "rejected JWT no seller role"
Assert-True ((Get-HttpCode GET "$base/api/v1/seller/orders" @{ Authorization = "Bearer $($rejectedLogin.accessToken)" }) -eq 403) "rejected buyer blocked from seller orders"

[pscustomobject]@{
    label = $Label
    approvedEmail = $approveEmail
    approvedCustomerId = $approvedClaims.userId
    approvedSellerId = $approveShop.id
    rejectedEmail = $rejectEmail
    rejectedCustomerId = $rejectedClaims.userId
    rejectedSellerId = $rejectShop.id
    result = "PASS"
} | ConvertTo-Json
