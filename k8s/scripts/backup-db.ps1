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
Write-Host "Erstelle Dump im Pod..."
kubectl exec razarion-mariadb-0 -- bash -c "mysqldump -u root -p`$MARIADB_ROOT_PASSWORD razarion > /tmp/backup.sql"
if ($LASTEXITCODE -ne 0) { throw "Fehler beim Erstellen des Dumps" }

# 2. Dump lokal herunterladen
Write-Host "Lade Dump herunter nach: $TempSqlFile"
# kubectl cp hat Probleme mit Windows-Pfaden, daher erst lokal dann verschieben
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
$FileCheck = (Get-Item $TempSqlFile).Length
if ($FileCheck -eq 0) {
    Remove-Item $TempSqlFile
    throw "Fehler: Backup-Datei ist leer"
}
Write-Host "Download OK ($([math]::Round($FileCheck/1MB, 2)) MB)"

# 3. ZIP erstellen
Write-Host "Erstelle ZIP-Archiv..."
Compress-Archive -Path $TempSqlFile -DestinationPath $ZipFile -Force

# 4. Temporaere SQL-Datei loeschen
Remove-Item $TempSqlFile

# 5. Alte Backups im Pod aufraeumen (behalte nur die letzten 3)
Write-Host "Raeume alte Backups im Pod auf..."
kubectl exec razarion-mariadb-0 -- bash -c "ls -t /tmp/backup*.sql 2>/dev/null | tail -n +4 | xargs -r rm"

# 6. Aktuelles Backup im Pod loeschen
kubectl exec razarion-mariadb-0 -- rm /tmp/backup.sql

$FileSize = (Get-Item $ZipFile).Length / 1MB
Write-Host ""
Write-Host "Backup erfolgreich erstellt: $ZipFile" -ForegroundColor Green
Write-Host "Groesse: $([math]::Round($FileSize, 2)) MB"
