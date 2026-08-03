$Root = Split-Path $PSScriptRoot -Parent
$pidFiles = @(
    (Join-Path $PSScriptRoot '.pids'),
    (Join-Path $PSScriptRoot '.frontend-pids')
)

foreach ($file in $pidFiles) {
    if (Test-Path $file) {
        Get-Content $file | ForEach-Object {
            $proc = Get-Process -Id $_ -ErrorAction SilentlyContinue
            if ($proc) {
                Stop-Process -Id $_ -Force -ErrorAction SilentlyContinue
                Write-Host "Stopped process $_"
            }
        }
        Remove-Item $file -Force
    }
}

$docker = Get-Command docker -ErrorAction SilentlyContinue
if ($docker -and (Test-Path (Join-Path $PSScriptRoot '.env-shop'))) {
    $mode = (Get-Content (Join-Path $PSScriptRoot '.env-shop') | Where-Object { $_ -like 'INFRA_MODE=*' })
    if ($mode -eq 'INFRA_MODE=docker') {
        docker compose -f (Join-Path $PSScriptRoot 'docker-compose.infra.yml') down
    }
}
Write-Host "All shop processes stopped"

