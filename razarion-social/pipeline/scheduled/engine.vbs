' Razarion Social - starts engine.mjs without a window, for the Windows task (install-engine.ps1).
' Waits for node to end and hands its exit code to the task, so the task's "restart on failure"
' sees a crash.
Set fso = CreateObject("Scripting.FileSystemObject")
Set shell = CreateObject("WScript.Shell")
pipeline = fso.GetParentFolderName(fso.GetParentFolderName(WScript.ScriptFullName))
shell.CurrentDirectory = pipeline
node = WScript.Arguments(0)
WScript.Quit shell.Run("""" & node & """ """ & pipeline & "\engine.mjs""", 0, True)
