param([Parameter(Mandatory = $true)][string]$Label)

$ErrorActionPreference = 'Stop'
$base = 'http://localhost:8080'
$suffix = $Label.ToLowerInvariant()

function Assert-True { param([bool]$Condition,[string]$Message); if (-not $Condition) { throw "ASSERT: $Message" } }
function Invoke-Api {
    param([string]$Method,[string]$Uri,[hashtable]$Headers=@{},[object]$Body=$null)
    $p=@{Method=$Method;Uri=$Uri;Headers=$Headers;TimeoutSec=20}
    if($null-ne $Body){$p.ContentType='application/json';$p.Body=ConvertTo-Json -InputObject $Body -Depth 12}
    Invoke-RestMethod @p
}
function Invoke-Form {
    param([string]$Method,[string]$Uri,[string]$Token,[hashtable]$Fields)
    $args=@('-sS','-X',$Method,$Uri,'-H',"Authorization: Bearer $Token")
    foreach($entry in $Fields.GetEnumerator()){$args+=@('-F',"$($entry.Key)=$($entry.Value)")}
    $raw=& curl.exe @args
    if($LASTEXITCODE-ne 0){throw "curl form request failed: $Uri"}
    $raw|ConvertFrom-Json
}

$admin=(Invoke-Api POST "$base/api/v1/auth/login-admin" @{} @{email='admin@ecommerce.local';password='Admin@123'}).data
$seller=(Invoke-Api POST "$base/api/v1/auth/login" @{} @{email='e2e.approve.reg2@ecommerce.local';password='Buyer123!'}).data
$ah=@{Authorization="Bearer $($admin.accessToken)"};$sh=@{Authorization="Bearer $($seller.accessToken)"}

# Customer CRUD: create, read, update, status twice (restore ACTIVE), and secret redaction.
$customer=(Invoke-Form POST "$base/api/v1/admin/khach-hang" $admin.accessToken @{
    code="CUS-$Label";name="Admin Customer $Label";email="admin.customer.$suffix@ecommerce.local";
    phoneNumber="091$((Get-Random -Minimum 1000000 -Maximum 9999999))";address='E2E address';gioiTinh='true'
}).data
Assert-True ([bool]$customer.id-and $null-eq$customer.password) 'admin customer create/redaction'
$customerUpdated=(Invoke-Form PUT "$base/api/v1/admin/khach-hang" $admin.accessToken @{
    id=$customer.id;code=$customer.code;name="Admin Customer Updated $Label";email=$customer.email;
    phoneNumber=$customer.phoneNumber;address='E2E updated';gioiTinh='true'
}).data
Assert-True ($customerUpdated.name-eq"Admin Customer Updated $Label"-and $null-eq$customerUpdated.password) 'admin customer update/redaction'
Invoke-Api PUT "$base/api/v1/admin/khach-hang/$($customer.id)/change-status" $ah|Out-Null
Invoke-Api PUT "$base/api/v1/admin/khach-hang/$($customer.id)/change-status" $ah|Out-Null
$customerRead=(Invoke-Api GET "$base/api/v1/admin/khach-hang/$($customer.id)" $ah).data
Assert-True ($customerRead.status-eq'ACTIVE'-and $null-eq$customerRead.password) 'admin customer status/read'

# Staff CRUD: create, read, update, role/status toggles (restore original state), and duplicate check.
$staff=(Invoke-Form POST "$base/api/v1/admin/nhan-vien" $admin.accessToken @{
    name="Admin Staff $Label";email="admin.staff.$suffix@ecommerce.local";
    phoneNumber="092$((Get-Random -Minimum 1000000 -Maximum 9999999))";identityNumber="08$((Get-Random -Minimum 1000000000 -Maximum 1999999999))";
    address='E2E staff';gioiTinh='false'
}).data
Assert-True ([bool]$staff.id-and $staff.role-eq'STAFF'-and $null-eq$staff.password) 'admin staff create/redaction'
$duplicate=Invoke-Api POST "$base/api/v1/admin/nhan-vien/check-duplicate" $ah @{field='email';value=$staff.email;excludeId=''}
Assert-True ($duplicate.exists-eq$true) 'admin staff duplicate check'
$staffUpdated=(Invoke-Form POST "$base/api/v1/admin/nhan-vien" $admin.accessToken @{
    id=$staff.id;code=$staff.code;name="Admin Staff Updated $Label";email=$staff.email;phoneNumber=$staff.phoneNumber;
    identityNumber=$staff.identityNumber;address='E2E staff updated';gioiTinh='false'
}).data
Assert-True ($staffUpdated.name-eq"Admin Staff Updated $Label"-and $null-eq$staffUpdated.password) 'admin staff update/redaction'
Invoke-Api PUT "$base/api/v1/admin/nhan-vien/$($staff.id)/change-role" $ah|Out-Null
Invoke-Api PUT "$base/api/v1/admin/nhan-vien/$($staff.id)/change-role" $ah|Out-Null
Invoke-Api PUT "$base/api/v1/admin/nhan-vien/$($staff.id)/change-status" $ah|Out-Null
Invoke-Api PUT "$base/api/v1/admin/nhan-vien/$($staff.id)/change-status" $ah|Out-Null
$staffRead=(Invoke-Api GET "$base/api/v1/admin/nhan-vien/$($staff.id)" $ah).data
Assert-True ($staffRead.role-eq'STAFF'-and $staffRead.status-eq'ACTIVE'-and $null-eq$staffRead.password) 'admin staff role/status/read'

# Category CRUD and suggestion configuration.
$root=Invoke-Api POST "$base/api/v1/admin/categories" $ah @{name="Admin Root $Label";code="ADMROOT-$Label";slug="admin-root-$suffix";displayOrder=98}
$leaf=Invoke-Api POST "$base/api/v1/admin/categories" $ah @{name="Admin Leaf $Label";code="ADMLEAF-$Label";slug="admin-leaf-$suffix";parentId=$root.id;displayOrder=1}
$leaf=Invoke-Api PUT "$base/api/v1/admin/categories/$($leaf.id)" $ah @{name="Admin Leaf Updated $Label";code="ADMLEAF-$Label";slug="admin-leaf-$suffix";parentId=$root.id;displayOrder=2}
Invoke-Api PUT "$base/api/v1/admin/categories/$($leaf.id)/attribute-suggestions" $ah @(@{definitionId='41000000-0000-0000-0000-000000000003';required=$false;filterable=$true;displayOrder=0})|Out-Null
$inactive=Invoke-Api PUT "$base/api/v1/admin/categories/$($leaf.id)/status" $ah @{status='INACTIVE'}
$active=Invoke-Api PUT "$base/api/v1/admin/categories/$($leaf.id)/status" $ah @{status='ACTIVE'}
Assert-True ($leaf.name-eq"Admin Leaf Updated $Label"-and $inactive.status-eq'INACTIVE'-and $active.status-eq'ACTIVE') 'admin category CRUD'

# Create a seller-defined attribute, then standardize, audit and hide it through moderation APIs.
$customName="E2E Custom Attribute $Label"
$product=Invoke-Api POST "$base/api/v1/seller/products" $sh @{
    categoryId=$leaf.id;code="ADM-PROD-$Label";name="Admin Moderation Product $Label";description='attribute moderation fixture';
    productImages=@(@{url="https://example.invalid/admin-$suffix.png";displayOrder=0;status='ACTIVE'});
    attributes=@(@{name=$customName;dataType='TEXT';valueText="Value $Label";displayOrder=0});variantAxes=@();
    variants=@(@{sku="ADM-SKU-$Label";salePrice=99000;quantity=3;defaultVariant=$true;status='ACTIVE';selectionValueKeys=@()})
}
$definitions=Invoke-Api GET "$base/api/v1/admin/product-attributes?q=$([uri]::EscapeDataString($customName))" $ah
$definition=@($definitions|Where-Object{$_.name-eq$customName})[0]
Assert-True ([bool]$product.id-and [bool]$definition.id-and $definition.verified-eq$false) 'seller-defined attribute enters moderation'
$standard=Invoke-Api PUT "$base/api/v1/admin/product-attributes/$($definition.id)/standardize" $ah @{name="$customName Standard";categoryIds=@($leaf.id);reason="E2E $Label"}
Assert-True ($standard.verified-eq$true-and $standard.name-eq"$customName Standard") 'attribute standardize/verify'
$audits=Invoke-Api GET "$base/api/v1/admin/product-attributes/moderation-audits" $ah
Assert-True (@($audits|Where-Object{$_.sourceDefinitionId-eq$definition.id-and $_.action-eq'STANDARDIZE'}).Count-gt0) 'attribute moderation audit'
Invoke-Api PUT "$base/api/v1/admin/product-attributes/$($definition.id)/hide" $ah @{reason="E2E cleanup $Label"}|Out-Null
$hidden=Invoke-Api GET "$base/api/v1/admin/product-attributes?q=$([uri]::EscapeDataString($customName))&status=INACTIVE" $ah
Assert-True (@($hidden|Where-Object{$_.id-eq$definition.id}).Count-eq1) 'attribute hide'

$dashboard=Invoke-Api GET "$base/api/v1/admin/thong-ke/marketplace-dashboard" $ah
Assert-True ($dashboard.gmv-gt0) 'admin dashboard statistics'

[pscustomobject]@{label=$Label;customerId=$customer.id;staffId=$staff.id;categoryId=$leaf.id;productId=$product.id;attributeId=$definition.id;gmv=$dashboard.gmv;result='PASS'}|ConvertTo-Json
