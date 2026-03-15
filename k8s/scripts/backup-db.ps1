# MariaDB Backup Script für Razarion K8s
# Erstellt ein Backup der MariaDB und speichert es lokal als ZIP

$ErrorActionPreference = "Stop"

$BackupDir = "C:\dev\backup\razarion_db"
$DateString = Get-Date -Format "yyyy_MM_dd"
$BackupName = "db_$DateString"
$TempSqlFile = "$BackupDir\$BackupName.sql"
$ZipFile = "$BackupDir\$BackupName.zip"

# Prüfe ob Backup-Verzeichnis existiert
if (-not (Test-Path $BackupDir)) {
    New-Item -ItemType Directory -Path $BackupDir | Out-Null
}

# Prüfe ob heute bereits ein Backup existiert
if (Test-Path $ZipFile) {
    $Counter = 2
    do {
        $ZipFile = "$BackupDir\${BackupName}_$Counter.zip"
        $Counter++
    } while (Test-Path $ZipFile)
    Write-Host "Backup fuer heute existiert bereits, verwende: $ZipFile"
}

Write-Host "Starte MariaDB Backup..."

# 1. Dump im Pod erstellen
Write-Host "Erstelle Dump im Pod..." -ForegroundColor Cyan
kubectl exec razarion-mariadb-0 -- bash -c "mysqldump --hex-blob -u root -p`$MARIADB_ROOT_PASSWORD razarion > /tmp/backup.sql 2>/dev/null"
if ($LASTEXITCODE -ne 0) { throw "Fehler beim Erstellen des Dumps" }

# Groesse im Pod ermitteln
$podSize = kubectl exec razarion-mariadb-0 -- bash -c "wc -c < /tmp/backup.sql"
$podSize = [long]$podSize.Trim()
Write-Host "Dump im Pod: $([math]::Round($podSize/1MB, 2)) MB" -ForegroundColor Cyan

# 2. Dump lokal herunterladen
Write-Host "Lade Dump herunter nach: $TempSqlFile" -ForegroundColor Cyan
$LocalTemp = ".\backup_temp.sql"
# Warnungen von kubectl cp ignorieren (tar warnings sind normal)
$ErrorActionPreference = "SilentlyContinue"
kubectl cp razarion-mariadb-0:/tmp/backup.sql $LocalTemp *>$null
$ErrorActionPreference = "Stop"

# Prüfe ob Datei existiert und nicht leer ist
if (-not (Test-Path $LocalTemp)) {
    throw "Fehler: Backup-Datei wurde nicht erstellt"
}

# Verschiebe in Zielverzeichnis
Move-Item -Path $LocalTemp -Destination $TempSqlFile -Force
$localSize = (Get-Item $TempSqlFile).Length
if ($localSize -eq 0) {
    Remove-Item $TempSqlFile
    throw "Fehler: Backup-Datei ist leer"
}

# 3. Groessen vergleichen — kubectl cp schneidet manchmal Dateien ab
if ($localSize -ne $podSize) {
    $localMB = [math]::Round($localSize/1MB, 2)
    $podMB = [math]::Round($podSize/1MB, 2)
    Write-Host "WARNUNG: Groessen stimmen nicht ueberein! Pod: $podMB MB, Lokal: $localMB MB" -ForegroundColor Red
    Write-Host "Versuche erneuten Download mit base64-Kodierung..." -ForegroundColor Yellow

    Remove-Item $TempSqlFile -Force

    # Fallback: base64-kodiert uebertragen um kubectl cp Probleme zu umgehen
    kubectl exec razarion-mariadb-0 -- bash -c "base64 /tmp/backup.sql" > "$TempSqlFile.b64"
    # Dekodieren mit certutil (Windows-Bordmittel)
    certutil -decode "$TempSqlFile.b64" $TempSqlFile > $null
    Remove-Item "$TempSqlFile.b64" -Force

    $localSize = (Get-Item $TempSqlFile).Length
    if ($localSize -ne $podSize) {
        $localMB = [math]::Round($localSize/1MB, 2)
        Remove-Item $TempSqlFile
        throw "Fehler: Auch base64-Transfer fehlgeschlagen. Pod: $podMB MB, Lokal: $localMB MB"
    }
}

Write-Host "Download OK ($([math]::Round($localSize/1MB, 2)) MB)" -ForegroundColor Cyan

# 4. ZIP erstellen
Write-Host "Erstelle ZIP-Archiv..."
Compress-Archive -Path $TempSqlFile -DestinationPath $ZipFile -Force

# 5. Temporaere SQL-Datei loeschen
Remove-Item $TempSqlFile

# 6. Backup im Pod aufraeumen
kubectl exec razarion-mariadb-0 -- rm -f /tmp/backup.sql

$FileSize = (Get-Item $ZipFile).Length / 1MB
Write-Host ""
Write-Host "Backup erfolgreich erstellt: $ZipFile" -ForegroundColor Green
Write-Host "Groesse: $([math]::Round($FileSize, 2)) MB"
