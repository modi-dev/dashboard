# 🔧 Исправление кодировки PowerShell для корректного отображения логов

## 🚨 Проблема
В PowerShell логи Spring Boot отображаются с неправильной кодировкой:
```
├Р┬г├С┬А├Р┬╛├Р┬▓├Р┬╡├Р┬╜├С┬М ├Р┬╗├Р┬╛
```

## ✅ Решение

### **Вариант 1: Автоматическая настройка (рекомендуется)**

1. **Запустите PowerShell от имени администратора**
2. **Выполните настройку:**
   ```powershell
   .\setup-encoding.ps1
   ```
3. **Перезапустите PowerShell**
4. **Запустите backend:**
   ```powershell
   .\run-backend-utf8.ps1
   ```

### **Вариант 2: Ручная настройка**

1. **Установите кодировку UTF-8:**
   ```powershell
   [Console]::OutputEncoding = [System.Text.Encoding]::UTF8
   [Console]::InputEncoding = [System.Text.Encoding]::UTF8
   $OutputEncoding = [System.Text.Encoding]::UTF8
   chcp 65001
   ```

2. **Настройте переменные окружения для Java:**
   ```powershell
   [Environment]::SetEnvironmentVariable("JAVA_TOOL_OPTIONS", "-Dfile.encoding=UTF-8", "User")
   [Environment]::SetEnvironmentVariable("MAVEN_OPTS", "-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8", "User")
   ```

3. **Запустите backend:**
   ```powershell
   cd backend
   mvn spring-boot:run
   ```

### **Вариант 3: Использование batch файла**

```cmd
.\run-backend-utf8.bat
```

## 📁 Созданные файлы

| Файл | Описание |
|------|----------|
| `setup-encoding.ps1` | Автоматическая настройка кодировки (требует админ права) |
| `run-backend-utf8.ps1` | Запуск backend с UTF-8 кодировкой |
| `run-backend-utf8.bat` | Batch версия для запуска |
| `fix-encoding.ps1` | Простая настройка кодировки |

## 🔍 Проверка результата

После настройки логи должны отображаться корректно:
```
2025-10-26 20:58:53 - Получение информации о подах в namespace: dev-tools
2025-10-26 20:58:53 - Используется kubectl: kubectl
2025-10-26 20:58:53 - Найдено 9 подов в JSON, успешно распарсено: 9
```

## 🚀 Быстрый старт

```powershell
# 1. Настройка (один раз)
.\setup-encoding.ps1

# 2. Запуск backend
.\run-backend-utf8.ps1

# 3. Проверка API
curl http://localhost:3001/api/version/pods
```

## ⚠️ Важные замечания

1. **Права администратора** - требуются для настройки системных переменных
2. **Перезапуск PowerShell** - необходим после настройки переменных окружения
3. **Кодировка файлов** - убедитесь, что файлы сохранены в UTF-8
4. **Шрифт консоли** - используйте шрифт, поддерживающий UTF-8 (Consolas, Courier New)

## 🎯 Результат

После применения исправлений:
- ✅ Логи отображаются с правильной кодировкой
- ✅ Русские символы читаемы
- ✅ Kubernetes интеграция работает корректно
- ✅ API возвращает данные о подах

---

**Следуйте инструкциям для исправления проблемы с кодировкой!** 🎉
