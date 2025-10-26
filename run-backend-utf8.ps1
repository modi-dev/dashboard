# PowerShell скрипт для запуска backend с правильной кодировкой UTF-8

Write-Host "🚀 Запуск backend с кодировкой UTF-8..." -ForegroundColor Green

# Устанавливаем кодировку UTF-8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::InputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8
chcp 65001 | Out-Null

# Переходим в папку backend
Set-Location backend

Write-Host "📁 Рабочая директория: $(Get-Location)" -ForegroundColor Cyan
Write-Host "🔧 Кодировка: UTF-8" -ForegroundColor Cyan

# Запускаем backend
Write-Host "`n🚀 Запуск Spring Boot приложения..." -ForegroundColor Yellow
Write-Host "Для остановки нажмите Ctrl+C" -ForegroundColor Gray

mvn spring-boot:run
