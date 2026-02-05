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
    
    /**
     * Использование CPU (used/hard) из quota
     */
    @Column(name = "quota_cpu_used", length = 255)
    private String quotaCpuUsed;
    
    @Column(name = "quota_cpu_hard", length = 255)
    private String quotaCpuHard;
    
    /**
     * Использование Memory (used/hard) из quota
     */
    @Column(name = "quota_memory_used", length = 255)
    private String quotaMemoryUsed;
    
    @Column(name = "quota_memory_hard", length = 255)
    private String quotaMemoryHard;
    
    /**
     * Использование Pods (used/hard) из quota
     */
    @Column(name = "quota_pods_used", length = 255)
    private String quotaPodsUsed;
    
    @Column(name = "quota_pods_hard", length = 255)
    private String quotaPodsHard;
    
    /**
     * Использование ConfigMaps (used/hard) из quota
     */
    @Column(name = "quota_configmaps_used", length = 255)
    private String quotaConfigmapsUsed;
    
    @Column(name = "quota_configmaps_hard", length = 255)
    private String quotaConfigmapsHard;
    
    /**
     * Использование Secrets (used/hard) из quota
     */
    @Column(name = "quota_secrets_used", length = 255)
    private String quotaSecretsUsed;
    
    @Column(name = "quota_secrets_hard", length = 255)
    private String quotaSecretsHard;
    
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
    
    public String getQuotaCpuUsed() {
        return quotaCpuUsed;
    }
    
    public void setQuotaCpuUsed(String quotaCpuUsed) {
        this.quotaCpuUsed = quotaCpuUsed;
    }
    
    public String getQuotaCpuHard() {
        return quotaCpuHard;
    }
    
    public void setQuotaCpuHard(String quotaCpuHard) {
        this.quotaCpuHard = quotaCpuHard;
    }
    
    public String getQuotaMemoryUsed() {
        return quotaMemoryUsed;
    }
    
    public void setQuotaMemoryUsed(String quotaMemoryUsed) {
        this.quotaMemoryUsed = quotaMemoryUsed;
    }
    
    public String getQuotaMemoryHard() {
        return quotaMemoryHard;
    }
    
    public void setQuotaMemoryHard(String quotaMemoryHard) {
        this.quotaMemoryHard = quotaMemoryHard;
    }
    
    public String getQuotaPodsUsed() {
        return quotaPodsUsed;
    }
    
    public void setQuotaPodsUsed(String quotaPodsUsed) {
        this.quotaPodsUsed = quotaPodsUsed;
    }
    
    public String getQuotaPodsHard() {
        return quotaPodsHard;
    }
    
    public void setQuotaPodsHard(String quotaPodsHard) {
        this.quotaPodsHard = quotaPodsHard;
    }
    
    public String getQuotaConfigmapsUsed() {
        return quotaConfigmapsUsed;
    }
    
    public void setQuotaConfigmapsUsed(String quotaConfigmapsUsed) {
        this.quotaConfigmapsUsed = quotaConfigmapsUsed;
    }
    
    public String getQuotaConfigmapsHard() {
        return quotaConfigmapsHard;
    }
    
    public void setQuotaConfigmapsHard(String quotaConfigmapsHard) {
        this.quotaConfigmapsHard = quotaConfigmapsHard;
    }
    
    public String getQuotaSecretsUsed() {
        return quotaSecretsUsed;
    }
    
    public void setQuotaSecretsUsed(String quotaSecretsUsed) {
        this.quotaSecretsUsed = quotaSecretsUsed;
    }
    
    public String getQuotaSecretsHard() {
        return quotaSecretsHard;
    }
    
    public void setQuotaSecretsHard(String quotaSecretsHard) {
        this.quotaSecretsHard = quotaSecretsHard;
    }
}

