<#
.SYNOPSIS
    Marks all quests up to a given level as completed for a user and sets the
    user to the target level. This lets you conveniently test level 10 and
    higher levels without manually playing through all previous quests.

.DESCRIPTION
    Flow:
      1. Login as admin -> JWT token (POST /rest/user/auth, HTTP Basic).
      2. Find the target user by email (GET .../get-user-backend-infos).
      3. Load all quests (GET .../get-quest-backend-infos) and collect every
         quest with levelNumber <= CompletedThroughLevel.
      4. Set those quest IDs as completed
         (POST .../set-completed-quests/{userId}).
      5. Deactivate the current quest (POST .../deactivate-quest/{userId}).
      6. Set the user to the target level
         (POST .../set-level/{userId}/{levelId}).

    All endpoints used require the ADMIN role.

.PARAMETER TargetEmail
    Email of the user to prepare.
    Default: same user as the admin (AdminEmail).

.PARAMETER CompletedThroughLevel
    All quests up to and including this level are marked as completed.
    Default: 9.

.PARAMETER TargetLevelNumber
    Level number the user is set to. 0 = do not change the level.
    Default: 10.

.PARAMETER BaseUrl
    Backend base URL. Default: http://localhost:8080

.PARAMETER AdminEmail / AdminPassword
    Admin credentials for the JWT login.
    Default: admin@admin.com / 1234 (local docker seed).

.EXAMPLE
    # Complete the admin user up to level 9 and set to level 10
    .\complete-quests-to-level.ps1

.EXAMPLE
    # Prepare a different user
    .\complete-quests-to-level.ps1 -TargetEmail tester@example.com

.EXAMPLE
    # Complete up to level 12 and set to level 13
    .\complete-quests-to-level.ps1 -CompletedThroughLevel 12 -TargetLevelNumber 13
#>
[CmdletBinding()]
param(
    [string]$TargetEmail,
    [int]$CompletedThroughLevel = 9,
    [int]$TargetLevelNumber = 10,
    [string]$BaseUrl = "http://localhost:8080",
    [string]$AdminEmail = "admin@admin.com",
    [string]$AdminPassword = "1234"
)

$ErrorActionPreference = "Stop"

function Write-Step($msg) { Write-Host "==> $msg" -ForegroundColor Cyan }

# --- 1. Admin login -> JWT --------------------------------------------------
Write-Step "Logging in as $AdminEmail at $BaseUrl"
$basic = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes("${AdminEmail}:${AdminPassword}"))
try {
    $token = Invoke-RestMethod -Method Post -Uri "$BaseUrl/rest/user/auth" `
        -Headers @{ Authorization = "Basic $basic" }
} catch {
    throw "Login failed. Is the backend running on $BaseUrl and are the admin credentials correct? $($_.Exception.Message)"
}
if ([string]::IsNullOrWhiteSpace($token)) { throw "No JWT token received." }
$auth = @{ Authorization = "Bearer $token" }
Write-Host "    JWT received." -ForegroundColor DarkGray

# Target = admin itself if no TargetEmail was given
if ([string]::IsNullOrWhiteSpace($TargetEmail)) { $TargetEmail = $AdminEmail }

# --- 2. Find target user ----------------------------------------------------
Write-Step "Looking up user '$TargetEmail'"
$users = Invoke-RestMethod -Method Get -Uri "$BaseUrl/rest/editor/user-mgmt/get-user-backend-infos" -Headers $auth
$user = $users | Where-Object { $_.email -eq $TargetEmail } | Select-Object -First 1
if (-not $user) {
    throw "User '$TargetEmail' not found. Existing emails: " + (($users | Where-Object { $_.email } | ForEach-Object { $_.email }) -join ', ')
}
$userId = $user.userId
Write-Host "    userId = $userId (current level: $($user.levelNumber))" -ForegroundColor DarkGray

# --- 3. Collect quests up to level N ----------------------------------------
Write-Step "Loading quests and filtering levelNumber <= $CompletedThroughLevel"
$quests = Invoke-RestMethod -Method Get -Uri "$BaseUrl/rest/editor/user-mgmt/get-quest-backend-infos" -Headers $auth
$questIds = @($quests | Where-Object { $_.levelNumber -le $CompletedThroughLevel } | ForEach-Object { $_.id })
Write-Host "    $($questIds.Count) quests will be marked as completed." -ForegroundColor DarkGray
if ($questIds.Count -eq 0) { Write-Warning "No matching quests found - will set anyway (empty list)." }

# --- 4. Set completed quests ------------------------------------------------
Write-Step "Setting completed quests"
$body = ConvertTo-Json @($questIds) -Compress
if ($questIds.Count -eq 1) { $body = "[$($questIds[0])]" }  # ConvertTo-Json doesn't make an array from 1 element
Invoke-RestMethod -Method Post -Uri "$BaseUrl/rest/editor/user-mgmt/set-completed-quests/$userId" `
    -Headers $auth -ContentType "application/json" -Body $body | Out-Null

# --- 5. Deactivate active quest ---------------------------------------------
Write-Step "Deactivating current quest"
try {
    Invoke-RestMethod -Method Post -Uri "$BaseUrl/rest/editor/user-mgmt/deactivate-quest/$userId" -Headers $auth | Out-Null
} catch {
    Write-Warning "deactivate-quest failed (maybe no active quest): $($_.Exception.Message)"
}

# --- 6. Set target level ----------------------------------------------------
if ($TargetLevelNumber -gt 0) {
    Write-Step "Resolving level id for level number $TargetLevelNumber"
    $levels = Invoke-RestMethod -Method Get -Uri "$BaseUrl/rest/editor/level/read" -Headers $auth
    $level = $levels | Where-Object { $_.number -eq $TargetLevelNumber } | Select-Object -First 1
    if (-not $level) {
        Write-Warning "Level with number $TargetLevelNumber not found - level not changed. Existing numbers: " + (($levels.number | Sort-Object) -join ', ')
    } else {
        Write-Step "Setting user to level $TargetLevelNumber (id=$($level.id))"
        Invoke-RestMethod -Method Post -Uri "$BaseUrl/rest/editor/user-mgmt/set-level/$userId/$($level.id)" -Headers $auth | Out-Null
    }
} else {
    Write-Host "    Level left unchanged (TargetLevelNumber=0)." -ForegroundColor DarkGray
}

Write-Host ""
Write-Host "Done. '$TargetEmail' has completed all quests up to level $CompletedThroughLevel" -ForegroundColor Green
if ($TargetLevelNumber -gt 0) { Write-Host "and is now at level $TargetLevelNumber. Happy testing!" -ForegroundColor Green }
