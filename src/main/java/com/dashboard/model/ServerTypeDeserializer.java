package com.dashboard.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class ServerTypeDeserializer extends JsonDeserializer<ServerType> {
    
    @Override
    public ServerType deserialize(JsonParser p, DeserializationContext ctxt) 
            throws IOException, JsonProcessingException {
        
        JsonNode node = p.getCodec().readTree(p);
        String value = node.asText();
        
        // Try to find by enum name first (e.g., "OTHER", "POSTGRES")
        try {
            return ServerType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            // If not found by enum name, try to find by display name
            for (ServerType type : ServerType.values()) {
                if (type.getDisplayName().equals(value)) {
                    return type;
                }
            }
            // If still not found, throw a more descriptive exception
            throw new IllegalArgumentException(
                "Cannot deserialize ServerType from '" + value + "'. " +
                "Valid values are: " + java.util.Arrays.toString(ServerType.values()) + 
                " or display names: " + java.util.Arrays.stream(ServerType.values())
                    .map(ServerType::getDisplayName)
                    .toArray()
            );
        }
    }
}
