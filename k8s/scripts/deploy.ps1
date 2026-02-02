# Razarion Deploy Script
# Baut den Server inkl. Frontend, erstellt Docker-Image und deployt auf GKE

$ErrorActionPreference = "Stop"
$ProjectRoot = (Get-Item $PSScriptRoot).Parent.Parent.FullName
$StartTime = Get-Date

Write-Host "=== Razarion Deploy ===" -ForegroundColor Cyan

# 0. JDK 21 Umgebung setzen
Write-Host "`n[0/5] Setting JDK 21 environment..." -ForegroundColor Yellow
. C:\dev\scripts\jdk21.ps1
Write-Host "JAVA_HOME: $env:JAVA_HOME" -ForegroundColor Gray

# 1. Full Maven Build (inkl. Frontend) - erster Durchlauf
Write-Host "`n[1/5] Building project with Maven (1st pass)..." -ForegroundColor Yellow
Set-Location $ProjectRoot
mvn clean install -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "Maven build (1st pass) failed!" -ForegroundColor Red
    exit 1
}
Write-Host "Build (1st pass) successful!" -ForegroundColor Green

# 2. Full Maven Build - zweiter Durchlauf
Write-Host "`n[2/5] Building project with Maven (2nd pass)..." -ForegroundColor Yellow
mvn clean install -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "Maven build (2nd pass) failed!" -ForegroundColor Red
    exit 1
}
Write-Host "Build (2nd pass) successful!" -ForegroundColor Green

# 3. Jib Build & Push
Write-Host "`n[3/5] Building and pushing Docker image with Jib..." -ForegroundColor Yellow
Set-Location "$ProjectRoot\razarion-server"
mvn compile jib:build
if ($LASTEXITCODE -ne 0) {
    Write-Host "Jib build failed!" -ForegroundColor Red
    exit 1
}
Write-Host "Image pushed successfully!" -ForegroundColor Green

# 4. Rollout Restart
Write-Host "`n[4/5] Restarting Kubernetes deployment..." -ForegroundColor Yellow
kubectl rollout restart deployment/razarion-server
if ($LASTEXITCODE -ne 0) {
    Write-Host "Rollout restart failed!" -ForegroundColor Red
    exit 1
}

# Wait for rollout
Write-Host "Waiting for rollout to complete..." -ForegroundColor Yellow
kubectl rollout status deployment/razarion-server --timeout=120s

# 5. Invalidate CDN Cache
Write-Host "`n[5/5] Invalidating CDN cache..." -ForegroundColor Yellow
gcloud compute url-maps invalidate-cdn-cache k8s2-um-pgzfjs2a-default-razarion-ingress-asi570ev --path "/*" --async
if ($LASTEXITCODE -ne 0) {
    Write-Host "CDN cache invalidation failed (non-critical)" -ForegroundColor Yellow
} else {
    Write-Host "CDN cache invalidation started!" -ForegroundColor Green
}

$EndTime = Get-Date
$Duration = $EndTime - $StartTime

Write-Host "`n=== Deploy Complete ===" -ForegroundColor Cyan
Write-Host "Total time: $($Duration.Minutes)m $($Duration.Seconds)s" -ForegroundColor Gray
Set-Location $ProjectRoot
