$ErrorActionPreference = 'Continue'
$base = 'http://localhost:8080'
$pass = 0
$fail = 0

function CallApi($m, $p, $token = '', $body = $null) {
    $headers = @{}
    if ($token) { $headers.Authorization = "Bearer $token" }
    $params = @{ Method = $m; Uri = "$base$p"; Headers = $headers; UseBasicParsing = $true }
    if ($null -ne $body) {
        $params.ContentType = 'application/json; charset=utf-8'
        $params.Body = [System.Text.Encoding]::UTF8.GetBytes(($body | ConvertTo-Json -Depth 8))
    }
    $resp = Invoke-WebRequest @params
    $obj = ([System.Text.Encoding]::UTF8.GetString($resp.RawContentStream.ToArray()) | ConvertFrom-Json)
    if ($obj.code -ne 0) { throw "API $m $p failed: $($obj.message)" }
    return $obj.data
}

function Check($name, $ok) {
    if ($ok) { $script:pass++; Write-Host "  [PASS] $name" -ForegroundColor Green }
    else { $script:fail++; Write-Host "  [FAIL] $name" -ForegroundColor Red }
}

Write-Host '===== 本轮修复专项测试 =====' -ForegroundColor Magenta
Add-Type -AssemblyName System.Drawing
$png = 'D:\buysth\deploy\data\test-avatar.png'
$bmp = New-Object System.Drawing.Bitmap 32, 32
$g = [System.Drawing.Graphics]::FromImage($bmp)
$g.Clear([System.Drawing.Color]::OrangeRed)
$g.Dispose()
$bmp.Save($png, [System.Drawing.Imaging.ImageFormat]::Png)
$bmp.Dispose()

$avatarJson = & curl.exe -s -X POST -F "file=@$png" "$base/api/auth/avatar/upload" | ConvertFrom-Json
Check '注册前免登录上传头像' ($avatarJson.code -eq 0 -and $avatarJson.data -like '/api/auth/avatar/files/*')

$user = CallApi 'POST' '/api/auth/login' '' @{username='user'; password='123456'}
$ut = $user.accessToken
$badPhone = $false
try {
    CallApi 'POST' '/api/order/create' $ut @{items=@(@{skuId=201;quantity=1}); address=@{receiverName='张三';receiverPhone='123';receiverAddress='南京'}} | Out-Null
} catch { $badPhone = $true }
Check '下单联系电话手机号校验' $badPhone

$before = (CallApi 'GET' '/api/product/101').sales
$order = CallApi 'POST' '/api/order/create' $ut @{items=@(@{skuId=201;quantity=1}); address=@{receiverName='张三';receiverPhone='13812345678';receiverAddress='南京'}}
$pay = CallApi 'POST' '/api/pay/create' $ut @{orderNo=$order.orderNo}
$payNo = $pay.payUrl.Split('/')[-1]
CallApi 'POST' "/api/pay/mock/notify/$payNo" | Out-Null
Start-Sleep -Seconds 3
$after = (CallApi 'GET' '/api/product/101').sales
Check '支付后商品销量递增' ($after -gt $before)

$admin = CallApi 'POST' '/api/auth/login' '' @{username='admin'; password='123456'}
$at = $admin.accessToken
$upJson = & curl.exe -s -X POST -H "Authorization: Bearer $at" -F "file=@$png" "$base/api/admin/upload?type=product" | ConvertFrom-Json
$mainUrl = $upJson.data
$prodForm = @{ categoryId=1; name='专项测试手机'; brand='测试'; region='南京'; mainImage=$mainUrl; subtitle='测试'; detail='<p>test</p>'; publishDate='2026-08-03T10:00:00'; status=1; skus=@(@{specName='版本';specValue='8GB+128GB';price=1999;stock=20;status=1}) }
CallApi 'POST' '/api/admin/products' $at $prodForm | Out-Null
$newList = CallApi 'GET' '/api/admin/products?current=1&size=50&keyword=专项测试手机' $at
$newProd = @($newList.records)[0]
$newOrder = CallApi 'POST' '/api/order/create' $ut @{items=@(@{skuId=$newProd.skus[0].id;quantity=1}); address=@{receiverName='张三';receiverPhone='13812345678';receiverAddress='南京'}}
$newDetail = CallApi 'GET' "/api/order/$($newOrder.orderNo)" $ut
Check '新商品订单图片回退主图' ($newDetail.items[0].image -eq $mainUrl)

$operator = CallApi 'POST' '/api/auth/login' '' @{username='operator'; password='123456'}
$ot = $operator.accessToken
$guard = $false
try {
    CallApi 'PUT' "/api/admin/users/$($user.user.id)/role" $ot @{roleIds=@(1)} | Out-Null
} catch { if ($_.Exception.Message -like '*无权*') { $guard = $true } }
Check '运营不能分配超级管理员角色' $guard

CallApi 'PUT' "/api/admin/users/$($user.user.id)/role" $at @{roleIds=@(3)} | Out-Null
$rolesAfter = CallApi 'GET' "/api/admin/users/$($user.user.id)/roles" $at
Check '管理员正常分配角色' (@($rolesAfter) -contains 3)

Write-Host ''
Write-Host "专项结果: 通过 $pass, 失败 $fail" -ForegroundColor Cyan
if ($fail -gt 0) { exit 1 }

