' Razarion Recorder - starts recorder.mjs without a window, for the Windows task (install-recorder.ps1).
' Waits for node to end and hands its exit code to the task, so the task's "restart on failure"
' sees a crash.
Set fso = CreateObject("Scripting.FileSystemObject")
Set shell = CreateObject("WScript.Shell")
recorder = fso.GetParentFolderName(fso.GetParentFolderName(WScript.ScriptFullName))
shell.CurrentDirectory = recorder
node = WScript.Arguments(0)
WScript.Quit shell.Run("""" & node & """ """ & recorder & "\recorder.mjs""", 0, True)
