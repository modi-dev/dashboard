# Конфигурация приложения

## Структура конфигурации

Приложение использует двухуровневую систему конфигурации:

1. **application.yml** - общие настройки приложения
2. **application.properties** - чувствительные данные с поддержкой переменных окружения

## Настройка для разработки

### 1. Создайте файл .env в корне backend/

Скопируйте `env.example` в `.env` и настройте под ваши нужды:

```bash
cp env.example .env
```

### 2. Настройте переменные окружения

Отредактируйте `.env` файл:

```bash
# Database Configuration
DB_URL=jdbc:postgresql://localhost:5432/your_database
DB_USERNAME=your_username
DB_PASSWORD=your_password
DB_DDL_AUTO=update
DB_SHOW_SQL=false
DB_FORMAT_SQL=true

# Logging Configuration
LOG_LEVEL_DASHBOARD=INFO
LOG_LEVEL_WEB=INFO
LOG_LEVEL_SQL=DEBUG
LOG_LEVEL_BINDER=TRACE

# Management Configuration
MANAGEMENT_ENDPOINTS=health,info,metrics
HEALTH_SHOW_DETAILS=always

# Monitoring Configuration
MONITORING_INTERVAL=30000
MONITORING_TIMEOUT=10000
```

### 3. Запуск приложения

Приложение автоматически загрузит переменные окружения из `.env` файла.

## Настройка для продакшена

В продакшене используйте системные переменные окружения или внешние конфигурационные файлы:

```bash
export DB_URL=jdbc:postgresql://prod-server:5432/prod_db
export DB_USERNAME=prod_user
export DB_PASSWORD=secure_password
# ... остальные переменные
```

## Безопасность

- ✅ `.env` файлы исключены из git через `.gitignore`
- ✅ Чувствительные данные вынесены в переменные окружения
- ✅ `application.yml` содержит только общие настройки
- ✅ `application.properties` использует переменные окружения с значениями по умолчанию

## Доступные переменные окружения

| Переменная | Описание | Значение по умолчанию |
|------------|----------|----------------------|
| `DB_URL` | URL базы данных | `jdbc:postgresql://localhost:5432/postgres` |
| `DB_USERNAME` | Имя пользователя БД | `postgres` |
| `DB_PASSWORD` | Пароль БД | `password` |
| `DB_DDL_AUTO` | Режим DDL | `update` |
| `DB_SHOW_SQL` | Показывать SQL запросы | `false` |
| `DB_FORMAT_SQL` | Форматировать SQL | `true` |
| `LOG_LEVEL_DASHBOARD` | Уровень логирования приложения | `INFO` |
| `LOG_LEVEL_WEB` | Уровень логирования Spring Web | `INFO` |
| `LOG_LEVEL_SQL` | Уровень логирования SQL | `DEBUG` |
| `LOG_LEVEL_BINDER` | Уровень логирования параметров | `TRACE` |
| `MANAGEMENT_ENDPOINTS` | Доступные endpoints | `health,info,metrics` |
| `HEALTH_SHOW_DETAILS` | Показывать детали health check | `always` |
| `MONITORING_INTERVAL` | Интервал мониторинга (мс) | `30000` |
| `MONITORING_TIMEOUT` | Таймаут мониторинга (мс) | `10000` |
