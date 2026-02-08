package com.dashboard.dto;

import com.dashboard.model.ServerStatus;
import com.dashboard.model.ServerType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ServerDtoTest {

    @Test
    void defaultConstructor_ShouldCreateEmptyDto() {
        ServerDto dto = new ServerDto();
        assertNull(dto.getId());
        assertNull(dto.getName());
        assertNull(dto.getUrl());
        assertNull(dto.getType());
    }

    @Test
    void parameterizedConstructor_ShouldSetFields() {
        ServerDto dto = new ServerDto("My Server", "http://localhost", ServerType.POSTGRES);
        assertEquals("My Server", dto.getName());
        assertEquals("http://localhost", dto.getUrl());
        assertEquals(ServerType.POSTGRES, dto.getType());
    }

    @Test
    void settersAndGetters_ShouldWork() {
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
        dto.setVersion("Redis 7.2.4");
        dto.setMetricsEndpoint("/metrics");
        dto.setVersionRegex("version=(.+)");

        assertEquals(1L, dto.getId());
        assertEquals("Test Server", dto.getName());
        assertEquals("http://test.com", dto.getUrl());
        assertEquals(ServerType.REDIS, dto.getType());
        assertEquals("/health", dto.getHealthcheck());
        assertEquals(ServerStatus.ONLINE, dto.getStatus());
        assertEquals(now, dto.getLastChecked());
        assertEquals(now, dto.getCreatedAt());
        assertEquals(now, dto.getUpdatedAt());
        assertEquals("Redis 7.2.4", dto.getVersion());
        assertEquals("/metrics", dto.getMetricsEndpoint());
        assertEquals("version=(.+)", dto.getVersionRegex());
    }
}
