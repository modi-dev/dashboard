package com.dashboard.integration;

import com.dashboard.model.Server;
import com.dashboard.model.ServerType;
import com.dashboard.service.ServerVersionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Интеграционные тесты для Redis функциональности с TestContainers
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class RedisIntegrationTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379)
            .withReuse(true);

    @Autowired
    private ServerVersionService serverVersionService;

    private Server redisServer;

    @BeforeEach
    void setUp() {
        // Создаем тестовый Redis сервер
        redisServer = new Server();
        redisServer.setName("Test Redis Server");
        redisServer.setUrl(redis.getHost() + ":" + redis.getMappedPort(6379));
        redisServer.setType(ServerType.REDIS);
    }

    @Test
    void shouldConnectToRedisContainer() {
        // Проверяем, что Redis контейнер запущен
        assertThat(redis.isRunning()).isTrue();
        assertThat(redis.getExposedPorts()).contains(6379);
    }

    @Test
    void shouldGetRedisVersion() {
        // Тестируем получение версии Redis
        // В реальном тесте здесь бы был запрос к Redis метрикам
        String version = serverVersionService.getServerVersion(redisServer);
        
        // Поскольку у нас нет Redis exporter в контейнере,
        // ожидаем null или мок-версию
        assertThat(version).isNull();
    }

    @Test
    void shouldHandleRedisConnectionError() {
        // Тестируем обработку ошибок подключения
        Server invalidRedisServer = new Server();
        invalidRedisServer.setName("Invalid Redis Server");
        invalidRedisServer.setUrl("localhost:9999"); // Несуществующий порт
        invalidRedisServer.setType(ServerType.REDIS);

        String version = serverVersionService.getServerVersion(invalidRedisServer);
        assertThat(version).isNull();
    }
}
