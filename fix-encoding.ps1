# PowerShell скрипт для исправления кодировки
# Запускать от имени администратора

Write-Host "🔧 Исправление кодировки PowerShell..." -ForegroundColor Green

# Устанавливаем кодировку UTF-8 для консоли
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::InputEncoding = [System.Text.Encoding]::UTF8

# Устанавливаем кодировку для PowerShell
$OutputEncoding = [System.Text.Encoding]::UTF8

# Настраиваем кодировку для Windows
chcp 65001 | Out-Null

Write-Host "✅ Кодировка установлена в UTF-8" -ForegroundColor Green

# Проверяем текущую кодировку
Write-Host "`n📊 Текущие настройки кодировки:" -ForegroundColor Cyan
Write-Host "Console Output: $([Console]::OutputEncoding.EncodingName)" -ForegroundColor White
Write-Host "Console Input: $([Console]::InputEncoding.EncodingName)" -ForegroundColor White
Write-Host "PowerShell Output: $($OutputEncoding.EncodingName)" -ForegroundColor White

# Тестируем отображение русских символов
Write-Host "`n🧪 Тест отображения русских символов:" -ForegroundColor Cyan
Write-Host "Привет мир! Тест кодировки UTF-8" -ForegroundColor White
Write-Host "Логи Spring Boot должны отображаться корректно" -ForegroundColor White

Write-Host "`n✅ Настройка кодировки завершена!" -ForegroundColor Green
Write-Host "Теперь запустите backend снова для проверки" -ForegroundColor Yellow
