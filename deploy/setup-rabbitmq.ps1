$ErrorActionPreference = 'Stop'
$Root = Split-Path $PSScriptRoot -Parent
$tools = Join-Path $PSScriptRoot 'tools'
$erlDir = Join-Path $tools 'erlang272'
$zip = Join-Path $tools 'otp_win64_27.2.3.zip'
$rabbitHome = 'D:\Program Files\RabbitMQ Server\rabbitmq_server-4.3.4'
$dataDir = Join-Path $PSScriptRoot 'data\rabbitmq'

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

if (Wait-Port 5672 3) {
    Write-Host "[rabbitmq] RabbitMQ is already running on 5672"
    exit 0
}

if (-not (Test-Path (Join-Path $rabbitHome 'sbin\rabbitmq-server.bat'))) {
    throw "RabbitMQ server not found at $rabbitHome"
}

if (-not (Test-Path (Join-Path $erlDir 'bin\erl.exe'))) {
    New-Item -ItemType Directory -Force -Path $tools | Out-Null
    if (-not (Test-Path $zip)) {
        Write-Host "[rabbitmq] Downloading Erlang/OTP 27.2.3 portable runtime (about 150MB)"
        & curl.exe -sL --connect-timeout 15 --max-time 540 -o $zip `
            'https://ghfast.top/https://github.com/erlang/otp/releases/download/OTP-27.2.3/otp_win64_27.2.3.zip'
        if ($LASTEXITCODE -ne 0 -or (Get-Item $zip).Length -lt 100000000) {
            Remove-Item $zip -Force -ErrorAction SilentlyContinue
            throw "Erlang download failed, please install Docker or fix RabbitMQ Erlang manually"
        }
    }
    Write-Host "[rabbitmq] Extracting portable Erlang"
    $tmp = Join-Path $tools 'erlang272-tmp'
    New-Item -ItemType Directory -Force -Path $tmp | Out-Null
    Expand-Archive -Path $zip -DestinationPath $tmp -Force
    Move-Item (Join-Path $tmp 'bin') (Join-Path $erlDir 'bin') -Force
    Move-Item (Join-Path $tmp 'erts-*') $erlDir -Force
    Move-Item (Join-Path $tmp 'lib') (Join-Path $erlDir 'lib') -Force
    Move-Item (Join-Path $tmp 'releases') (Join-Path $erlDir 'releases') -Force
    Remove-Item $tmp -Recurse -Force -ErrorAction SilentlyContinue
}

New-Item -ItemType Directory -Force -Path $dataDir | Out-Null
$env:ERLANG_HOME = $erlDir
$env:PATH = (Join-Path $erlDir 'bin') + ';' + $env:PATH
$env:RABBITMQ_BASE = $dataDir
$env:RABBITMQ_ERLANG_COOKIE = 'shop-demo-cookie-2026'
$env:USERPROFILE = Join-Path $PSScriptRoot 'data'
$env:HOME = Join-Path $PSScriptRoot 'data'

Write-Host "[rabbitmq] Starting RabbitMQ with portable Erlang 27.2.3"
Start-Process -FilePath 'cmd.exe' -ArgumentList @('/c', "call `"$rabbitHome\sbin\rabbitmq-server.bat`"") `
    -WorkingDirectory (Join-Path $PSScriptRoot 'data') -WindowStyle Hidden
if (-not (Wait-Port 5672 150)) {
    throw "RabbitMQ failed to start, check deploy/data/rabbitmq/log"
}
Write-Host "[rabbitmq] RabbitMQ is running on 5672" -ForegroundColor Green

