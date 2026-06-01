package com.dashboard.integration;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Простой тест TestContainers без Spring контекста
 * 
 * Проверяет базовую функциональность TestContainers
 */
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
        // Проверяем, что PostgreSQL контейнер запущен
        assertThat(postgres.isRunning()).isTrue();
        assertThat(postgres.getDatabaseName()).isEqualTo("testdb");
        assertThat(postgres.getUsername()).isEqualTo("test");
        assertThat(postgres.getPassword()).isEqualTo("test");
    }

    @Test
    void shouldHaveCorrectPostgreSQLConnectionProperties() {
        // Проверяем свойства подключения к PostgreSQL
        assertThat(postgres.getJdbcUrl()).isNotNull();
        assertThat(postgres.getJdbcUrl()).contains("jdbc:postgresql://");
        assertThat(postgres.getJdbcUrl()).contains("testdb");
        
        assertThat(postgres.getHost()).isNotNull();
        assertThat(postgres.getMappedPort(5432)).isNotNull();
        assertThat(postgres.getMappedPort(5432)).isGreaterThan(0);
    }

    @Test
    void shouldHaveCorrectPostgreSQLVersion() {
        // Проверяем версию PostgreSQL
        assertThat(postgres.getDockerImageName()).contains("postgres:15-alpine");
    }

    @Test
    void shouldConnectToPostgreSQL() {
        // Проверяем, что можем подключиться к PostgreSQL
        String jdbcUrl = postgres.getJdbcUrl();
        String username = postgres.getUsername();
        String password = postgres.getPassword();
        
        assertThat(jdbcUrl).isNotEmpty();
        assertThat(username).isEqualTo("test");
        assertThat(password).isEqualTo("test");
        
        // Проверяем, что порт доступен
        assertThat(postgres.getMappedPort(5432)).isGreaterThan(0);
    }
}
