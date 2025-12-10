package com.dashboard.dto;

import com.dashboard.model.ServerType;
import com.dashboard.model.ServerStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class ServerDto {
    
    private Long id;
    
    @NotBlank(message = "Server name is required")
    private String name;
    
    @NotBlank(message = "Server URL is required")
    private String url;
    
    @NotNull(message = "Server type is required")
    private ServerType type;
    
    private String healthcheck;
    
    private ServerStatus status;
    
    private LocalDateTime lastChecked;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private String version;
    
    private String metricsEndpoint;
    
    private String versionRegex;
    
    // Constructors
    public ServerDto() {}
    
    public ServerDto(String name, String url, ServerType type) {
        this.name = name;
        this.url = url;
        this.type = type;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public ServerType getType() {
        return type;
    }
    
    public void setType(ServerType type) {
        this.type = type;
    }
    
    public String getHealthcheck() {
        return healthcheck;
    }
    
    public void setHealthcheck(String healthcheck) {
        this.healthcheck = healthcheck;
    }
    
    public ServerStatus getStatus() {
        return status;
    }
    
    public void setStatus(ServerStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getLastChecked() {
        return lastChecked;
    }
    
    public void setLastChecked(LocalDateTime lastChecked) {
        this.lastChecked = lastChecked;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getMetricsEndpoint() {
        return metricsEndpoint;
    }
    
    public void setMetricsEndpoint(String metricsEndpoint) {
        this.metricsEndpoint = metricsEndpoint;
    }
    
    public String getVersionRegex() {
        return versionRegex;
    }
    
    public void setVersionRegex(String versionRegex) {
        this.versionRegex = versionRegex;
    }
}

