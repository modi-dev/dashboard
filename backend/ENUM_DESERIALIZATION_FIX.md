# Исправление проблемы десериализации ServerType

## Проблема

При отправке JSON запроса с типом сервера "Другое" возникала ошибка:

```
JSON parse error: Cannot deserialize value of type `com.dashboard.model.ServerType` from String "Другое": not one of the values accepted for Enum class: [OTHER, POSTGRES, REDIS, KAFKA, ASTRA_LINUX]
```

## Причина

Jackson по умолчанию ожидает имя enum константы (например, "OTHER"), а не displayName ("Другое"). В нашем случае:

- Enum константа: `OTHER`
- Display name: `"Другое"`

## Решение

### 1. Создан кастомный десериализатор

`ServerTypeDeserializer.java` - обрабатывает десериализацию как по имени enum, так и по displayName.

### 2. Обновлен ServerType enum

Добавлены аннотации Jackson:
- `@JsonDeserialize(using = ServerTypeDeserializer.class)` - использует кастомный десериализатор
- `@JsonValue` - для сериализации возвращает displayName
- `@JsonCreator` - статический метод `fromString()` для десериализации

### 3. Поддерживаемые форматы

Теперь API принимает ServerType в следующих форматах:

#### По displayName (рекомендуется для фронтенда):
```json
{
  "name": "Test Server",
  "url": "http://localhost:8080",
  "type": "Другое",
  "healthcheck": "http://localhost:8080/health"
}
```

#### По имени enum:
```json
{
  "name": "Test Server", 
  "url": "http://localhost:8080",
  "type": "OTHER",
  "healthcheck": "http://localhost:8080/health"
}
```

### 4. Сериализация

При возврате данных API всегда возвращает displayName:

```json
{
  "id": 1,
  "name": "Test Server",
  "url": "http://localhost:8080", 
  "type": "Другое",
  "status": "ONLINE"
}
```

## Тестирование

Создан тест `ServerTypeTest.java` который проверяет:

- ✅ Десериализацию по displayName ("Другое")
- ✅ Десериализацию по имени enum ("OTHER", "other")
- ✅ Сериализацию в displayName
- ✅ Обработку неверных значений
- ✅ Статический метод `fromString()`

## Использование

### Для фронтенда (рекомендуется):
Используйте displayName в JSON запросах:
```javascript
const serverData = {
  name: "My Server",
  url: "http://localhost:8080",
  type: "Другое",  // Используем displayName
  healthcheck: "http://localhost:8080/health"
};
```

### Для API интеграции:
Можно использовать как displayName, так и enum name:
```json
{
  "type": "Другое"    // displayName
}
```
или
```json
{
  "type": "OTHER"     // enum name
}
```

## Доступные типы серверов

| Enum Name | Display Name |
|-----------|--------------|
| `POSTGRES` | `"Postgres"` |
| `REDIS` | `"Redis"` |
| `KAFKA` | `"Kafka"` |
| `ASTRA_LINUX` | `"Astra Linux"` |
| `OTHER` | `"Другое"` |
