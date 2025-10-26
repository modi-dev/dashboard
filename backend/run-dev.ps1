# PowerShell скрипт для запуска Spring Boot приложения с профилем dev
# Устанавливает кодировку UTF-8 и запускает с профилем разработки

# Устанавливаем кодировку UTF-8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::InputEncoding = [System.Text.Encoding]::UTF8

# Устанавливаем переменные окружения для кодировки
$env:JAVA_TOOL_OPTIONS = "-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -Duser.timezone=UTC"
$env:MAVEN_OPTS = "-Dfile.encoding=UTF-8"
$env:SPRING_PROFILES_ACTIVE = "dev"

Write-Host "Starting Server Dashboard with dev profile..." -ForegroundColor Green
Write-Host ""

# Запускаем приложение с профилем dev
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"

Write-Host "Application stopped." -ForegroundColor Yellow
Read-Host "Press Enter to exit"
