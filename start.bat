@echo off
chcp 65001 > nul
set JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8
echo Starting Version Dashboard with UTF-8 encoding...
mvn spring-boot:run
pause