@echo off
REM Скрипт для запуска Spring Boot приложения с профилем dev
REM Устанавливает кодировку UTF-8 и запускает с профилем разработки

REM Устанавливаем кодировку UTF-8
chcp 65001

REM Устанавливаем переменные окружения для кодировки
set JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8
set MAVEN_OPTS=-Dfile.encoding=UTF-8

echo Starting Server Dashboard with dev profile...
echo.

REM Запускаем приложение с профилем dev
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"

pause
