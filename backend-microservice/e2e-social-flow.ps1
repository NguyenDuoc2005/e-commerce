param(
 [string]$Label='SOCIAL1',
 [Parameter(Mandatory=$true)][string]$SellerId,
 [Parameter(Mandatory=$true)][string]$ProductId,
 [Parameter(Mandatory=$true)][string]$VariantId,
 [Parameter(Mandatory=$true)][string]$OtherVariant,
 [Parameter(Mandatory=$true)][string]$OrderSellerId
)

$ErrorActionPreference='Stop';$base='http://localhost:8080'
function Assert-True{param([bool]$Condition,[string]$Message);if(-not $Condition){throw "ASSERT: $Message"}}
function Invoke-Api{param([string]$Method,[string]$Uri,[hashtable]$Headers=@{},[object]$Body=$null);$p=@{Method=$Method;Uri=$Uri;Headers=$Headers;TimeoutSec=20};if($null-ne $Body){$p.ContentType='application/json';$p.Body=ConvertTo-Json -InputObject $Body -Depth 8};Invoke-RestMethod @p}
function Get-Code{param([string]$Method,[string]$Uri,[hashtable]$Headers=@{},[object]$Body=$null);try{$p=@{Method=$Method;Uri=$Uri;Headers=$Headers;TimeoutSec=20;UseBasicParsing=$true};if($null-ne $Body){$p.ContentType='application/json';$p.Body=ConvertTo-Json -InputObject $Body -Depth 8};[int](Invoke-WebRequest @p).StatusCode}catch{[int]$_.Exception.Response.StatusCode}}
function Login{param([string]$Email,[string]$Password);(Invoke-Api POST "$base/api/v1/auth/login" @{} @{email=$Email;password=$Password}).data}

$reviewBuyer=Login 'e2e.approve.reg2@ecommerce.local' 'Buyer123!';$reviewHeaders=@{Authorization="Bearer $($reviewBuyer.accessToken)"};$review= (Invoke-Api POST "$base/api/v1/buyer/reviews" $reviewHeaders @{orderSellerId=$OrderSellerId;productDetailId=$VariantId;productRating=5;shopRating=4;comment="Review $Label";imageUrls=@()}).data
Assert-True ([bool]$review.id) 'completed purchase review'
Assert-True ((Get-Code POST "$base/api/v1/buyer/reviews" $reviewHeaders @{orderSellerId=$OrderSellerId;productDetailId=$VariantId;productRating=5;shopRating=5;comment='duplicate';imageUrls=@()})-eq 400) 'duplicate review rejected'
Assert-True ((Get-Code POST "$base/api/v1/buyer/reviews" $reviewHeaders @{orderSellerId=$OrderSellerId;productDetailId=$OtherVariant;productRating=5;shopRating=5;comment='not purchased';imageUrls=@()})-eq 400) 'unpurchased variant review rejected'
$product=Invoke-Api GET "$base/api/v1/permitall/products/$ProductId"
Assert-True ([int]$product.ratingCount-ge 1 -and [double]$product.ratingAverage-gt 0) 'catalog rating synchronized through internal contract'
Invoke-Api PUT "$base/api/v1/seller/reviews/$($review.id)/reply" $reviewHeaders @{reply="Seller reply $Label"}|Out-Null

$buyer=Login 'e2e.buyer2@ecommerce.local' 'Buyer123';$seller=Login 'e2e.approve.reg2@ecommerce.local' 'Buyer123!';$buyerHeaders=@{Authorization="Bearer $($buyer.accessToken)"};$sellerHeaders=@{Authorization="Bearer $($seller.accessToken)"}
$first=(Invoke-Api POST "$base/api/v1/buyer/shops/$SellerId/follow" $buyerHeaders).data;$second=(Invoke-Api POST "$base/api/v1/buyer/shops/$SellerId/follow" $buyerHeaders).data
Assert-True ($first.following-and $second.following-and $first.followerCount-eq $second.followerCount) 'follow uniqueness/idempotency'
Invoke-Api DELETE "$base/api/v1/buyer/shops/$SellerId/follow" $buyerHeaders|Out-Null

$conversation=Invoke-Api POST "$base/api/v1/buyer/chat/conversations" $buyerHeaders @{sellerId=$SellerId}
Invoke-Api POST "$base/api/v1/buyer/chat/conversations/$($conversation.id)/read" $buyerHeaders|Out-Null;Invoke-Api POST "$base/api/v1/seller/chat/conversations/$($conversation.id)/read" $sellerHeaders|Out-Null
Invoke-Api POST "$base/api/v1/buyer/chat/conversations/$($conversation.id)/messages" $buyerHeaders @{content="Buyer $Label"}|Out-Null
$sellerList=Invoke-Api GET "$base/api/v1/seller/chat/conversations" $sellerHeaders;$sellerConversation=@($sellerList)|Where-Object id -eq $conversation.id|Select-Object -First 1
Assert-True ($sellerConversation.unreadCount-eq 1) 'seller unread increments'
Invoke-Api POST "$base/api/v1/seller/chat/conversations/$($conversation.id)/read" $sellerHeaders|Out-Null
$sellerList=Invoke-Api GET "$base/api/v1/seller/chat/conversations" $sellerHeaders;$sellerConversation=@($sellerList)|Where-Object id -eq $conversation.id|Select-Object -First 1;Assert-True ($sellerConversation.unreadCount-eq 0) 'seller mark read'
Invoke-Api POST "$base/api/v1/seller/chat/conversations/$($conversation.id)/messages" $sellerHeaders @{content="Seller $Label"}|Out-Null
$buyerList=Invoke-Api GET "$base/api/v1/buyer/chat/conversations" $buyerHeaders;$buyerConversation=@($buyerList)|Where-Object id -eq $conversation.id|Select-Object -First 1;Assert-True ($buyerConversation.unreadCount-eq 1) 'buyer unread increments'
Invoke-Api POST "$base/api/v1/buyer/chat/conversations/$($conversation.id)/read" $buyerHeaders|Out-Null
$buyerList=Invoke-Api GET "$base/api/v1/buyer/chat/conversations" $buyerHeaders;$buyerConversation=@($buyerList)|Where-Object id -eq $conversation.id|Select-Object -First 1;Assert-True ($buyerConversation.unreadCount-eq 0) 'buyer mark read'

[pscustomobject]@{label=$Label;reviewId=$review.id;ratingAverage=$product.ratingAverage;ratingCount=$product.ratingCount;conversationId=$conversation.id;followUnique=$true;unreadCounts='1->0 each side';result='PASS'}|ConvertTo-Json
