# Creata a shortcut to Journo on the desktop
$scriptDir = split-path -parent $MyInvocation.MyCommand.Definition
# if the above does not work, try to use $scriptDir = $PSScriptRoot instead
$WScriptObj = New-Object -ComObject ("WScript.Shell")
$shortcut = $WscriptObj.CreateShortcut("$env:USERPROFILE\Desktop\Journo.lnk")
$shortcut.TargetPath = "C:\Windows\System32"
$shortcut.Arguments = "/c $scriptDir\journo.cmd"
$shortcut.WorkingDirectory = "$scriptDir"
$shortcut.IconLocation = "$scriptDir\journo-rounded.ico, 0"
$shortcut.Save()