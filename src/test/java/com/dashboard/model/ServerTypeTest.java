package com.dashboard.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ServerTypeTest {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Test
    public void testDeserializationFromDisplayName() throws Exception {
        // Test deserialization from display name (Russian)
        String json = "\"Другое\"";
        ServerType result = objectMapper.readValue(json, ServerType.class);
        assertEquals(ServerType.OTHER, result);
    }
    
    @Test
    public void testDeserializationFromEnumName() throws Exception {
        // Test deserialization from enum name
        String json = "\"OTHER\"";
        ServerType result = objectMapper.readValue(json, ServerType.class);
        assertEquals(ServerType.OTHER, result);
    }
    
    @Test
    public void testDeserializationFromEnumNameLowerCase() throws Exception {
        // Test deserialization from enum name (lowercase)
        String json = "\"other\"";
        ServerType result = objectMapper.readValue(json, ServerType.class);
        assertEquals(ServerType.OTHER, result);
    }
    
    @Test
    public void testDeserializationFromEnglishDisplayName() throws Exception {
        // Test deserialization from English display name
        String json = "\"Postgres\"";
        ServerType result = objectMapper.readValue(json, ServerType.class);
        assertEquals(ServerType.POSTGRES, result);
    }
    
    @Test
    public void testSerialization() throws Exception {
        // Test serialization to JSON
        String json = objectMapper.writeValueAsString(ServerType.OTHER);
        assertEquals("\"Другое\"", json);
    }
    
    @Test
    public void testInvalidValue() {
        // Test invalid value
        String json = "\"INVALID_TYPE\"";
        assertThrows(Exception.class, () -> {
            objectMapper.readValue(json, ServerType.class);
        });
    }
    
    @Test
    public void testFromStringMethod() {
        // Test fromString method directly
        assertEquals(ServerType.OTHER, ServerType.fromString("Другое"));
        assertEquals(ServerType.OTHER, ServerType.fromString("OTHER"));
        assertEquals(ServerType.OTHER, ServerType.fromString("other"));
        assertEquals(ServerType.POSTGRES, ServerType.fromString("Postgres"));
        
        assertThrows(IllegalArgumentException.class, () -> {
            ServerType.fromString("INVALID");
        });
    }
}
