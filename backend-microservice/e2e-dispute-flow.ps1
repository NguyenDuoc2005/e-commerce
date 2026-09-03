param(
 [string]$Label='DISPUTE1',
 [Parameter(Mandatory=$true)][string]$SellerId,
 [Parameter(Mandatory=$true)][string]$Variant
)

$ErrorActionPreference='Stop';$base='http://localhost:8080'
function Assert-True{param([bool]$Condition,[string]$Message);if(-not $Condition){throw "ASSERT: $Message"}}
function Invoke-Api{param([string]$Method,[string]$Uri,[hashtable]$Headers=@{},[object]$Body=$null);$p=@{Method=$Method;Uri=$Uri;Headers=$Headers;TimeoutSec=20};if($null-ne $Body){$p.ContentType='application/json';$p.Body=ConvertTo-Json -InputObject $Body -Depth 8};Invoke-RestMethod @p}
function Sql-Scalar{param([string]$Sql);$v=& docker compose -f (Join-Path $PSScriptRoot 'docker-compose.yml') exec -T -e MYSQL_PWD=12345678 mysql mysql -uroot --batch --skip-column-names -e $Sql;if($LASTEXITCODE-ne 0){throw 'mysql failed'};$v}
function New-CompletedOrder{
 param([string]$Suffix)
 $order=Invoke-Api POST "$base/api/orders/create" $buyerHeaders @{hoTen="Dispute $Suffix";soDienThoai='0399999500';address='E2E';email='e2e.approve.reg2@ecommerce.local';hinhThucThanhToan='COD';tongTien=1;phiShip=1;giamGia=1;tongCong=1;product=@(@{id=$Variant;quantity=1})}
 $sub=@((Invoke-Api GET "$base/api/v1/seller/orders?q=$($order.code)" $sellerHeaders))[0]
 foreach($action in @('confirm','ready-to-ship','shipping','complete')){Invoke-Api POST "$base/api/v1/seller/orders/$($sub.id)/$action" $sellerHeaders|Out-Null}
 [pscustomobject]@{order=$order;sub=$sub}
}
function Resolve-Flow{
 param([string]$SubOrderId,[string]$Decision,[object]$Amount,[string]$Suffix)
 $requested=if($null-ne $Amount){[double]$Amount}else{1000D}
 $dispute=Invoke-Api POST "$base/api/v1/buyer/disputes" $buyerHeaders @{orderSellerId=$SubOrderId;disputeType='OTHER';reason="Reason $Suffix";description="E2E $Label";evidenceUrls=@();requestedAmount=$requested}
 Invoke-Api POST "$base/api/v1/seller/disputes/$($dispute.id)/respond" $sellerHeaders @{message="Seller response $Suffix";attachmentUrls=@()}|Out-Null
 Invoke-Api POST "$base/api/v1/admin/disputes/$($dispute.id)/take-review" $adminHeaders|Out-Null
 $body=@{decision=$Decision;note="Admin $Suffix"};if($null-ne $Amount){$body.resolvedAmount=[double]$Amount}
 $resolved=Invoke-Api POST "$base/api/v1/admin/disputes/$($dispute.id)/resolve" $adminHeaders $body
 [pscustomobject]@{dispute=$dispute;resolved=$resolved}
}

$buyer=(Invoke-Api POST "$base/api/v1/auth/login" @{} @{email='e2e.approve.reg2@ecommerce.local';password='Buyer123!'}).data;$admin=(Invoke-Api POST "$base/api/v1/auth/login-admin" @{} @{email='admin@ecommerce.local';password='Admin@123'}).data
$buyerHeaders=@{Authorization="Bearer $($buyer.accessToken)"};$sellerHeaders=$buyerHeaders;$adminHeaders=@{Authorization="Bearer $($admin.accessToken)"}

$paidOrder=New-CompletedOrder "$Label-paid";$paidSubOrder=$paidOrder.sub.id;$paidReceivable=[string](Sql-Scalar "SELECT id FROM ecommerce_payout.seller_receivable WHERE order_seller_id='$paidSubOrder';")
Sql-Scalar "UPDATE ecommerce_payout.seller_receivable SET available_at=DATE_SUB(UTC_TIMESTAMP(),INTERVAL 1 SECOND) WHERE id='$paidReceivable';SELECT ROW_COUNT();"|Out-Null;Invoke-Api POST "$base/api/v1/admin/payout/receivables/release-eligible" $adminHeaders|Out-Null
Invoke-Api POST "$base/api/v1/admin/payout/batches" $adminHeaders @{receivableIds=@($paidReceivable);note="Dispute paid fixture $Label"}|Out-Null
Assert-True ((Sql-Scalar "SELECT status FROM ecommerce_payout.seller_receivable WHERE id='$paidReceivable';")-eq 'PAID') 'paid receivable fixture'
$paidGross=[double](Sql-Scalar "SELECT gross_amount FROM ecommerce_payout.seller_receivable WHERE id='$paidReceivable';")
$pendingBeforePaidRefund=[double](Sql-Scalar "SELECT pending_amount FROM ecommerce_payout.seller_wallet WHERE seller_id='$SellerId';")
$paidFlow=Resolve-Flow $paidSubOrder 'REFUND_BUYER' $null "$Label-paid"
$paidAdjustment=[double](Sql-Scalar "SELECT amount FROM ecommerce_payout.payout_adjustment WHERE dispute_id='$($paidFlow.dispute.id)';")
$pendingAfterPaidRefund=[double](Sql-Scalar "SELECT pending_amount FROM ecommerce_payout.seller_wallet WHERE seller_id='$SellerId';")
Assert-True ($paidFlow.resolved.status-eq 'RESOLVED_REFUND_BUYER' -and $paidAdjustment-eq -$paidGross -and [Math]::Abs(($pendingAfterPaidRefund-$pendingBeforePaidRefund)+$paidGross)-lt 0.01) 'full refund adjusts already-paid wallet debt'

$availableOrder=New-CompletedOrder "$Label-available";$availableReceivable=[string](Sql-Scalar "SELECT id FROM ecommerce_payout.seller_receivable WHERE order_seller_id='$($availableOrder.sub.id)';")
Sql-Scalar "UPDATE ecommerce_payout.seller_receivable SET available_at=DATE_SUB(UTC_TIMESTAMP(),INTERVAL 1 SECOND) WHERE id='$availableReceivable';SELECT ROW_COUNT();"|Out-Null;Invoke-Api POST "$base/api/v1/admin/payout/receivables/release-eligible" $adminHeaders|Out-Null
$availableBefore=[double](Sql-Scalar "SELECT available_amount FROM ecommerce_payout.seller_wallet WHERE seller_id='$SellerId';");$pendingBeforeAvailable=[double](Sql-Scalar "SELECT pending_amount FROM ecommerce_payout.seller_wallet WHERE seller_id='$SellerId';");$netBefore=[double](Sql-Scalar "SELECT net_amount FROM ecommerce_payout.seller_receivable WHERE id='$availableReceivable';")
$partialFlow=Resolve-Flow $availableOrder.sub.id 'PARTIAL_REFUND' 50000 "$Label-available"
$availableAfter=[double](Sql-Scalar "SELECT available_amount FROM ecommerce_payout.seller_wallet WHERE seller_id='$SellerId';");$pendingAfterAvailable=[double](Sql-Scalar "SELECT pending_amount FROM ecommerce_payout.seller_wallet WHERE seller_id='$SellerId';");$netAfter=[double](Sql-Scalar "SELECT net_amount FROM ecommerce_payout.seller_receivable WHERE id='$availableReceivable';");$partialAdjustment=[double](Sql-Scalar "SELECT amount FROM ecommerce_payout.payout_adjustment WHERE dispute_id='$($partialFlow.dispute.id)';")
$walletReduction=($availableBefore+$pendingBeforeAvailable)-($availableAfter+$pendingAfterAvailable)
Assert-True ($partialFlow.resolved.status-eq 'RESOLVED_PARTIAL_REFUND' -and $partialAdjustment-eq -50000 -and [Math]::Abs($walletReduction-($netBefore-$netAfter))-lt 0.01) 'partial refund reduces available receivable/wallet'

$pendingOrder=New-CompletedOrder "$Label-pending";$pendingReceivable=[string](Sql-Scalar "SELECT id FROM ecommerce_payout.seller_receivable WHERE order_seller_id='$($pendingOrder.sub.id)';");$pendingWalletBefore=[double](Sql-Scalar "SELECT pending_amount FROM ecommerce_payout.seller_wallet WHERE seller_id='$SellerId';")
$rejectFlow=Resolve-Flow $pendingOrder.sub.id 'REJECT_BUYER' $null "$Label-pending";$pendingWalletAfter=[double](Sql-Scalar "SELECT pending_amount FROM ecommerce_payout.seller_wallet WHERE seller_id='$SellerId';");$rejectAdjustments=[int](Sql-Scalar "SELECT COUNT(*) FROM ecommerce_payout.payout_adjustment WHERE dispute_id='$($rejectFlow.dispute.id)';")
Assert-True ($rejectFlow.resolved.status-eq 'RESOLVED_REJECT_BUYER' -and $rejectAdjustments-eq 0 -and [Math]::Abs($pendingWalletAfter-$pendingWalletBefore)-lt 0.01) 'rejected dispute has no payout adjustment'
foreach($id in @($paidFlow.dispute.id,$partialFlow.dispute.id,$rejectFlow.dispute.id)){Invoke-Api POST "$base/api/v1/admin/disputes/$id/close" $adminHeaders|Out-Null}

[pscustomobject]@{label=$Label;fullRefundDispute=$paidFlow.dispute.id;fullAdjustment=$paidAdjustment;partialRefundDispute=$partialFlow.dispute.id;partialAdjustment=$partialAdjustment;rejectDispute=$rejectFlow.dispute.id;result='PASS'}|ConvertTo-Json
