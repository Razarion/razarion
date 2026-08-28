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
Write-Host "`n[0/6] Setting JDK 21 environment..." -ForegroundColor Yellow
. C:\dev\scripts\jdk21.ps1
Write-Host "JAVA_HOME: $env:JAVA_HOME" -ForegroundColor Gray

# 0b. Commit ermitteln. Das Image wird zusaetzlich zu :latest mit dem Kurz-SHA getaggt und der
#     Rollout laeuft auf diesen festen Tag. Damit ist ablesbar, welcher Stand live ist, und ein
#     Rollback ist ein kubectl-Aufruf statt zweier Maven-Durchlaeufe.
#     Uncommittete Aenderungen brechen ab: ein Image, das keinem Commit entspricht, macht genau
#     die Zuordnung wertlos, wegen der das hier steht.
$Sha = (git -C $ProjectRoot rev-parse --short HEAD 2>$null)
if (-not $Sha) {
    Write-Host "Kein git-Commit ermittelbar - Abbruch." -ForegroundColor Red
    exit 1
}
$Sha = $Sha.Trim()
if (git -C $ProjectRoot status --porcelain) {
    Write-Host "Arbeitsverzeichnis nicht sauber. Das Image waere keinem Commit zuzuordnen." -ForegroundColor Red
    Write-Host "Committen oder verwerfen, dann erneut deployen." -ForegroundColor Yellow
    exit 1
}
Write-Host "Deploying commit $Sha" -ForegroundColor Gray

# 1. Full Maven Build (inkl. Frontend) - erster Durchlauf
# -Pprod aktiviert das TeaVM-Profil der beiden WASM-Module (minifying,
# optimizationLevel=FULL, kein debugInfo/sourceMaps). Ohne den Schalter lief die Engine
# auf PROD als Debug-Build: Worker-Tick im Schnitt 179 ms statt der 100 ms, die er hat,
# und die Module zusammen 2,6 statt 1,3 MB. Das Profil gibt es nur in
# razarion-client-teavm und razarion-client-worker-teavm - fuer den Jib-Schritt unten
# ist es deshalb weder noetig noch gueltig.
Write-Host "`n[1/6] Building project with Maven (1st pass)..." -ForegroundColor Yellow
Set-Location $ProjectRoot
mvn clean install -DskipTests -Pprod
if ($LASTEXITCODE -ne 0) {
    Write-Host "Maven build (1st pass) failed!" -ForegroundColor Red
    exit 1
}
Write-Host "Build (1st pass) successful!" -ForegroundColor Green

# 2. Full Maven Build - zweiter Durchlauf
Write-Host "`n[2/6] Building project with Maven (2nd pass)..." -ForegroundColor Yellow
mvn clean install -DskipTests -Pprod
if ($LASTEXITCODE -ne 0) {
    Write-Host "Maven build (2nd pass) failed!" -ForegroundColor Red
    exit 1
}
Write-Host "Build (2nd pass) successful!" -ForegroundColor Green

# 3. Jib Build & Push
Write-Host "`n[3/6] Building and pushing Docker image with Jib ($Sha)..." -ForegroundColor Yellow
Set-Location "$ProjectRoot\razarion-server"
# jib.to.tags kommt zusaetzlich zu dem :latest aus der pom - der bleibt, damit nichts bricht,
# was sich darauf verlaesst.
mvn compile jib:build "-Djib.to.tags=$Sha"
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

# 5. Rollout auf den festen Tag. Das Deployment-Manifest liegt unter k8s\templates\, damit das
#    kubectl apply in Schritt 4 es nicht mit dem Platzhalter im Image anwendet - apply -f auf ein
#    Verzeichnis ist nicht rekursiv, deshalb genuegt das Unterverzeichnis.
#    Ueber eine Datei statt per Pipe, weil Windows PowerShell beim Weiterreichen an ein natives
#    Programm eine Byte-Order-Mark voranstellen kann, an der der YAML-Parser scheitert.
Write-Host "`n[5/6] Rolling out $Sha..." -ForegroundColor Yellow
$Template = "$ProjectRoot\k8s\templates\razarion-server-deployment.yaml"
$Rendered = Join-Path $env:TEMP "razarion-server-deployment.$Sha.yaml"
[System.IO.File]::WriteAllText($Rendered, (Get-Content $Template -Raw).Replace('__TAG__', $Sha))
kubectl apply -f $Rendered
$applyExit = $LASTEXITCODE
Remove-Item $Rendered -Force -ErrorAction SilentlyContinue
if ($applyExit -ne 0) {
    Write-Host "Rollout failed!" -ForegroundColor Red
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
Write-Host "Live: $Sha  https://github.com/Razarion/razarion/commit/$Sha" -ForegroundColor Gray
Write-Host "Rollback: kubectl set image deployment/razarion-server razarion-server=us-central1-docker.pkg.dev/neural-passkey-426618-j3/razarion-repo/razarion-server:<sha>" -ForegroundColor DarkGray
Set-Location $ProjectRoot
