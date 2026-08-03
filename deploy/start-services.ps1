param(
    [switch]$SkipBuild
)

$ErrorActionPreference = 'Stop'
$Root = Split-Path $PSScriptRoot -Parent
$EnvFile = Join-Path $PSScriptRoot '.env-shop'
$PidFile = Join-Path $PSScriptRoot '.pids'
$LogDir = Join-Path $PSScriptRoot 'logs'
New-Item -ItemType Directory -Force -Path $LogDir | Out-Null

if (Test-Path $EnvFile) {
    Get-Content $EnvFile | ForEach-Object {
        if ($_ -match '^([^#=]+)=(.*)$') {
            [Environment]::SetEnvironmentVariable($matches[1].Trim(), $matches[2].Trim(), 'Process')
        }
    }
}

$mvn = 'D:\apache-maven-3.6.3\bin\mvn.cmd'
$services = @(
    @{ Name = 'gateway'; Port = 8080; Jar = 'gateway\target\gateway-1.0.0.jar' },
    @{ Name = 'auth-service'; Port = 8081; Jar = 'auth-service\target\auth-service-1.0.0.jar' },
    @{ Name = 'product-service'; Port = 8082; Jar = 'product-service\target\product-service-1.0.0.jar' },
    @{ Name = 'order-service'; Port = 8083; Jar = 'order-service\target\order-service-1.0.0.jar' },
    @{ Name = 'pay-service'; Port = 8084; Jar = 'pay-service\target\pay-service-1.0.0.jar' },
    @{ Name = 'seckill-service'; Port = 8085; Jar = 'seckill-service\target\seckill-service-1.0.0.jar' },
    @{ Name = 'logistics-service'; Port = 8086; Jar = 'logistics-service\target\logistics-service-1.0.0.jar' },
    @{ Name = 'notify-service'; Port = 8087; Jar = 'notify-service\target\notify-service-1.0.0.jar' }
)

function Wait-Port($port, $timeoutSeconds) {
    $deadline = (Get-Date).AddSeconds($timeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        if (Test-NetConnection -ComputerName 127.0.0.1 -Port $port -WarningAction SilentlyContinue -InformationLevel Quiet) {
            return $true
        }
        Start-Sleep -Seconds 3
    }
    return $false
}

if (-not $SkipBuild) {
    Write-Host "[services] Building backend jars"
    & $mvn -f (Join-Path $Root 'backend\pom.xml') package -DskipTests
    if ($LASTEXITCODE -ne 0) {
        throw "Backend build failed"
    }
}

$pids = @()
foreach ($svc in $services) {
    $jar = Join-Path $Root ('backend\' + $svc.Jar)
    if (-not (Test-Path $jar)) {
        throw "Jar not found: $jar"
    }
    $out = Join-Path $LogDir "$($svc.Name).log"
    $err = Join-Path $LogDir "$($svc.Name).err.log"
    Write-Host "[services] starting $($svc.Name) on port $($svc.Port)"
    $proc = Start-Process -FilePath 'java' -ArgumentList '-jar', $jar -WorkingDirectory $Root -WindowStyle Hidden -RedirectStandardOutput $out -RedirectStandardError $err -PassThru
    $pids += $proc.Id
}
$pids | Set-Content $PidFile

foreach ($svc in $services) {
    if (-not (Wait-Port $svc.Port 150)) {
        Write-Host "[services] $($svc.Name) did not open port $($svc.Port), check deploy/logs/$($svc.Name).err.log" -ForegroundColor Yellow
    }
}
Write-Host "[services] All services started. Gateway: http://localhost:8080" -ForegroundColor Green

