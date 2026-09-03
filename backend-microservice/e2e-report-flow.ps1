param(
 [string]$Label='REPORT1',
 [Parameter(Mandatory=$true)][string]$ProductId,
 [Parameter(Mandatory=$true)][string]$ShopId,
 [Parameter(Mandatory=$true)][string]$ShopSlug,
 [Parameter(Mandatory=$true)][string]$ReviewId,
 [Parameter(Mandatory=$true)][string]$UserId
)

$ErrorActionPreference='Stop';$base='http://localhost:8080'
function Assert-True{param([bool]$Condition,[string]$Message);if(-not $Condition){throw "ASSERT: $Message"}}
function Invoke-Api{param([string]$Method,[string]$Uri,[hashtable]$Headers=@{},[object]$Body=$null);$p=@{Method=$Method;Uri=$Uri;Headers=$Headers;TimeoutSec=20};if($null-ne $Body){$p.ContentType='application/json';$p.Body=ConvertTo-Json -InputObject $Body -Depth 8};Invoke-RestMethod @p}
function Get-Code{param([string]$Method,[string]$Uri,[hashtable]$Headers=@{},[object]$Body=$null);try{$p=@{Method=$Method;Uri=$Uri;Headers=$Headers;TimeoutSec=20;UseBasicParsing=$true};if($null-ne $Body){$p.ContentType='application/json';$p.Body=ConvertTo-Json -InputObject $Body -Depth 8};[int](Invoke-WebRequest @p).StatusCode}catch{[int]$_.Exception.Response.StatusCode}}
function Create-Resolve{param([string]$Endpoint,[hashtable]$ReporterHeaders,[string]$TargetType,[string]$TargetId,[string]$Action);$reason=switch($TargetType){'PRODUCT'{'FAKE_PRODUCT'}'SHOP'{'SCAM'}'REVIEW'{'FAKE_REVIEW'}'USER'{'OFFENSIVE_CONTENT'}default{'OTHER'}};$report=Invoke-Api POST "$base$Endpoint" $ReporterHeaders @{targetType=$TargetType;targetId=$TargetId;reasonCode=$reason;description="E2E $TargetType $Label";evidenceUrls=@()};Invoke-Api POST "$base/api/v1/admin/reports/$($report.id)/review" $adminHeaders|Out-Null;$resolved=Invoke-Api POST "$base/api/v1/admin/reports/$($report.id)/resolve" $adminHeaders @{actionTaken=$Action;note="$Action $Label"};[pscustomobject]@{report=$report;resolved=$resolved}}

$buyer=(Invoke-Api POST "$base/api/v1/auth/login" @{} @{email='e2e.buyer2@ecommerce.local';password='Buyer123'}).data;$seller=(Invoke-Api POST "$base/api/v1/auth/login" @{} @{email='e2e.approve.reg2@ecommerce.local';password='Buyer123!'}).data;$admin=(Invoke-Api POST "$base/api/v1/auth/login-admin" @{} @{email='admin@ecommerce.local';password='Admin@123'}).data
$buyerHeaders=@{Authorization="Bearer $($buyer.accessToken)"};$sellerHeaders=@{Authorization="Bearer $($seller.accessToken)"};$adminHeaders=@{Authorization="Bearer $($admin.accessToken)"}

Write-Host 'STEP product report'
$productReport=Create-Resolve '/api/v1/buyer/reports' $buyerHeaders 'PRODUCT' $ProductId 'PRODUCT_DELISTED';Assert-True ($productReport.resolved.status-eq 'ACTION_TAKEN' -and (Get-Code GET "$base/api/v1/permitall/products/$ProductId")-eq 404) 'product report delists public product'
Invoke-Api PUT "$base/api/v1/seller/products/$ProductId/status" $sellerHeaders @{status='ACTIVE'}|Out-Null

Write-Host 'STEP shop report'
$shopReport=Create-Resolve '/api/v1/buyer/reports' $buyerHeaders 'SHOP' $ShopId 'SHOP_SUSPENDED';Assert-True ($shopReport.resolved.status-eq 'ACTION_TAKEN' -and (Get-Code GET "$base/api/v1/permitall/shops/$ShopSlug")-eq 404 -and (Get-Code GET "$base/api/v1/seller/profile" $sellerHeaders)-eq 403) 'shop report suspends shop and invalidates old token'
Invoke-Api POST "$base/api/v1/admin/sellers/$ShopId/reopen" $adminHeaders|Out-Null
$seller=(Invoke-Api POST "$base/api/v1/auth/login" @{} @{email='e2e.approve.reg2@ecommerce.local';password='Buyer123!'}).data;$sellerHeaders=@{Authorization="Bearer $($seller.accessToken)"}

Write-Host 'STEP review report'
$reviewReport=Create-Resolve '/api/v1/buyer/reports' $buyerHeaders 'REVIEW' $ReviewId 'REVIEW_HIDDEN';$publicReviews=(Invoke-Api GET "$base/api/v1/permitall/reviews?productId=$ProductId").data;Assert-True ($reviewReport.resolved.status-eq 'ACTION_TAKEN' -and @($publicReviews.id)-notcontains $ReviewId) 'review report hides public review'
Write-Host 'STEP user report'
$userReport=Create-Resolve '/api/v1/seller/reports' $sellerHeaders 'USER' $UserId 'WARNING_SENT';Assert-True ($userReport.resolved.status-eq 'ACTION_TAKEN') 'user warning action'
Write-Host 'STEP dismissed report'
$dismissed=Create-Resolve '/api/v1/buyer/reports' $buyerHeaders 'PRODUCT' $ProductId 'NO_ACTION';Assert-True ($dismissed.resolved.status-eq 'DISMISSED' -and (Get-Code GET "$base/api/v1/permitall/products/$ProductId")-eq 200) 'no-action dismisses without changing target'

[pscustomobject]@{label=$Label;productReport=$productReport.report.id;shopReport=$shopReport.report.id;reviewReport=$reviewReport.report.id;userReport=$userReport.report.id;dismissedReport=$dismissed.report.id;result='PASS'}|ConvertTo-Json
