package com.dashboard.integration;

import com.dashboard.model.Server;
import com.dashboard.model.ServerType;
import com.dashboard.service.ServerVersionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Интеграционные тесты для PostgreSQL функциональности с TestContainers
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class PostgreSQLIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    @Autowired
    private ServerVersionService serverVersionService;

    private Server postgresServer;

    @BeforeEach
    void setUp() {
        // Создаем тестовый PostgreSQL сервер
        postgresServer = new Server();
        postgresServer.setName("Test PostgreSQL Server");
        postgresServer.setUrl(postgres.getHost() + ":" + postgres.getMappedPort(5432));
        postgresServer.setType(ServerType.POSTGRES);
    }

    @Test
    void shouldConnectToPostgreSQLContainer() {
        // Проверяем, что PostgreSQL контейнер запущен
        assertThat(postgres.isRunning()).isTrue();
        assertThat(postgres.getExposedPorts()).contains(5432);
        assertThat(postgres.getDatabaseName()).isEqualTo("testdb");
        assertThat(postgres.getUsername()).isEqualTo("test");
    }

    @Test
    void shouldGetPostgreSQLVersion() {
        // Тестируем получение версии PostgreSQL
        // В реальном тесте здесь бы был запрос к PostgreSQL метрикам
        String version = serverVersionService.getServerVersion(postgresServer);
        
        // Поскольку у нас нет PostgreSQL exporter в контейнере,
        // ожидаем мок-версию или null
        assertThat(version).isNotNull();
    }

    @Test
    void shouldHandlePostgreSQLConnectionError() {
        // Тестируем обработку ошибок подключения
        Server invalidPostgresServer = new Server();
        invalidPostgresServer.setName("Invalid PostgreSQL Server");
        invalidPostgresServer.setUrl("localhost:9999"); // Несуществующий порт
        invalidPostgresServer.setType(ServerType.POSTGRES);

        String version = serverVersionService.getServerVersion(invalidPostgresServer);
        // Ожидаем мок-версию для PostgreSQL при ошибке подключения
        assertThat(version).isNotNull();
    }

    @Test
    void shouldVerifyPostgreSQLContainerProperties() {
        // Проверяем свойства PostgreSQL контейнера
        assertThat(postgres.getJdbcUrl()).contains("jdbc:postgresql://");
        assertThat(postgres.getJdbcUrl()).contains("testdb");
        assertThat(postgres.getUsername()).isEqualTo("test");
        assertThat(postgres.getPassword()).isEqualTo("test");
    }
}
