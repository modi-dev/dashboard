# Server Dashboard - Java Spring Boot Backend

Система мониторинга серверов на Java Spring Boot с Maven.

## 🚀 Быстрый старт

### Требования
- Java 17+
- Maven 3.6+
- PostgreSQL 12+

### Установка и запуск

1. **Клонирование и сборка:**
```bash
cd backend
mvn clean install
```

2. **Настройка базы данных:**
```bash
# Создание базы данных PostgreSQL
createdb server_dashboard

# Или через Docker
docker run --name postgres-dashboard -e POSTGRES_PASSWORD=password -e POSTGRES_DB=server_dashboard -p 5432:5432 -d postgres:15
```

3. **Настройка переменных окружения:**
```bash
export DB_URL=jdbc:postgresql://localhost:5432/server_dashboard
export DB_USERNAME=postgres
export DB_PASSWORD=password
```

4. **Запуск приложения:**
```bash
mvn spring-boot:run
```

## 📋 API Endpoints

### Основные маршруты

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Информация об API |
| GET | `/api/servers` | Получить все серверы |
| POST | `/api/servers` | Создать сервер |
| GET | `/api/servers/{id}` | Получить сервер по ID |
| PUT | `/api/servers/{id}` | Обновить сервер |
| DELETE | `/api/servers/{id}` | Удалить сервер |
| POST | `/api/servers/{id}/check` | Проверить сервер |

### Мониторинг

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/actuator/health` | Статус приложения |
| GET | `/actuator/metrics` | Метрики приложения |

## 🔧 Конфигурация

### application.yml
```yaml
server:
  port: 3001

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/server_dashboard
    username: postgres
    password: password
  
  jpa:
    hibernate:
      ddl-auto: update

monitoring:
  interval: 30000  # Интервал проверки (мс)
  timeout: 10000   # Таймаут проверки (мс)
```

## 🏗️ Архитектура

### Структура проекта
```
backend/
├── src/main/java/com/dashboard/
│   ├── ServerDashboardApplication.java    # Главный класс
│   ├── controller/                        # REST контроллеры
│   │   ├── ServerController.java
│   │   └── HomeController.java
│   ├── model/                            # JPA модели
│   │   ├── Server.java
│   │   ├── ServerType.java
│   │   └── ServerStatus.java
│   ├── dto/                              # DTO классы
│   │   └── ServerDto.java
│   ├── repository/                       # JPA репозитории
│   │   └── ServerRepository.java
│   ├── service/                          # Бизнес логика
│   │   └── ServerMonitorService.java
│   └── config/                           # Конфигурация
│       └── WebClientConfig.java
├── src/main/resources/
│   └── application.yml                   # Конфигурация
└── pom.xml                               # Maven конфигурация
```

## 🎯 Типы серверов

### Поддерживаемые типы:
- **POSTGRES** - PostgreSQL база данных
- **REDIS** - Redis кэш
- **KAFKA** - Apache Kafka
- **ASTRA_LINUX** - Astra Linux сервер
- **OTHER** - Кастомные HTTP серверы

### Методы проверки:
- **TCP соединение** - для PostgreSQL, Redis, Kafka, Astra Linux
- **HTTP запросы** - для кастомных серверов с healthcheck

## 🔄 Мониторинг

### Автоматическая проверка:
- Интервал: 30 секунд (настраивается)
- Таймаут: 10 секунд (настраивается)
- Параллельная проверка всех серверов

### Логирование:
```
✓ Server: My DB | Type: POSTGRES | Status: ONLINE | Time: 2ms
✗ Server: Redis Cache | Type: REDIS | Status: OFFLINE | Time: 10000ms
```

## 🧪 Тестирование

### Запуск тестов:
```bash
mvn test
```

### Интеграционные тесты:
```bash
mvn test -Dtest=*IntegrationTest
```

## 📊 Мониторинг и метрики

### Actuator endpoints:
- `/actuator/health` - Статус здоровья
- `/actuator/metrics` - Метрики приложения
- `/actuator/info` - Информация о приложении

## 🚀 Развертывание

### JAR файл:
```bash
mvn clean package
java -jar target/server-dashboard-1.0.0.jar
```

### Docker:
```bash
# Создание образа
docker build -t server-dashboard .

# Запуск контейнера
docker run -p 3001:3001 server-dashboard
```

## 🔧 Разработка

### Горячая перезагрузка:
```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.devtools.restart.enabled=true"
```

### Профили:
```bash
# Development
mvn spring-boot:run -Dspring.profiles.active=dev

# Production
mvn spring-boot:run -Dspring.profiles.active=prod
```

