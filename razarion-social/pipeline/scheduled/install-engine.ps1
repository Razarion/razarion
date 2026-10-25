# Razarion Social - registers engine.mjs as a Windows task: started at logon, hidden, restarted
# when it crashes, never stopped for running too long. Then it starts it right away.
#
#   .\install-engine.ps1            # register (or re-register) and start
#   .\install-engine.ps1 -Remove    # stop and unregister
#
# The task runs as you and only while you are logged on: the texts come from your Claude login
# (claude -p) and the battle clips need the GPU of your session. The dashboard is then at
# http://127.0.0.1:4711.

param([switch]$Remove)

$TaskName = "Razarion Social Engine"
$Launcher = Join-Path $PSScriptRoot "engine.vbs"

$existing = Get-ScheduledTask -TaskName $TaskName -ErrorAction SilentlyContinue
if ($existing) {
    Stop-ScheduledTask -TaskName $TaskName -ErrorAction SilentlyContinue
    Unregister-ScheduledTask -TaskName $TaskName -Confirm:$false
    Write-Host "Removed the old task."
}
# Stopping the task ends wscript, not always the node it started.
Get-CimInstance Win32_Process -Filter "Name = 'node.exe'" |
    Where-Object { $_.CommandLine -like "*engine.mjs*" } |
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
    -Description "Razarion social pipeline: makes, approves and publishes posts on a schedule. Dashboard: http://127.0.0.1:4711" | Out-Null
Start-ScheduledTask -TaskName $TaskName
Write-Host "Registered and started '$TaskName'. Dashboard: http://127.0.0.1:4711"
