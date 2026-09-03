param([string]$Label = "CHECK1")

$ErrorActionPreference="Stop"
$base="http://localhost:8080"
$sellerA="70000000-0000-0000-0000-000000000001"
$sellerB="70000000-0000-0000-0000-000000000002"
$variantA="38000000-0000-0000-0000-000000000001"
$variantLeave="38000000-0000-0000-0000-000000000002"
$variantVnpay="38000000-0000-0000-0000-000000000003"
$variantB="38000000-0000-0000-0000-000000000011"

function Assert-True{param([bool]$Condition,[string]$Message);if(-not $Condition){throw "ASSERT: $Message"}}
function Invoke-Api{
 param([string]$Method,[string]$Uri,[hashtable]$Headers=@{},[object]$Body=$null)
 $p=@{Method=$Method;Uri=$Uri;Headers=$Headers;TimeoutSec=15}
 if($null-ne $Body){$p.ContentType="application/json";$p.Body=ConvertTo-Json -InputObject $Body -Depth 10}
 Invoke-RestMethod @p
}
function Get-Code{
 param([string]$Method,[string]$Uri,[hashtable]$Headers=@{},[object]$Body=$null)
 try{$p=@{Method=$Method;Uri=$Uri;Headers=$Headers;TimeoutSec=15;UseBasicParsing=$true};if($null-ne $Body){$p.ContentType="application/json";$p.Body=ConvertTo-Json -InputObject $Body -Depth 10};[int](Invoke-WebRequest @p).StatusCode}catch{[int]$_.Exception.Response.StatusCode}
}
function Get-Stock{
 param([string]$ProductId,[string]$VariantId)
 $detail=Invoke-Api GET "$base/api/v1/permitall/products/$ProductId"
 [int]((@($detail.variants)|Where-Object id -eq $VariantId|Select-Object -First 1).quantity)
}
function Get-OrderStatus{
 param([string]$OrderId,[hashtable]$Headers)
 $compose=Join-Path $PSScriptRoot "docker-compose.yml"
 $sql="SELECT order_status FROM ecommerce_order.orders WHERE id='$OrderId';"
 $value=& docker compose -f $compose exec -T -e MYSQL_PWD=12345678 mysql mysql -uroot --batch --skip-column-names -e $sql
 if($LASTEXITCODE-ne 0 -or $null-eq $value){return -1}
 [int]$value
}
function Get-Redirect{
 param([string]$Uri)
 $result=& curl.exe --silent --output NUL --write-out '%{http_code}|%{redirect_url}' $Uri
 if($LASTEXITCODE-ne 0){throw "Callback request failed"}
 ($result -split '\|',2)[1]
}
function Encode-VnpValue{
 param([string]$Value)
 ([Uri]::EscapeDataString($Value).Replace('%20','+').Replace('~','%7E'))
}
function New-VNPayCallbackQuery{
 param([string]$PaymentUrl)
 Add-Type -AssemblyName System.Web
 $source=[System.Web.HttpUtility]::ParseQueryString(([Uri]$PaymentUrl).Query)
 $parameters=[ordered]@{}
 foreach($key in @($source.AllKeys|Where-Object {$_-and $_-ne 'vnp_SecureHash'}|Sort-Object)){$parameters[$key]=$source[$key]}
 $parameters['vnp_ResponseCode']='00'
 $sorted=[ordered]@{}
 foreach($key in @($parameters.Keys|Sort-Object)){$sorted[$key]=$parameters[$key]}
 $hashData=(@($sorted.GetEnumerator()|ForEach-Object {"$($_.Key)=$(Encode-VnpValue ([string]$_.Value))"}))-join '&'
 $config=Get-Content -Raw -Encoding UTF8 -LiteralPath (Join-Path $PSScriptRoot 'order-service\src\main\resources\application.yml')
 if($config-notmatch 'hash-secret:\s*\$\{VNPAY_HASH_SECRET:(?<secret>[^}]+)\}') { throw 'VNPay local hash secret not found' }
 $hmac=New-Object System.Security.Cryptography.HMACSHA512
 try{$hmac.Key=[Text.Encoding]::UTF8.GetBytes($Matches.secret);$hash=(-join ($hmac.ComputeHash([Text.Encoding]::UTF8.GetBytes($hashData))|ForEach-Object {$_.ToString('x2')}))}finally{$hmac.Dispose()}
 $sorted['vnp_SecureHash']=$hash
 '?' + ((@($sorted.GetEnumerator()|ForEach-Object {"$($_.Key)=$(Encode-VnpValue ([string]$_.Value))"}))-join '&')
}

$login=(Invoke-Api POST "$base/api/v1/auth/login" @{} @{email="e2e.approve.reg2@ecommerce.local";password="Buyer123!"}).data
$headers=@{Authorization="Bearer $($login.accessToken)"}
$tokenPayload=$login.accessToken.Split('.')[1].Replace('-', '+').Replace('_', '/')
while($tokenPayload.Length%4){$tokenPayload+='='}
$customerId=([Text.Encoding]::UTF8.GetString([Convert]::FromBase64String($tokenPayload))|ConvertFrom-Json).userId

$mismatch=@{
 hoTen="Voucher Mismatch";soDienThoai="0399999200";address="E2E";email="e2e.approve.reg2@ecommerce.local"
 hinhThucThanhToan="COD";maGiamGia="SHOP50";tongTien=1;phiShip=1;giamGia=999999;tongCong=1
 product=@(@{id=$variantB;quantity=1})
}
Assert-True ((Get-Code POST "$base/api/orders/create" $headers $mismatch)-eq 400) "shop voucher cannot apply to another seller"

foreach($id in @($variantA,$variantLeave,$variantB)){Invoke-Api POST "$base/api/v1/buyer/cart" $headers @{idSPCT=$id;price=1;quantity=1}|Out-Null}
$stockABefore=Get-Stock "37000000-0000-0000-0000-000000000001" $variantA
$stockBBefore=Get-Stock "37000000-0000-0000-0000-000000000004" $variantB
$cod=Invoke-Api POST "$base/api/orders/create" $headers @{
 hoTen="Authoritative $Label";soDienThoai="0399999201";address="E2E COD";email="e2e.approve.reg2@ecommerce.local"
 hinhThucThanhToan="COD";maGiamGia="WELCOME10";tongTien=1;phiShip=999999;giamGia=999999;tongCong=1
 Customer="00000000-0000-0000-0000-000000000099";product=@(@{id=$variantA;quantity=1},@{id=$variantB;quantity=1})
}
Assert-True ($cod.customer_id-eq $customerId) "customer comes from JWT"
Assert-True ([double]$cod.total_amount-eq 1690000 -and [double]$cod.shipping_fee-eq 30000 -and [double]$cod.discount_amount-eq 150000 -and [double]$cod.total_after_discount-eq 1570000) "server authoritative COD totals"
Assert-True ((Get-Stock "37000000-0000-0000-0000-000000000001" $variantA)-eq ($stockABefore-1)) "COD stock seller A"
Assert-True ((Get-Stock "37000000-0000-0000-0000-000000000004" $variantB)-eq ($stockBBefore-1)) "COD stock seller B"
$cartAfterCod=Invoke-Api GET "$base/api/v1/buyer/cart" $headers
$remainingIds=@($cartAfterCod.data.items.productVariantId)
Assert-True ($remainingIds-notcontains $variantA -and $remainingIds-notcontains $variantB -and $remainingIds-contains $variantLeave) "only checked out cart items removed"

Invoke-Api POST "$base/api/v1/buyer/cart" $headers @{idSPCT=$variantVnpay;price=1;quantity=1}|Out-Null
$vnpayStockBefore=Get-Stock "37000000-0000-0000-0000-000000000001" $variantVnpay
$payment=Invoke-Api POST "$base/api/orders/create" $headers @{
 hoTen="VNPay $Label";soDienThoai="0399999202";address="E2E VNPAY";email="e2e.approve.reg2@ecommerce.local"
 hinhThucThanhToan="VNPAY";tongTien=1;phiShip=1;giamGia=1;tongCong=1;product=@(@{id=$variantVnpay;quantity=1})
}
Assert-True ((Get-OrderStatus $payment.orderId $headers)-eq 6) "VNPay starts LUU_TAM"
Assert-True ((Get-Stock "37000000-0000-0000-0000-000000000001" $variantVnpay)-eq $vnpayStockBefore) "VNPay does not reduce stock before callback"
$query=New-VNPayCallbackQuery $payment.paymentUrl
$badQuery=$query -replace 'vnp_Amount=\d+','vnp_Amount=1'
$badLocation=Get-Redirect "$base/api/orders/vnpay-return$badQuery"
Assert-True ($badLocation-like '*trang-chu*' -and (Get-OrderStatus $payment.orderId $headers)-eq 6) "bad HMAC rejected"
Assert-True ((Get-Stock "37000000-0000-0000-0000-000000000001" $variantVnpay)-eq $vnpayStockBefore) "bad HMAC has no stock side effect"
$goodLocation=Get-Redirect "$base/api/orders/vnpay-return$query"
Assert-True ($goodLocation-like '*thanh-toan-thanh-cong*' -and (Get-OrderStatus $payment.orderId $headers)-eq 0) "valid HMAC confirms order"
$vnpayStockAfter=Get-Stock "37000000-0000-0000-0000-000000000001" $variantVnpay
Assert-True ($vnpayStockAfter-eq ($vnpayStockBefore-1)) "valid VNPay decrements stock once"
$cartAfterVnpay=Invoke-Api GET "$base/api/v1/buyer/cart" $headers
Assert-True (@($cartAfterVnpay.data.items.productVariantId)-notcontains $variantVnpay) "valid VNPay clears cart"
Get-Redirect "$base/api/orders/vnpay-return$query"|Out-Null
Assert-True ((Get-Stock "37000000-0000-0000-0000-000000000001" $variantVnpay)-eq $vnpayStockAfter) "VNPay callback idempotent"
Assert-True ((Get-Code POST "$base/api/orders/create" @{} @{hoTen="Forged";soDienThoai="1";address="x";hinhThucThanhToan="COD";product=@(@{id=$variantA;quantity=1})})-eq 401) "legacy checkout requires buyer JWT"

[pscustomobject]@{label=$Label;codOrderId=$cod.id;vnpayOrderId=$payment.orderId;codTotal=$cod.total_after_discount;vnpayStatus=0;badHmac="rejected";result="PASS"}|ConvertTo-Json
