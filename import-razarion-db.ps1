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

# Create test users (admin@admin.com and user@user.com, both pwd: 1234)
Write-Host "Creating test users ..." -ForegroundColor Cyan
$testUsersSql = @'
INSERT INTO RAZARION_USER (email, passwordHash, verificationDoneDate, admin, crystals, xp, level_id, userId)
VALUES ('admin@admin.com',
        '$2a$12$BmpbwogZZcxbt2rIjFULS.oBbjhuecmFQsLj3brjPP5m6eFrESwWy',
        '2020-01-27 20:00:00', true, 0, 0, 272,
        'dc0b3681-8d56-47f0-81a6-1b292f64717e');
INSERT INTO RAZARION_USER (email, passwordHash, verificationDoneDate, admin, crystals, xp, level_id, userId)
VALUES ('user@user.com',
        '$2a$12$BmpbwogZZcxbt2rIjFULS.oBbjhuecmFQsLj3brjPP5m6eFrESwWy',
        '2020-01-27 20:00:00', false, 0, 0, 272,
        '5f5d9792-4efa-457e-8ba1-bfdb2dac12e2');
'@
docker exec db mariadb -uroot -p1234 razarion -e $testUsersSql
if ($LASTEXITCODE -ne 0) { Write-Error "Test user creation failed"; exit 1 }
Write-Host "  admin@admin.com (admin, pwd: 1234)" -ForegroundColor Gray
Write-Host "  user@user.com (user, pwd: 1234)" -ForegroundColor Gray

# Cleanup
docker exec db rm /tmp/import.sql
Remove-Item $tempDir -Recurse -Force

Write-Host "Import complete!" -ForegroundColor Green
