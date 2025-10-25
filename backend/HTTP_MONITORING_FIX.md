# Исправление проблемы мониторинга HTTP серверов

## Проблема

Серверы с типом "Другое" и URL `https://google.com/` показывали статус "оффлайн", хотя сайт доступен.

## Причины проблемы

1. **Отсутствие User-Agent**: Многие сайты (включая Google) блокируют запросы без правильного User-Agent
2. **Обработка редиректов**: Google.com возвращает 301 редирект, который не обрабатывался корректно
3. **Недостаточное логирование**: Сложно было понять, что происходит при проверке

## Решение

### 1. Улучшенные HTTP заголовки

Добавлены реалистичные заголовки браузера:

```java
.defaultHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
.defaultHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
.defaultHeader("Accept-Language", "en-US,en;q=0.5")
.defaultHeader("Accept-Encoding", "gzip, deflate")
.defaultHeader("Connection", "keep-alive")
.defaultHeader("Upgrade-Insecure-Requests", "1")
```

### 2. Поддержка редиректов

Теперь считаются успешными как 2xx, так и 3xx ответы:

```java
boolean isSuccessful = response.getStatusCode().is2xxSuccessful() || 
                     (response.getStatusCode().value() >= 300 && response.getStatusCode().value() < 400);
```

### 3. Улучшенное логирование

Добавлено детальное логирование для отладки:

```java
logger.debug("Checking HTTP endpoint: {} for server: {}", checkUrl, server.getName());
logger.debug("HTTP response for {}: {} - {}", checkUrl, response.getStatusCode(), isSuccessful ? "SUCCESS" : "FAILED");
```

### 4. Конфигурация логирования

Добавлен отдельный уровень логирования для мониторинга:

```properties
logging.level.com.dashboard.service.ServerMonitorService=${LOG_LEVEL_MONITOR:DEBUG}
```

## Результаты тестирования

### ✅ Google.com
- **Статус**: ONLINE
- **Ответ**: 301 MOVED_PERMANENTLY
- **Время**: ~27-547ms

### ✅ Httpbin.org
- **Статус**: ONLINE  
- **Ответ**: 200 OK
- **Время**: ~1000-1500ms

### ✅ С healthcheck
- **URL**: `https://httpbin.org/status/200`
- **Статус**: ONLINE
- **Ответ**: 200 OK

## Логи для отладки

При включенном DEBUG уровне логирования вы увидите:

```
DEBUG - Checking HTTP endpoint: https://google.com/ for server: Google Test
DEBUG - HTTP response for https://google.com/: 301 MOVED_PERMANENTLY - SUCCESS
INFO  - ✓ Server: Google Test | Type: Другое | Status: online | Time: 27ms
```

## Настройка для продакшена

### Переменные окружения

```bash
# Включить DEBUG логирование для мониторинга
LOG_LEVEL_MONITOR=DEBUG

# Или отключить для продакшена
LOG_LEVEL_MONITOR=INFO
```

### Рекомендации

1. **Для популярных сайтов**: Используйте healthcheck endpoints вместо корневых URL
2. **Для API**: Указывайте конкретные healthcheck пути
3. **Мониторинг**: Включите DEBUG логирование для диагностики проблем

## Примеры использования

### С healthcheck (рекомендуется)
```json
{
  "name": "My API",
  "url": "https://api.example.com",
  "type": "Другое",
  "healthcheck": "/health"
}
```

### Без healthcheck (для простых сайтов)
```json
{
  "name": "Google",
  "url": "https://google.com/",
  "type": "Другое"
}
```

## Поддерживаемые типы ответов

- ✅ **2xx** - Успешные ответы (200, 201, 202, etc.)
- ✅ **3xx** - Редиректы (301, 302, 303, etc.)
- ❌ **4xx** - Ошибки клиента (404, 403, etc.)
- ❌ **5xx** - Ошибки сервера (500, 502, etc.)

Теперь мониторинг HTTP серверов работает корректно! 🚀
