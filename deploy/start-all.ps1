$ErrorActionPreference = 'Stop'
Write-Host "========== 数码商城一键部署 ==========" -ForegroundColor Magenta

& (Join-Path $PSScriptRoot 'start-infra.ps1')
$envFile = Join-Path $PSScriptRoot '.env-shop'
$dbPassword = 'root123456'
if (Test-Path $envFile) {
    $mode = (Get-Content $envFile | Where-Object { $_ -like 'INFRA_MODE=*' })
    if ($mode -eq 'INFRA_MODE=local') {
        $dbPassword = 'root'
    }
}
& (Join-Path $PSScriptRoot 'init-db.ps1') -MysqlPassword $dbPassword
& (Join-Path $PSScriptRoot 'start-services.ps1')
& (Join-Path $PSScriptRoot 'start-frontends.ps1')

Write-Host ""
Write-Host "部署完成："
Write-Host "  用户端:  http://localhost:5173"
Write-Host "  管理端:  http://localhost:5174"
Write-Host "  网关:    http://localhost:8080"
Write-Host "  Nacos:   http://localhost:8848/nacos"
Write-Host "  RabbitMQ: http://localhost:15672"
Write-Host "账号: 用户 user/123456，管理员 admin/123456"
