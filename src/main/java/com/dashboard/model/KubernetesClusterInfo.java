package com.dashboard.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Модель для хранения информации о Kubernetes кластере
 * Хранит версию кластера и текущий namespace
 * 
 * JPA Entity для кэширования данных о кластере в БД
 */
@Entity
@Table(name = "kubernetes_cluster_info")
public class KubernetesClusterInfo {
    
    /**
     * ID записи - всегда будет только одна запись
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Версия Kubernetes кластера
     */
    @Column(name = "kubernetes_version", nullable = true, length = 255)
    private String kubernetesVersion;
    
    /**
     * Текущий namespace
     */
    @Column(name = "namespace", nullable = false, length = 255)
    private String namespace;
    
    /**
     * Дата создания записи
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Дата последнего обновления
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * Дата последнего запроса к kubectl
     */
    @Column(name = "k8s_queried_at")
    private LocalDateTime k8sQueriedAt;
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
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
    
    public String getKubernetesVersion() {
        return kubernetesVersion;
    }
    
    public void setKubernetesVersion(String kubernetesVersion) {
        this.kubernetesVersion = kubernetesVersion;
    }
    
    public String getNamespace() {
        return namespace;
    }
    
    public void setNamespace(String namespace) {
        this.namespace = namespace;
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
    
    public LocalDateTime getK8sQueriedAt() {
        return k8sQueriedAt;
    }
    
    public void setK8sQueriedAt(LocalDateTime k8sQueriedAt) {
        this.k8sQueriedAt = k8sQueriedAt;
    }
}

