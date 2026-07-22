# Razarion Deploy Script
# Baut den Server inkl. Frontend, erstellt Docker-Image und deployt auf GKE
#
# Vor dem Rollout bekommen alle verbundenen Spieler eine Neustart-Ankuendigung
# (Countdown-Banner im Client). Das ersetzt die manuelle Admin-Message der alten
# GWT-Version. Voraussetzung sind die Env-Variablen RAZARION_ADMIN_USER und
# RAZARION_ADMIN_PASSWORD; fehlen sie, wird die Ankuendigung uebersprungen und der
# Deploy laeuft trotzdem durch.

$ErrorActionPreference = "Stop"
$ProjectRoot = (Get-Item $PSScriptRoot).Parent.Parent.FullName
$StartTime = Get-Date

# Vorwarnzeit in Sekunden zwischen Ankuendigung und Rollout.
$RestartAnnounceSeconds = 180
$ProdBaseUrl = "https://www.razarion.com"

Write-Host "=== Razarion Deploy ===" -ForegroundColor Cyan

# 0. JDK 21 Umgebung setzen
Write-Host "`n[0/5] Setting JDK 21 environment..." -ForegroundColor Yellow
. C:\dev\scripts\jdk21.ps1
Write-Host "JAVA_HOME: $env:JAVA_HOME" -ForegroundColor Gray

# 1. Full Maven Build (inkl. Frontend) - erster Durchlauf
Write-Host "`n[1/6] Building project with Maven (1st pass)..." -ForegroundColor Yellow
Set-Location $ProjectRoot
mvn clean install -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "Maven build (1st pass) failed!" -ForegroundColor Red
    exit 1
}
Write-Host "Build (1st pass) successful!" -ForegroundColor Green

# 2. Full Maven Build - zweiter Durchlauf
Write-Host "`n[2/6] Building project with Maven (2nd pass)..." -ForegroundColor Yellow
mvn clean install -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "Maven build (2nd pass) failed!" -ForegroundColor Red
    exit 1
}
Write-Host "Build (2nd pass) successful!" -ForegroundColor Green

# 3. Jib Build & Push
Write-Host "`n[3/6] Building and pushing Docker image with Jib..." -ForegroundColor Yellow
Set-Location "$ProjectRoot\razarion-server"
mvn compile jib:build
if ($LASTEXITCODE -ne 0) {
    Write-Host "Jib build failed!" -ForegroundColor Red
    exit 1
}
Write-Host "Image pushed successfully!" -ForegroundColor Green

# 4. Apply k8s manifests (BackendConfig, Service, Deployment etc.) so any
#    YAML changes since the last deploy land in the cluster. `kubectl apply`
#    is a no-op for unchanged manifests.
Write-Host "`n[4/6] Applying Kubernetes manifests..." -ForegroundColor Yellow
kubectl apply -f "$ProjectRoot\k8s\"
if ($LASTEXITCODE -ne 0) {
    Write-Host "kubectl apply failed!" -ForegroundColor Red
    exit 1
}
Write-Host "Manifests applied!" -ForegroundColor Green

# 4b. Spieler vorwarnen. Der laufende Server bekommt die Ankuendigung und schickt sie
#     per SystemConnection an alle Clients, die einen Countdown anzeigen. Fehler hier
#     duerfen den Deploy nicht abbrechen - schlimmstenfalls kommt der Neustart eben
#     unangekuendigt, und der Client faengt das mit dem Reconnect-Overlay ab.
Write-Host "`n[4b/6] Announcing server restart to players ($RestartAnnounceSeconds s)..." -ForegroundColor Yellow
$announced = $false
if (-not $env:RAZARION_ADMIN_USER -or -not $env:RAZARION_ADMIN_PASSWORD) {
    Write-Host "RAZARION_ADMIN_USER / RAZARION_ADMIN_PASSWORD not set - skipping announcement." -ForegroundColor Yellow
} else {
    try {
        $basic = [Convert]::ToBase64String(
            [Text.Encoding]::UTF8.GetBytes("$($env:RAZARION_ADMIN_USER):$($env:RAZARION_ADMIN_PASSWORD)"))
        $token = Invoke-RestMethod -Method Post -Uri "$ProdBaseUrl/rest/user/auth" `
            -Headers @{ Authorization = "Basic $basic" } -ContentType "application/json"
        Invoke-RestMethod -Method Post `
            -Uri "$ProdBaseUrl/rest/planet-mgmt-controller/announceServerRestart?inSeconds=$RestartAnnounceSeconds" `
            -Headers @{ Authorization = "Bearer $token"; "Content-Length" = "0" } | Out-Null
        $announced = $true
        Write-Host "Announcement sent. Waiting $RestartAnnounceSeconds s before the rollout..." -ForegroundColor Green
        Start-Sleep -Seconds $RestartAnnounceSeconds
    } catch {
        Write-Host "Announcement failed (non-critical): $($_.Exception.Message)" -ForegroundColor Yellow
    }
}
if (-not $announced) {
    Write-Host "Continuing without announcement." -ForegroundColor Yellow
}

# 5. Rollout Restart
Write-Host "`n[5/6] Restarting Kubernetes deployment..." -ForegroundColor Yellow
kubectl rollout restart deployment/razarion-server
if ($LASTEXITCODE -ne 0) {
    Write-Host "Rollout restart failed!" -ForegroundColor Red
    exit 1
}

# Wait for rollout
Write-Host "Waiting for rollout to complete..." -ForegroundColor Yellow
kubectl rollout status deployment/razarion-server --timeout=120s

# 6. Invalidate CDN Cache
Write-Host "`n[6/6] Invalidating CDN cache..." -ForegroundColor Yellow
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
