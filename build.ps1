$ErrorActionPreference = "Stop"
$ProjectRoot = $PSScriptRoot
$StartTime = Get-Date

Write-Host "=== Razarion Build ===" -ForegroundColor Cyan

# JDK 21 Umgebung setzen
Write-Host "`n[0/2] Setting JDK 21 environment..." -ForegroundColor Yellow
. C:\dev\scripts\jdk21.ps1
Write-Host "JAVA_HOME: $env:JAVA_HOME" -ForegroundColor Gray

# Maven Build - erster Durchlauf
# -Pprod aktiviert das TeaVM-Profil beider WASM-Module (minifying, optimizationLevel=FULL,
# kein debugInfo/sourceMaps). Ohne diesen Schalter laeuft die Engine als Debug-Build: der
# Worker-Tick kostete auf PROD im Schnitt 179 ms statt der 100 ms, die er hat.
Write-Host "`n[1/2] Building project with Maven (1st pass)..." -ForegroundColor Yellow
Set-Location $ProjectRoot
mvn clean install -DskipTests -Pprod
if ($LASTEXITCODE -ne 0) {
    Write-Host "Maven build (1st pass) failed!" -ForegroundColor Red
    exit 1
}
Write-Host "Build (1st pass) successful!" -ForegroundColor Green

# Maven Build - zweiter Durchlauf
Write-Host "`n[2/2] Building project with Maven (2nd pass)..." -ForegroundColor Yellow
mvn clean install -DskipTests -Pprod
if ($LASTEXITCODE -ne 0) {
    Write-Host "Maven build (2nd pass) failed!" -ForegroundColor Red
    exit 1
}
Write-Host "Build (2nd pass) successful!" -ForegroundColor Green

$EndTime = Get-Date
$Duration = $EndTime - $StartTime

Write-Host "`n=== Build Complete ===" -ForegroundColor Cyan
Write-Host "Total time: $($Duration.Minutes)m $($Duration.Seconds)s" -ForegroundColor Gray
Set-Location $ProjectRoot
