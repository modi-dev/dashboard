package com.dashboard.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Server model
 */
class ServerTest {

    @Test
    void testDefaultConstructor() {
        Server server = new Server();
        
        assertNull(server.getId());
        assertNull(server.getName());
        assertNull(server.getUrl());
        assertNull(server.getType());
        assertNull(server.getHealthcheck());
        assertEquals(ServerStatus.UNKNOWN, server.getStatus()); // Default status
        assertNull(server.getLastChecked());
        assertNull(server.getCreatedAt());
        assertNull(server.getUpdatedAt());
        assertNull(server.getVersion());
        assertNull(server.getMetricsEndpoint());
        assertNull(server.getVersionRegex());
    }

    @Test
    void testParameterizedConstructor() {
        Server server = new Server("Test Server", "http://test.com", ServerType.POSTGRES);
        
        assertEquals("Test Server", server.getName());
        assertEquals("http://test.com", server.getUrl());
        assertEquals(ServerType.POSTGRES, server.getType());
        assertNotNull(server.getCreatedAt());
        assertNotNull(server.getUpdatedAt());
        assertEquals(ServerStatus.UNKNOWN, server.getStatus());
    }

    @Test
    void testSettersAndGetters() {
        Server server = new Server();
        LocalDateTime now = LocalDateTime.now();
        
        server.setId(1L);
        server.setName("Test Server");
        server.setUrl("http://test.com");
        server.setType(ServerType.REDIS);
        server.setHealthcheck("/health");
        server.setStatus(ServerStatus.ONLINE);
        server.setLastChecked(now);
        server.setCreatedAt(now);
        server.setUpdatedAt(now);
        server.setVersion("1.0.0");
        server.setMetricsEndpoint("/metrics");
        server.setVersionRegex("version=(.*)");
        
        assertEquals(1L, server.getId());
        assertEquals("Test Server", server.getName());
        assertEquals("http://test.com", server.getUrl());
        assertEquals(ServerType.REDIS, server.getType());
        assertEquals("/health", server.getHealthcheck());
        assertEquals(ServerStatus.ONLINE, server.getStatus());
        assertEquals(now, server.getLastChecked());
        assertEquals(now, server.getCreatedAt());
        assertEquals(now, server.getUpdatedAt());
        assertEquals("1.0.0", server.getVersion());
        assertEquals("/metrics", server.getMetricsEndpoint());
        assertEquals("version=(.*)", server.getVersionRegex());
    }

    @Test
    void testOnCreate() {
        Server server = new Server();
        LocalDateTime before = LocalDateTime.now();
        
        // Simulate JPA PrePersist
        server.onCreate();
        
        LocalDateTime after = LocalDateTime.now();
        
        assertNotNull(server.getCreatedAt());
        assertNotNull(server.getUpdatedAt());
        // Both should be set to approximately the same time
        assertTrue(!server.getCreatedAt().isBefore(before));
        assertTrue(!server.getCreatedAt().isAfter(after));
        assertTrue(!server.getUpdatedAt().isBefore(before));
        assertTrue(!server.getUpdatedAt().isAfter(after));
    }

    @Test
    void testOnUpdate() {
        Server server = new Server();
        LocalDateTime oldTime = LocalDateTime.of(2024, 1, 1, 0, 0);
        server.setUpdatedAt(oldTime);
        
        // Simulate JPA PreUpdate
        server.onUpdate();
        
        assertNotNull(server.getUpdatedAt());
        assertTrue(server.getUpdatedAt().isAfter(oldTime));
    }

    @Test
    void testAllServerTypes() {
        for (ServerType type : ServerType.values()) {
            Server server = new Server("Test", "url", type);
            assertEquals(type, server.getType());
        }
    }

    @Test
    void testAllServerStatuses() {
        for (ServerStatus status : ServerStatus.values()) {
            Server server = new Server();
            server.setStatus(status);
            assertEquals(status, server.getStatus());
        }
    }

    @Test
    void testNullValues() {
        Server server = new Server("Test", "url", ServerType.OTHER);
        
        server.setName(null);
        server.setUrl(null);
        server.setType(null);
        server.setHealthcheck(null);
        server.setStatus(null);
        server.setLastChecked(null);
        server.setVersion(null);
        server.setMetricsEndpoint(null);
        server.setVersionRegex(null);
        
        assertNull(server.getName());
        assertNull(server.getUrl());
        assertNull(server.getType());
        assertNull(server.getHealthcheck());
        assertNull(server.getStatus());
        assertNull(server.getLastChecked());
        assertNull(server.getVersion());
        assertNull(server.getMetricsEndpoint());
        assertNull(server.getVersionRegex());
    }

    @Test
    void testConstructorSetsTimestamps() {
        LocalDateTime before = LocalDateTime.now();
        Server server = new Server("Test", "url", ServerType.POSTGRES);
        LocalDateTime after = LocalDateTime.now();
        
        assertNotNull(server.getCreatedAt());
        assertNotNull(server.getUpdatedAt());
        
        assertTrue(!server.getCreatedAt().isBefore(before));
        assertTrue(!server.getCreatedAt().isAfter(after));
    }
    
    @Test
    void testServerStatusTransitions() {
        Server server = new Server("Test", "url", ServerType.POSTGRES);
        
        // Initial state
        assertEquals(ServerStatus.UNKNOWN, server.getStatus());
        
        // Transition to ONLINE
        server.setStatus(ServerStatus.ONLINE);
        assertEquals(ServerStatus.ONLINE, server.getStatus());
        
        // Transition to OFFLINE
        server.setStatus(ServerStatus.OFFLINE);
        assertEquals(ServerStatus.OFFLINE, server.getStatus());
        
        // Transition back to UNKNOWN
        server.setStatus(ServerStatus.UNKNOWN);
        assertEquals(ServerStatus.UNKNOWN, server.getStatus());
    }
    
    @Test
    void testDifferentUrlFormats() {
        Server server1 = new Server("Test 1", "localhost:5432", ServerType.POSTGRES);
        Server server2 = new Server("Test 2", "http://example.com:8080", ServerType.OTHER);
        Server server3 = new Server("Test 3", "https://secure.example.com/api", ServerType.OTHER);
        Server server4 = new Server("Test 4", "192.168.1.100", ServerType.REDIS);
        
        assertEquals("localhost:5432", server1.getUrl());
        assertEquals("http://example.com:8080", server2.getUrl());
        assertEquals("https://secure.example.com/api", server3.getUrl());
        assertEquals("192.168.1.100", server4.getUrl());
    }
}
