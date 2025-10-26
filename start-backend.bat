@echo off
echo 🚀 Запуск backend с настройкой кодировки...
echo.

REM Установка кодировки UTF-8
chcp 65001 >nul
echo ✅ Кодировка UTF-8 установлена

REM Настройка переменных окружения для Java
set JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8
set MAVEN_OPTS=-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8
echo ☕ Переменные окружения Java настроены

REM Переход в папку backend
cd backend
echo 📁 Рабочая директория: %CD%

REM Проверка наличия Maven
where mvn >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Maven не найден. Установите Maven
    pause
    exit /b 1
)
echo 🔨 Maven найден

REM Запуск Spring Boot
echo.
echo 🚀 Запуск Spring Boot приложения...
echo Для остановки нажмите Ctrl+C
echo Логи должны отображаться с правильной кодировкой
echo.
echo ==================================================
mvn spring-boot:run

pause
