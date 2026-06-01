package com.dashboard.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Модель для хранения информации о поде в Kubernetes
 * Интегрирует функциональность из version.sh скрипта
 * 
 * JPA Entity для кэширования данных о подах в БД
 */
@Entity
@Table(name = "pods", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"pod_name", "namespace"}))
public class PodInfo {
    
    /**
     * ID пода - уникальный номер каждого пода в БД
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Полное имя пода (metadata.name) - используется как часть уникального ключа
     */
    @Column(name = "pod_name", nullable = false, length = 512)
    private String podName;
    
    /**
     * Namespace пода - используется как часть уникального ключа
     */
    @Column(name = "namespace", nullable = false, length = 255)
    private String namespace;
    
    /**
     * Имя приложения (из labels.app)
     */
    @Column(name = "name", nullable = true, length = 255)
    private String name;
    
    /**
     * Версия образа
     */
    @Column(name = "version", nullable = true, length = 255)
    private String version;
    
    /**
     * Ветка микросервиса
     */
    @Column(name = "ms_branch", nullable = true, length = 255)
    private String msBranch;
    
    /**
     * Ветка конфигурации
     */
    @Column(name = "config_branch", length = 255)
    private String configBranch;
    
    /**
     * Опции сборщика мусора
     */
    @Column(name = "gc_options", columnDefinition = "TEXT")
    private String gcOptions;
    
    /**
     * Дата создания пода
     */
    @Column(name = "creation_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime creationDate;
    
    /**
     * Порты контейнера (все порты через запятую)
     */
    @Column(name = "port", nullable = true, length = 255)
    private String port;
    
    /**
     * Запрошенные CPU ресурсы
     */
    @Column(name = "cpu_request", nullable = true, length = 255)
    private String cpuRequest;
    
    /**
     * Запрошенные RAM ресурсы
     */
    @Column(name = "memory_request", nullable = true, length = 255)
    private String memoryRequest;
    
    /**
     * Количество рестартов контейнеров в поде (сумма)
     */
    @Column(name = "restarts")
    private Integer restarts;
    
    /**
     * Время от создания до статуса Ready
     */
    @Column(name = "ready_time",nullable = false, length = 255)
    private String readyTime;
    
    /**
     * URL кластера базы данных из секретов (переменная DATABASE_CLUSTER_URL)
     */
    @Column(name = "database_cluster_url", nullable = true, length = 512)
    private String databaseClusterUrl;
    
    /**
     * Когда под был добавлен в БД
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Когда информация о поде была последний раз обновлена
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Время последнего запроса к Kubernetes (kubectl get pods)
     */
    @Column(name = "k8s_queried_at")
    private LocalDateTime k8sQueriedAt;
    
    // Конструкторы
    public PodInfo() {}
    
    public PodInfo(String name, String version, String msBranch, String configBranch, 
                   String gcOptions, LocalDateTime creationDate, String port, 
                   String cpuRequest, String memoryRequest) {
        this.name = name;
        this.version = version;
        this.msBranch = msBranch;
        this.configBranch = configBranch;
        this.gcOptions = gcOptions;
        this.creationDate = creationDate;
        this.port = port;
        this.cpuRequest = cpuRequest;
        this.memoryRequest = memoryRequest;
    }
    
    /**
     * Вызывается перед сохранением НОВОГО объекта в БД
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Вызывается перед ОБНОВЛЕНИЕМ существующего объекта в БД
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters и Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getMsBranch() {
        return msBranch;
    }
    
    public void setMsBranch(String msBranch) {
        this.msBranch = msBranch;
    }
    
    public String getConfigBranch() {
        return configBranch;
    }
    
    public void setConfigBranch(String configBranch) {
        this.configBranch = configBranch;
    }
    
    public String getGcOptions() {
        return gcOptions;
    }
    
    public void setGcOptions(String gcOptions) {
        this.gcOptions = gcOptions;
    }
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime getCreationDate() {
        return creationDate;
    }
    
    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }
    
    public String getPort() {
        return port;
    }
    
    public void setPort(String port) {
        this.port = port;
    }
    
    public String getCpuRequest() {
        return cpuRequest;
    }
    
    public void setCpuRequest(String cpuRequest) {
        this.cpuRequest = cpuRequest;
    }
    
    public String getMemoryRequest() {
        return memoryRequest;
    }
    
    public void setMemoryRequest(String memoryRequest) {
        this.memoryRequest = memoryRequest;
    }
    
    public Integer getRestarts() {
        return restarts;
    }
    
    public void setRestarts(Integer restarts) {
        this.restarts = restarts;
    }
    
    public String getPodName() {
        return podName;
    }
    
    public void setPodName(String podName) {
        this.podName = podName;
    }
    
    public String getReadyTime() {
        return readyTime;
    }
    
    public void setReadyTime(String readyTime) {
        this.readyTime = readyTime;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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
    
    public String getDatabaseClusterUrl() {
        return databaseClusterUrl;
    }
    
    public void setDatabaseClusterUrl(String databaseClusterUrl) {
        this.databaseClusterUrl = databaseClusterUrl;
    }

    @Override
    public String toString() {
        return "PodInfo{" +
                "id=" + id +
                ", podName='" + podName + '\'' +
                ", namespace='" + namespace + '\'' +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", msBranch='" + msBranch + '\'' +
                ", configBranch='" + configBranch + '\'' +
                ", gcOptions='" + gcOptions + '\'' +
                ", creationDate=" + creationDate +
                ", port='" + port + '\'' +
                ", cpuRequest='" + cpuRequest + '\'' +
                ", memoryRequest='" + memoryRequest + '\'' +
                ", restarts=" + restarts +
                ", readyTime='" + readyTime + '\'' +
                ", databaseClusterUrl='" + databaseClusterUrl + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", k8sQueriedAt=" + k8sQueriedAt +
                '}';
    }
}
