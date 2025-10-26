@echo off
chcp 65001 >nul
echo 🚀 Запуск backend с кодировкой UTF-8...
cd backend
echo 📁 Рабочая директория: %CD%
echo 🔧 Кодировка: UTF-8
echo.
echo 🚀 Запуск Spring Boot приложения...
echo Для остановки нажмите Ctrl+C
echo.
mvn spring-boot:run
pause
