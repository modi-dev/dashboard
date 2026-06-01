package com.dashboard.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = ServerTypeDeserializer.class)
public enum ServerType {
    POSTGRES("Postgres"),
    REDIS("Redis"),
    KAFKA("Kafka"),
    ASTRA_LINUX("Astra Linux"),
    OTHER("Другое");
    
    private final String displayName;
    
    ServerType(String displayName) {
        this.displayName = displayName;
    }
    
    @JsonValue
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
    
    @JsonCreator
    public static ServerType fromString(String value) {
        if (value == null) {
            return null;
        }
        
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

