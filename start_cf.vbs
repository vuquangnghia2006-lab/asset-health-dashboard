Set WshShell = CreateObject("WScript.Shell")
WshShell.Run "cmd /c ""C:\Users\vuqua\OneDrive\Documents\Default Project\cloudflared.exe"" tunnel --url http://localhost:8080 > ""C:\Users\vuqua\OneDrive\Documents\Default Project\cf_output.log"" 2>&1", 0, False
