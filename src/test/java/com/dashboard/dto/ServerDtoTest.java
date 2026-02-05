package com.dashboard.dto;

import com.dashboard.model.ServerStatus;
import com.dashboard.model.ServerType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ServerDto
 */
class ServerDtoTest {

    @Test
    void testDefaultConstructor() {
        ServerDto dto = new ServerDto();
        
        assertNull(dto.getId());
        assertNull(dto.getName());
        assertNull(dto.getUrl());
        assertNull(dto.getType());
        assertNull(dto.getHealthcheck());
        assertNull(dto.getStatus());
        assertNull(dto.getLastChecked());
        assertNull(dto.getCreatedAt());
        assertNull(dto.getUpdatedAt());
        assertNull(dto.getVersion());
        assertNull(dto.getMetricsEndpoint());
        assertNull(dto.getVersionRegex());
    }

    @Test
    void testParameterizedConstructor() {
        ServerDto dto = new ServerDto("Test Server", "http://test.com", ServerType.POSTGRES);
        
        assertEquals("Test Server", dto.getName());
        assertEquals("http://test.com", dto.getUrl());
        assertEquals(ServerType.POSTGRES, dto.getType());
        assertNull(dto.getId());
    }

    @Test
    void testSettersAndGetters() {
        ServerDto dto = new ServerDto();
        LocalDateTime now = LocalDateTime.now();
        
        dto.setId(1L);
        dto.setName("Test Server");
        dto.setUrl("http://test.com");
        dto.setType(ServerType.REDIS);
        dto.setHealthcheck("/health");
        dto.setStatus(ServerStatus.ONLINE);
        dto.setLastChecked(now);
        dto.setCreatedAt(now);
        dto.setUpdatedAt(now);
        dto.setVersion("1.0.0");
        dto.setMetricsEndpoint("/metrics");
        dto.setVersionRegex("version=(.*)");
        
        assertEquals(1L, dto.getId());
        assertEquals("Test Server", dto.getName());
        assertEquals("http://test.com", dto.getUrl());
        assertEquals(ServerType.REDIS, dto.getType());
        assertEquals("/health", dto.getHealthcheck());
        assertEquals(ServerStatus.ONLINE, dto.getStatus());
        assertEquals(now, dto.getLastChecked());
        assertEquals(now, dto.getCreatedAt());
        assertEquals(now, dto.getUpdatedAt());
        assertEquals("1.0.0", dto.getVersion());
        assertEquals("/metrics", dto.getMetricsEndpoint());
        assertEquals("version=(.*)", dto.getVersionRegex());
    }

    @Test
    void testAllServerTypes() {
        for (ServerType type : ServerType.values()) {
            ServerDto dto = new ServerDto("Test", "url", type);
            assertEquals(type, dto.getType());
        }
    }

    @Test
    void testAllServerStatuses() {
        for (ServerStatus status : ServerStatus.values()) {
            ServerDto dto = new ServerDto();
            dto.setStatus(status);
            assertEquals(status, dto.getStatus());
        }
    }

    @Test
    void testNullValues() {
        ServerDto dto = new ServerDto("Test", "url", ServerType.OTHER);
        
        dto.setName(null);
        dto.setUrl(null);
        dto.setType(null);
        dto.setHealthcheck(null);
        dto.setStatus(null);
        dto.setLastChecked(null);
        dto.setCreatedAt(null);
        dto.setUpdatedAt(null);
        dto.setVersion(null);
        dto.setMetricsEndpoint(null);
        dto.setVersionRegex(null);
        
        assertNull(dto.getName());
        assertNull(dto.getUrl());
        assertNull(dto.getType());
        assertNull(dto.getHealthcheck());
        assertNull(dto.getStatus());
        assertNull(dto.getLastChecked());
        assertNull(dto.getCreatedAt());
        assertNull(dto.getUpdatedAt());
        assertNull(dto.getVersion());
        assertNull(dto.getMetricsEndpoint());
        assertNull(dto.getVersionRegex());
    }
}
