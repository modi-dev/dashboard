# Скрипт для исправления политики выполнения PowerShell

Write-Host "🔧 Исправление политики выполнения PowerShell..." -ForegroundColor Green

# Проверка текущей политики
Write-Host "📋 Текущая политика выполнения:" -ForegroundColor Yellow
Get-ExecutionPolicy -List

# Попытка установить политику для текущего пользователя
Write-Host "🔧 Установка политики RemoteSigned для CurrentUser..." -ForegroundColor Yellow
try {
    Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser -Force
    Write-Host "✅ Политика установлена успешно" -ForegroundColor Green
} catch {
    Write-Host "❌ Не удалось установить политику: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "💡 Попробуйте запустить PowerShell от имени администратора" -ForegroundColor Yellow
}

# Проверка новой политики
Write-Host "📋 Новая политика выполнения:" -ForegroundColor Yellow
Get-ExecutionPolicy -List

Write-Host "🎯 Теперь вы можете запустить:" -ForegroundColor Cyan
Write-Host ".\deploy-dev-tools.ps1" -ForegroundColor White
Write-Host "или" -ForegroundColor White
Write-Host ".\run-dev-tools.bat" -ForegroundColor White

Write-Host "✅ Готово!" -ForegroundColor Green

