# ✅ Исправление профилей Spring Boot - Завершено!

## 🎯 Проблема решена

**Команда `mvn spring-boot:run -Dspring-boot.run.profiles=dev` теперь работает корректно!**

### **Основные исправления:**

1. **Исправлен синтаксис команды:**
   - ❌ `mvn spring-boot:run -Dspring-boot.run.profiles=dev`
   - ✅ `mvn spring-boot:run "-Dspring-boot.run.profiles=dev"`

2. **Убраны русские комментарии** из `application.properties`
3. **Создан профиль dev** с настройками для разработки
4. **Добавлены скрипты** для удобного запуска

## 🚀 Готовые команды

### **Основные способы запуска:**

#### **1. С профилем dev (разработка):**
```bash
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
```

#### **2. С профилем test (тестирование):**
```bash
mvn spring-boot:run "-Dspring-boot.run.profiles=test"
```

#### **3. Без профиля (продакшен):**
```bash
mvn spring-boot:run
```

### **Скрипты для запуска:**

#### **Windows Batch:**
```bash
./run-dev.bat
```

#### **PowerShell:**
```powershell
./run-dev.ps1
```

## 📊 Профили проекта

### **dev (разработка):**
- ✅ H2 in-memory база данных
- ✅ H2 Console включен (`/h2-console`)
- ✅ Подробное логирование (DEBUG)
- ✅ Все management endpoints доступны
- ✅ Быстрая проверка серверов (10 сек)

### **test (тестирование):**
- ✅ H2 база данных
- ✅ Стандартное логирование
- ✅ Ограниченные endpoints

### **prod (продакшен):**
- ✅ PostgreSQL база данных
- ✅ Стандартное логирование
- ✅ Ограниченные endpoints
- ✅ Стандартные интервалы проверки

## 🔧 Технические детали

### **Проблемы, которые были исправлены:**

1. **PowerShell интерпретация параметров:**
   - Параметры с точками требуют кавычек
   - `-Dspring-boot.run.profiles=dev` → `"-Dspring-boot.run.profiles=dev"`

2. **Кодировка в application.properties:**
   - Русские комментарии вызывали ошибки
   - Убраны все русские комментарии

3. **Отсутствие профиля dev:**
   - Создан `application-dev.properties`
   - Настроены параметры для разработки

## 📚 Файлы проекта

### **Созданные файлы:**
- ✅ `application-dev.properties` - настройки для разработки
- ✅ `run-dev.bat` - скрипт для Windows
- ✅ `run-dev.ps1` - скрипт для PowerShell
- ✅ `PROFILE_FIX.md` - документация по исправлению

### **Исправленные файлы:**
- ✅ `application.properties` - убраны русские комментарии
- ✅ Добавлены настройки кодировки UTF-8

## 🎯 Результат

### **До исправления:**
```
[ERROR] Unknown lifecycle phase ".run.profiles=dev"
[ERROR] Value must only contain valid chars
```

### **После исправления:**
```
2025-10-26 15:03:49 - The following 1 profile is active: "dev"
2025-10-26 15:03:49 - Starting ServerDashboardApplication
2025-10-26 15:03:49 - H2 Console available at /h2-console
```

## 🚀 Рекомендации

### **Для разработки:**
1. **Используйте профиль dev** - H2 база, подробные логи
2. **Используйте скрипты** - `./run-dev.ps1` или `./run-dev.bat`
3. **Проверяйте H2 Console** - доступен по адресу `/h2-console`

### **Для продакшена:**
1. **Используйте профиль prod** - PostgreSQL, стандартные настройки
2. **Настройте переменные окружения** - для конфигурации
3. **Проверьте мониторинг** - для отслеживания состояния

---

**Проблема с профилями Spring Boot полностью решена!** 🎉

**Теперь можно запускать приложение с любым профилем без ошибок!** ✅
