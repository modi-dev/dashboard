# 🔧 Исправление проблемы с профилями Spring Boot

## 🎯 Проблема

При запуске команды `mvn spring-boot:run -Dspring-boot.run.profiles=dev` возникали ошибки:

1. **Неправильный синтаксис команды** - PowerShell неправильно интерпретировал параметры
2. **Проблемы с кодировкой** - русские комментарии в `application.properties` вызывали ошибки
3. **Отсутствие профиля dev** - не было настроек для разработки

## ✅ Решение

### 1. **Исправлен синтаксис команды**

#### **Неправильно:**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

#### **Правильно:**
```bash
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
```

### 2. **Убраны русские комментарии из application.properties**

#### **До исправления:**
```properties
management.endpoints.web.exposure.include=${MANAGEMENT_ENDPOINTS:health,info,metrics} # Какие management endpoint'ы будут доступны извне
```

#### **После исправления:**
```properties
management.endpoints.web.exposure.include=${MANAGEMENT_ENDPOINTS:health,info,metrics}
```

### 3. **Создан профиль dev (application-dev.properties)**

```properties
# Development profile configuration
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.h2.console.enabled=true
spring.jpa.hibernate.ddl-auto=create-drop
logging.level.com.dashboard=DEBUG
management.endpoints.web.exposure.include=*
```

## 🚀 Способы запуска

### **Способ 1: С профилем dev**
```bash
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
```

### **Способ 2: С переменной окружения**
```bash
set SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run
```

### **Способ 3: В PowerShell**
```powershell
$env:SPRING_PROFILES_ACTIVE="dev"
mvn spring-boot:run
```

### **Способ 4: С JVM параметрами**
```bash
mvn spring-boot:run "-Dspring.profiles.active=dev"
```

## 📊 Профили проекта

### **dev (разработка):**
- H2 in-memory база данных
- H2 Console включен
- Подробное логирование
- Все management endpoints доступны
- Быстрая проверка серверов (10 сек)

### **test (тестирование):**
- H2 база данных
- Минимальное логирование
- Только необходимые endpoints

### **prod (продакшен):**
- PostgreSQL база данных
- Стандартное логирование
- Ограниченные endpoints
- Стандартные интервалы проверки

## 🔧 Технические детали

### **Проблемы с PowerShell:**
1. **Параметры с точками** - требуют кавычек
2. **Кодировка символов** - русские комментарии вызывают ошибки
3. **Переменные окружения** - могут конфликтовать

### **Исправления:**
1. **Кавычки для параметров** - `"-Dspring-boot.run.profiles=dev"`
2. **Убраны русские комментарии** - только английские
3. **Создан профиль dev** - отдельные настройки для разработки

## 📚 Примеры использования

### **Запуск с профилем dev:**
```bash
# Основной способ
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"

# Альтернативный способ
mvn spring-boot:run "-Dspring.profiles.active=dev"
```

### **Запуск с профилем test:**
```bash
mvn spring-boot:run "-Dspring-boot.run.profiles=test"
```

### **Запуск без профиля (prod):**
```bash
mvn spring-boot:run
```

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
```

## 📋 Рекомендации

### **Для разработки:**
1. **Используйте профиль dev** - H2 база, подробные логи
2. **Проверяйте кодировку** - только английские комментарии
3. **Используйте кавычки** - для параметров с точками

### **Для продакшена:**
1. **Используйте профиль prod** - PostgreSQL, стандартные настройки
2. **Проверьте переменные окружения** - для конфигурации
3. **Настройте мониторинг** - для отслеживания состояния

---

**Проблема с профилями Spring Boot исправлена!** 🎉
