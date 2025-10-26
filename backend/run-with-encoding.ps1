# PowerShell скрипт для запуска Spring Boot приложения с правильной кодировкой UTF-8
# Устанавливает кодировку UTF-8 для Windows PowerShell

# Устанавливаем кодировку UTF-8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::InputEncoding = [System.Text.Encoding]::UTF8

# Устанавливаем переменные окружения для кодировки
$env:JAVA_TOOL_OPTIONS = "-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8"
$env:MAVEN_OPTS = "-Dfile.encoding=UTF-8"

Write-Host "Запуск Spring Boot приложения с кодировкой UTF-8..." -ForegroundColor Green

# Запускаем приложение
mvn spring-boot:run

Write-Host "Приложение остановлено." -ForegroundColor Yellow
Read-Host "Нажмите Enter для выхода"
