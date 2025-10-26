# PowerShell скрипт для запуска Spring Boot с правильной кодировкой
# Полностью исправляет проблему с нечитаемыми символами

# Устанавливаем кодировку UTF-8 для консоли
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::InputEncoding = [System.Text.Encoding]::UTF8

# Устанавливаем переменные окружения для Java
$env:JAVA_TOOL_OPTIONS = "-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -Duser.timezone=UTC -Djava.awt.headless=true"
$env:MAVEN_OPTS = "-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8"
$env:SPRING_PROFILES_ACTIVE = "dev"

# Устанавливаем кодировку для PowerShell
$OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "Starting Server Dashboard with clean encoding..." -ForegroundColor Green
Write-Host "Encoding: UTF-8" -ForegroundColor Yellow
Write-Host "Profile: dev" -ForegroundColor Yellow
Write-Host ""

# Запускаем приложение
mvn spring-boot:run "-Dspring-boot.run.profiles=dev" "-Dfile.encoding=UTF-8"

Write-Host "Application stopped." -ForegroundColor Yellow
Read-Host "Press Enter to exit"
