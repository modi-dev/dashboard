package com.dashboard.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;

@Entity
@Table(name = "servers")
public class Server {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Server name is required")
    @Column(nullable = false)
    private String name;
    
    @NotBlank(message = "Server URL is required")
    @Pattern(regexp = "^(https?|postgres|redis|kafka|ssh)://.*", 
             message = "URL must start with http://, https://, postgres://, redis://, kafka://, or ssh://")
    @Column(nullable = false)
    private String url;
    
    @NotNull(message = "Server type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServerType type;
    
    @Column(name = "healthcheck_path")
    private String healthcheck;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServerStatus status = ServerStatus.UNKNOWN;
    
    @Column(name = "last_checked")
    private LocalDateTime lastChecked;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public Server() {}
    
    public Server(String name, String url, ServerType type) {
        this.name = name;
        this.url = url;
        this.type = type;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // JPA Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
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
}

