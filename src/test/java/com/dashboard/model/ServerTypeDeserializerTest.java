package com.dashboard.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServerTypeDeserializerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void deserialize_ShouldParseEnumName_POSTGRES() throws Exception {
        ServerType type = objectMapper.readValue("\"POSTGRES\"", ServerType.class);
        assertEquals(ServerType.POSTGRES, type);
    }

    @Test
    void deserialize_ShouldParseEnumName_REDIS() throws Exception {
        ServerType type = objectMapper.readValue("\"REDIS\"", ServerType.class);
        assertEquals(ServerType.REDIS, type);
    }

    @Test
    void deserialize_ShouldParseEnumName_KAFKA() throws Exception {
        ServerType type = objectMapper.readValue("\"KAFKA\"", ServerType.class);
        assertEquals(ServerType.KAFKA, type);
    }

    @Test
    void deserialize_ShouldParseEnumName_OTHER() throws Exception {
        ServerType type = objectMapper.readValue("\"OTHER\"", ServerType.class);
        assertEquals(ServerType.OTHER, type);
    }

    @Test
    void deserialize_ShouldParseEnumName_ASTRA_LINUX() throws Exception {
        ServerType type = objectMapper.readValue("\"ASTRA_LINUX\"", ServerType.class);
        assertEquals(ServerType.ASTRA_LINUX, type);
    }

    @Test
    void deserialize_ShouldParseDisplayName_Postgres() throws Exception {
        ServerType type = objectMapper.readValue("\"Postgres\"", ServerType.class);
        assertEquals(ServerType.POSTGRES, type);
    }

    @Test
    void deserialize_ShouldParseDisplayName_Redis() throws Exception {
        ServerType type = objectMapper.readValue("\"Redis\"", ServerType.class);
        assertEquals(ServerType.REDIS, type);
    }

    @Test
    void deserialize_ShouldParseDisplayName_Kafka() throws Exception {
        ServerType type = objectMapper.readValue("\"Kafka\"", ServerType.class);
        assertEquals(ServerType.KAFKA, type);
    }

    @Test
    void deserialize_ShouldParseDisplayName_AstraLinux() throws Exception {
        ServerType type = objectMapper.readValue("\"Astra Linux\"", ServerType.class);
        assertEquals(ServerType.ASTRA_LINUX, type);
    }

    @Test
    void deserialize_ShouldParseDisplayName_Drugoe() throws Exception {
        ServerType type = objectMapper.readValue("\"Другое\"", ServerType.class);
        assertEquals(ServerType.OTHER, type);
    }

    @Test
    void deserialize_ShouldThrowForInvalidValue() {
        assertThrows(Exception.class, () -> {
            objectMapper.readValue("\"INVALID_TYPE\"", ServerType.class);
        });
    }

    @Test
    void deserialize_ShouldParseLowercase() throws Exception {
        ServerType type = objectMapper.readValue("\"postgres\"", ServerType.class);
        assertEquals(ServerType.POSTGRES, type);
    }
}
