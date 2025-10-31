# Version Dashboard - Java Spring Boot Backend

Сервис мониторинга серверов и подов K8s на Java Spring Boot с Maven.

## 🚀 Быстрый старт

### Требования
- Java 17+
- Maven 3.6+
- PostgreSQL 12+
- (Опционально) Kubernetes кластер для мониторинга подов

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
docker run --name postgres-dashboard \
  -e POSTGRES_PASSWORD=password \
  -e POSTGRES_DB=server_dashboard \
  -p 5432:5432 -d postgres:15
```

3. **Настройка переменных окружения:**
```bash
# База данных
export DATABASE_CLUSTER_URL=jdbc:postgresql://localhost:5432
export DATABASE_NAME=server_dashboard
export DATABASE_USERNAME=postgres
export DATABASE_PASSWORD=password

# Приложение (опционально)
export APP_PORT=3001
export MANAGEMENT_PORT=3001

# Kubernetes (опционально)
export KUBERNETES_ENABLED=true
export KUBERNETES_NAMESPACE=dev-tools
export KUBERNETES_KUBECTL_PATH=kubectl  # Используется встроенный kubectl если не указан
```

4. **Запуск приложения:**

**Windows:**
```bash
mvn spring-boot:run
```

**Linux/Mac:**
```bash
mvn spring-boot:run
```

Приложение будет доступно по адресу: `http://localhost:3001`

## 📋 Web UI

Приложение включает полнофункциональный веб-интерфейс:

- **Dashboard** (`/`) - главная страница с обзором серверов и подов
- **Servers** (`/servers`) - управление серверами
- **Pods** (`/pods`) - просмотр информации о Kubernetes подах

### Особенности UI:
- ✅ Полностью автономный (все ресурсы локальные, без зависимости от CDN)
- ✅ Адаптивный дизайн для всех устройств
- ✅ Боковое меню для добавления серверов
- ✅ Экспорт данных в CSV (с поддержкой UTF-8)
- ✅ Статистика в реальном времени

## 📋 API Endpoints

### Основные маршруты

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Dashboard главная страница |
| GET | `/servers` | Страница управления серверами |
| GET | `/pods` | Страница с информацией о подах |
| GET | `/api/servers` | Получить все серверы (JSON) |
| POST | `/api/servers` | Создать сервер |
| GET | `/api/servers/{id}` | Получить сервер по ID |
| PUT | `/api/servers/{id}` | Обновить сервер |
| DELETE | `/api/servers/{id}` | Удалить сервер |
| POST | `/api/servers/{id}/check` | Проверить сервер вручную |
| GET | `/api/servers/export/csv` | Экспорт серверов в CSV |

### Мониторинг и Health Checks

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/actuator/health` | Общий статус здоровья |
| GET | `/actuator/health/liveness` | Kubernetes liveness probe |
| GET | `/actuator/health/readiness` | Kubernetes readiness probe |
| GET | `/actuator/metrics` | Метрики приложения |
| GET | `/actuator/info` | Информация о приложении |

**Примечание:** Порт Actuator можно настроить через переменную `MANAGEMENT_PORT` (по умолчанию совпадает с портом приложения).

### Kubernetes интеграция

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/pods` | Информация о подах (JSON) |
| GET | `/api/pods/export/csv` | Экспорт подов в CSV |
| GET | `/api/pods/namespace` | Текущий namespace |
| GET | `/api/pods/summary` | Краткая информация о подах |

**Примечание:** Приложение включает встроенный `kubectl` (v1.34.1) для Linux. Для других ОС или при необходимости использовать внешний `kubectl` задайте `KUBERNETES_KUBECTL_PATH`.

## 🔧 Конфигурация

### Переменные окружения

```bash
# База данных
export DATABASE_CLUSTER_URL=jdbc:postgresql://localhost:5432
export DATABASE_NAME=server_dashboard
export DATABASE_USERNAME=postgres
export DATABASE_PASSWORD=password
export DATABASE_CLUSTER_PARAMETERS=  # Дополнительные параметры подключения

# Сервер
export APP_PORT=3001
export SERVER_CONTEXT_PATH=  # Префикс пути (по умолчанию пусто)

# Actuator
export MANAGEMENT_PORT=3001  # Порт для health endpoints
export MANAGEMENT_ENDPOINTS=health,info,metrics  # Разрешенные endpoints
export MANAGEMENT_HEALTH_SHOW_DETAILS=always  # Детали health checks

# Kubernetes
export KUBERNETES_ENABLED=true  # Включить интеграцию с Kubernetes
export KUBERNETES_NAMESPACE=dev-tools  # Namespace для мониторинга
export KUBERNETES_KUBECTL_PATH=kubectl  # Путь к kubectl (используется встроенный если не указан)

# Мониторинг
export MONITORING_INTERVAL=5      # Интервал проверки серверов (в минутах, по умолчанию: 5)
export MONITORING_TIMEOUT=0.17    # Таймаут проверки (в минутах, по умолчанию: 0.17 = ~10 секунд)

# Логирование
export LOG_LEVEL_DASHBOARD=INFO  # Общий уровень логирования
export LOG_LEVEL_MONITOR=DEBUG  # Логирование мониторинга
export LOG_LEVEL_KUBERNETES=INFO  # Логирование Kubernetes
```

### application.yml

Основная конфигурация находится в `src/main/resources/application.yml`. Все параметры можно переопределить через переменные окружения.

**Важные настройки:**
- `spring.jpa.open-in-view: false` - оптимизация производительности
- `spring.jpa.hibernate.ddl-auto: update` - автоматическое обновление схемы БД
- `monitoring.interval: 5` - проверка серверов каждые 5 минут (значение в минутах)

## 🏗️ Архитектура

### Структура проекта
```
dashboard/
├── src/main/java/com/dashboard/
│   ├── ServerDashboardApplication.java    # Главный класс
│   ├── controller/                        # REST контроллеры
│   │   ├── ServerController.java
│   │   ├── HomeController.java
│   │   ├── PodsController.java
│   │   └── VersionController.java
│   ├── model/                            # JPA модели
│   │   ├── Server.java
│   │   ├── ServerType.java
│   │   ├── ServerStatus.java
│   │   └── PodInfo.java
│   ├── dto/                              # DTO классы
│   │   └── ServerDto.java
│   ├── repository/                       # JPA репозитории
│   │   └── ServerRepository.java
│   ├── service/                          # Бизнес логика
│   │   ├── ServerMonitorService.java
│   │   ├── ServerMonitorScheduler.java
│   │   ├── ServerVersionService.java
│   │   ├── KubernetesService.java
│   │   ├── EmbeddedKubectlService.java   # Управление встроенным kubectl
│   │   └── CsvExportService.java
│   └── config/                           # Конфигурация
│       ├── WebClientConfig.java
│       └── KubernetesConfig.java
├── src/main/resources/
│   ├── application.yml                   # Конфигурация
│   ├── templates/                        # Thymeleaf шаблоны
│   │   ├── layout.html
│   │   ├── index.html
│   │   ├── dashboard.html
│   │   ├── servers.html
│   │   ├── pods.html
│   │   └── fragments/
│   └── static/                           # Статические ресурсы (локальные)
│       ├── css/
│       │   ├── bootstrap.min.css
│       │   └── all.min.css (Font Awesome)
│       ├── js/
│       │   └── bootstrap.bundle.min.js
│       └── webfonts/                     # Font Awesome шрифты
│   └── binaries/                         # Встроенные бинарники
│       └── kubectl/
│           └── kubectl-linux-amd64
├── src/test/                             # Тесты
│   ├── java/com/dashboard/
│   │   ├── integration/                  # Интеграционные тесты
│   │   │   ├── TestContainersBaseTest.java
│   │   │   └── ...
│   │   └── service/
│   └── resources/
│       └── application-test.yml          # Конфигурация для тестов
├── Dockerfile                            # Docker образ
├── pom.xml                               # Maven конфигурация
└── README.md
```

## 🎯 Типы серверов

### Поддерживаемые типы:
- **POSTGRES** - PostgreSQL база данных
- **REDIS** - Redis кэш
- **KAFKA** - Apache Kafka
- **ASTRA_LINUX** - Astra Linux сервер
- **OTHER** - Кастомные HTTP серверы (требуется healthcheck endpoint)

### Методы проверки:
- **TCP соединение** - для PostgreSQL, Redis, Kafka, Astra Linux
- **HTTP запросы** - для кастомных серверов (тип OTHER)

### Особенности:
- Протокол (http://, https://, postgres:// и т.д.) добавляется автоматически при проверке
- Для типа OTHER можно указать custom metrics endpoint и regex для извлечения версии

## 🔄 Мониторинг

### Автоматическая проверка:
- Интервал: 5 минут (настраивается через `MONITORING_INTERVAL`, значение в минутах)
- Таймаут: 0.17 минут (~10 секунд, настраивается через `MONITORING_TIMEOUT`, значение в минутах)
- Параллельная проверка всех серверов

### Логирование:
Все проверки логируются с уровнем DEBUG для `ServerMonitorService`.

## 🧪 Тестирование

### Запуск всех тестов:
```bash
mvn test
```

### Интеграционные тесты с TestContainers:
```bash
# Тесты используют Docker для запуска PostgreSQL, Redis и Kafka
mvn test -Dtest=*IntegrationTest
```

**Примечание:** Для интеграционных тестов требуется Docker.

### Структура тестов:
- **Unit тесты** - `src/test/java/com/dashboard/service/`
- **Integration тесты** - `src/test/java/com/dashboard/integration/`
- **TestContainers** - автоматический запуск зависимостей в Docker

## 📊 CSV Экспорт

### Экспорт серверов:
```bash
curl http://localhost:3001/api/servers/export/csv -o servers.csv
```

### Экспорт подов:
```bash
curl http://localhost:3001/api/pods/export/csv -o pods.csv
```

**Особенности:**
- UTF-8 кодировка с BOM для корректного отображения в Excel
- Разделитель: точка с запятой (`;`)
- Автоматическое экранирование специальных символов

## 🚀 Развертывание

### JAR файл:
```bash
mvn clean package
java -jar target/ms-dashboard-*.jar
```

### Docker:
```bash
# Сборка образа
docker build -t ms-dashboard .

# Запуск контейнера
docker run -p 3001:3001 \
  -e DATABASE_CLUSTER_URL=jdbc:postgresql://host.docker.internal:5432 \
  -e DATABASE_NAME=server_dashboard \
  -e DATABASE_USERNAME=postgres \
  -e DATABASE_PASSWORD=password \
  -e KUBERNETES_ENABLED=true \
  ms-dashboard
```

**Примечание:** В Docker контейнере автоматически используется встроенный `kubectl` для Linux.

### Kubernetes Deployment:
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ms-dashboard
spec:
  replicas: 1
  template:
    spec:
      containers:
      - name: dashboard
        image: ms-dashboard:latest
        ports:
        - containerPort: 3001
        - containerPort: 8080  # Actuator port
        env:
        - name: DATABASE_CLUSTER_URL
          value: "jdbc:postgresql://postgres:5432"
        - name: DATABASE_NAME
          value: "server_dashboard"
        - name: KUBERNETES_ENABLED
          value: "true"
        - name: KUBERNETES_NAMESPACE
          valueFrom:
            fieldRef:
              fieldPath: metadata.namespace
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
```

## 🔧 Разработка

### Запуск в режиме разработки:
```bash
mvn spring-boot:run
```

### С отладкой:
```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
```

### Профили:
Профили настраиваются через переменную окружения `SPRING_PROFILES_ACTIVE` или через `application-{profile}.yml`.

## 📝 Особенности реализации

### Встроенный kubectl
- Встроенный `kubectl` v1.34.1 для Linux (amd64)
- Автоматически извлекается при первом запуске
- Для других ОС требуется установка внешнего `kubectl`

### Локальные UI ресурсы
- Все CSS и JS файлы включены в JAR
- Bootstrap 5.1.3
- Font Awesome 6.0.0
- Работает полностью автономно без доступа к интернету

### Оптимизации
- `spring.jpa.open-in-view: false` - предотвращает N+1 проблемы
- Кэширование подключений к БД через HikariCP
- Параллельная проверка серверов