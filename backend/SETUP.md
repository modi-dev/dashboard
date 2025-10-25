# Установка и настройка Java Spring Boot Backend

## 📋 Требования

### 1. Java 17+
```bash
# Проверка версии Java
java -version

# Если Java не установлена, скачайте с:
# https://adoptium.net/ или https://www.oracle.com/java/technologies/downloads/
```

### 2. Maven 3.6+
```bash
# Проверка версии Maven
mvn -version

# Если Maven не установлен:
# Windows: https://maven.apache.org/download.cgi
# Или через Chocolatey: choco install maven
# Или через Scoop: scoop install maven
```

### 3. PostgreSQL 12+
```bash
# Проверка PostgreSQL
psql --version

# Если PostgreSQL не установлен:
# Windows: https://www.postgresql.org/download/windows/
# Или через Docker: docker run --name postgres -e POSTGRES_PASSWORD=password -p 5432:5432 -d postgres:15
```

## 🚀 Быстрый старт

### 1. Установка зависимостей
```bash
cd backend
mvn clean install
```

### 2. Настройка базы данных
```bash
# Создание базы данных
createdb server_dashboard

# Или через psql
psql -U postgres -c "CREATE DATABASE server_dashboard;"
```

### 3. Настройка переменных окружения
```bash
# Windows (PowerShell)
$env:DB_URL="jdbc:postgresql://localhost:5432/server_dashboard"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="password"

# Windows (CMD)
set DB_URL=jdbc:postgresql://localhost:5432/server_dashboard
set DB_USERNAME=postgres
set DB_PASSWORD=password

# Linux/Mac
export DB_URL=jdbc:postgresql://localhost:5432/server_dashboard
export DB_USERNAME=postgres
export DB_PASSWORD=password
```

### 4. Запуск приложения
```bash
mvn spring-boot:run
```

## 🔧 Альтернативные способы запуска

### Через JAR файл
```bash
mvn clean package
java -jar target/server-dashboard-1.0.0.jar
```

### Через IDE
1. Откройте проект в IntelliJ IDEA или Eclipse
2. Импортируйте как Maven проект
3. Запустите `ServerDashboardApplication.java`

## 🐳 Docker (альтернатива)

### Запуск через Docker Compose
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
  
  app:
    build: .
    ports:
      - "3001:3001"
    depends_on:
      - postgres
    environment:
      DB_URL: jdbc:postgresql://postgres:5432/server_dashboard
      DB_USERNAME: postgres
      DB_PASSWORD: password
```

```bash
docker-compose up -d
```

## 🧪 Тестирование

### Запуск тестов
```bash
mvn test
```

### Интеграционные тесты
```bash
mvn test -Dtest=*IntegrationTest
```

## 📊 Мониторинг

### Health Check
```bash
curl http://localhost:3001/actuator/health
```

### Метрики
```bash
curl http://localhost:3001/actuator/metrics
```

## 🔍 Отладка

### Логи
```bash
# Включить debug логи
mvn spring-boot:run -Dlogging.level.com.dashboard=DEBUG
```

### Профили
```bash
# Development профиль
mvn spring-boot:run -Dspring.profiles.active=dev

# Production профиль
mvn spring-boot:run -Dspring.profiles.active=prod
```

## 🚀 Production Deployment

### JAR файл
```bash
mvn clean package -Pprod
java -jar target/server-dashboard-1.0.0.jar
```

### Docker
```bash
docker build -t server-dashboard .
docker run -p 3001:3001 server-dashboard
```

## ❓ Решение проблем

### Maven не найден
```bash
# Добавьте Maven в PATH
# Windows: Добавьте путь к Maven в переменную PATH
# Linux/Mac: export PATH=$PATH:/path/to/maven/bin
```

### Java не найдена
```bash
# Установите Java 17+ и добавьте в PATH
# Windows: JAVA_HOME должен указывать на папку с Java
```

### PostgreSQL не запускается
```bash
# Проверьте, что PostgreSQL запущен
# Windows: services.msc -> PostgreSQL
# Linux: sudo systemctl start postgresql
```

### Порт 3001 занят
```bash
# Измените порт в application.yml
server:
  port: 3002
```

