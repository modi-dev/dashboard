# Установка и настройка Version Dashboard

## 📋 Требования

### 1. Java 17+
```bash
# Проверка версии Java
java -version

# Если Java не установлена:
# Windows: https://adoptium.net/ или https://www.oracle.com/java/technologies/downloads/
# Linux: sudo apt-get install openjdk-17-jdk
# Mac: brew install openjdk@17
```

### 2. Maven 3.6+
```bash
# Проверка версии Maven
mvn -version

# Если Maven не установлен:
# Windows: https://maven.apache.org/download.cgi
# Или через Chocolatey: choco install maven
# Или через Scoop: scoop install maven
# Linux: sudo apt-get install maven
# Mac: brew install maven
```

### 3. PostgreSQL 12+
```bash
# Проверка PostgreSQL
psql --version

# Если PostgreSQL не установлен:
# Windows: https://www.postgresql.org/download/windows/
# Linux: sudo apt-get install postgresql
# Mac: brew install postgresql
# Или через Docker: docker run --name postgres -e POSTGRES_PASSWORD=password -p 5432:5432 -d postgres:15
```

### 4. (Опционально) Docker для интеграционных тестов
```bash
# Проверка Docker
docker --version

# Если Docker не установлен:
# https://docs.docker.com/get-docker/
```

### 5. (Опционально) Kubernetes доступ
Если планируется мониторинг Kubernetes подов:
- Доступ к Kubernetes кластеру
- Права на чтение информации о подах
- (Опционально) kubectl установлен (иначе используется встроенный для Linux)

## 🚀 Быстрый старт

### 1. Клонирование и сборка
```bash
git clone <repository-url>
cd dashboard
mvn clean install
```

### 2. Настройка базы данных

#### Вариант A: Локальная установка PostgreSQL
```bash
# Создание базы данных
createdb server_dashboard

# Или через psql
psql -U postgres -c "CREATE DATABASE server_dashboard;"
```

#### Вариант B: Docker PostgreSQL
```bash
docker run --name postgres-dashboard \
  -e POSTGRES_PASSWORD=password \
  -e POSTGRES_DB=server_dashboard \
  -p 5432:5432 \
  -d postgres:15
```

### 3. Настройка переменных окружения

#### Windows (PowerShell)
```powershell
$env:DATABASE_CLUSTER_URL="jdbc:postgresql://localhost:5432"
$env:DATABASE_NAME="server_dashboard"
$env:DATABASE_USERNAME="postgres"
$env:DATABASE_PASSWORD="password"
```

#### Windows (CMD)
```cmd
set DATABASE_CLUSTER_URL=jdbc:postgresql://localhost:5432
set DATABASE_NAME=server_dashboard
set DATABASE_USERNAME=postgres
set DATABASE_PASSWORD=password
```

#### Linux/Mac
```bash
export DATABASE_CLUSTER_URL=jdbc:postgresql://localhost:5432
export DATABASE_NAME=server_dashboard
export DATABASE_USERNAME=postgres
export DATABASE_PASSWORD=password
```

#### Kubernetes (опционально)
```bash
# Включить интеграцию с Kubernetes
export KUBERNETES_ENABLED=true
export KUBERNETES_NAMESPACE=dev-tools

# Использовать внешний kubectl (опционально)
# Если не указано, используется встроенный kubectl для Linux
export KUBERNETES_KUBECTL_PATH=kubectl
```

### 4. Запуск приложения
```bash
mvn spring-boot:run
```

Приложение будет доступно по адресу: `http://localhost:3001`

## 🔧 Альтернативные способы запуска

### Через JAR файл
```bash
# Сборка JAR
mvn clean package

# Запуск
java -jar target/ms-dashboard-*.jar
```

### Через IDE
1. Откройте проект в IntelliJ IDEA, Eclipse или VS Code
2. Импортируйте как Maven проект
3. Запустите `ServerDashboardApplication.java`
4. Настройте переменные окружения в конфигурации запуска

### Через Docker

#### Сборка образа
```bash
docker build -t ms-dashboard .
```

#### Запуск контейнера
```bash
docker run -p 3001:3001 \
  -e DATABASE_CLUSTER_URL=jdbc:postgresql://host.docker.internal:5432 \
  -e DATABASE_NAME=server_dashboard \
  -e DATABASE_USERNAME=postgres \
  -e DATABASE_PASSWORD=password \
  -e KUBERNETES_ENABLED=true \
  ms-dashboard
```

**Примечание:** Для доступа к PostgreSQL на хосте используйте `host.docker.internal` (Windows/Mac) или IP адрес хоста (Linux).

#### Docker Compose
```yaml
# docker-compose.yml
version: '3.8'
services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: server_dashboard
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
  
  dashboard:
    build: .
    ports:
      - "3001:3001"
      - "8080:8080"  # Actuator port
    depends_on:
      - postgres
    environment:
      DATABASE_CLUSTER_URL: jdbc:postgresql://postgres:5432
      DATABASE_NAME: server_dashboard
      DATABASE_USERNAME: postgres
      DATABASE_PASSWORD: password
      MANAGEMENT_PORT: 8080
      KUBERNETES_ENABLED: "true"
      KUBERNETES_NAMESPACE: default

volumes:
  postgres_data:
```

```bash
docker-compose up -d
```

## 🧪 Тестирование

### Запуск всех тестов
```bash
mvn test
```

### Запуск только unit тестов
```bash
mvn test -Dtest=*Test -Dtest=\!*IntegrationTest
```

### Интеграционные тесты с TestContainers
```bash
# Тесты используют Docker для запуска PostgreSQL, Redis, Kafka
mvn test -Dtest=*IntegrationTest
```

**Требования для интеграционных тестов:**
- Docker должен быть запущен
- Доступ к Docker daemon

### Структура тестов
- **Unit тесты** - `src/test/java/com/dashboard/service/`
- **Integration тесты** - `src/test/java/com/dashboard/integration/`
- **TestContainers** - автоматический запуск зависимостей

## 📊 Мониторинг и Health Checks

### Проверка статуса приложения
```bash
# Общий статус
curl http://localhost:3001/actuator/health

# Liveness probe (для Kubernetes)
curl http://localhost:3001/actuator/health/liveness

# Readiness probe (для Kubernetes)
curl http://localhost:3001/actuator/health/readiness
```

### Метрики
```bash
curl http://localhost:3001/actuator/metrics
```

### Информация о приложении
```bash
curl http://localhost:3001/actuator/info
```

**Примечание:** Порт Actuator настраивается через переменную `MANAGEMENT_PORT` (по умолчанию совпадает с портом приложения).

## 🔍 Отладка

### Логирование

#### Включить debug логи
```bash
mvn spring-boot:run -DLOG_LEVEL_DASHBOARD=DEBUG
```

#### Или через переменную окружения
```bash
export LOG_LEVEL_DASHBOARD=DEBUG
export LOG_LEVEL_MONITOR=DEBUG
export LOG_LEVEL_KUBERNETES=DEBUG
mvn spring-boot:run
```

### Отладка в IDE

#### IntelliJ IDEA
1. Откройте `ServerDashboardApplication.java`
2. Кликните правой кнопкой → Debug
3. Или создайте конфигурацию запуска с переменными окружения

#### Eclipse
1. Правой кнопкой на `ServerDashboardApplication.java`
2. Debug As → Spring Boot App
3. Настройте переменные окружения в Run Configurations

## 🚀 Production Deployment

### JAR файл
```bash
# Сборка production версии
mvn clean package -DskipTests

# Запуск
java -jar -Xmx512m -Xms256m \
  -DDATABASE_CLUSTER_URL=jdbc:postgresql://db-host:5432 \
  -DDATABASE_NAME=server_dashboard \
  -DDATABASE_USERNAME=postgres \
  -DDATABASE_PASSWORD=secure_password \
  target/ms-dashboard-*.jar
```

### Docker
```bash
# Сборка образа
docker build -t ms-dashboard:latest .

# Запуск с production настройками
docker run -d \
  --name ms-dashboard \
  -p 3001:3001 \
  -p 8080:8080 \
  -e DATABASE_CLUSTER_URL=jdbc:postgresql://db-host:5432 \
  -e DATABASE_NAME=server_dashboard \
  -e DATABASE_USERNAME=postgres \
  -e DATABASE_PASSWORD=secure_password \
  -e MANAGEMENT_PORT=8080 \
  -e KUBERNETES_ENABLED=true \
  ms-dashboard:latest
```

### Kubernetes

#### Пример Deployment
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ms-dashboard
  namespace: monitoring
spec:
  replicas: 1
  selector:
    matchLabels:
      app: ms-dashboard
  template:
    metadata:
      labels:
        app: ms-dashboard
    spec:
      containers:
      - name: dashboard
        image: ms-dashboard:latest
        ports:
        - name: http
          containerPort: 3001
        - name: actuator
          containerPort: 8080
        env:
        - name: DATABASE_CLUSTER_URL
          value: "jdbc:postgresql://postgres:5432"
        - name: DATABASE_NAME
          value: "server_dashboard"
        - name: DATABASE_USERNAME
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: username
        - name: DATABASE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: password
        - name: KUBERNETES_ENABLED
          value: "true"
        - name: KUBERNETES_NAMESPACE
          valueFrom:
            fieldRef:
              fieldPath: metadata.namespace
        - name: MANAGEMENT_PORT
          value: "8080"
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
          timeoutSeconds: 5
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 3
---
apiVersion: v1
kind: Service
metadata:
  name: ms-dashboard
  namespace: monitoring
spec:
  selector:
    app: ms-dashboard
  ports:
  - name: http
    port: 3001
    targetPort: 3001
  - name: actuator
    port: 8080
    targetPort: 8080
  type: ClusterIP
```

## ❓ Решение проблем

### Maven не найден
```bash
# Проверьте, что Maven в PATH
mvn -version

# Добавьте Maven в PATH
# Windows: Добавьте путь к Maven\bin в переменную PATH
# Linux/Mac: export PATH=$PATH:/path/to/maven/bin
```

### Java не найдена
```bash
# Проверьте версию Java
java -version

# Убедитесь, что JAVA_HOME установлен
# Windows: set JAVA_HOME=C:\Program Files\Java\jdk-17
# Linux/Mac: export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
```

### PostgreSQL не запускается
```bash
# Windows: services.msc -> PostgreSQL
# Linux: sudo systemctl start postgresql
# Mac: brew services start postgresql
# Docker: docker start postgres-dashboard
```

### Порт 3001 занят
```bash
# Измените порт через переменную окружения
export APP_PORT=3002
mvn spring-boot:run

# Или через application.yml
server:
  port: 3002
```

### Ошибка подключения к базе данных
```bash
# Проверьте, что PostgreSQL запущен
psql -U postgres -l

# Проверьте переменные окружения
echo $DATABASE_CLUSTER_URL
echo $DATABASE_NAME
echo $DATABASE_USERNAME

# Проверьте логи приложения для деталей ошибки
```

### Kubernetes интеграция не работает
```bash
# Проверьте доступность kubectl
kubectl get pods

# Проверьте переменные окружения
echo $KUBERNETES_ENABLED
echo $KUBERNETES_NAMESPACE

# Проверьте права доступа
kubectl auth can-i get pods --namespace $KUBERNETES_NAMESPACE

# В Docker используйте встроенный kubectl
# Встроенный kubectl работает только на Linux
```

### Тесты не проходят
```bash
# Убедитесь, что Docker запущен (для интеграционных тестов)
docker ps

# Запустите тесты с более подробным выводом
mvn test -X

# Запустите конкретный тест
mvn test -Dtest=ServerMonitorServiceTest
```

### CSV экспорт с неправильной кодировкой
- CSV файлы используют UTF-8 с BOM
- Откройте файл в Excel или другом редакторе, поддерживающем UTF-8
- Проверьте, что браузер не перекодирует файл

## 📝 Дополнительные настройки

### Настройка мониторинга
```bash
# Интервал проверки серверов (в минутах)
export MONITORING_INTERVAL=5      # 5 минут

# Таймаут проверки (в минутах)
export MONITORING_TIMEOUT=0.17    # 0.17 минут = ~10 секунд
```

### Настройка Actuator
```bash
# Порт для Actuator endpoints
export MANAGEMENT_PORT=8080

# Разрешенные endpoints
export MANAGEMENT_ENDPOINTS=health,info,metrics

# Детали health checks
export MANAGEMENT_HEALTH_SHOW_DETAILS=always
```

### Настройка логирования
```bash
# Общий уровень логирования
export LOG_LEVEL_DASHBOARD=INFO

# Логирование мониторинга
export LOG_LEVEL_MONITOR=DEBUG

# Логирование Kubernetes
export LOG_LEVEL_KUBERNETES=INFO
```

## 🔒 Безопасность

### Production рекомендации:
1. Используйте сильные пароли для базы данных
2. Храните секреты в Kubernetes Secrets или внешних системах
3. Ограничьте доступ к Actuator endpoints через firewall
4. Используйте HTTPS в production
5. Регулярно обновляйте зависимости

### Пример использования Kubernetes Secrets:
```bash
# Создание секрета
kubectl create secret generic db-secret \
  --from-literal=username=postgres \
  --from-literal=password=secure_password \
  -n monitoring
```

## 📚 Полезные ссылки

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [TestContainers Documentation](https://www.testcontainers.org/)