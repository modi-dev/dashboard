package com.dashboard.dto;

import com.dashboard.model.ServerStatus;
import com.dashboard.model.ServerType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ServerDtoTest {

    @Test
    void constructorSetsRequiredFields() {
        ServerDto dto = new ServerDto("Search API", "https://api.example.com", ServerType.OTHER);

        assertEquals("Search API", dto.getName());
        assertEquals("https://api.example.com", dto.getUrl());
        assertEquals(ServerType.OTHER, dto.getType());
    }

    @Test
    void settersAndGettersRoundTrip() {
        ServerDto dto = new ServerDto();
        LocalDateTime now = LocalDateTime.now();

        dto.setId(42L);
        dto.setName("Redis");
        dto.setUrl("redis.internal");
        dto.setType(ServerType.REDIS);
        dto.setHealthcheck("/health");
        dto.setStatus(ServerStatus.ONLINE);
        dto.setLastChecked(now);
        dto.setCreatedAt(now.minusDays(1));
        dto.setUpdatedAt(now);
        dto.setVersion("7.2.1");
        dto.setMetricsEndpoint("/metrics");
        dto.setVersionRegex("version=([0-9.]+)");

        assertEquals(42L, dto.getId());
        assertEquals("Redis", dto.getName());
        assertEquals("redis.internal", dto.getUrl());
        assertEquals(ServerType.REDIS, dto.getType());
        assertEquals("/health", dto.getHealthcheck());
        assertEquals(ServerStatus.ONLINE, dto.getStatus());
        assertEquals(now, dto.getLastChecked());
        assertEquals(now.minusDays(1), dto.getCreatedAt());
        assertEquals(now, dto.getUpdatedAt());
        assertEquals("7.2.1", dto.getVersion());
        assertEquals("/metrics", dto.getMetricsEndpoint());
        assertEquals("version=([0-9.]+)", dto.getVersionRegex());
    }
}
