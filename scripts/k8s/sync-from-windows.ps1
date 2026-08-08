# Sync repo to build host via scp/rsync (Windows)
# Usage (from repo root):
#   powershell -ExecutionPolicy Bypass -File scripts\k8s\sync-from-windows.ps1
# Options:
#   -BuildHost kssrol@100.66.0.3
#   -RemoteDir ~/jianghu_ling
#   -IncludeGit

param(
    [string]$BuildHost = "kssrol@100.66.0.3",
    [string]$RemoteDir = "~/jianghu_ling",
    [switch]$IncludeGit
)

$ErrorActionPreference = "Stop"

$RepoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
Set-Location $RepoRoot

Write-Host "==> Source: $RepoRoot"
Write-Host "==> Target: ${BuildHost}:${RemoteDir}"

ssh $BuildHost "mkdir -p $RemoteDir"
if ($LASTEXITCODE -ne 0) {
    throw "ssh mkdir failed, exit=$LASTEXITCODE"
}

$rsyncCmd = Get-Command rsync -ErrorAction SilentlyContinue
if ($null -ne $rsyncCmd) {
    $excludeArgs = @(
        "--exclude=node_modules",
        "--exclude=frontend/node_modules",
        "--exclude=backend/target",
        "--exclude=frontend/dist",
        "--exclude=.idea",
        "--exclude=.vscode",
        "--exclude=*.iml",
        "--exclude=Thumbs.db",
        "--exclude=.DS_Store",
        "--exclude=docker/.env",
        "--exclude=.env"
    )
    if (-not $IncludeGit) {
        $excludeArgs += "--exclude=.git"
    }
    & rsync -avz --delete @excludeArgs "./" "${BuildHost}:${RemoteDir}/"
    if ($LASTEXITCODE -ne 0) {
        throw "rsync failed, exit=$LASTEXITCODE"
    }
} else {
    Write-Host "rsync not found; using robocopy + scp"
    $stamp = Get-Date -Format "yyyyMMddHHmmss"
    $tmp = Join-Path $env:TEMP "jianghu_ling_sync_$stamp"
    if (Test-Path $tmp) {
        Remove-Item -Recurse -Force $tmp
    }
    New-Item -ItemType Directory -Path $tmp | Out-Null

    $xd = @("node_modules", "target", "dist", ".idea", ".vscode")
    if (-not $IncludeGit) {
        $xd += ".git"
    }
    $xf = @("*.iml", "Thumbs.db", ".DS_Store", ".env")

    $rcArgs = @($RepoRoot, $tmp, "/E", "/NFL", "/NDL", "/NJH", "/NJS", "/XD") + $xd + @("/XF") + $xf
    & robocopy @rcArgs | Out-Null
    $roboExit = $LASTEXITCODE
    if ($roboExit -ge 8) {
        Remove-Item -Recurse -Force $tmp -ErrorAction SilentlyContinue
        throw "robocopy failed, exit=$roboExit"
    }

    & scp -r "$tmp\*" "${BuildHost}:${RemoteDir}/"
    $scpExit = $LASTEXITCODE
    Remove-Item -Recurse -Force $tmp -ErrorAction SilentlyContinue
    if ($scpExit -ne 0) {
        throw "scp failed, exit=$scpExit"
    }
}

Write-Host "==> Sync done. On build host run:"
Write-Host "    cd ~/jianghu_ling"
Write-Host "    bash scripts/k8s/02-build-push.sh"
Write-Host "    bash scripts/k8s/03-deploy.sh"
