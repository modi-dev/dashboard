# Version Dashboard - Java Spring Boot Backend

Сервис мониторинга серверов и подов Kubernetes на Java Spring Boot с Maven.

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
export KUBERNETES_PODS_SYNC_INTERVAL=5  # Интервал синхронизации подов (в минутах)

# Мониторинг
export MONITORING_INTERVAL=5      # Интервал проверки серверов (в минутах)
export MONITORING_TIMEOUT=10      # Таймаут проверки (в секундах, по умолчанию: 10)
export MONITORING_ASYNC_POOL_SIZE=4  # Размер пула потоков для асинхронного мониторинга
```

4. **Запуск приложения:**
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
- ✅ Темная/светлая тема с переключением
- ✅ Интерактивная инструкция по использованию приложения
- ✅ Боковое меню для добавления серверов
- ✅ Экспорт данных в CSV (с поддержкой UTF-8 и защитой от автоформатирования Excel)
- ✅ Статистика в реальном времени
- ✅ Настройка видимости столбцов в таблицах
- ✅ Сохранение пользовательских настроек (тема, ширина столбцов) в localStorage

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
| POST | `/api/servers/{id}/check` | Проверить сервер вручную (асинхронно) |
| POST | `/api/servers/refresh` | Обновить статусы всех серверов (асинхронно) |
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
| GET | `/api/pods/pods` | Информация о подах (JSON) |
| POST | `/api/pods/refresh` | Обновить информацию о подах (синхронизация с kubectl) |
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
export SERVER_CONTEXT_PATH=/dashboard  # Префикс пути (по умолчанию /dashboard)

# Actuator
export MANAGEMENT_PORT=3001  # Порт для health endpoints
export MANAGEMENT_ENDPOINTS=health,info,metrics  # Разрешенные endpoints
export MANAGEMENT_HEALTH_SHOW_DETAILS=always  # Детали health checks

# Kubernetes
export KUBERNETES_ENABLED=true  # Включить интеграцию с Kubernetes
export KUBERNETES_NAMESPACE=dev-tools  # Namespace для мониторинга
export KUBERNETES_KUBECTL_PATH=kubectl  # Путь к kubectl (используется встроенный если не указан)
export KUBERNETES_PODS_SYNC_INTERVAL=5  # Интервал синхронизации подов (в минутах, по умолчанию: 5)
export KUBERNETES_COMMAND_TIMEOUT=30  # Таймаут команд kubectl (в секундах, по умолчанию: 30)

# Мониторинг
export MONITORING_INTERVAL=5      # Интервал проверки серверов (в минутах, по умолчанию: 5)
export MONITORING_TIMEOUT=10      # Таймаут проверки (в секундах, по умолчанию: 10)
export MONITORING_ASYNC_POOL_SIZE=4  # Размер пула потоков для асинхронного мониторинга (по умолчанию: 4)

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
- `kubernetes.pods.sync-interval: 5` - синхронизация подов каждые 5 минут (версия Kubernetes синхронизируется вместе с подами)

## 🏗️ Архитектура

### Структура проекта
```
dashboard/
├── src/main/java/com/dashboard/
│   ├── ServerDashboardApplication.java    # Главный класс
│   ├── controller/                        # REST контроллеры
│   │   ├── DashboardController.java       # Главная страница
│   │   ├── ServerController.java          # Управление серверами
│   │   ├── PodsController.java           # Страница подов
│   │   ├── VersionController.java        # API для подов
│   │   ├── HomeController.java           # Редиректы
│   │   └── LoginController.java          # Аутентификация
│   ├── model/                            # JPA модели
│   │   ├── Server.java
│   │   ├── ServerType.java
│   │   ├── ServerStatus.java
│   │   ├── PodInfo.java                  # Информация о подах (кэшируется в БД)
│   │   └── KubernetesClusterInfo.java    # Информация о кластере (версия, namespace)
│   ├── dto/                              # DTO классы
│   │   └── ServerDto.java
│   ├── repository/                       # JPA репозитории
│   │   ├── ServerRepository.java
│   │   ├── PodRepository.java
│   │   └── KubernetesClusterInfoRepository.java
│   ├── service/                          # Бизнес логика
│   │   ├── ServerMonitorService.java      # Мониторинг серверов (асинхронный)
│   │   ├── ServerMonitorScheduler.java    # Планировщик проверки серверов
│   │   ├── ServerVersionService.java     # Получение версий серверов
│   │   ├── KubernetesService.java        # Работа с kubectl
│   │   ├── KubernetesPodsSyncService.java # Синхронизация подов в БД
│   │   ├── KubernetesPodsScheduler.java   # Планировщик синхронизации подов
│   │   ├── KubernetesClusterInfoSyncService.java # Синхронизация версии K8s
│   │   ├── KubernetesPodParser.java      # Парсинг вывода kubectl
│   │   ├── EmbeddedKubectlService.java   # Управление встроенным kubectl
│   │   └── CsvExportService.java         # Экспорт в CSV
│   └── config/                           # Конфигурация
│       ├── WebClientConfig.java          # HTTP клиент
│       ├── KubernetesConfig.java        # Конфигурация Kubernetes
│       ├── SecurityConfig.java          # Безопасность
│       ├── MonitoringAsyncConfig.java   # Асинхронный мониторинг
│       ├── PodsInitializer.java         # Инициализация подов при старте
│       └── ClusterInfoInitializer.java   # Инициализация информации о кластере
├── src/main/resources/
│   ├── application.yml                   # Конфигурация
│   ├── templates/                        # Thymeleaf шаблоны
│   │   ├── layout.html                   # Базовый layout
│   │   ├── index.html                    # Главная страница
│   │   ├── servers.html                  # Страница серверов
│   │   ├── pods.html                     # Страница подов
│   │   └── fragments/                    # Переиспользуемые фрагменты
│   │       ├── pods.html                 # Фрагменты для подов
│   │       ├── servers.html              # Фрагменты для серверов
│   │       └── instructions.html         # Модальное окно инструкций
│   └── static/                           # Статические ресурсы (локальные)
│       ├── css/
│       │   ├── bootstrap.min.css
│       │   ├── theme-dark.css
│       │   └── common.css
│       ├── js/
│       │   └── app.js                    # Основная логика UI
│       └── webfonts/                     # Font Awesome шрифты
│   └── binaries/                         # Встроенные бинарники
│       └── kubectl/
│           └── kubectl-linux-amd64
├── src/test/                             # Тесты
│   ├── java/com/dashboard/
│   │   ├── integration/                  # Интеграционные тесты
│   │   │   ├── TestContainersBaseTest.java
│   │   │   └── ...
│   │   ├── controller/                   # Тесты контроллеров
│   │   ├── service/                      # Тесты сервисов
│   │   └── config/                       # Тесты конфигурации
│   └── resources/
│       └── application-test.yml          # Конфигурация для тестов
├── Dockerfile                            # Docker образ
├── pom.xml                               # Maven конфигурация
└── README.md
```

### Кэширование данных

Приложение использует базу данных PostgreSQL для кэширования данных:

- **Поды Kubernetes** - синхронизируются из kubectl в БД каждые 5 минут (настраивается)
- **Версия Kubernetes** - синхронизируется вместе с подами (не требует отдельного вызова kubectl)
- **Информация о кластере** - версия и namespace хранятся в БД и обновляются при синхронизации подов

Это позволяет:
- Ускорить загрузку страниц (данные читаются из БД, а не из kubectl)
- Снизить нагрузку на Kubernetes API
- Обеспечить доступность данных даже при временной недоступности kubectl

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
- Версия сервера обновляется автоматически при проверке статуса (если сервер онлайн)

## 🔄 Мониторинг

### Автоматическая проверка серверов:
- **Интервал:** 5 минут (настраивается через `MONITORING_INTERVAL`, значение в минутах)
- **Таймаут:** 10 секунд (настраивается через `MONITORING_TIMEOUT`, значение в секундах)
- **Асинхронный:** проверка выполняется в фоновых потоках, не блокирует UI
- **Параллельная:** все серверы проверяются параллельно
- **Обновление версий:** версия сервера обновляется автоматически при успешной проверке

### Синхронизация подов Kubernetes:
- **Интервал:** 5 минут (настраивается через `KUBERNETES_PODS_SYNC_INTERVAL`, значение в минутах)
- **Версия кластера:** синхронизируется вместе с подами (не требует отдельного вызова kubectl)
- **Кэширование:** все данные сохраняются в БД для быстрого доступа

### Логирование:
Все проверки логируются с уровнем DEBUG для `ServerMonitorService` и `KubernetesPodsSyncService`.

## 🧪 Тестирование

### Запуск всех тестов:
```bash
mvn test
```

### Покрытие тестами:
```bash
mvn clean test jacoco:report
```

Отчет о покрытии будет доступен в `target/site/jacoco/index.html`

**Текущее покрытие:** 85% (315 тестов)

### Интеграционные тесты с TestContainers:
```bash
# Тесты используют Docker для запуска PostgreSQL, Redis и Kafka
mvn test -Dtest=*IntegrationTest
```

**Примечание:** Для интеграционных тестов требуется Docker.

### Структура тестов:
- **Unit тесты** - `src/test/java/com/dashboard/service/`
- **Integration тесты** - `src/test/java/com/dashboard/integration/`
- **Controller тесты** - `src/test/java/com/dashboard/controller/`
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
- Защита от автоформатирования Excel (даты, числа)
- Многострочные данные преобразуются в одну строку

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
            port: 3001
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 3001
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

### Оптимизации производительности
- `spring.jpa.open-in-view: false` - предотвращает N+1 проблемы
- Кэширование подключений к БД через HikariCP
- Асинхронная проверка серверов (не блокирует UI)
- Кэширование данных Kubernetes в БД (быстрая загрузка страниц)
- Версия Kubernetes синхронизируется вместе с подами (меньше вызовов kubectl)

### Безопасность
- Spring Security с базовой аутентификацией
- Настраивается через переменные окружения
- Можно отключить через `SECURITY_ENABLED=false`

## 📈 Производительность

### Оптимизации загрузки UI:
- Данные подов и серверов читаются из БД (не из kubectl/HTTP)
- Версия Kubernetes кэшируется в БД
- Асинхронная проверка серверов не блокирует UI
- Пользовательские настройки сохраняются в localStorage

### Масштабируемость:
- Параллельная проверка серверов через пул потоков
- Настраиваемый размер пула через `MONITORING_ASYNC_POOL_SIZE`
- Кэширование данных в БД снижает нагрузку на внешние системы

## 🐛 Известные ограничения

- Встроенный `kubectl` доступен только для Linux (amd64)
- Для Windows/Mac требуется установка внешнего `kubectl`
- CSV экспорт преобразует многострочные данные в одну строку

## 📄 Лицензия

[Указать лицензию, если применимо]

## 🤝 Вклад

[Инструкции по внесению вклада, если применимо]
