# 📚 Руководство по Maven для Server Dashboard

## 🎯 Обзор проекта

Этот Maven проект использует Spring Boot 3.2.0 для создания серверного дашборда мониторинга. Проект структурирован согласно стандартам Maven и Spring Boot.

## 📁 Структура проекта

```
backend/
├── pom.xml                    # Основной файл конфигурации Maven
├── src/
│   ├── main/
│   │   ├── java/              # Исходный код Java
│   │   │   └── com/dashboard/
│   │   │       ├── ServerDashboardApplication.java
│   │   │       ├── config/    # Конфигурационные классы
│   │   │       ├── controller/ # REST контроллеры
│   │   │       ├── dto/       # Data Transfer Objects
│   │   │       ├── model/     # JPA сущности
│   │   │       ├── repository/ # JPA репозитории
│   │   │       └── service/   # Бизнес-логика
│   │   └── resources/         # Ресурсы приложения
│   │       ├── application.yml
│   │       └── application.properties
│   └── test/                  # Тестовый код
│       └── java/
└── target/                    # Скомпилированные файлы (генерируется)
```

## 🔧 Основные команды Maven

### Компиляция и сборка
```bash
# Компиляция проекта
mvn compile

# Сборка проекта (компиляция + тесты)
mvn build

# Очистка и сборка
mvn clean build

# Создание JAR файла
mvn package

# Установка в локальный репозиторий
mvn install
```

### Запуск приложения
```bash
# Запуск через Maven
mvn spring-boot:run

# Запуск с профилем
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Запуск JAR файла
java -jar target/server-dashboard-1.0.0.jar
```

### Тестирование
```bash
# Запуск всех тестов
mvn test

# Запуск конкретного теста
mvn test -Dtest=ServerTypeTest

# Запуск тестов с покрытием
mvn test jacoco:report
```

## 📦 Зависимости проекта

### Spring Boot Starters
- **spring-boot-starter-web** - REST API, Tomcat, Jackson
- **spring-boot-starter-data-jpa** - Hibernate, JPA, Spring Data
- **spring-boot-starter-validation** - Bean Validation
- **spring-boot-starter-actuator** - Health checks, Metrics
- **spring-boot-starter-webflux** - Реактивный HTTP клиент

### Базы данных
- **postgresql** - Основная БД для продакшена
- **h2** - Встроенная БД для тестирования

### Тестирование
- **spring-boot-starter-test** - JUnit 5, Mockito, Spring Test
- **testcontainers** - Интеграционные тесты с Docker

### Инструменты разработки
- **spring-boot-devtools** - Автоперезагрузка

## ⚙️ Конфигурация

### Java версия
```xml
<properties>
    <java.version>17</java.version>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
</properties>
```

### Кодировка
```xml
<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
```

### Плагины
- **spring-boot-maven-plugin** - Сборка и запуск Spring Boot
- **maven-compiler-plugin** - Компиляция Java
- **maven-surefire-plugin** - Запуск тестов

## 🚀 Профили Maven

### Разработка (dev)
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Тестирование (test)
```bash
mvn test -Dspring.profiles.active=test
```

### Продакшен (prod)
```bash
mvn package -Pprod
java -jar target/server-dashboard-1.0.0.jar --spring.profiles.active=prod
```

## 📊 Жизненный цикл Maven

### Основные фазы:
1. **validate** - Проверка корректности проекта
2. **compile** - Компиляция исходного кода
3. **test** - Запуск unit тестов
4. **package** - Упаковка в JAR/WAR
5. **verify** - Проверка качества пакета
6. **install** - Установка в локальный репозиторий
7. **deploy** - Развертывание в удаленный репозиторий

### Команды по фазам:
```bash
# Выполнить до определенной фазы
mvn compile
mvn test
mvn package
mvn install
```

## 🔍 Отладка и диагностика

### Подробный вывод
```bash
# Подробный вывод Maven
mvn -X clean build

# Отладочный вывод Spring Boot
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
```

### Анализ зависимостей
```bash
# Показать дерево зависимостей
mvn dependency:tree

# Показать конфликты зависимостей
mvn dependency:analyze

# Обновить зависимости
mvn versions:display-dependency-updates
```

## 🧪 Тестирование

### Unit тесты
```bash
# Запуск всех тестов
mvn test

# Запуск конкретного класса
mvn test -Dtest=ServerTypeTest

# Запуск с покрытием
mvn test jacoco:report
```

### Интеграционные тесты
```bash
# Запуск с Testcontainers
mvn test -Dtest=ServerMonitorServiceTest

# Запуск с профилем test
mvn test -Dspring.profiles.active=test
```

## 📦 Упаковка и развертывание

### Создание JAR
```bash
# Создание executable JAR
mvn clean package

# JAR будет создан в target/server-dashboard-1.0.0.jar
```

### Docker
```bash
# Создание Docker образа (если есть Dockerfile)
docker build -t server-dashboard .

# Запуск в Docker
docker run -p 3001:3001 server-dashboard
```

## 🔧 Настройка IDE

### IntelliJ IDEA
1. Откройте папку `backend` как Maven проект
2. Дождитесь индексации зависимостей
3. Настройте Run Configuration для Spring Boot

### Eclipse
1. Import → Existing Maven Projects
2. Выберите папку `backend`
3. Настройте Run Configuration

### VS Code
1. Установите расширение "Extension Pack for Java"
2. Откройте папку `backend`
3. Дождитесь загрузки зависимостей

## 🚨 Устранение неполадок

### Проблемы с зависимостями
```bash
# Очистка локального репозитория
mvn dependency:purge-local-repository

# Обновление зависимостей
mvn clean install -U
```

### Проблемы с компиляцией
```bash
# Очистка и перекомпиляция
mvn clean compile

# Проверка версии Java
java -version
mvn -version
```

### Проблемы с тестами
```bash
# Пропуск тестов
mvn package -DskipTests

# Запуск тестов в отладочном режиме
mvn test -Dmaven.surefire.debug
```

## 📚 Полезные ресурсы

### Документация
- [Maven Official Guide](https://maven.apache.org/guides/)
- [Spring Boot Maven Plugin](https://docs.spring.io/spring-boot/docs/current/maven-plugin/reference/htmlsingle/)
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)

### Команды для изучения
```bash
# Помощь по Maven
mvn help:help

# Список доступных плагинов
mvn help:describe -Dplugin=spring-boot

# Информация о проекте
mvn help:effective-pom
```

---

**Maven проект Server Dashboard готов к разработке!** 🚀
