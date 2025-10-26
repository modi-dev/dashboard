# PowerShell скрипт для настройки системной кодировки
# Запускать от имени администратора

Write-Host "🔧 Настройка системной кодировки для корректного отображения логов..." -ForegroundColor Green

# Проверяем права администратора
if (-NOT ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole] "Administrator")) {
    Write-Host "❌ Требуются права администратора для настройки системной кодировки" -ForegroundColor Red
    Write-Host "Запустите PowerShell от имени администратора" -ForegroundColor Yellow
    exit 1
}

# Устанавливаем кодировку UTF-8 для консоли
Write-Host "📝 Настройка кодировки консоли..." -ForegroundColor Cyan
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::InputEncoding = [System.Text.Encoding]::UTF8

# Устанавливаем кодировку для PowerShell
$OutputEncoding = [System.Text.Encoding]::UTF8

# Настраиваем кодировку для Windows
chcp 65001 | Out-Null

# Настраиваем переменные окружения для Java
Write-Host "☕ Настройка переменных окружения для Java..." -ForegroundColor Cyan
[Environment]::SetEnvironmentVariable("JAVA_TOOL_OPTIONS", "-Dfile.encoding=UTF-8", "User")
[Environment]::SetEnvironmentVariable("MAVEN_OPTS", "-Dfile.encoding=UTF-8", "User")

# Настраиваем кодировку для Maven
Write-Host "🔨 Настройка кодировки для Maven..." -ForegroundColor Cyan
$mavenOpts = "-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8"
[Environment]::SetEnvironmentVariable("MAVEN_OPTS", $mavenOpts, "User")

Write-Host "✅ Настройка кодировки завершена!" -ForegroundColor Green

# Проверяем настройки
Write-Host "`n📊 Текущие настройки кодировки:" -ForegroundColor Cyan
Write-Host "Console Output: $([Console]::OutputEncoding.EncodingName)" -ForegroundColor White
Write-Host "Console Input: $([Console]::InputEncoding.EncodingName)" -ForegroundColor White
Write-Host "PowerShell Output: $($OutputEncoding.EncodingName)" -ForegroundColor White
Write-Host "JAVA_TOOL_OPTIONS: $([Environment]::GetEnvironmentVariable('JAVA_TOOL_OPTIONS', 'User'))" -ForegroundColor White
Write-Host "MAVEN_OPTS: $([Environment]::GetEnvironmentVariable('MAVEN_OPTS', 'User'))" -ForegroundColor White

# Тестируем отображение
Write-Host "`n🧪 Тест отображения русских символов:" -ForegroundColor Cyan
Write-Host "Привет мир! Тест кодировки UTF-8" -ForegroundColor White
Write-Host "Логи Spring Boot должны отображаться корректно" -ForegroundColor White
Write-Host "Kubernetes интеграция работает" -ForegroundColor White

Write-Host "`n✅ Настройка завершена! Перезапустите PowerShell и запустите backend" -ForegroundColor Green
Write-Host "Используйте: .\run-backend-utf8.ps1" -ForegroundColor Yellow
