# Director mode — Phase 1 manual test helper.
#
# Drives the dev-only /rest/director endpoints (local server, port 8080) so you
# can fly + record the live world before the studio control UI (Phase 2) exists.
#
# Prereq: local RazarionServer running (razarion.director.enabled=true) and the
# frontend serving the /director route (npm start, then open http://localhost:4200/director).
#
# Usage:
#   . .\director-test.ps1            # dot-source to load the functions
#   $id = New-DirectorPlan           # create the sample orbit plan, returns its id
#   Send-Director LOAD_PLAN $id      # tell the /director client to load it
#   Send-Director PLAY               # fly the camera
#   Send-Director STOP               # back to start
#   Send-Director SEEK -TimeMs 12000 # jump to t=12s (paused preview)
#   Start-DirectorRecording $id      # load + record + auto-download WebM at end
#
# Coordinates are Babylon world space (x,z = ground plane, y = up). The sample
# orbits the planet centre (~2560,2560); adjust targets to where your bots are.

$Base = "http://localhost:8080/rest/director"

# The endpoints require an ADMIN JWT. Grab yours from the browser console on a
# logged-in studio/game tab:  localStorage.getItem("app.token")
# then:  $env:DIRECTOR_TOKEN = '<paste token>'
$Token = $env:DIRECTOR_TOKEN
function DirHeaders {
    if ($Token) { return @{ Authorization = "Bearer $Token" } }
    Write-Warning "No DIRECTOR_TOKEN set — calls will 403 (admin required). Set `$env:DIRECTOR_TOKEN."
    return @{}
}

function New-DirectorPlan {
    $plan = @{
        version    = 1
        durationMs = 24000
        cameraKeys = @(
            @{ time = 0;     mode = "orbit"; target = @(2560, 30, 2560); alpha = 0.0;            beta = 0.55; radius = 1800; easing = "ease" },
            @{ time = 12000; mode = "orbit"; target = @(2560, 20, 2560); alpha = [math]::PI;     beta = 0.50; radius = 1200; easing = "ease" },
            @{ time = 24000; mode = "orbit"; target = @(2560, 30, 2560); alpha = 2 * [math]::PI; beta = 0.60; radius = 1600; easing = "ease" }
        )
        cues = @()
    }
    $dto = @{
        name        = "Sample orbit"
        jsonContent = ($plan | ConvertTo-Json -Depth 10 -Compress)
    }
    $res = Invoke-RestMethod -Method Post -Uri "$Base/plan" -ContentType "application/json" -Headers (DirHeaders) -Body ($dto | ConvertTo-Json -Depth 10)
    Write-Host "Created plan id=$($res.id) name='$($res.name)'"
    return $res.id
}

function Send-Director {
    param(
        [Parameter(Mandatory = $true)][string]$Type,
        [int]$PlanId,
        [int]$TimeMs,
        [string]$FileName
    )
    $cmd = @{ type = $Type }
    if ($PSBoundParameters.ContainsKey('PlanId'))   { $cmd.planId = $PlanId }
    if ($PSBoundParameters.ContainsKey('TimeMs'))   { $cmd.timeMs = $TimeMs }
    if ($PSBoundParameters.ContainsKey('FileName')) { $cmd.fileName = $FileName }
    # Positional plan id: "Send-Director LOAD_PLAN 5"
    if ($args.Count -ge 1 -and -not $PSBoundParameters.ContainsKey('PlanId')) { $cmd.planId = [int]$args[0] }
    $res = Invoke-RestMethod -Method Post -Uri "$Base/command" -ContentType "application/json" -Headers (DirHeaders) -Body ($cmd | ConvertTo-Json)
    Write-Host "Sent $Type (seq=$($res.seq))"
}

function Start-DirectorRecording {
    param([Parameter(Mandatory = $true)][int]$PlanId, [string]$FileName = "director.webm")
    Send-Director LOAD_PLAN -PlanId $PlanId
    Start-Sleep -Milliseconds 800   # let the client load the plan
    Send-Director RECORD_START -FileName $FileName
    Write-Host "Recording started; WebM downloads automatically when the $($PlanId) plan reaches its end."
}
