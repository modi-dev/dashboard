# Version Dashboard - Java Spring Boot Backend

Сервис мониторинга серверов и подов K8s на Java Spring Boot с Maven.

## 🚀 Быстрый старт

### Требования
- Java 17+
- Maven 3.6+
- PostgreSQL 12+

### Установка и запуск

1. **Клонирование и сборка:**
```bash
git clone <repository-url>
cd dashboard
mvn clean install
```

2. **Настройка базы данных:**
```bash
# Создание базы данных PostgreSQL
createdb server_dashboard

# Или через Docker
docker run -d --name postgres -p 5432:5432 -e POSTGRES_PASSWORD=password --network postgres_net postgres
docker run -d --name pgadmin -p 8080:80 -e PGADMIN_DEFAULT_EMAIL=user@example.com -e PGADMIN_DEFAULT_PASSWORD=password --network postgres_net dpage/pgadmin4
docker run --name postgres-dashboard -e POSTGRES_PASSWORD=password -e POSTGRES_DB=server_dashboard -p 5432:5432 -d postgres:15
```

3. **Настройка переменных окружения:**
```bash
export DB_URL=jdbc:postgresql://localhost:5432/server_dashboard
export DB_USERNAME=postgres
export DB_PASSWORD=password
```

4. **Запуск приложения:**

**Windows:**
```bash
start.bat
```

**Linux/Mac:**
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

### Kubernetes интеграция

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/pods/pods` | Информация о подах (JSON) |
| GET | `/api/pods/html` | HTML страница с подами |
| GET | `/api/pods/namespace` | Текущий namespace |
| GET | `/api/pods/summary` | Краткая информация о подах |
| GET | `/api/pods/pods/{name}` | Информация о конкретном поде |
| GET | `/api/pods/health` | Проверка доступности Kubernetes |

## 🔧 Конфигурация

### application.properties
```properties
# Server configuration
server.port=3001

# Database configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/server_dashboard
spring.datasource.username=postgres
spring.datasource.password=password

# JPA configuration
spring.jpa.hibernate.ddl-auto=update

# Monitoring configuration
monitoring.interval=30000  # Интервал проверки (мс)
monitoring.timeout=10000   # Таймаут проверки (мс)

# Kubernetes configuration
kubernetes.namespace=default
kubernetes.kubectl.path=kubectl
kubernetes.enabled=false
```

### Переменные окружения:
```bash
# Database
export DB_URL=jdbc:postgresql://localhost:5432/server_dashboard
export DB_USERNAME=postgres
export DB_PASSWORD=password

# Kubernetes (опционально)
export KUBERNETES_ENABLED=true
export KUBERNETES_NAMESPACE=my-namespace
export KUBERNETES_KUBECTL_PATH=kubectl
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
│   │   ├── ServerMonitorService.java
│   │   └── KubernetesService.java
│   └── config/                           # Конфигурация
│       ├── WebClientConfig.java
│       └── KubernetesConfig.java
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

