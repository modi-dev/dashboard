@echo off
REM Скрипт для запуска Spring Boot приложения с правильной кодировкой UTF-8
REM Устанавливает кодировку UTF-8 для Windows

REM Устанавливаем кодировку UTF-8
chcp 65001

REM Устанавливаем переменные окружения для кодировки
set JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8
set MAVEN_OPTS=-Dfile.encoding=UTF-8

REM Запускаем приложение
mvn spring-boot:run

pause
