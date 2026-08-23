# Reads the client render telemetry back out of PROD and aggregates it.
#
# Each playing client emits one "[RenderTelemetry] ..." line every 10 s (see
# razarion-frontend/src/app/game/renderer/render-telemetry.ts). AppComponent's console hook
# forwards it to /rest/remote_logging/angularJsonLogger, the server logs it, and it lands in GKE
# Cloud Logging. This turns those lines into the two tables that answer "does it lag?":
#
#   1. the overall distribution  -- is the median player fine and the tail broken, or everyone?
#   2. the worst sessions        -- which device, which GPU, low fps or freezes?
#
# Low fps and freezes are different bugs. fpsP50 tells you the first; long100/frameMax tell you
# the second, and a session can be bad at one while looking perfect at the other.
#
# Usage:  .\render-telemetry-report.ps1 [-Hours 24] [-Top 15] [-InputFile dump.txt]
#
# Note on the filter quoting: PowerShell eats the quotes inside a gcloud filter unless they are
# backslash-escaped inside a single-quoted string. Leave the escaping alone.

param(
    [int]$Hours = 24,
    [int]$Top = 15,
    # Re-analyse a saved dump instead of querying again. A full day is a slow gcloud call, and the
    # same lines usually get looked at from more than one angle.
    [string]$InputFile
)

$ErrorActionPreference = "Stop"

if ($InputFile) {
    Write-Host "=== Render telemetry from $InputFile ===" -ForegroundColor Cyan
    $raw = Get-Content $InputFile
} else {
    $from = (Get-Date).ToUniversalTime().AddHours(-$Hours).ToString("yyyy-MM-ddTHH:mm:ssZ")
    Write-Host "=== Render telemetry since $from ===" -ForegroundColor Cyan

    $filter = 'resource.type=\"k8s_container\" AND resource.labels.container_name=\"razarion-server\" AND timestamp>=\"' + $from + '\" AND textPayload:\"RenderTelemetry\"'
    $raw = gcloud logging read $filter --limit=200000 --format='value(textPayload)' --order=asc
    if ($LASTEXITCODE -ne 0) {
        Write-Host "gcloud logging read failed" -ForegroundColor Red
        exit 1
    }
}

# One record per emitted period. Unquoted values first, quoted ones (gpu) kept whole.
$records = @()
foreach ($line in $raw) {
    if ($line -notmatch "\[RenderTelemetry\]") { continue }
    $record = @{}
    foreach ($pair in [regex]::Matches($line, '(\w+)=("[^"]*"|\S+)')) {
        $record[$pair.Groups[1].Value] = $pair.Groups[2].Value.Trim('"')
    }
    $records += [pscustomobject]$record
}

if ($records.Count -eq 0) {
    Write-Host "No telemetry lines found. Is RenderTelemetry.ENABLED still true in the deployed build?" -ForegroundColor Yellow
    exit 0
}

$sessions = $records | Group-Object session
Write-Host "$($records.Count) periods from $($sessions.Count) sessions`n" -ForegroundColor Gray

# Invariant formatting: the Swiss locale writes 1250 as 1'250, which neither a spreadsheet nor a
# grep wants back.
function Fmt([double]$value, [int]$decimals = 1) {
    return $value.ToString("F$decimals", [System.Globalization.CultureInfo]::InvariantCulture)
}

function Percentile([double[]]$values, [double]$q) {
    if ($values.Count -eq 0) { return 0 }
    $sorted = $values | Sort-Object
    return $sorted[[Math]::Min($sorted.Count - 1, [Math]::Floor($q * $sorted.Count))]
}

# --- 1. Overall distribution, one period = one sample -------------------------------------------
Write-Host "Distribution over all periods" -ForegroundColor Cyan
$metrics = @(
    @{Name = "fps"; Key = "fps"},
    @{Name = "frameP50 (ms)"; Key = "frameP50"},
    @{Name = "frameP95 (ms)"; Key = "frameP95"},
    @{Name = "frameMax (ms)"; Key = "frameMax"},
    @{Name = "renderP50 (ms)"; Key = "renderP50"},
    @{Name = "long100/10s"; Key = "long100"},
    @{Name = "tickGapMax (ms)"; Key = "tickGapMax"},
    @{Name = "activeMeshes"; Key = "activeMeshes"}
)
$rows = foreach ($metric in $metrics) {
    $values = [double[]]($records | ForEach-Object { [double]$_.($metric.Key) })
    [pscustomobject]@{
        Metric = $metric.Name
        p10    = Fmt (Percentile $values 0.10)
        p50    = Fmt (Percentile $values 0.50)
        p90    = Fmt (Percentile $values 0.90)
        p99    = Fmt (Percentile $values 0.99)
        worst  = Fmt ($values | Measure-Object -Maximum).Maximum
    }
}
$rows | Format-Table -AutoSize

# --- 2. The sessions that actually lagged -------------------------------------------------------
# Ranked by median fps, because a player judges the whole session, not its best period.
Write-Host "Worst sessions by median fps" -ForegroundColor Cyan
$sessionRows = foreach ($session in $sessions) {
    $fps = [double[]]($session.Group | ForEach-Object { [double]$_.fps })
    $frameMax = ($session.Group | ForEach-Object { [double]$_.frameMax } | Measure-Object -Maximum).Maximum
    $freezes = ($session.Group | ForEach-Object { [int]$_.long100 } | Measure-Object -Sum).Sum
    $last = $session.Group[-1]
    [pscustomobject]@{
        Session    = $session.Name.Substring(0, [Math]::Min(8, $session.Name.Length))
        Periods    = $session.Count
        fpsP50     = Fmt (Percentile $fps 0.50)
        fpsWorst   = Fmt ($fps | Measure-Object -Minimum).Minimum
        FreezeMax  = Fmt $frameMax 0
        Freezes    = $freezes
        Backbuffer = $last.backbuffer
        Touch      = $last.touch
        GPU        = $last.gpu
    }
}
$sessionRows | Sort-Object { [double]$_.fpsP50 } | Select-Object -First $Top | Format-Table -AutoSize

# --- 3. What is in scene.meshes, and does it explain the frame time? ----------------------------
# PROD 21.08.2026: renderP50 = 12.9 ms + 0.00132 ms * scene.meshes (R^2 = 0.90) while activeMeshes
# was flat (R^2 = 0.00) -- the frame time follows the size of the mesh array, not what is drawn.
# Bucketing by scene size re-checks that on every new dump; meshTop names who filled the array.
Write-Host "Frame time by scene size" -ForegroundColor Cyan
$records | Group-Object { [Math]::Floor([double]$_.meshes / 5000) } | Sort-Object { [int]$_.Name } | ForEach-Object {
    $group = $_.Group
    $avg = { param($key) ($group | ForEach-Object { [double]$_.$key } | Measure-Object -Average).Average }
    [pscustomobject]@{
        Meshes       = "{0,6} - {1}" -f ([int]$_.Name * 5000), ([int]$_.Name * 5000 + 4999)
        Periods      = $_.Count
        fps          = Fmt (& $avg "fps")
        renderP50    = Fmt (& $avg "renderP50")
        activeMeshes = Fmt (& $avg "activeMeshes") 0
        disabled     = if ($group[0].PSObject.Properties["disabledMeshes"]) { Fmt (& $avg "disabledMeshes") 0 } else { "n/a" }
        shadowCasters = if ($group[0].PSObject.Properties["shadowCasters"]) { Fmt (& $avg "shadowCasters") 0 } else { "n/a" }
    }
} | Format-Table -AutoSize

# Who owns the array, taken from the largest period so the answer is about the worst case.
$withCensus = $records | Where-Object { $_.meshTop }
if ($withCensus) {
    $worst = $withCensus | Sort-Object { [double]$_.meshes } -Descending | Select-Object -First 1
    Write-Host "Largest scene seen: $($worst.meshes) meshes ($($worst.disabledMeshes) disabled, $($worst.instanced) instanced, $($worst.shadowCasters) shadow casters)" -ForegroundColor Gray
    Write-Host "  owners: $($worst.meshTop)`n" -ForegroundColor Gray
} else {
    Write-Host "No mesh census in these lines -- dump predates the census fields.`n" -ForegroundColor Yellow
}

# --- 4. The parked-mesh filter A/B (F7) ---------------------------------------------------------
# The toggle parks nothing and unparks nothing, so meshes/parked stay put across it. Comparing
# periods of comparable scene size on either side of the flag isolates the filter's effect.
$ab = $records | Where-Object { $_.parkingFilter }
if ($ab) {
    Write-Host "Parked-mesh filter A/B (F7)" -ForegroundColor Cyan
    $ab | Group-Object parkingFilter | Sort-Object Name -Descending | ForEach-Object {
        $group = $_.Group
        $avg = { param($key) ($group | ForEach-Object { [double]$_.$key } | Measure-Object -Average).Average }
        [pscustomobject]@{
            Filter       = if ($_.Name -eq "true") { "on (fix)" } else { "off (before)" }
            Periods      = $_.Count
            fps          = Fmt (& $avg "fps")
            renderP50    = Fmt (& $avg "renderP50")
            meshes       = Fmt (& $avg "meshes") 0
            parked       = Fmt (& $avg "parked") 0
            activeMeshes = Fmt (& $avg "activeMeshes") 0
        }
    } | Format-Table -AutoSize
    Write-Host "Compare rows only when 'meshes' is comparable -- the filter changes the cost of the array, not its size.`n" -ForegroundColor Gray
}

# --- 5. Does the GPU explain it? ----------------------------------------------------------------
Write-Host "Median fps per GPU" -ForegroundColor Cyan
$records | Group-Object gpu | ForEach-Object {
    $fps = [double[]]($_.Group | ForEach-Object { [double]$_.fps })
    [pscustomobject]@{
        GPU      = $_.Name
        Periods  = $_.Count
        fpsP50   = Fmt (Percentile $fps 0.50)
        fpsP10   = Fmt (Percentile $fps 0.10)
    }
} | Sort-Object { [double]$_.fpsP50 } | Format-Table -AutoSize
