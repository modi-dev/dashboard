package com.dashboard.model;

public enum ServerStatus {
    ONLINE("online"),
    OFFLINE("offline"),
    UNKNOWN("unknown");
    
    private final String value;
    
    ServerStatus(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return value;
    }
}

