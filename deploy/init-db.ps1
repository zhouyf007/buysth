param(
    [string]$MysqlUser = 'root',
    [string]$MysqlPassword = ''
)

$ErrorActionPreference = 'Stop'
$Root = Split-Path $PSScriptRoot -Parent
$EnvFile = Join-Path $PSScriptRoot '.env-shop'
$mysqlPort = 3306
if (Test-Path $EnvFile) {
    $lines = Get-Content $EnvFile
    $mysqlLine = $lines | Where-Object { $_ -like 'MYSQL_PORT=*' }
    if ($mysqlLine) { $mysqlPort = ($mysqlLine -split '=')[1] }
}

$mysql = 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe'
if (-not (Test-Path $mysql)) {
    $mysql = 'mysql'
}

Write-Host "[db] Initializing databases and seed data on port $mysqlPort"
if ($MysqlPassword -eq '') {
    & $mysql -h 127.0.0.1 -P $mysqlPort -u $MysqlUser -e "SELECT 1"
    if ($LASTEXITCODE -ne 0) {
        $secure = Read-Host "请输入 MySQL root 密码"
        $MysqlPassword = $secure
    }
}

Get-ChildItem (Join-Path $Root 'sql') -Filter '*.sql' | Sort-Object Name | ForEach-Object {
    Write-Host "[db] applying $($_.Name)"
    if ($MysqlPassword -eq '') {
        & $mysql -h 127.0.0.1 -P $mysqlPort -u $MysqlUser --default-character-set=utf8mb4 -e "source $($_.FullName)"
    } else {
        & $mysql -h 127.0.0.1 -P $mysqlPort -u $MysqlUser -p$MysqlPassword --default-character-set=utf8mb4 -e "source $($_.FullName)"
    }
    if ($LASTEXITCODE -ne 0) {
        throw "Failed to apply $($_.Name)"
    }
}
Write-Host "[db] Database initialized" -ForegroundColor Green

