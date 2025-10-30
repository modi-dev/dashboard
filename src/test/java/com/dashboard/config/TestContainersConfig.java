package com.dashboard.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Конфигурация TestContainers для интеграционных тестов
 * 
 * Настраивает контейнеры для тестирования:
 * - PostgreSQL для тестирования базы данных
 * - Redis для тестирования Redis функциональности
 * - Kafka для тестирования Kafka функциональности
 */
@TestConfiguration
public class TestContainersConfig {

    /**
     * PostgreSQL контейнер для тестирования базы данных
     */
    @Bean(destroyMethod = "stop")
    @SuppressWarnings("resource")
    @ServiceConnection
    public PostgreSQLContainer<?> postgresContainer() {
        PostgreSQLContainer<?> container = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test")
                .withReuse(true); // Переиспользование контейнера для ускорения тестов
        return container;
    }

    /**
     * Redis контейнер для тестирования Redis функциональности
     */
    @Bean(destroyMethod = "stop")
    @SuppressWarnings("resource")
    public GenericContainer<?> redisContainer() {
        GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                .withExposedPorts(6379)
                .withReuse(true);
        return container;
    }

    /**
     * Kafka контейнер для тестирования Kafka функциональности
     */
    @Bean(destroyMethod = "stop")
    @SuppressWarnings("resource")
    public GenericContainer<?> kafkaContainer() {
        GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse("confluentinc/cp-kafka:latest"))
                .withExposedPorts(9092)
                .withEnv("KAFKA_ZOOKEEPER_CONNECT", "localhost:2181")
                .withEnv("KAFKA_ADVERTISED_LISTENERS", "PLAINTEXT://localhost:9092")
                .withEnv("KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", "1")
                .withReuse(true);
        return container;
    }
}
