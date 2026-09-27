# Razarion Social - scheduled run
#
# Fills the queue (plan.mjs) and publishes what stands on "ok" in the review files - on X,
# Instagram, Facebook and YouTube. New posts are NOT approved automatically - they wait for
# "Approve" in review.mjs.
#
# That is the division of labour: the run makes, prepares and delivers; you decide on the review
# page what gets approved.
#
#   .\run.ps1                 # fill the queue, publish one post per network
#   .\run.ps1 -Limit 2
#   .\run.ps1 -PrepareOnly    # fill and prepare only, publish nothing
#   .\run.ps1 -NoPlan         # make nothing new

param(
    [int]$Limit = 1,
    [switch]$PrepareOnly,
    [switch]$NoPlan
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

# A failed step must not stop the ones after it. If Instagram is stuck, Facebook should still
# deliver - and the token refresh runs independently of both anyway.
#
# The window shows each step's outcome lines ([ok], [!], [fail]) and, for a publish step, which
# post went out; the full output goes to the log only. With just the step names on screen, two runs
# that each published one post looked like two runs that did nothing.
$script:Published = @()
function Invoke-Step([string]$Name, [string[]]$NodeArgs) {
    Write-Log "--- $Name"
    $output = & node @NodeArgs 2>&1
    $exit = $LASTEXITCODE
    $preview = $null
    foreach ($line in $output) {
        $text = "$line"
        Add-Content -Path $Log -Value ("    " + $text) -Encoding utf8
        # The first line of the post's text, as the publishers print it under "[1/1] <date>".
        if (-not $preview -and $Name -like "Publish*" -and $text -match '^\s+> (?!waiting|X:|YouTube:|composed|data/)(.+)$') {
            $preview = $Matches[1]
        }
        if ($text -match '\[ok\]|\[!\]|\[fail\]') { Write-Host ("    " + $text.Trim()) }
        if ($Name -like "Publish*" -and $text -match 'published as|X: posted|uploaded as') {
            $script:Published += ("{0}: {1}" -f ($Name -replace '^Publish to ', ''), $preview)
        }
    }
    if ($exit -ne 0) {
        Write-Log "    FAILED (exit $exit) - moving on to the next step"
        return $false
    }
    return $true
}

Set-Location $Pipeline
Write-Log "=== Run started"

# The token only renews when fewer than 14 days are left, so calling it more often costs nothing.
# Instagram tokens expire after 60 days and can then only be recreated by hand in the Meta
# dashboard; that is why this comes first.
Invoke-Step "Check Instagram token" @("refresh_token.mjs") | Out-Null

# X is no longer the source. Posts are made here, so there is nothing to fetch - and every read
# from the X API costs money without adding anything. sync_new.mjs stays in the repo in case
# mirroring is ever needed again; the schedule no longer calls it.

# The planner makes posts up to "review" and never further. It needs this user's Claude login for
# the texts; without it, the template text goes in.
if (-not $NoPlan) {
    Invoke-Step "Fill the queue" @("plan.mjs") | Out-Null
}

if ($PrepareOnly) {
    Write-Log "=== PrepareOnly - nothing published"
    exit 0
}

# Publishes only what is already on "ok". A run with nothing approved reports "nothing to do" and
# is a no-op.
# Instagram and Facebook fetch the media from a public URL. upload_media.mjs uploads only what is
# on "ok", and skips what is already up.
Invoke-Step "Upload media for Instagram" @("upload_media.mjs") | Out-Null
Invoke-Step "Upload media for Facebook" @("upload_media.mjs", "--source", "fb") | Out-Null
Invoke-Step "Publish to Instagram" @("publish.mjs", "--live", "--limit", "$Limit") | Out-Null
Invoke-Step "Publish to Facebook" @("publish_fb.mjs", "--live", "--limit", "$Limit") | Out-Null
Invoke-Step "Publish to X" @("publish_x.mjs", "--live", "--limit", "$Limit") | Out-Null
# YouTube runs last: an upload costs 1600 of 10000 quota points a day, and unlike the three before
# it, it holds the run up for minutes while the file goes up.
Invoke-Step "Publish to YouTube" @("publish_youtube.mjs", "--live", "--limit", "$Limit") | Out-Null

# How far the posts got. Only posts younger than seven days are asked again, so on X a run costs a
# few cents at most.
Invoke-Step "Collect metrics" @("metrics.mjs") | Out-Null

Write-Host ""
if ($script:Published.Count) {
    Write-Host "Published in this run:"
    foreach ($p in $script:Published) { Write-Host ("  " + $p) }
} else {
    Write-Host "Nothing published in this run - nothing approved was waiting."
}
Write-Host "One post per network per run, oldest approved first. The full output is in state\scheduled.log."
Write-Host ""

Write-Log "=== Run finished"
