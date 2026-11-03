# Razarion Recorder - registers recorder.mjs as a Windows task: started at logon, hidden, restarted
# when it crashes, never stopped for running too long. Then it starts it right away.
#
#   .\install-recorder.ps1            # register (or re-register) and start
#   .\install-recorder.ps1 -Remove    # stop and unregister
#
# The same shape as razarion-social/pipeline/scheduled/install-engine.ps1. The dashboard is then at
# http://127.0.0.1:4712.

param([switch]$Remove)

$TaskName = "Razarion Recorder"
$Launcher = Join-Path $PSScriptRoot "recorder.vbs"

$existing = Get-ScheduledTask -TaskName $TaskName -ErrorAction SilentlyContinue
if ($existing) {
    Stop-ScheduledTask -TaskName $TaskName -ErrorAction SilentlyContinue
    Unregister-ScheduledTask -TaskName $TaskName -Confirm:$false
    Write-Host "Removed the old task."
}
# Stopping the task ends wscript, not always the node it started.
Get-CimInstance Win32_Process -Filter "Name = 'node.exe'" |
    Where-Object { $_.CommandLine -like "*recorder.mjs*" } |
    ForEach-Object { Stop-Process -Id $_.ProcessId -Force }

if ($Remove) { exit 0 }

$node = (Get-Command node -ErrorAction Stop).Source
$action = New-ScheduledTaskAction -Execute "wscript.exe" -Argument "`"$Launcher`" `"$node`""
$trigger = New-ScheduledTaskTrigger -AtLogOn -User "$env:USERDOMAIN\$env:USERNAME"
$settings = New-ScheduledTaskSettingsSet `
    -AllowStartIfOnBatteries -DontStopIfGoingOnBatteries -StartWhenAvailable `
    -ExecutionTimeLimit ([TimeSpan]::Zero) `
    -RestartCount 999 -RestartInterval (New-TimeSpan -Minutes 1) `
    -MultipleInstances IgnoreNew
$principal = New-ScheduledTaskPrincipal -UserId "$env:USERDOMAIN\$env:USERNAME" -LogonType Interactive -RunLevel Limited

Register-ScheduledTask -TaskName $TaskName -Action $action -Trigger $trigger -Settings $settings -Principal $principal `
    -Description "Razarion recorder: watches the live planet and keeps it in memory. Dashboard: http://127.0.0.1:4712" | Out-Null
Start-ScheduledTask -TaskName $TaskName
Write-Host "Registered and started '$TaskName'. Dashboard: http://127.0.0.1:4712"
