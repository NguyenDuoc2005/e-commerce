param(
 [string]$Label="PROMO1",
 [Parameter(Mandatory=$true)][string]$SellerId,
 [Parameter(Mandatory=$true)][string]$VariantA,
 [Parameter(Mandatory=$true)][string]$VariantWithdraw,
 [Parameter(Mandatory=$true)][string]$VariantReject
)

$ErrorActionPreference="Stop"
$base="http://localhost:8080"
$variantOtherSeller="38000000-0000-0000-0000-000000000011"

function Assert-True{param([bool]$Condition,[string]$Message);if(-not $Condition){throw "ASSERT: $Message"}}
function Invoke-Json{
 param([string]$Method,[string]$Uri,[hashtable]$Headers=@{},[object]$Body=$null)
 $p=@{Method=$Method;Uri=$Uri;Headers=$Headers;TimeoutSec=20};if($null-ne $Body){$p.ContentType='application/json';$p.Body=ConvertTo-Json -InputObject $Body -Depth 10};Invoke-RestMethod @p
}
function Invoke-Form{param([string]$Uri,[hashtable]$Headers,[hashtable]$Body);Invoke-RestMethod -Method Post -Uri $Uri -Headers $Headers -Body $Body -TimeoutSec 20}
function Get-Code{
 param([string]$Method,[string]$Uri,[hashtable]$Headers=@{},[object]$Body=$null,[string]$ContentType='application/json')
 try{$p=@{Method=$Method;Uri=$Uri;Headers=$Headers;TimeoutSec=20;UseBasicParsing=$true};if($null-ne $Body){$p.ContentType=$ContentType;$p.Body=if($ContentType-eq 'application/json'){ConvertTo-Json -InputObject $Body -Depth 10}else{$Body}};[int](Invoke-WebRequest @p).StatusCode}catch{[int]$_.Exception.Response.StatusCode}
}

$seller=(Invoke-Json POST "$base/api/v1/auth/login" @{} @{email='e2e.approve.reg2@ecommerce.local';password='Buyer123!'}).data
$admin=(Invoke-Json POST "$base/api/v1/auth/login-admin" @{} @{email='admin@ecommerce.local';password='Admin@123'}).data
$sellerHeaders=@{Authorization="Bearer $($seller.accessToken)"};$adminHeaders=@{Authorization="Bearer $($admin.accessToken)"}
$today=(Get-Date).ToString('yyyy-MM-dd');$future=(Get-Date).AddMonths(2).ToString('yyyy-MM-dd')

$platformForm=@{code="PLAT$Label";name="Platform $Label";loiPhanNay='8';quantity='12';startDate=$today;endDate=$future;conditionAmount='100000';maxDiscountAmount='40000';discountType='false';discountMethod='true'}
$platform=(Invoke-Form "$base/api/v1/admin/voucher" $adminHeaders $platformForm).data
Assert-True ($null-eq $platform.sellerId) "admin voucher is platform scoped"
$platformForm.id=$platform.id;$platformForm.name="Platform $Label Updated";$platformUpdated=(Invoke-Form "$base/api/v1/admin/voucher" $adminHeaders $platformForm).data
Assert-True ($platformUpdated.name-like '*Updated') "admin voucher update"
$sellerAttack=@{}+$platformForm
Assert-True ((Get-Code POST "$base/api/v1/seller/vouchers" $sellerHeaders $sellerAttack 'application/x-www-form-urlencoded')-in 400,403) "seller cannot update platform voucher"
Assert-True ((Get-Code PUT "$base/api/v1/seller/vouchers/$($platform.id)/change-status" $sellerHeaders)-in 400,403) "seller cannot change platform voucher status"

$shopForm=@{code="SHOP$Label";name="Shop $Label";loiPhanNay='9';quantity='10';startDate=$today;endDate=$future;conditionAmount='50000';maxDiscountAmount='30000';discountType='false';discountMethod='true'}
$shop=(Invoke-Form "$base/api/v1/seller/vouchers" $sellerHeaders $shopForm).data
Assert-True ($shop.sellerId-eq $SellerId) "seller voucher scope"
$shopForm.id=$shop.id;$shopForm.name="Shop $Label Updated";$shopUpdated=(Invoke-Form "$base/api/v1/seller/vouchers" $sellerHeaders $shopForm).data
Assert-True ($shopUpdated.name-like '*Updated') "seller voucher update"
Invoke-Json PUT "$base/api/v1/seller/vouchers/$($shop.id)/change-status" $sellerHeaders|Out-Null

$now=[DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()
$campaign=Invoke-Json POST "$base/api/v1/seller/promotions" $sellerHeaders @{
 name="Seller Campaign $Label";value=10;startDate=$now;endDate=$now+86400000;idProductDetails=@(@{id=$variantA})
}
Assert-True ($campaign.sellerId-eq $SellerId) "seller campaign scope"
Assert-True ((Get-Code POST "$base/api/v1/seller/promotions" $sellerHeaders @{name="Invalid Other $Label";value=10;startDate=$now;endDate=$now+86400000;idProductDetails=@(@{id=$variantOtherSeller})})-in 400,403) "campaign variant ownership"

$registrationStart=$now-60000;$registrationEnd=$now+12000;$saleStart=$now+15000;$saleEnd=$now+120000
$flash=Invoke-Json POST "$base/api/v1/admin/flash-sales" $adminHeaders @{
 name="Flash $Label";description='E2E';registrationStartDate=$registrationStart;registrationEndDate=$registrationEnd;startDate=$saleStart;endDate=$saleEnd
}
Assert-True ([bool]$flash.registrationOpen) "flash sale registration window"
Assert-True ((Get-Code POST "$base/api/v1/seller/flash-sales/$($flash.id)/registrations" $sellerHeaders @{productVariantId=$variantOtherSeller;flashPrice=100000})-eq 403) "flash registration ownership"
$approved=Invoke-Json POST "$base/api/v1/seller/flash-sales/$($flash.id)/registrations" $sellerHeaders @{productVariantId=$variantA;flashPrice=90000}
$withdrawn=Invoke-Json POST "$base/api/v1/seller/flash-sales/$($flash.id)/registrations" $sellerHeaders @{productVariantId=$variantWithdraw;flashPrice=85000}
$rejected=Invoke-Json POST "$base/api/v1/seller/flash-sales/$($flash.id)/registrations" $sellerHeaders @{productVariantId=$variantReject;flashPrice=80000}
$approved=Invoke-Json POST "$base/api/v1/admin/flash-sales/$($flash.id)/registrations/$($approved.id)/review" $adminHeaders @{decision='APPROVE'}
$withdrawn=Invoke-Json POST "$base/api/v1/admin/flash-sales/$($flash.id)/registrations/$($withdrawn.id)/review" $adminHeaders @{decision='APPROVE'}
$rejected=Invoke-Json POST "$base/api/v1/admin/flash-sales/$($flash.id)/registrations/$($rejected.id)/review" $adminHeaders @{decision='REJECT';reason="E2E reject $Label"}
$withdrawn=Invoke-Json POST "$base/api/v1/seller/flash-sales/$($flash.id)/registrations/$($withdrawn.id)/withdraw" $sellerHeaders
Assert-True ($withdrawn.registrationStatus-eq 'WITHDRAWN' -and $rejected.registrationStatus-eq 'REJECTED') "withdraw before start and reject"
$waitMs=[Math]::Max(0,$saleStart-[DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()+500)
if($waitMs-gt 0){Start-Sleep -Milliseconds $waitMs}
Assert-True ((Get-Code POST "$base/api/v1/seller/flash-sales/$($flash.id)/registrations/$($approved.id)/withdraw" $sellerHeaders)-eq 400) "approved registration cannot withdraw after start"
$public=Invoke-Json GET "$base/api/v1/permitall/flash-sales"
$publicFlash=@($public)|Where-Object id -eq $flash.id|Select-Object -First 1
$publicIds=@($publicFlash.products.productVariantId)
Assert-True ($publicIds-contains $variantA -and $publicIds-notcontains $variantWithdraw -and $publicIds-notcontains $variantReject) "public flash sale contains approved only"

[pscustomobject]@{label=$Label;platformVoucherId=$platform.id;shopVoucherId=$shop.id;sellerCampaignId=$campaign.id;flashSaleId=$flash.id;approvedRegistrationId=$approved.id;result='PASS'}|ConvertTo-Json
