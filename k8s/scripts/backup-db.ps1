# MariaDB Backup Script fuer Razarion K8s
# Holt einen Dump via kubectl port-forward + lokales docker-mysqldump
#
# Sicherheit:
#  - kubectl port-forward ist KEINE oeffentliche Exposition. Tunnel laeuft
#    TLS-verschluesselt via kube-apiserver. Lokaler Port bindet explizit
#    nur an 127.0.0.1. Service mariadb-service bleibt clusterIP: None.
#  - Root-PW kommt aus dem k8s-Secret, nie hardcoded. Uebergabe via MYSQL_PWD
#    env var (nicht via -p<pw> -> nicht in der Prozessliste sichtbar).
#  - Port-Forward Job wird im finally-Block beendet, auch bei Fehlern.

$ErrorActionPreference = "Stop"

$BackupDir       = "C:\dev\backup\razarion_db"
$DateString      = Get-Date -Format "yyyy_MM_dd"
$BackupName      = "db_$DateString"
$TempSqlFile     = "$BackupDir\$BackupName.sql"
$ZipFile         = "$BackupDir\$BackupName.zip"
$LocalPort       = 33306
$DockerContainer = "db"   # lokaler MariaDB-Container, liefert die mysqldump-Binary

if (-not (Test-Path $BackupDir)) {
    New-Item -ItemType Directory -Path $BackupDir | Out-Null
}

if (Test-Path $ZipFile) {
    $Counter = 2
    do {
        $ZipFile = "$BackupDir\${BackupName}_$Counter.zip"
        $Counter++
    } while (Test-Path $ZipFile)
    Write-Host "Backup fuer heute existiert bereits, verwende: $ZipFile"
}

Write-Host "Starte MariaDB Backup via Port-Forward..." -ForegroundColor Cyan

# 1. Root-Passwort aus k8s-Secret lesen
$rootPwB64 = kubectl get secret mariadb-secrets -o jsonpath='{.data.root-password}'
if (-not $rootPwB64) { throw "Konnte mariadb-secrets nicht lesen (kubectl-Context aktiv?)" }
$rootPw = [System.Text.Encoding]::UTF8.GetString([Convert]::FromBase64String($rootPwB64))

# 2. Lokalen docker-Container pruefen (fuer mysqldump)
$running = docker ps --filter "name=^${DockerContainer}$" --format "{{.Names}}"
if (-not $running) {
    throw "Lokaler MariaDB-Container '$DockerContainer' laeuft nicht. Bitte 'docker-compose up -d' im razarion-server/docker Verzeichnis ausfuehren."
}

# 3. Port-Forward im Hintergrund starten (explizit nur 127.0.0.1)
Write-Host "Starte port-forward 127.0.0.1:$LocalPort -> razarion-mariadb-0:3306..." -ForegroundColor Cyan
$pfJob = Start-Job -ScriptBlock {
    param($port)
    kubectl port-forward --address 127.0.0.1 razarion-mariadb-0 "${port}:3306"
} -ArgumentList $LocalPort

try {
    # Warten bis Port reagiert (max 15s)
    $ready = $false
    for ($i = 0; $i -lt 30; $i++) {
        Start-Sleep -Milliseconds 500
        try {
            $tc = New-Object System.Net.Sockets.TcpClient
            $tc.Connect("127.0.0.1", $LocalPort)
            $tc.Close()
            $ready = $true
            break
        } catch { }
    }
    if (-not $ready) { throw "Port-Forward konnte nicht aufgebaut werden" }
    Write-Host "Port-Forward bereit." -ForegroundColor Green

    # 4. mariadb-dump im docker-Container, Output in Container-Datei
    #    (Linux-Filesystem -> kein PowerShell CRLF/Encoding/BOM Problem)
    #    'mariadb-dump' ist der Nachfolger von 'mysqldump' in MariaDB 10.5+
    Write-Host "Erstelle Dump via docker exec $DockerContainer mariadb-dump..." -ForegroundColor Cyan
    #    --max-allowed-packet=1G: GLTF.glb rows are large binary blobs; the client must accept
    #    packets bigger than its small default or the connection is dropped mid-row (error 2013).
    docker exec -e "MYSQL_PWD=$rootPw" $DockerContainer sh -c `
        "mariadb-dump --hex-blob --single-transaction --quick --max-allowed-packet=1G -h host.docker.internal -P $LocalPort -u root razarion > /tmp/prod_dump.sql 2>/tmp/prod_dump.err"
    $dumpExit = $LASTEXITCODE
    if ($dumpExit -ne 0) {
        $err = docker exec $DockerContainer cat /tmp/prod_dump.err
        docker exec $DockerContainer rm -f /tmp/prod_dump.sql /tmp/prod_dump.err 2>$null
        throw "mariadb-dump fehlgeschlagen (ExitCode $dumpExit): $err"
    }

    # Container-Dateigroesse fuer spaeteren Vergleich
    $podSizeOut = docker exec $DockerContainer sh -c "wc -c < /tmp/prod_dump.sql"
    $podSize = [long]$podSizeOut.Trim()
    Write-Host "Dump im Container: $([math]::Round($podSize/1MB, 2)) MB" -ForegroundColor Cyan

    # 5. docker cp aus Container heraus (zuverlaessiger als kubectl cp auf Windows)
    Write-Host "Kopiere Dump aus Container..." -ForegroundColor Cyan
    docker cp "${DockerContainer}:/tmp/prod_dump.sql" $TempSqlFile
    if ($LASTEXITCODE -ne 0) { throw "docker cp fehlgeschlagen" }

    docker exec $DockerContainer rm -f /tmp/prod_dump.sql /tmp/prod_dump.err

    if (-not (Test-Path $TempSqlFile)) { throw "Lokale SQL-Datei nicht vorhanden" }
    $localSize = (Get-Item $TempSqlFile).Length
    if ($localSize -ne $podSize) {
        Remove-Item $TempSqlFile -Force
        throw "Groessen stimmen nicht ueberein! Container: $podSize Lokal: $localSize"
    }
    Write-Host "Download OK: $([math]::Round($localSize/1MB, 2)) MB" -ForegroundColor Green
}
finally {
    Write-Host "Beende port-forward..." -ForegroundColor Cyan
    Stop-Job $pfJob -ErrorAction SilentlyContinue | Out-Null
    Remove-Job $pfJob -Force -ErrorAction SilentlyContinue | Out-Null
}

# 6. ZIP erstellen
Write-Host "Erstelle ZIP-Archiv..." -ForegroundColor Cyan
Compress-Archive -Path $TempSqlFile -DestinationPath $ZipFile -Force
Remove-Item $TempSqlFile

$FileSize = (Get-Item $ZipFile).Length / 1MB
Write-Host ""
Write-Host "Backup erfolgreich erstellt: $ZipFile" -ForegroundColor Green
Write-Host "Groesse: $([math]::Round($FileSize, 2)) MB"
