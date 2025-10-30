package com.dashboard.integration;

import com.dashboard.model.Server;
import com.dashboard.model.ServerStatus;
import com.dashboard.model.ServerType;
import com.dashboard.repository.ServerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Интеграционные тесты с Spring Boot и TestContainers
 * 
 * Использует H2 в памяти для тестирования, но демонстрирует работу с TestContainers
 */
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

    @BeforeEach
    void setUp() {
        // Очищаем базу данных перед каждым тестом
        serverRepository.deleteAll();
    }

    @Test
    void contextLoads() {
        // Проверяем, что Spring контекст загружается корректно
        assertThat(serverRepository).isNotNull();
    }

    @Test
    void shouldCreateAndRetrieveServer() {
        // Создаем тестовый сервер
        Server server = new Server();
        server.setName("Test Server");
        server.setUrl("localhost:8080");
        server.setType(ServerType.OTHER);
        server.setStatus(ServerStatus.ONLINE);
        server.setCreatedAt(LocalDateTime.now());
        server.setUpdatedAt(LocalDateTime.now());

        // Сохраняем в базу данных
        Server savedServer = serverRepository.save(server);

        // Проверяем, что сервер сохранился
        assertThat(savedServer.getId()).isNotNull();
        assertThat(savedServer.getName()).isEqualTo("Test Server");
        assertThat(savedServer.getUrl()).isEqualTo("localhost:8080");
        assertThat(savedServer.getType()).isEqualTo(ServerType.OTHER);
        assertThat(savedServer.getStatus()).isEqualTo(ServerStatus.ONLINE);

        // Проверяем, что можем найти сервер по ID
        Server foundServer = serverRepository.findById(savedServer.getId()).orElse(null);
        assertThat(foundServer).isNotNull();
        assertThat(foundServer.getName()).isEqualTo("Test Server");
    }

    @Test
    void shouldUpdateServerStatus() {
        // Создаем сервер
        Server server = new Server();
        server.setName("Status Test Server");
        server.setUrl("localhost:8080");
        server.setType(ServerType.OTHER);
        server.setStatus(ServerStatus.UNKNOWN);
        server.setCreatedAt(LocalDateTime.now());
        server.setUpdatedAt(LocalDateTime.now());

        Server savedServer = serverRepository.save(server);

        // Обновляем статус
        savedServer.setStatus(ServerStatus.OFFLINE);
        savedServer.setLastChecked(LocalDateTime.now());
        serverRepository.save(savedServer);

        // Проверяем обновление
        Server updatedServer = serverRepository.findById(savedServer.getId()).orElse(null);
        assertThat(updatedServer).isNotNull();
        assertThat(updatedServer.getStatus()).isEqualTo(ServerStatus.OFFLINE);
        assertThat(updatedServer.getLastChecked()).isNotNull();
    }

    @Test
    void shouldDeleteServer() {
        // Создаем сервер
        Server server = new Server();
        server.setName("Delete Test Server");
        server.setUrl("localhost:8080");
        server.setType(ServerType.OTHER);
        server.setStatus(ServerStatus.ONLINE);
        server.setCreatedAt(LocalDateTime.now());
        server.setUpdatedAt(LocalDateTime.now());

        Server savedServer = serverRepository.save(server);
        Long serverId = savedServer.getId();

        // Удаляем сервер
        serverRepository.deleteById(serverId);

        // Проверяем, что сервер удален
        assertThat(serverRepository.findById(serverId)).isEmpty();
    }

    @Test
    void shouldFindServersByType() {
        // Создаем серверы разных типов
        Server postgresServer = new Server();
        postgresServer.setName("PostgreSQL Server");
        postgresServer.setUrl("localhost:5432");
        postgresServer.setType(ServerType.POSTGRES);
        postgresServer.setStatus(ServerStatus.ONLINE);
        postgresServer.setCreatedAt(LocalDateTime.now());
        postgresServer.setUpdatedAt(LocalDateTime.now());

        Server redisServer = new Server();
        redisServer.setName("Redis Server");
        redisServer.setUrl("localhost:6379");
        redisServer.setType(ServerType.REDIS);
        redisServer.setStatus(ServerStatus.ONLINE);
        redisServer.setCreatedAt(LocalDateTime.now());
        redisServer.setUpdatedAt(LocalDateTime.now());

        serverRepository.save(postgresServer);
        serverRepository.save(redisServer);

        // Проверяем поиск по типу
        assertThat(serverRepository.findByType(ServerType.POSTGRES)).hasSize(1);
        assertThat(serverRepository.findByType(ServerType.REDIS)).hasSize(1);
        assertThat(serverRepository.findByType(ServerType.KAFKA)).hasSize(0);
    }

    @Test
    void shouldVerifyTestContainerIsRunning() {
        // Проверяем, что TestContainer PostgreSQL запущен
        assertThat(postgres.isRunning()).isTrue();
        assertThat(postgres.getDatabaseName()).isEqualTo("testdb");
        assertThat(postgres.getUsername()).isEqualTo("test");
        assertThat(postgres.getPassword()).isEqualTo("test");
    }
}
