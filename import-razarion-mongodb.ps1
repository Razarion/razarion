param(
    [Parameter(Mandatory=$true)]
    [string]$AtlasUri
)

$ErrorActionPreference = "Stop"
$dbName = "razarion"
$tempDir = "/tmp/mongodump"

# Find local mongo container
$mongoId = docker ps -qf "ancestor=mongo" 2>$null
if (-not $mongoId) {
    Write-Error "Local mongo container not found. Is docker-compose running?"
    exit 1
}

$localMongoHost = (docker inspect $mongoId --format '{{.Name}}').TrimStart('/')
$networkName = (docker inspect $mongoId --format '{{range $k,$v := .NetworkSettings.Networks}}{{$k}}{{end}}')

Write-Host "Local mongo container: $localMongoHost (network: $networkName)" -ForegroundColor Cyan

# Step 1: Dump from Atlas (use /tmp which is writable)
Write-Host "Dumping from Atlas ..." -ForegroundColor Cyan
docker run --rm `
    -v razarion_mongodump:/tmp/mongodump `
    mongo `
    bash -c "chmod 777 $tempDir && mongodump --uri='$AtlasUri' --db=$dbName --out=$tempDir"

if ($LASTEXITCODE -ne 0) { Write-Error "mongodump from Atlas failed"; exit 1 }

# Step 2: Drop local DB and restore
Write-Host "Restoring to local MongoDB (drop + restore) ..." -ForegroundColor Yellow
docker run --rm `
    --network $networkName `
    -v razarion_mongodump:/tmp/mongodump `
    mongo `
    mongorestore --host=$localMongoHost --port=27017 --db=$dbName --drop $tempDir/$dbName

if ($LASTEXITCODE -ne 0) { Write-Error "mongorestore failed"; exit 1 }

# Cleanup volume
$ErrorActionPreference = "SilentlyContinue"
docker volume rm razarion_mongodump 2>&1 | Out-Null
$ErrorActionPreference = "Stop"

Write-Host "MongoDB import complete!" -ForegroundColor Green
