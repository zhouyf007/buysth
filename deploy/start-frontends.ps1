$ErrorActionPreference = 'Stop'
$Root = Split-Path $PSScriptRoot -Parent
$PidFile = Join-Path $PSScriptRoot '.frontend-pids'
$LogDir = Join-Path $PSScriptRoot 'logs'
New-Item -ItemType Directory -Force -Path $LogDir | Out-Null

$apps = @(
    @{ Name = 'user-web'; Port = 5173; Dir = 'frontend\user-web' },
    @{ Name = 'admin-web'; Port = 5174; Dir = 'frontend\admin-web' }
)

foreach ($app in $apps) {
    $dir = Join-Path $Root $app.Dir
    if (-not (Test-Path (Join-Path $dir 'node_modules'))) {
        Write-Host "[frontend] installing $($app.Name) dependencies"
        Push-Location $dir
        npm install --no-audit --no-fund
        Pop-Location
    }
    Write-Host "[frontend] starting $($app.Name) on port $($app.Port)"
    $out = Join-Path $LogDir "$($app.Name).log"
    $err = Join-Path $LogDir "$($app.Name).err.log"
    $proc = Start-Process -FilePath 'npm.cmd' -ArgumentList 'run', 'dev' -WorkingDirectory $dir -WindowStyle Hidden -RedirectStandardOutput $out -RedirectStandardError $err -PassThru
    $proc.Id | Add-Content $PidFile
}
Write-Host "[frontend] user: http://localhost:5173  admin: http://localhost:5174" -ForegroundColor Green

