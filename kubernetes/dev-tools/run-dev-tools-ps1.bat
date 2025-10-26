@echo off
echo 🛠️  Запуск Dev-Tools через PowerShell с обходом политики...

REM Запуск PowerShell с обходом политики выполнения
powershell.exe -ExecutionPolicy Bypass -File "deploy-dev-tools.ps1"

pause

