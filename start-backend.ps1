# PowerShell скрипт для запуска backend с правильной кодировкой
# Этот скрипт настраивает кодировку и запускает Spring Boot

Write-Host "🚀 Запуск backend с настройкой кодировки..." -ForegroundColor Green

# Настройка кодировки для текущей сессии
try {
    [Console]::OutputEncoding = [System.Text.Encoding]::UTF8
    [Console]::InputEncoding = [System.Text.Encoding]::UTF8
    $OutputEncoding = [System.Text.Encoding]::UTF8
    chcp 65001 | Out-Null
    Write-Host "✅ Кодировка UTF-8 установлена" -ForegroundColor Green
} catch {
    Write-Host "⚠️ Не удалось установить кодировку: $($_.Exception.Message)" -ForegroundColor Yellow
}

# Настройка переменных окружения для Java
$env:JAVA_TOOL_OPTIONS = "-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8"
$env:MAVEN_OPTS = "-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8"

Write-Host "☕ Переменные окружения Java настроены" -ForegroundColor Cyan

# Переход в папку backend
Set-Location backend
Write-Host "📁 Рабочая директория: $(Get-Location)" -ForegroundColor Cyan

# Проверка наличия Maven
if (Get-Command mvn -ErrorAction SilentlyContinue) {
    Write-Host "🔨 Maven найден" -ForegroundColor Green
} else {
    Write-Host "❌ Maven не найден. Установите Maven" -ForegroundColor Red
    exit 1
}

# Запуск Spring Boot
Write-Host "`n🚀 Запуск Spring Boot приложения..." -ForegroundColor Yellow
Write-Host "Для остановки нажмите Ctrl+C" -ForegroundColor Gray
Write-Host "Логи должны отображаться с правильной кодировкой" -ForegroundColor White
Write-Host "`n" + "="*50 -ForegroundColor DarkGray

mvn spring-boot:run
