param(
    [switch]$ForceLocal
)

$ErrorActionPreference = 'Stop'
$Root = Split-Path $PSScriptRoot -Parent
$EnvFile = Join-Path $PSScriptRoot '.env-shop'

function Write-Step($msg) {
    Write-Host "[infra] $msg" -ForegroundColor Cyan
}

function Wait-Port($port, $timeoutSeconds) {
    $deadline = (Get-Date).AddSeconds($timeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        if (Test-NetConnection -ComputerName 127.0.0.1 -Port $port -WarningAction SilentlyContinue -InformationLevel Quiet) {
            return $true
        }
        Start-Sleep -Seconds 2
    }
    return $false
}

$docker = Get-Command docker -ErrorAction SilentlyContinue
$useDocker = $docker -and -not $ForceLocal

if ($useDocker) {
    Write-Step "Docker detected, starting MySQL/Redis/RabbitMQ/Nacos containers"
    docker compose -f (Join-Path $PSScriptRoot 'docker-compose.infra.yml') up -d
    Set-Content -Path $EnvFile -Value @"
MYSQL_HOST=127.0.0.1
MYSQL_PORT=3307
REDIS_HOST=127.0.0.1
REDIS_PORT=6380
RABBITMQ_HOST=127.0.0.1
RABBITMQ_PORT=5672
RABBITMQ_USER=shop
RABBITMQ_PASSWORD=shop123456
INFRA_MODE=docker
"@
    if (-not (Wait-Port 3307 120)) {
        Write-Host "[infra] MySQL container did not become ready in time" -ForegroundColor Yellow
    }
    if (-not (Wait-Port 8848 120)) {
        Write-Host "[infra] Nacos container did not become ready in time" -ForegroundColor Yellow
    }
    exit 0
}

Write-Step "Docker not found or forced local mode, using local services"

if (-not (Wait-Port 3306 5)) {
    throw "Local MySQL is not listening on 3306. Start MySQL80 or install Docker and rerun."
}
Write-Step "MySQL is running on 3306"

$redisExe = 'D:\Program Files\Redis-x64-3.0.504\redis-server.exe'
if (-not (Wait-Port 6379 5)) {
    if (Test-Path $redisExe) {
        Write-Step "Starting local Redis"
        Start-Process -FilePath $redisExe -WindowStyle Hidden
        if (-not (Wait-Port 6379 30)) {
            throw "Redis failed to start on 6379"
        }
    } else {
        throw "Redis not running and redis-server.exe not found at $redisExe"
    }
}
Write-Step "Redis is running on 6379"

$rabbit = Get-Service -Name RabbitMQ -ErrorAction SilentlyContinue
if (-not (Wait-Port 5672 5)) {
    if ($rabbit) {
        Write-Step "Starting RabbitMQ service"
        try {
            Start-Service -Name RabbitMQ
        } catch {
            Write-Host "Start RabbitMQ service failed: $_" -ForegroundColor Yellow
            Write-Host "Run PowerShell as Administrator, or install Docker and rerun." -ForegroundColor Yellow
        }
        if (-not (Wait-Port 5672 120)) {
            Write-Host "RabbitMQ service could not start, trying portable Erlang mode" -ForegroundColor Yellow
            & (Join-Path $PSScriptRoot 'setup-rabbitmq.ps1')
        }
    } else {
        throw "RabbitMQ service not found. Install RabbitMQ or use Docker."
    }
}
Write-Step "RabbitMQ is running on 5672"

if (-not (Wait-Port 8848 5)) {
    $toolsDir = Join-Path $PSScriptRoot 'tools'
    $nacosDir = Join-Path $toolsDir 'nacos'
    New-Item -ItemType Directory -Force -Path $toolsDir | Out-Null
    if (-not (Test-Path (Join-Path $nacosDir 'bin\startup.cmd'))) {
        $zip = Join-Path $toolsDir 'nacos-server-2.2.3.zip'
    if (-not (Test-Path $zip)) {
            Write-Step "Downloading Nacos 2.2.3 (~150MB), please wait"
            & curl.exe -sL --connect-timeout 15 --max-time 540 -o $zip 'https://ghfast.top/https://github.com/alibaba/nacos/releases/download/2.2.3/nacos-server-2.2.3.zip'
            if ($LASTEXITCODE -ne 0 -or (Get-Item $zip).Length -lt 100000000) {
                Remove-Item $zip -Force -ErrorAction SilentlyContinue
                Invoke-WebRequest -Uri 'https://github.com/alibaba/nacos/releases/download/2.2.3/nacos-server-2.2.3.zip' -OutFile $zip
            }
        }
        Write-Step "Extracting Nacos"
        Expand-Archive -Path $zip -DestinationPath $toolsDir -Force
        if (-not (Test-Path (Join-Path $nacosDir 'bin\startup.cmd'))) {
            $extractTmp = Join-Path $toolsDir 'nacos-extract'
            Expand-Archive -Path $zip -DestinationPath $extractTmp -Force
            Move-Item (Join-Path $extractTmp 'nacos') $nacosDir -Force
            Remove-Item $extractTmp -Recurse -Force -ErrorAction SilentlyContinue
        }
    }
    Write-Step "Starting Nacos standalone"
    Start-Process -FilePath 'cmd.exe' -ArgumentList '/c', "call `"$nacosDir\bin\startup.cmd`" -m standalone" -WindowStyle Hidden
    if (-not (Wait-Port 8848 180)) {
        Write-Host "Nacos did not open 8848 yet, continue anyway" -ForegroundColor Yellow
    }
}
Write-Step "Nacos is running on 8848"

Set-Content -Path $EnvFile -Value @"
MYSQL_HOST=127.0.0.1
MYSQL_PORT=3306
REDIS_HOST=127.0.0.1
REDIS_PORT=6379
RABBITMQ_HOST=127.0.0.1
RABBITMQ_PORT=5672
RABBITMQ_USER=guest
RABBITMQ_PASSWORD=guest
INFRA_MODE=local
"@
Write-Step "Infrastructure ready, environment file written to $EnvFile"
