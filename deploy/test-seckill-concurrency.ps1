$ErrorActionPreference = 'Continue'
$base = 'http://localhost:8080'
$activityId = 901
$seckillProductId = 911
$userCount = 20
$suffix = (Get-Date -Format 'yyyyMMddHHmmss')

function Post-Json {
    param([string]$Path, [object]$Body, [string]$Token = '')
    $headers = @{}
    if ($Token) { $headers.Authorization = "Bearer $Token" }
    $resp = Invoke-RestMethod -Method Post -Uri ($base + $Path) -Headers $headers `
        -ContentType 'application/json; charset=utf-8' `
        -Body ([System.Text.Encoding]::UTF8.GetBytes(($Body | ConvertTo-Json -Depth 5)))
    if ($resp.code -ne 0) { throw "API $Path failed: $($resp.message)" }
    return $resp.data
}

Write-Host "========== 秒杀库存击穿并发测试 ==========" -ForegroundColor Magenta

& 'D:\Program Files\Redis-x64-3.0.504\redis-cli.exe' -p 6379 FLUSHDB | Out-Null
& 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe' -h 127.0.0.1 -P 3306 -u root -proot --default-character-set=utf8mb4 -e "UPDATE seckill_db.seckill_product SET seckill_stock=10 WHERE id=$seckillProductId; DELETE FROM seckill_db.seckill_record WHERE activity_id=$activityId" 2>$null

Write-Host "注册 $userCount 个测试用户并登录"
$tokens = @()
for ($i = 1; $i -le $userCount; $i++) {
    $username = "conc$suffix$i"
    Post-Json -Path '/api/auth/register' -Body @{ username = $username; password = '123456' } | Out-Null
    $login = Post-Json -Path '/api/auth/login' -Body @{ username = $username; password = '123456' }
    $tokens += $login.accessToken
}
Write-Host "已准备 $($tokens.Count) 个并发请求"

Add-Type -AssemblyName System.Net.Http
$client = New-Object System.Net.Http.HttpClient
$url = "$base/api/seckill/$activityId/products/$seckillProductId"
$tasks = @()
foreach ($token in $tokens) {
    $req = New-Object System.Net.Http.HttpRequestMessage('POST', $url)
    $req.Headers.Authorization = New-Object System.Net.Http.Headers.AuthenticationHeaderValue('Bearer', $token)
    $req.Content = New-Object System.Net.Http.StringContent('{}', [System.Text.Encoding]::UTF8, 'application/json')
    $tasks += $client.SendAsync($req)
}
[System.Threading.Tasks.Task]::WaitAll($tasks)

$success = 0
$orderNos = @()
foreach ($task in $tasks) {
    $resp = $task.Result
    $body = $resp.Content.ReadAsStringAsync().Result | ConvertFrom-Json
    if ($body.data.success -eq $true) {
        $success++
        $orderNos += $body.data.orderNo
    }
}
Write-Host "并发请求 $($tokens.Count) 个，抢购成功 $success 个" -ForegroundColor Cyan

$remain = [int]((& 'D:\Program Files\Redis-x64-3.0.504\redis-cli.exe' -p 6379 GET "seckill:stock:$seckillProductId") -replace '\D', '0')
Write-Host "Redis 剩余库存: $remain"

$dbCount = [int]((& 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe' -h 127.0.0.1 -P 3306 -u root -proot -N -e "SELECT COUNT(*) FROM seckill_db.seckill_record WHERE activity_id=$activityId AND seckill_product_id=$seckillProductId") 2>$null)
Write-Host "秒杀记录数: $dbCount"

Start-Sleep -Seconds 10
$orderNosJoined = "'" + ($orderNos -join "','") + "'"
$orderCount = 0
if ($orderNos.Count -gt 0) {
    $orderCount = [int]((& 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe' -h 127.0.0.1 -P 3306 -u root -proot -N -e "SELECT COUNT(*) FROM order_db.orders WHERE order_type='SECKILL' AND order_no IN ($orderNosJoined)") 2>$null)
}
Write-Host "MQ 异步落单数: $orderCount"

$ok = $true
if ($success -gt 10) { Write-Host "[FAIL] 出现超卖：成功 $success 个，超过库存 10" -ForegroundColor Red; $ok = $false }
if ($remain -lt 0) { Write-Host "[FAIL] Redis 库存出现负数：$remain" -ForegroundColor Red; $ok = $false }
if ($dbCount -ne $success) { Write-Host "[FAIL] 数据库秒杀记录 $dbCount 与成功数 $success 不一致" -ForegroundColor Red; $ok = $false }
if ($orderCount -ne $success) { Write-Host "[FAIL] MQ 异步订单 $orderCount 与成功数 $success 不一致" -ForegroundColor Red; $ok = $false }

if ($ok) {
    Write-Host "[PASS] 库存 10 时并发 20 请求，无超卖、无负数、落单一致" -ForegroundColor Green
} else {
    Write-Host "[FAIL] 秒杀并发测试未通过" -ForegroundColor Red
    exit 1
}
