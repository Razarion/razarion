# Razarion Social - geplanter Lauf
#
# Veroeffentlicht, was in den Review-Dateien auf "ok" steht - auf X, Instagram, Facebook und YouTube.
# Beitraege entstehen mit compose.mjs; dieser Lauf verteilt sie nur. Neue Beitraege werden NICHT automatisch freigegeben - sie warten
# auf status "ok" in captions.json und fb_posts.json.
#
# Damit ist die Arbeitsteilung: der Zeitplan holt, bereitet auf und liefert aus; du entscheidest
# einmal pro Woche, was freigegeben wird.
#
#   .\run.ps1                 # ein Beitrag je Netzwerk aus dem freigegebenen Vorrat
#   .\run.ps1 -Limit 2
#   .\run.ps1 -PrepareOnly    # nur holen und aufbereiten, nichts veroeffentlichen

param(
    [int]$Limit = 1,
    [switch]$PrepareOnly
)

$ErrorActionPreference = "Continue"
$Pipeline = (Get-Item $PSScriptRoot).Parent.FullName
$LogDir = Join-Path $Pipeline "state"
$Log = Join-Path $LogDir "scheduled.log"

if (-not (Test-Path $LogDir)) { New-Item -ItemType Directory -Path $LogDir | Out-Null }

function Write-Log([string]$Message) {
    $line = "{0}  {1}" -f (Get-Date -Format "yyyy-MM-dd HH:mm:ss"), $Message
    Write-Host $line
    Add-Content -Path $Log -Value $line -Encoding utf8
}

# Ein fehlgeschlagener Schritt darf die folgenden nicht verhindern. Wenn Instagram klemmt, soll
# Facebook trotzdem ausliefern - und der Token-Refresh laeuft ohnehin unabhaengig von beidem.
function Invoke-Step([string]$Name, [string[]]$NodeArgs) {
    Write-Log "--- $Name"
    $output = & node @NodeArgs 2>&1
    $exit = $LASTEXITCODE
    foreach ($line in $output) { Add-Content -Path $Log -Value ("    " + $line) -Encoding utf8 }
    if ($exit -ne 0) {
        Write-Log "    FEHLGESCHLAGEN (exit $exit) - weiter mit dem naechsten Schritt"
        return $false
    }
    return $true
}

Set-Location $Pipeline
Write-Log "=== Lauf gestartet"

# Der Token erneuert sich nur, wenn weniger als 14 Tage Restlaufzeit bleiben - haeufiger
# aufzurufen kostet nichts. Instagram-Token laufen nach 60 Tagen ab und lassen sich danach nur
# noch von Hand im Meta-Dashboard neu erzeugen; deshalb steht das an erster Stelle.
Invoke-Step "Instagram-Token pruefen" @("refresh_token.mjs") | Out-Null

# X ist nicht mehr Quelle. Beitraege entstehen mit compose.mjs, also gibt es nichts zu holen -
# und jeder Lesezugriff auf die X-API kostet Geld, ohne etwas beizutragen. sync_new.mjs bleibt im
# Repo, falls das Spiegeln je wieder gebraucht wird; der Zeitplan ruft es nicht mehr auf.

if ($PrepareOnly) {
    Write-Log "=== PrepareOnly - nichts veroeffentlicht"
    exit 0
}

# Veroeffentlicht ausschliesslich, was bereits auf "ok" steht. Ein Lauf ohne freigegebenen Vorrat
# meldet "nothing to do" und ist damit ein No-op.
Invoke-Step "Instagram veroeffentlichen" @("publish.mjs", "--live", "--limit", "$Limit") | Out-Null
Invoke-Step "Facebook veroeffentlichen" @("publish_fb.mjs", "--live", "--limit", "$Limit") | Out-Null
Invoke-Step "X veroeffentlichen" @("publish_x.mjs", "--live", "--limit", "$Limit") | Out-Null
# YouTube laeuft zuletzt: ein Upload kostet 1600 von 10000 Kontingentpunkten am Tag, und anders als
# die drei davor haelt er den Lauf minutenlang auf, waehrend die Datei hochgeht.
Invoke-Step "YouTube veroeffentlichen" @("publish_youtube.mjs", "--live", "--limit", "$Limit") | Out-Null

Write-Log "=== Lauf beendet"
