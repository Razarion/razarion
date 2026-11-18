# Razarion Daily Video - registers scripts/daily-video.mjs as a Windows task: every day at 17:20 it
# renders the last 24 h of the recorder tape as a 64x time-lapse and puts it public on YouTube.
# If the PC is off at 17:20 it runs when it is back (the tape then still holds the last 24 h).
#
#   .\install-daily-video.ps1                 # register (or re-register)
#   .\install-daily-video.ps1 -At 09:00       # another time of day
#   .\install-daily-video.ps1 -Remove         # unregister
#
# Log: state\daily-video.log, uploads: state\daily-video.json.

param([switch]$Remove, [string]$At = "17:20")

$TaskName = "Razarion Daily Video"
$Launcher = Join-Path $PSScriptRoot "daily-video.vbs"

if (Get-ScheduledTask -TaskName $TaskName -ErrorAction SilentlyContinue) {
    Unregister-ScheduledTask -TaskName $TaskName -Confirm:$false
    Write-Host "Removed the old task."
}
if ($Remove) { exit 0 }

$node = (Get-Command node -ErrorAction Stop).Source
$action = New-ScheduledTaskAction -Execute "wscript.exe" -Argument "`"$Launcher`" `"$node`""
$trigger = New-ScheduledTaskTrigger -Daily -At $At
$settings = New-ScheduledTaskSettingsSet `
    -AllowStartIfOnBatteries -DontStopIfGoingOnBatteries -StartWhenAvailable `
    -ExecutionTimeLimit (New-TimeSpan -Hours 9) -MultipleInstances IgnoreNew
$principal = New-ScheduledTaskPrincipal -UserId "$env:USERDOMAIN\$env:USERNAME" -LogonType Interactive -RunLevel Limited

Register-ScheduledTask -TaskName $TaskName -Action $action -Trigger $trigger -Settings $settings -Principal $principal `
    -Description "Razarion: renders the last 24 h of the recorder tape at 64x and uploads it public to YouTube. Log: razarion-recorder\state\daily-video.log" | Out-Null
Write-Host "Registered '$TaskName', daily at $At."
