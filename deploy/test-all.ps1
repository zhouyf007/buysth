$ErrorActionPreference = 'Stop'
$base = 'http://localhost:8080'
$passed = 0
$failed = 0

function Wait-Gateway {
    $deadline = (Get-Date).AddSeconds(90)
    while ((Get-Date) -lt $deadline) {
        if (Test-NetConnection -ComputerName 127.0.0.1 -Port 8080 -WarningAction SilentlyContinue -InformationLevel Quiet) {
            return
        }
        Start-Sleep -Seconds 3
    }
    throw "Gateway 8080 not reachable"
}

function Call-Api {
    param(
        [string]$Method,
        [string]$Path,
        [string]$Token = '',
        [object]$Body = $null
    )
    $headers = @{}
    if ($Token) { $headers.Authorization = "Bearer $Token" }
    $params = @{
        Method = $Method
        Uri = "$base$Path"
        Headers = $headers
        UseBasicParsing = $true
    }
    if ($null -ne $Body) {
        $params.ContentType = 'application/json; charset=utf-8'
        $params.Body = [System.Text.Encoding]::UTF8.GetBytes(($Body | ConvertTo-Json -Depth 8))
    }
    $resp = Invoke-WebRequest @params
    $bytes = $resp.RawContentStream.ToArray()
    $json = [System.Text.Encoding]::UTF8.GetString($bytes)
    $obj = $json | ConvertFrom-Json
    if ($obj.code -ne 0) {
        throw "API $Method $Path failed: $($obj.message)"
    }
    return $obj.data
}

function Check {
    param([string]$Name, [bool]$Ok)
    if ($Ok) {
        $script:passed++
        Write-Host "  [PASS] $Name" -ForegroundColor Green
    } else {
        $script:failed++
        Write-Host "  [FAIL] $Name" -ForegroundColor Red
    }
}

function Wait-Until {
    param([scriptblock]$Test, [string]$Desc, [int]$Seconds = 30)
    $deadline = (Get-Date).AddSeconds($Seconds)
    while ((Get-Date) -lt $deadline) {
        try {
            $result = & $Test
            if ($result) { return $result }
        } catch { }
        Start-Sleep -Seconds 2
    }
    throw "Timed out waiting for $Desc"
}

Write-Host "========== 商城功能冒烟测试 ==========" -ForegroundColor Magenta
Wait-Gateway
Write-Host "Gateway reachable" -ForegroundColor Cyan
Start-Sleep -Seconds 15

# 1. 用户登录
$userLogin = Call-Api -Method 'POST' -Path '/api/auth/login' -Body @{ username = 'user'; password = '123456' }
$userToken = $userLogin.accessToken
Check "用户登录 user/123456" ($userToken.Length -gt 0)

# 2. 商品浏览/搜索/缓存
$categories = Call-Api -Method 'GET' -Path '/api/product/categories'
Check "商品分类加载" ($categories.Count -gt 0)
$products = Call-Api -Method 'GET' -Path '/api/product/list?current=1&size=5'
Check "商品列表分页" ($products.records.Count -gt 0)
$first = $products.records[0]
$detail = Call-Api -Method 'GET' -Path "/api/product/$($first.id)"
Check "商品详情(含SKU)" ($detail.skus.Count -gt 0)
$hot = Call-Api -Method 'GET' -Path '/api/product/hot'
Check "热销榜单(Redis缓存)" ($hot.Count -gt 0)

# 2.5 个人资料 + 商品评价
$me = Call-Api -Method 'GET' -Path '/api/auth/me' -Token $userToken
Check "个人信息查询" ($me.username -eq 'user')
$updated = Call-Api -Method 'PUT' -Path '/api/auth/profile' -Token $userToken -Body @{
    nickname = '冒烟测试用户'
    phone = '13900000000'
    email = 'smoke@demo.com'
}
Check "修改个人资料" ($updated.nickname -eq '冒烟测试用户')
Call-Api -Method 'POST' -Path '/api/product/101/reviews' -Token $userToken -Body @{
    rating = 5
    content = '冒烟测试评价内容'
} | Out-Null
$reviews = Call-Api -Method 'GET' -Path '/api/product/101/reviews?current=1&size=5'
Check "商品评价发布与列表" ($reviews.total -ge 1)

# 3. 购物车 + 下单 + 模拟支付
$skuId = $detail.skus[0].id
Call-Api -Method 'POST' -Path '/api/order/cart' -Token $userToken -Body @{ skuId = $skuId; quantity = 1 } | Out-Null
$cart = Call-Api -Method 'GET' -Path '/api/order/cart' -Token $userToken
Check "加入购物车" (@($cart).Count -gt 0)
$cartItemId = @($cart)[0].id
Call-Api -Method 'PUT' -Path "/api/order/cart/$cartItemId" -Token $userToken -Body @{ quantity = 3 } | Out-Null
$cartAfterQty = Call-Api -Method 'GET' -Path '/api/order/cart' -Token $userToken
$qtyMatch = @($cartAfterQty) | Where-Object { $_.id -eq $cartItemId -and $_.quantity -eq 3 }
$qtyOk = @($qtyMatch).Count -gt 0
Check "购物车增加数量" $qtyOk
Call-Api -Method 'PUT' -Path "/api/order/cart/$cartItemId" -Token $userToken -Body @{ checked = 0 } | Out-Null
$cartAfterCheck = Call-Api -Method 'GET' -Path '/api/order/cart' -Token $userToken
$checkMatch = @($cartAfterCheck) | Where-Object { $_.id -eq $cartItemId -and $_.checked -eq 0 }
$checkOk = @($checkMatch).Count -gt 0
Check "购物车取消勾选" $checkOk
Call-Api -Method 'DELETE' -Path "/api/order/cart/$cartItemId" -Token $userToken | Out-Null
$cartAfterDelete = Call-Api -Method 'GET' -Path '/api/order/cart' -Token $userToken
$deleteLeft = @($cartAfterDelete) | Where-Object { $_.id -eq $cartItemId }
Check "购物车删除商品" (@($deleteLeft).Count -eq 0)
$order = Call-Api -Method 'POST' -Path '/api/order/create' -Token $userToken -Body @{
    items = @(@{ skuId = $skuId; quantity = 1 })
    address = @{ receiverName = '测试用户'; receiverPhone = '13800000000'; receiverAddress = '江苏省南京市测试路1号' }
    remark = 'smoke test'
}
Check "创建订单" ($order.orderNo.Length -gt 0)
$orderNo = $order.orderNo
$pay = Call-Api -Method 'POST' -Path '/api/pay/create' -Token $userToken -Body @{ orderNo = $orderNo }
Check "创建支付单(mock网关)" ($pay.payUrl -like "/api/pay/mock/*")
$payNo = $pay.payUrl.Split('/')[-1]
$payPage = Invoke-WebRequest -Method Get -Uri ($base + $pay.payUrl) -UseBasicParsing
$payHtml = [System.Text.Encoding]::UTF8.GetString($payPage.RawContentStream.ToArray())
Check "模拟支付页免登录打开" ($payPage.StatusCode -eq 200 -and $payHtml -like '*模拟支付*')
Call-Api -Method 'POST' -Path "/api/pay/mock/notify/$payNo" | Out-Null
$paidOrder = Wait-Until -Test {
    $st = (Call-Api -Method 'GET' -Path "/api/order/$orderNo" -Token $userToken).status
    if ($st -eq 'PAID' -or $st -eq 'SHIPPED') { return $st }
} -Desc "订单支付状态"
Check "支付回调驱动订单PAID" ($paidOrder -eq 'PAID' -or $paidOrder -eq 'SHIPPED')

# 4. 物流 + 通知
$shipment = Wait-Until -Test {
    try {
        $s = Call-Api -Method 'GET' -Path "/api/logistics/track/$orderNo" -Token $userToken
        if ($s.trackingNo) { return $s }
    } catch { return $null }
} -Desc "物流运单创建"
Check "支付后自动创建物流" ($shipment.tracks.Count -gt 0)
$messages = Wait-Until -Test {
    $m = Call-Api -Method 'GET' -Path '/api/notify/messages?current=1&size=5' -Token $userToken
    if ($m.total -gt 0) { return $m }
} -Desc "订单通知消息"
Check "MQ驱动订单通知" ($messages.total -gt 0)

# 5. 秒杀链路
$activities = Call-Api -Method 'GET' -Path '/api/seckill/activities?type=SECKILL'
Check "秒杀活动列表" ($activities.records.Count -gt 0)
$activity = $activities.records[0]
$seckillProduct = $activity.products[0]
$seckillResult = Call-Api -Method 'POST' -Path "/api/seckill/$($activity.id)/products/$($seckillProduct.id)" -Token $userToken
Check "Redis+Lua秒杀抢购" ($seckillResult.success -eq $true)
$seckillOrder = Wait-Until -Test {
    $page = Call-Api -Method 'GET' -Path '/api/order/list?current=1&size=20' -Token $userToken
    $found = $page.records | Where-Object { $_.orderNo -eq $seckillResult.orderNo }
    if ($found) { return $found }
} -Desc "秒杀MQ异步落单"
Check "秒杀MQ异步创建订单" ($seckillOrder.orderType -eq 'SECKILL')

# 6. 优惠活动
$promoOrder = Call-Api -Method 'POST' -Path '/api/order/create' -Token $userToken -Body @{
    items = @(@{ skuId = $skuId; quantity = 1 })
    address = @{ receiverName = '测试用户'; receiverPhone = '13800000000'; receiverAddress = '江苏省南京市测试路1号' }
    promotionCode = 'back-to-school'
}
Check "优惠码95折生效" ($promoOrder.payAmount -lt $order.payAmount)
Call-Api -Method 'POST' -Path "/api/order/cancel/$($promoOrder.orderNo)" -Token $userToken | Out-Null
$cancelled = Call-Api -Method 'GET' -Path "/api/order/$($promoOrder.orderNo)" -Token $userToken
Check "订单取消+库存MQ释放" ($cancelled.status -eq 'CANCELLED')

# 7. 管理端
$adminLogin = Call-Api -Method 'POST' -Path '/api/auth/login' -Body @{ username = 'admin'; password = '123456' }
$adminToken = $adminLogin.accessToken
Check "管理员登录" ($adminLogin.user.roles -contains 'SUPER_ADMIN')
$adminProducts = Call-Api -Method 'GET' -Path '/api/admin/products?current=1&size=5' -Token $adminToken
Check "管理端商品列表" ($adminProducts.total -gt 0)
$adminOrders = Call-Api -Method 'GET' -Path '/api/admin/orders?current=1&size=5' -Token $adminToken
Check "管理端订单列表" ($adminOrders.total -gt 0)
$adminUsers = Call-Api -Method 'GET' -Path '/api/admin/users?current=1&size=5' -Token $adminToken
Check "管理端用户列表" ($adminUsers.total -gt 0)
$adminActivities = Call-Api -Method 'GET' -Path '/api/admin/activities?current=1&size=5' -Token $adminToken
Check "管理端活动列表" ($null -ne $adminActivities -and $adminActivities.total -gt 0)
$adminPayments = Call-Api -Method 'GET' -Path '/api/admin/payments?current=1&size=5' -Token $adminToken
Check "管理端支付列表" ($adminPayments.total -gt 0)
$adminAnnouncements = Call-Api -Method 'GET' -Path '/api/admin/announcements?current=1&size=5' -Token $adminToken
Check "管理端公告列表" ($adminAnnouncements.total -gt 0)
$adminLogistics = Call-Api -Method 'GET' -Path '/api/admin/shipments?current=1&size=5' -Token $adminToken
Check "管理端物流列表" ($adminLogistics.total -gt 0)

Write-Host ""
Write-Host "测试结果: 通过 $passed, 失败 $failed" -ForegroundColor Cyan
if ($failed -gt 0) {
    exit 1
}
Write-Host "全部核心链路测试通过" -ForegroundColor Green
