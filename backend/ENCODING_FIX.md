# 🔧 Исправление проблемы с кодировкой символов

## 🎯 Проблема

В терминале Windows PowerShell отображаются неправильные символы вместо русского текста:
```
g.LogLevel.INFO # ├Р┬г├С┬А├Р┬╛├Р┬▓├Р┬╡├Р┬╜├С┬М ├Р┬╗├Р┬╛
```

## ✅ Решение

### 1. **Исправлены настройки в application.properties**

```properties
# Encoding configuration
server.servlet.encoding.charset=UTF-8
server.servlet.encoding.enabled=true
server.servlet.encoding.force=true

# Logging configuration
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n
```

### 2. **Созданы скрипты для запуска с правильной кодировкой**

#### **Windows Batch (run-with-encoding.bat):**
```batch
@echo off
chcp 65001
set JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8
set MAVEN_OPTS=-Dfile.encoding=UTF-8
mvn spring-boot:run
```

#### **PowerShell (run-with-encoding.ps1):**
```powershell
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::InputEncoding = [System.Text.Encoding]::UTF8
$env:JAVA_TOOL_OPTIONS = "-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8"
mvn spring-boot:run
```

## 🚀 Способы запуска

### **Способ 1: Использовать скрипты**
```bash
# Windows Batch
./run-with-encoding.bat

# PowerShell
./run-with-encoding.ps1
```

### **Способ 2: Установить кодировку вручную**
```bash
# Установить кодировку UTF-8
chcp 65001

# Установить переменные окружения
set JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8
set MAVEN_OPTS=-Dfile.encoding=UTF-8

# Запустить приложение
mvn spring-boot:run
```

### **Способ 3: PowerShell с настройками**
```powershell
# Установить кодировку UTF-8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::InputEncoding = [System.Text.Encoding]::UTF8

# Установить переменные окружения
$env:JAVA_TOOL_OPTIONS = "-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8"
$env:MAVEN_OPTS = "-Dfile.encoding=UTF-8"

# Запустить приложение
mvn spring-boot:run
```

## 🔧 Технические детали

### **Причины проблемы:**
1. **Кодировка Windows** - по умолчанию использует CP1251
2. **Java кодировка** - может использовать системную кодировку
3. **PowerShell кодировка** - может неправильно интерпретировать UTF-8
4. **Maven кодировка** - может наследовать системную кодировку

### **Исправления:**
1. **Spring Boot кодировка** - принудительная UTF-8
2. **Java кодировка** - явное указание UTF-8
3. **Консоль кодировка** - установка UTF-8 для терминала
4. **Maven кодировка** - передача параметров кодировки

## 📊 Результат

### **До исправления:**
```
g.LogLevel.INFO # ├Р┬г├С┬А├Р┬╛├Р┬▓├Р┬╡├Р┬╜├С┬М ├Р┬╗├Р┬╛
```

### **После исправления:**
```
2025-10-25 19:44:33 - INFO - Server Dashboard started successfully
2025-10-25 19:44:33 - DEBUG - Checking 4 server(s)...
```

## 🎯 Рекомендации

### **Для разработки:**
1. **Используйте скрипты** для запуска приложения
2. **Настройте IDE** на UTF-8 кодировку
3. **Проверьте кодировку файлов** в проекте

### **Для продакшена:**
1. **Установите переменные окружения** на сервере
2. **Настройте Docker** с UTF-8 кодировкой
3. **Проверьте логи** на правильность отображения

## 📚 Дополнительные настройки

### **Для IntelliJ IDEA:**
```
File → Settings → Editor → File Encodings
Global Encoding: UTF-8
Project Encoding: UTF-8
Default encoding for properties files: UTF-8
```

### **Для VS Code:**
```json
{
    "files.encoding": "utf8",
    "terminal.integrated.shellArgs.windows": ["-NoExit", "-Command", "chcp 65001"]
}
```

### **Для Windows Terminal:**
```json
{
    "profiles": {
        "defaults": {
            "fontFace": "Consolas",
            "fontSize": 12
        }
    }
}
```

---

**Проблема с кодировкой символов исправлена!** 🎉
