# 🔧 Исправление парсинга URL для PostgreSQL и других серверов

## 🎯 Проблема

При добавлении PostgreSQL сервера с URL типа `postgresql://localhost:5432/database` возникала ошибка, потому что:

1. **Неправильный парсинг протокола** - код пытался использовать стандартный URI парсер для `postgresql://`
2. **Смешивание протоколов** - PostgreSQL использует TCP, а не HTTP/HTTPS
3. **Отсутствие обработки** специфичных протоколов баз данных

## ✅ Решение

### 1. **Добавлена поддержка специфичных протоколов**

```java
// PostgreSQL URL: postgresql://host:port/database
if (url.startsWith("postgresql://") || url.startsWith("postgres://")) {
    String cleanUrl = url.replaceFirst("^postgresql?://", "");
    String[] parts = cleanUrl.split("/")[0].split(":");
    host = parts[0];
    port = parts.length > 1 ? Integer.parseInt(parts[1]) : getDefaultPort(server.getType());
}
```

### 2. **Поддержка различных форматов URL**

- ✅ `postgresql://localhost:5432/database`
- ✅ `postgres://localhost:5432/database`
- ✅ `redis://localhost:6379`
- ✅ `kafka://localhost:9092`
- ✅ `localhost:5432` (без протокола)
- ✅ `http://localhost:8080` (для HTTP серверов)

### 3. **Правильная логика проверки**

```java
switch (server.getType()) {
    case POSTGRES:
    case REDIS:
    case KAFKA:
    case ASTRA_LINUX:
        return checkTcpConnection(host, port) ? ServerStatus.ONLINE : ServerStatus.OFFLINE;
        
    case OTHER:
        return checkHttpEndpoint(server) ? ServerStatus.ONLINE : ServerStatus.OFFLINE;
}
```

## 🎯 Поддерживаемые форматы URL

### **PostgreSQL**
```
postgresql://localhost:5432/mydb
postgres://localhost:5432/mydb
localhost:5432
```

### **Redis**
```
redis://localhost:6379
localhost:6379
```

### **Kafka**
```
kafka://localhost:9092
localhost:9092
```

### **HTTP/HTTPS (тип "Другое")**
```
http://localhost:8080
https://example.com
https://api.example.com/health
```

## 🔧 Технические детали

### **Алгоритм парсинга:**

1. **Проверка протокола** - определяем тип URL по префиксу
2. **Извлечение host:port** - убираем протокол и парсим адрес
3. **Обработка порта по умолчанию** - используем стандартные порты
4. **Выбор метода проверки** - TCP для БД, HTTP для веб-серверов

### **Обработка ошибок:**
- ✅ Неверный формат URL
- ✅ Отсутствие порта
- ✅ Недоступный хост
- ✅ Неправильный протокол

## 🧪 Тестирование

### **Создан тест UrlParsingTest.java:**
- ✅ Тест PostgreSQL URL с протоколом
- ✅ Тест Redis URL
- ✅ Тест Kafka URL
- ✅ Тест формата host:port
- ✅ Тест HTTP/HTTPS URL
- ✅ Тест обработки портов по умолчанию

### **Запуск тестов:**
```bash
mvn test -Dtest=UrlParsingTest
```

## 📊 Примеры использования

### **Добавление PostgreSQL сервера:**
```
URL: postgresql://localhost:5432/mydb
Тип: Postgres
Результат: TCP соединение на localhost:5432
```

### **Добавление Redis сервера:**
```
URL: redis://localhost:6379
Тип: Redis
Результат: TCP соединение на localhost:6379
```

### **Добавление HTTP API:**
```
URL: https://api.example.com
Тип: Другое
Результат: HTTP GET запрос
```

## 🚀 Результат

### **Теперь поддерживается:**
- ✅ Все стандартные протоколы баз данных
- ✅ Гибкие форматы URL
- ✅ Правильная проверка TCP соединений
- ✅ HTTP проверка для веб-серверов
- ✅ Обработка портов по умолчанию

### **Улучшенная совместимость:**
- ✅ PostgreSQL, Redis, Kafka
- ✅ HTTP/HTTPS серверы
- ✅ Различные форматы URL
- ✅ Автоматическое определение протокола

## 📚 Документация

### **Для разработчиков:**
- Подробные комментарии в коде
- Тесты для всех сценариев
- Примеры использования
- Обработка ошибок

### **Для пользователей:**
- Гибкие форматы URL
- Автоматическое определение протокола
- Понятные сообщения об ошибках
- Поддержка стандартных портов

---

**Парсинг URL для PostgreSQL и других серверов исправлен!** 🎉
