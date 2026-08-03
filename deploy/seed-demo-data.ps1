$ErrorActionPreference = 'Continue'
$base = 'http://localhost:8080'

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

Write-Host '===== 生成第二个演示用户及其订单/支付/物流/消息 =====' -ForegroundColor Magenta

try {
    CallApi 'POST' '/api/auth/register' '' @{username='user2'; password='123456'; nickname='演示用户二'; phone='13912345678'; email='user2@demo.com'} | Out-Null
} catch {
    # 已存在则继续
}
$login = CallApi 'POST' '/api/auth/login' '' @{username='user2'; password='123456'}
$token = $login.accessToken
$order = CallApi 'POST' '/api/order/create' $token @{
    items = @(@{ skuId = 201; quantity = 1 })
    address = @{ receiverName = '演示用户二'; receiverPhone = '13912345678'; receiverAddress = '江苏省南京市演示路2号' }
    remark = '演示数据'
}
$pay = CallApi 'POST' '/api/pay/create' $token @{ orderNo = $order.orderNo }
$payNo = $pay.payUrl.Split('/')[-1]
CallApi 'POST' "/api/pay/mock/notify/$payNo" | Out-Null
Start-Sleep -Seconds 5
$shipment = $null
try {
    $shipment = CallApi 'GET' "/api/logistics/track/$($order.orderNo)" $token
} catch { }

Write-Host "第二个用户: user2 / 123456" -ForegroundColor Cyan
Write-Host ("订单号: " + $order.orderNo)
Write-Host ("支付单: " + $pay.payUrl)
if ($shipment) {
    Write-Host ("物流单: " + $shipment.trackingNo)
}
Write-Host '演示数据生成完成'

