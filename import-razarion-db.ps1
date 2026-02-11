param(
    [Parameter(Mandatory=$true)]
    [string]$ZipFile
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path $ZipFile)) {
    Write-Error "File not found: $ZipFile"
    exit 1
}

# Extract SQL from ZIP to temp dir
$tempDir = Join-Path $env:TEMP "razarion_db_import"
if (Test-Path $tempDir) { Remove-Item $tempDir -Recurse -Force }
New-Item -ItemType Directory -Path $tempDir | Out-Null

Write-Host "Extracting $ZipFile ..." -ForegroundColor Cyan
Add-Type -AssemblyName System.IO.Compression.FileSystem
[System.IO.Compression.ZipFile]::ExtractToDirectory($ZipFile, $tempDir)

$sqlFile = Get-ChildItem -Path $tempDir -Filter "*.sql" | Select-Object -First 1
if (-not $sqlFile) {
    Write-Error "No .sql file found in ZIP"
    exit 1
}
Write-Host "Found: $($sqlFile.Name) ($([math]::Round($sqlFile.Length / 1MB, 1)) MB)" -ForegroundColor Cyan

# Copy SQL file into Docker container
Write-Host "Copying to Docker container ..." -ForegroundColor Cyan
docker cp $sqlFile.FullName db:/tmp/import.sql
if ($LASTEXITCODE -ne 0) { Write-Error "docker cp failed"; exit 1 }

# Drop and recreate database, then import
Write-Host "Importing into MariaDB (drop + recreate razarion) ..." -ForegroundColor Yellow
docker exec db mariadb -uroot -p1234 -e "DROP DATABASE IF EXISTS razarion; CREATE DATABASE razarion; GRANT ALL ON razarion.* TO 'raz_user'@'%';"
if ($LASTEXITCODE -ne 0) { Write-Error "Database reset failed"; exit 1 }

docker exec db bash -c "mariadb -uroot -p1234 razarion < /tmp/import.sql"
if ($LASTEXITCODE -ne 0) { Write-Error "SQL import failed"; exit 1 }

# Cleanup
docker exec db rm /tmp/import.sql
Remove-Item $tempDir -Recurse -Force

Write-Host "Import complete!" -ForegroundColor Green
