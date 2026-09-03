param(
    [string]$ProductId = "b8bc80f7-29b2-4e9d-b0e4-6956fc774ba3",
    [string]$VariantId = "a64e1868-9cc4-46f2-ab6e-d206408299e4"
)

$ErrorActionPreference = "Stop"
$base = "http://localhost:8080"
$otherVariantId = "38000000-0000-0000-0000-000000000011"

function Assert-True { param([bool]$Condition,[string]$Message); if(-not $Condition){throw "ASSERT: $Message"} }
function Invoke-Api {
    param([string]$Method,[string]$Uri,[hashtable]$Headers=@{},[object]$Body=$null)
    $p=@{Method=$Method;Uri=$Uri;Headers=$Headers;TimeoutSec=15}
    if($null-ne $Body){$p.ContentType="application/json";$p.Body=ConvertTo-Json -InputObject $Body -Depth 15}
    Invoke-RestMethod @p
}

$buyer=(Invoke-Api POST "$base/api/v1/auth/login" @{} @{email="e2e.buyer2@ecommerce.local";password="Buyer123"}).data
$seller=(Invoke-Api POST "$base/api/v1/auth/login" @{} @{email="e2e.approve.reg2@ecommerce.local";password="Buyer123!"}).data
$buyerHeaders=@{Authorization="Bearer $($buyer.accessToken)"}
$sellerHeaders=@{Authorization="Bearer $($seller.accessToken)"}

Invoke-Api POST "$base/api/v1/buyer/cart" $buyerHeaders @{idSPCT=$VariantId;price=1;quantity=1}|Out-Null
Invoke-Api POST "$base/api/v1/buyer/cart" $buyerHeaders @{idSPCT=$otherVariantId;price=1;quantity=1}|Out-Null
$cart=Invoke-Api GET "$base/api/v1/buyer/cart" $buyerHeaders
$item=@($cart.data.items)|Where-Object productVariantId -eq $VariantId|Select-Object -First 1
$other=@($cart.data.items)|Where-Object productVariantId -eq $otherVariantId|Select-Object -First 1
Assert-True ($null-ne $item -and $null-ne $other) "two seller items in cart"
Assert-True ($item.price -eq [double]$item.productVariant.salePrice -and $item.price -ne 1) "server price snapshot"
$sellerGroups=@($cart.data.shopGroups)|ForEach-Object {$_.sellerId}|Sort-Object -Unique
Assert-True ($sellerGroups.Count -ge 2 -and $sellerGroups -contains $item.sellerId -and $sellerGroups -contains $other.sellerId) "shopGroups by seller"

$detail=Invoke-Api GET "$base/api/v1/seller/products/$ProductId" $sellerHeaders
$valueKeys=@{}
$axes=@()
for($axisIndex=0;$axisIndex-lt @($detail.variantAxes).Count;$axisIndex++){
    $sourceAxis=@($detail.variantAxes)[$axisIndex]
    $axisKey="axis-$axisIndex"
    $values=@()
    for($valueIndex=0;$valueIndex-lt @($sourceAxis.values).Count;$valueIndex++){
        $sourceValue=@($sourceAxis.values)[$valueIndex]
        $key="value-$axisIndex-$valueIndex"
        $valueKeys[$sourceValue.id]=$key
        $values+=@{id=$sourceValue.id;clientKey=$key;value=$sourceValue.value;displayOrder=$sourceValue.displayOrder}
    }
    $axes+=@{id=$sourceAxis.id;clientKey=$axisKey;name=$sourceAxis.name;nameSuggestionId=$sourceAxis.nameSuggestionId;displayOrder=$sourceAxis.displayOrder;values=$values}
}
$variants=@()
$oldPrice=0D
$newPrice=0D
foreach($sourceVariant in @($detail.variants)){
    $price=[double]$sourceVariant.salePrice
    if($sourceVariant.id-eq $VariantId){$oldPrice=$price;$price+=7000;$newPrice=$price}
    $variants+=@{
        id=$sourceVariant.id;sku=$sourceVariant.sku;salePrice=$price;quantity=$sourceVariant.quantity
        imageUrl=$sourceVariant.imageUrl;defaultVariant=[bool]$sourceVariant.isDefault;status=$sourceVariant.status
        selectionValueKeys=@($sourceVariant.selections|ForEach-Object {$valueKeys[$_.valueId]})
    }
}
$images=@($detail.productImages|ForEach-Object {@{id=$_.id;url=$_.url;displayOrder=$_.displayOrder;status=$_.status}})
$attributes=@($detail.attributes|ForEach-Object {@{
    definitionId=$_.definitionId;dataType=$_.dataType;valueText=$_.valueText;valueNumber=$_.valueNumber
    unit=$_.unit;selectedOptionIds=@($_.selectedOptions.id);displayOrder=$_.displayOrder
}})
$updated=Invoke-Api PUT "$base/api/v1/seller/products/$ProductId" $sellerHeaders @{
    categoryId=$detail.category.id;code=$detail.code;name=$detail.name;description=$detail.description
    productImages=$images;attributes=$attributes;variantAxes=$axes;variants=$variants
}
Assert-True ((@($updated.variants)|Where-Object id -eq $VariantId).salePrice -eq $newPrice) "catalog price update"

$refreshed=Invoke-Api GET "$base/api/v1/buyer/cart" $buyerHeaders
$refreshedItem=@($refreshed.data.items)|Where-Object productVariantId -eq $VariantId|Select-Object -First 1
Assert-True ($refreshedItem.price -eq $newPrice -and $refreshedItem.price -ne $oldPrice) "cart refreshes changed price"
Invoke-Api PUT "$base/api/v1/buyer/cart/$($refreshedItem.id)" $buyerHeaders|Out-Null
$remaining=Invoke-Api GET "$base/api/v1/buyer/cart" $buyerHeaders
Assert-True ($null-eq (@($remaining.data.items)|Where-Object id -eq $refreshedItem.id|Select-Object -First 1)) "legacy PUT removes selected item"
Invoke-Api PUT "$base/api/v1/buyer/cart/$($other.id)" $buyerHeaders|Out-Null

[pscustomobject]@{productId=$ProductId;variantId=$VariantId;oldPrice=$oldPrice;newPrice=$newPrice;shopGroupCount=$sellerGroups.Count;result="PASS"}|ConvertTo-Json
