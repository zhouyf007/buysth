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

Write-Host '===== 新功能验收测试 =====' -ForegroundColor Magenta

# 1. 注册校验与用户名重复
$bad = $false
try { CallApi 'POST' '/api/auth/register' '' @{username='regtest'; password='123456'; phone='123'; email='bad'} | Out-Null } catch { $bad = $true }
Check '注册手机号/邮箱格式校验' $bad
$dup = $false
try { CallApi 'POST' '/api/auth/register' '' @{username='user'; password='123456'} | Out-Null } catch { $dup = $true }
Check '注册用户名不能重复' $dup
CallApi 'POST' '/api/auth/register' '' @{username='regtest'; password='123456'; phone='13812345678'; email='reg@demo.com'} | Out-Null
$login = CallApi 'POST' '/api/auth/login' '' @{username='regtest'; password='123456'}
$token = $login.accessToken
Check '合法注册成功' ($token.Length -gt 0)

# 2. 头像上传与公开访问
Add-Type -AssemblyName System.Drawing
$png = 'D:\buysth\deploy\data\test-avatar.png'
$bmp = New-Object System.Drawing.Bitmap 32, 32
$g = [System.Drawing.Graphics]::FromImage($bmp)
$g.Clear([System.Drawing.Color]::OrangeRed)
$g.Dispose()
$bmp.Save($png, [System.Drawing.Imaging.ImageFormat]::Png)
$bmp.Dispose()
$avatarJson = & curl.exe -s -X POST -H "Authorization: Bearer $token" -F "file=@$png" "$base/api/auth/avatar/upload" | ConvertFrom-Json
$avatarUrl = $avatarJson.data
Check '头像上传成功' ($avatarJson.code -eq 0 -and $avatarUrl -like '/api/auth/avatar/files/*')
$avatarGet = Invoke-WebRequest -Method Get -Uri ($base + $avatarUrl) -UseBasicParsing
Check '头像公开访问' ($avatarGet.StatusCode -eq 200)

# 3. 修改密码
CallApi 'PUT' '/api/auth/password' $token @{oldPassword='123456'; newPassword='1234567'} | Out-Null
$relogin = CallApi 'POST' '/api/auth/login' '' @{username='regtest'; password='1234567'}
Check '修改密码后新密码登录' ($relogin.accessToken.Length -gt 0)
CallApi 'PUT' '/api/auth/password' $relogin.accessToken @{oldPassword='1234567'; newPassword='123456'} | Out-Null

# 4. 商品数量与图片统一处理
$products = CallApi 'GET' '/api/product/list?current=1&size=30'
Check '商品数量增加(>=20)' ($products.total -ge 20)
$admin4 = CallApi 'POST' '/api/auth/login' '' @{username='admin'; password='123456'}
$iconJson = & curl.exe -s -X POST -H "Authorization: Bearer $($admin4.accessToken)" -F "file=@$png" "$base/api/admin/upload?type=icon" | ConvertFrom-Json
Check '图标上传并统一处理' ($iconJson.code -eq 0 -and $iconJson.data -like '/api/product/uploads/*')

# 5. 订单删除/恢复
$order = CallApi 'POST' '/api/order/create' $token @{items=@(@{skuId=201;quantity=1}); address=@{receiverName='测试';receiverPhone='13812345678';receiverAddress='南京'}}
$orderNo = $order.orderNo
CallApi 'DELETE' "/api/order/$orderNo" $token | Out-Null
$delPage = CallApi 'GET' '/api/order/deleted?current=1&size=10' $token
$delFound = @($delPage.records) | Where-Object { $_.orderNo -eq $orderNo }
Check '订单删除进入回收站' (@($delFound).Count -gt 0)
CallApi 'POST' "/api/order/$orderNo/restore" $token | Out-Null
$list = CallApi 'GET' '/api/order/list?current=1&size=20' $token
$restoreFound = @($list.records) | Where-Object { $_.orderNo -eq $orderNo }
Check '订单恢复' (@($restoreFound).Count -gt 0)

# 6. 支付时使用优惠码
$order2 = CallApi 'POST' '/api/order/create' $token @{items=@(@{skuId=201;quantity=1}); address=@{receiverName='测试';receiverPhone='13812345678';receiverAddress='南京'}}
CallApi 'POST' "/api/order/$($order2.orderNo)/promotion" $token @{promotionCode='back-to-school'} | Out-Null
$order2Detail = CallApi 'GET' "/api/order/$($order2.orderNo)" $token
Check '支付前优惠码生效' ($order2Detail.payAmount -lt $order2Detail.totalAmount)

# 7. 收银台手机号校验
$pay = CallApi 'POST' '/api/pay/create' $token @{orderNo=$order2.orderNo}
$payHtml = [System.Text.Encoding]::UTF8.GetString((Invoke-WebRequest -Method Get -Uri ($base + $pay.payUrl) -UseBasicParsing).RawContentStream.ToArray())
Check '收银台无需手机号直接支付' ($payHtml -like '*确认支付*' -and $payHtml -notlike '*支付手机号*')

# 8. 消息删除/批量
$notifyPage = CallApi 'GET' '/api/notify/messages?current=1&size=20' $token
if (@($notifyPage.records).Count -gt 0) {
    $mid = @($notifyPage.records)[0].id
    CallApi 'DELETE' "/api/notify/messages/$mid" $token | Out-Null
    $after = CallApi 'GET' '/api/notify/messages?current=1&size=20' $token
    $msgLeft = @($after.records) | Where-Object { $_.id -eq $mid }
    Check '消息单条删除' (@($msgLeft).Count -eq 0)
}
$remainIds = @((CallApi 'GET' '/api/notify/messages?current=1&size=20' $token).records).id
if (@($remainIds).Count -gt 0) {
    CallApi 'POST' '/api/notify/messages/batch-delete' $token @{ids=@($remainIds)} | Out-Null
    $after2 = CallApi 'GET' '/api/notify/messages?current=1&size=20' $token
    Check '消息批量删除' (@($after2.records).Count -eq 0)
} else {
    Check '消息批量删除' $true
}

# 9. 管理端订单/用户角色/消息
$admin = CallApi 'POST' '/api/auth/login' '' @{username='admin'; password='123456'}
$at = $admin.accessToken
$adminOrders = CallApi 'GET' '/api/admin/orders?current=1&size=20' $at
if (@($adminOrders.records).Count -gt 0) {
    $ano = @($adminOrders.records)[0].orderNo
    CallApi 'DELETE' "/api/admin/orders/$ano" $at | Out-Null
    $afterOrders = CallApi 'GET' '/api/admin/orders?current=1&size=20' $at
    $adminOrderLeft = @($afterOrders.records) | Where-Object { $_.orderNo -eq $ano }
    Check '管理端订单单独删除' (@($adminOrderLeft).Count -eq 0)
}
$roleIds = CallApi 'GET' "/api/admin/users/$($login.user.id)/roles" $at
Check '管理端用户角色回显' (@($roleIds).Count -ge 1)
$adminMsgs = CallApi 'GET' '/api/admin/messages?current=1&size=20' $at
if (@($adminMsgs.records).Count -gt 0) {
    $amid = @($adminMsgs.records)[0].id
    CallApi 'DELETE' "/api/admin/messages/$amid" $at | Out-Null
    $afterMsgs = CallApi 'GET' '/api/admin/messages?current=1&size=20' $at
    $adminMsgLeft = @($afterMsgs.records) | Where-Object { $_.id -eq $amid }
    Check '管理端消息删除' (@($adminMsgLeft).Count -eq 0)
} else {
    Check '管理端消息删除' $true
}

Write-Host ''
Write-Host "验收结果: 通过 $pass, 失败 $fail" -ForegroundColor Cyan
if ($fail -gt 0) { exit 1 }
