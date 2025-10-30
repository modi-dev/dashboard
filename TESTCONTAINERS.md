# TestContainers Integration

Этот проект использует TestContainers для интеграционного тестирования с реальными контейнерами.

## Что такое TestContainers?

TestContainers - это Java библиотека, которая позволяет запускать Docker контейнеры во время тестов. Это позволяет тестировать приложение с реальными базами данных и сервисами, а не с моками.

## Настройка

### Зависимости

В `pom.xml` добавлены следующие зависимости:

```xml
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-testcontainers</artifactId>
    <scope>test</scope>
</dependency>
```

### Конфигурация

Создан тестовый профиль `application-test.yml`:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    username: sa
    password: 
    driver-class-name: org.h2.Driver
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true

# Отключаем Kubernetes интеграцию для тестов
kubernetes:
  enabled: false
```

## Тесты

### 1. SimpleTestContainersTest

Простой тест без Spring контекста, проверяющий базовую функциональность TestContainers:

```java
@Testcontainers
class SimpleTestContainersTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    @Test
    void shouldStartPostgreSQLContainer() {
        assertThat(postgres.isRunning()).isTrue();
        assertThat(postgres.getDatabaseName()).isEqualTo("testdb");
    }
}
```

### 2. SpringBootTestContainersTest

Интеграционный тест с Spring Boot, использующий TestContainers:

```java
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class SpringBootTestContainersTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    @Autowired
    private ServerRepository serverRepository;

    @Test
    void shouldCreateAndRetrieveServer() {
        // Тестируем работу с базой данных
        Server server = new Server();
        server.setName("Test Server");
        server.setUrl("localhost:8080");
        server.setType(ServerType.OTHER);
        server.setStatus(ServerStatus.ONLINE);
        
        Server savedServer = serverRepository.save(server);
        assertThat(savedServer.getId()).isNotNull();
    }
}
```

### 3. TestContainersBaseTest

Базовый тест для проверки работы TestContainers с Spring Boot.

## Запуск тестов

### Все TestContainers тесты

```bash
mvn test -Dtest="*TestContainers*"
```

### Конкретный тест

```bash
mvn test -Dtest=SimpleTestContainersTest
mvn test -Dtest=SpringBootTestContainersTest
```

## Особенности

### Переиспользование контейнеров

Для ускорения тестов можно включить переиспользование контейнеров:

1. Создайте файл `~/.testcontainers.properties`
2. Добавьте строку: `testcontainers.reuse.enable=true`

### Docker

TestContainers требует запущенный Docker. Убедитесь, что Docker Desktop запущен перед запуском тестов.

### Порты

TestContainers автоматически назначает свободные порты для контейнеров. Не нужно беспокоиться о конфликтах портов.

## Преимущества

1. **Реальные условия**: Тесты работают с реальными базами данных и сервисами
2. **Изоляция**: Каждый тест получает свежий контейнер
3. **Автоматизация**: Контейнеры автоматически запускаются и останавливаются
4. **Портабельность**: Тесты работают одинаково на всех машинах с Docker

## Ограничения

1. **Время выполнения**: Тесты с TestContainers выполняются дольше из-за запуска контейнеров
2. **Docker зависимость**: Требуется установленный и запущенный Docker
3. **Ресурсы**: Контейнеры потребляют системные ресурсы

## Примеры использования

### Тестирование с PostgreSQL

```java
@Container
static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

@Test
void shouldConnectToDatabase() {
    String jdbcUrl = postgres.getJdbcUrl();
    // Используем jdbcUrl для подключения к базе данных
}
```

### Тестирование с Redis

```java
@Container
static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
        .withExposedPorts(6379);

@Test
void shouldConnectToRedis() {
    String host = redis.getHost();
    int port = redis.getMappedPort(6379);
    // Подключаемся к Redis
}
```

## Логирование

TestContainers выводит подробные логи о запуске и остановке контейнеров. Это помогает в отладке тестов.

## Заключение

TestContainers значительно улучшает качество интеграционных тестов, позволяя тестировать приложение в условиях, максимально приближенных к продакшену.
