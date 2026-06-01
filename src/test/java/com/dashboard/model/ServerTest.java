package com.dashboard.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ServerTest {

    @Test
    void defaultConstructor_ShouldCreateEmptyServer() {
        Server server = new Server();
        assertNull(server.getId());
        assertNull(server.getName());
        assertEquals(ServerStatus.UNKNOWN, server.getStatus());
    }

    @Test
    void parameterizedConstructor_ShouldSetFields() {
        Server server = new Server("My Server", "http://localhost:8080", ServerType.OTHER);

        assertEquals("My Server", server.getName());
        assertEquals("http://localhost:8080", server.getUrl());
        assertEquals(ServerType.OTHER, server.getType());
        assertNotNull(server.getCreatedAt());
        assertNotNull(server.getUpdatedAt());
    }

    @Test
    void settersAndGetters_ShouldWork() {
        Server server = new Server();
        LocalDateTime now = LocalDateTime.now();

        server.setId(1L);
        server.setName("Test Server");
        server.setUrl("http://test.com");
        server.setType(ServerType.POSTGRES);
        server.setHealthcheck("/health");
        server.setStatus(ServerStatus.ONLINE);
        server.setLastChecked(now);
        server.setCreatedAt(now);
        server.setUpdatedAt(now);
        server.setVersion("PostgreSQL 15.8");
        server.setMetricsEndpoint("/metrics");
        server.setVersionRegex("version=\"(.+)\"");

        assertEquals(1L, server.getId());
        assertEquals("Test Server", server.getName());
        assertEquals("http://test.com", server.getUrl());
        assertEquals(ServerType.POSTGRES, server.getType());
        assertEquals("/health", server.getHealthcheck());
        assertEquals(ServerStatus.ONLINE, server.getStatus());
        assertEquals(now, server.getLastChecked());
        assertEquals(now, server.getCreatedAt());
        assertEquals(now, server.getUpdatedAt());
        assertEquals("PostgreSQL 15.8", server.getVersion());
        assertEquals("/metrics", server.getMetricsEndpoint());
        assertEquals("version=\"(.+)\"", server.getVersionRegex());
    }

    @Test
    void onCreate_ShouldSetTimestamps() {
        Server server = new Server();
        server.onCreate();
        assertNotNull(server.getCreatedAt());
        assertNotNull(server.getUpdatedAt());
    }

    @Test
    void onUpdate_ShouldUpdateTimestamp() {
        Server server = new Server();
        server.onCreate();
        LocalDateTime first = server.getUpdatedAt();

        server.onUpdate();

        assertNotNull(server.getUpdatedAt());
    }
}
