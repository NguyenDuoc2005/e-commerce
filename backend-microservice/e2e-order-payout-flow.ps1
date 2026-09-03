param(
 [string]$Label="ORDER1",
 [Parameter(Mandatory=$true)][string]$VariantA,
 [Parameter(Mandatory=$true)][string]$SellerAId
)

$ErrorActionPreference="Stop"
$base="http://localhost:8080"
$variantB="38000000-0000-0000-0000-000000000011"

function Assert-True{param([bool]$Condition,[string]$Message);if(-not $Condition){throw "ASSERT: $Message"}}
function Invoke-Api{
 param([string]$Method,[string]$Uri,[hashtable]$Headers=@{},[object]$Body=$null)
 $p=@{Method=$Method;Uri=$Uri;Headers=$Headers;TimeoutSec=20}
 if($null-ne $Body){$p.ContentType="application/json";$p.Body=ConvertTo-Json -InputObject $Body -Depth 10}
 Invoke-RestMethod @p
}
function Get-Code{
 param([string]$Method,[string]$Uri,[hashtable]$Headers=@{},[object]$Body=$null)
 try{$p=@{Method=$Method;Uri=$Uri;Headers=$Headers;TimeoutSec=20;UseBasicParsing=$true};if($null-ne $Body){$p.ContentType="application/json";$p.Body=ConvertTo-Json -InputObject $Body -Depth 10};[int](Invoke-WebRequest @p).StatusCode}catch{[int]$_.Exception.Response.StatusCode}
}
function Sql-Scalar{
 param([string]$Sql)
 $compose=Join-Path $PSScriptRoot 'docker-compose.yml'
 $value=& docker compose -f $compose exec -T -e MYSQL_PWD=12345678 mysql mysql -uroot --batch --skip-column-names -e $Sql
 if($LASTEXITCODE-ne 0){throw 'mysql query failed'}
 $value
}
function New-Order{
 param([object[]]$Items,[string]$Suffix)
 Invoke-Api POST "$base/api/orders/create" $buyerHeaders @{
  hoTen="Order $Suffix";soDienThoai="0399999400";address="E2E state";email="e2e.approve.reg2@ecommerce.local"
  hinhThucThanhToan="COD";tongTien=1;phiShip=1;giamGia=1;tongCong=1;product=$Items
 }
}
function Get-SubOrder{
 param([string]$Code,[hashtable]$Headers)
 @((Invoke-Api GET "$base/api/v1/seller/orders?q=$Code" $Headers))[0]
}
function Act{param([string]$SubId,[string]$Action,[hashtable]$Headers);Invoke-Api POST "$base/api/v1/seller/orders/$SubId/$Action" $Headers|Out-Null}

$buyer=(Invoke-Api POST "$base/api/v1/auth/login" @{} @{email="e2e.approve.reg2@ecommerce.local";password="Buyer123!"}).data
$sellerB=(Invoke-Api POST "$base/api/v1/auth/login" @{} @{email="customer2@ecommerce.local";password="Admin@123"}).data
$admin=(Invoke-Api POST "$base/api/v1/auth/login-admin" @{} @{email="admin@ecommerce.local";password="Admin@123"}).data
$buyerHeaders=@{Authorization="Bearer $($buyer.accessToken)"}
$sellerAHeaders=$buyerHeaders
$sellerBHeaders=@{Authorization="Bearer $($sellerB.accessToken)"}
$adminHeaders=@{Authorization="Bearer $($admin.accessToken)"}

$stockStart=[int](Sql-Scalar "SELECT quantity FROM ecommerce_catalog.product_variant WHERE id='$variantA';")

$cancelWaiting=New-Order @(@{id=$variantA;quantity=1}) "$Label-waiting"
$sub=Get-SubOrder $cancelWaiting.code $sellerAHeaders
Act $sub.id 'cancel' $sellerAHeaders
Assert-True ([int](Sql-Scalar "SELECT order_status FROM ecommerce_order.orders WHERE id='$($cancelWaiting.id)';")-eq 5) "cancel from waiting"

$cancelConfirmed=New-Order @(@{id=$variantA;quantity=1}) "$Label-confirmed"
$sub=Get-SubOrder $cancelConfirmed.code $sellerAHeaders
Assert-True ((Get-Code POST "$base/api/v1/seller/orders/$($sub.id)/ready-to-ship" $sellerAHeaders)-eq 400) "invalid skip transition"
Act $sub.id 'confirm' $sellerAHeaders
Assert-True ((Get-Code POST "$base/api/v1/seller/orders/$($sub.id)/confirm" $sellerAHeaders)-eq 400) "duplicate transition rejected"
Act $sub.id 'cancel' $sellerAHeaders

$cancelReady=New-Order @(@{id=$variantA;quantity=1}) "$Label-ready"
$sub=Get-SubOrder $cancelReady.code $sellerAHeaders
Act $sub.id 'confirm' $sellerAHeaders;Act $sub.id 'ready-to-ship' $sellerAHeaders;Act $sub.id 'cancel' $sellerAHeaders

$cancelShipping=New-Order @(@{id=$variantA;quantity=1}) "$Label-shipping"
$sub=Get-SubOrder $cancelShipping.code $sellerAHeaders
Act $sub.id 'confirm' $sellerAHeaders;Act $sub.id 'ready-to-ship' $sellerAHeaders;Act $sub.id 'shipping' $sellerAHeaders;Act $sub.id 'cancel' $sellerAHeaders
Assert-True ([int](Sql-Scalar "SELECT COUNT(*) FROM ecommerce_order.orders WHERE id IN ('$($cancelWaiting.id)','$($cancelConfirmed.id)','$($cancelReady.id)','$($cancelShipping.id)') AND order_status=5;")-eq 4) "all valid seller cancellations aggregate root"
Assert-True ([int](Sql-Scalar "SELECT quantity FROM ecommerce_catalog.product_variant WHERE id='$variantA';")-eq $stockStart) "seller cancellation restores stock"

$mixed=New-Order @(@{id=$variantA;quantity=1},@{id=$variantB;quantity=1}) "$Label-mixed"
$mixedA=Get-SubOrder $mixed.code $sellerAHeaders
$mixedB=Get-SubOrder $mixed.code $sellerBHeaders
Act $mixedA.id 'confirm' $sellerAHeaders
$buyerCancel=Invoke-Api PUT "$base/api/v1/buyer/orders/change-status?maOrder=$($mixed.code)&status=DA_HUY&note=blocked" $buyerHeaders
Assert-True ($buyerCancel.status-eq 'BAD_REQUEST') "buyer cannot cancel after one seller progressed"
Assert-True ([int](Sql-Scalar "SELECT order_status FROM ecommerce_order.orders WHERE id='$($mixed.id)';")-ne 5) "blocked buyer cancel preserves order"
Act $mixedA.id 'cancel' $sellerAHeaders;Act $mixedB.id 'cancel' $sellerBHeaders

$walletPendingBefore=[double](Sql-Scalar "SELECT COALESCE((SELECT pending_amount FROM ecommerce_payout.seller_wallet WHERE seller_id='$SellerAId'),0);")
$complete=New-Order @(@{id=$variantA;quantity=1}) "$Label-complete"
$completedSub=Get-SubOrder $complete.code $sellerAHeaders
foreach($action in @('confirm','ready-to-ship','shipping','complete')){Act $completedSub.id $action $sellerAHeaders}
Assert-True ((Get-Code POST "$base/api/v1/seller/orders/$($completedSub.id)/cancel" $sellerAHeaders)-eq 400) "completed order cannot cancel"
Assert-True ([int](Sql-Scalar "SELECT order_status FROM ecommerce_order.orders WHERE id='$($complete.id)';")-eq 4) "completed root aggregation"
$receivableId=[string](Sql-Scalar "SELECT id FROM ecommerce_payout.seller_receivable WHERE order_seller_id='$($completedSub.id)';")
Assert-True (-not [string]::IsNullOrWhiteSpace($receivableId)) "receivable created"
$gross=[double](Sql-Scalar "SELECT gross_amount FROM ecommerce_payout.seller_receivable WHERE id='$receivableId';")
$commission=[double](Sql-Scalar "SELECT commission_amount FROM ecommerce_payout.seller_receivable WHERE id='$receivableId';")
$net=[double](Sql-Scalar "SELECT net_amount FROM ecommerce_payout.seller_receivable WHERE id='$receivableId';")
Assert-True ([Math]::Abs($commission-($gross*0.05))-lt 0.01 -and [Math]::Abs($net-($gross-$commission))-lt 0.01) "default five percent commission"
$walletPendingAfter=[double](Sql-Scalar "SELECT pending_amount FROM ecommerce_payout.seller_wallet WHERE seller_id='$SellerAId';")
Assert-True ([Math]::Abs(($walletPendingAfter-$walletPendingBefore)-$net)-lt 0.01) "wallet pending increment"

Sql-Scalar "UPDATE ecommerce_payout.seller_receivable SET available_at=DATE_SUB(UTC_TIMESTAMP(),INTERVAL 1 SECOND) WHERE id='$receivableId'; SELECT ROW_COUNT();"|Out-Null
Invoke-Api POST "$base/api/v1/admin/payout/receivables/release-eligible" $adminHeaders|Out-Null
Assert-True ((Sql-Scalar "SELECT status FROM ecommerce_payout.seller_receivable WHERE id='$receivableId';")-eq 'AVAILABLE') "scheduler release pending to available"
$availableBeforePay=[double](Sql-Scalar "SELECT available_amount FROM ecommerce_payout.seller_wallet WHERE seller_id='$SellerAId';")
$paidBefore=[double](Sql-Scalar "SELECT paid_amount FROM ecommerce_payout.seller_wallet WHERE seller_id='$SellerAId';")
$batch=Invoke-Api POST "$base/api/v1/admin/payout/batches" $adminHeaders @{receivableIds=@($receivableId);note="E2E $Label"}
Assert-True ((Sql-Scalar "SELECT status FROM ecommerce_payout.seller_receivable WHERE id='$receivableId';")-eq 'PAID') "payout batch marks paid"
$availableAfterPay=[double](Sql-Scalar "SELECT available_amount FROM ecommerce_payout.seller_wallet WHERE seller_id='$SellerAId';")
$paidAfter=[double](Sql-Scalar "SELECT paid_amount FROM ecommerce_payout.seller_wallet WHERE seller_id='$SellerAId';")
Assert-True ([Math]::Abs(($availableBeforePay-$availableAfterPay)-$net)-lt 0.01 -and [Math]::Abs(($paidAfter-$paidBefore)-$net)-lt 0.01) "wallet available to paid"

[pscustomobject]@{label=$Label;cancelledOrders=5;completedOrderId=$complete.id;receivableId=$receivableId;gross=$gross;commission=$commission;net=$net;payoutBatchId=$batch.id;result='PASS'}|ConvertTo-Json
