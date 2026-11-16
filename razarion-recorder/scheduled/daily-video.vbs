' Razarion Daily Video - runs scripts/daily-video.mjs without a window, for the Windows task
' (install-daily-video.ps1), and hands its exit code to the task.
Set fso = CreateObject("Scripting.FileSystemObject")
Set shell = CreateObject("WScript.Shell")
recorder = fso.GetParentFolderName(fso.GetParentFolderName(WScript.ScriptFullName))
shell.CurrentDirectory = recorder
node = WScript.Arguments(0)
WScript.Quit shell.Run("""" & node & """ """ & recorder & "\scripts\daily-video.mjs""", 0, True)
