package com.dashboard.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;

/**
 * Модель Сервера - представляет один сервер в базе данных
 * 
 * Это главная сущность приложения. Каждая строка в таблице "servers" - это объект Server.
 * JPA (Java Persistence API) автоматически преобразует объекты в записи БД и обратно.
 * 
 * Аннотации JPA:
 * @Entity - говорит, что это сущность БД (будет таблица)
 * @Table - указывает имя таблицы в базе данных
 */
@Entity
@Table(name = "servers")
public class Server {
    
    // === ПОЛЯ КЛАССА (колонки в таблице БД) ===
    
    /**
     * ID сервера - уникальный номер каждого сервера
     * @Id - это первичный ключ (primary key)
     * @GeneratedValue - БД сама генерирует ID автоматически (1, 2, 3...)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Название сервера (например: "Production API", "Test Database")
     * @NotBlank - поле обязательное, не может быть пустым
     * @Column(nullable = false) - в БД тоже не может быть NULL
     */
    @NotBlank(message = "Server name is required")
    @Column(nullable = false)
    private String name;
    
    /**
     * URL адрес сервера для проверки доступности
     * Примеры: https://google.com, postgresql://localhost:5432
     * 
     * @Pattern - проверяет формат URL (должен начинаться с правильного протокола)
     */
    @NotBlank(message = "Server URL is required")
    @Pattern(regexp = "^(https?|postgres|redis|kafka|ssh)://.*", 
             message = "URL must start with http://, https://, postgres://, redis://, kafka://, or ssh://")
    @Column(nullable = false)
    private String url;
    
    /**
     * Тип сервера: POSTGRES, REDIS, KAFKA, ASTRA_LINUX или OTHER
     * @Enumerated(EnumType.STRING) - сохраняем как текст (не номер)
     */
    @NotNull(message = "Server type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServerType type;
    
    /**
     * Путь для проверки здоровья (healthcheck)
     * Используется только для серверов типа OTHER
     * Пример: /health, /api/status
     */
    @Column(name = "healthcheck_path")
    private String healthcheck;
    
    /**
     * Текущий статус сервера: ONLINE, OFFLINE или UNKNOWN
     * По умолчанию UNKNOWN пока не проверили
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServerStatus status = ServerStatus.UNKNOWN;
    
    /**
     * Когда последний раз проверяли сервер
     * Обновляется автоматически при каждой проверке
     */
    @Column(name = "last_checked")
    private LocalDateTime lastChecked;
    
    /**
     * Когда сервер был добавлен в систему
     * updatable = false - это поле нельзя изменить после создания
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Когда последний раз обновляли информацию о сервере
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Версия сервера (получается автоматически через метрики)
     * Пример: "PostgreSQL 15.8", "Redis 7.0.5"
     */
    @Column(name = "version")
    private String version;
    
    /**
     * Кастомный endpoint для получения метрик (только для типа OTHER)
     * Пример: "/custom-metrics", "/api/version"
     */
    @Column(name = "metrics_endpoint")
    private String metricsEndpoint;
    
    /**
     * Кастомное регулярное выражение для извлечения версии из метрик (только для типа OTHER)
     * Пример: "version\\s*=\\s*\"([^\"]+)\""
     */
    @Column(name = "version_regex")
    private String versionRegex;
    
    // === КОНСТРУКТОРЫ ===
    
    /**
     * Пустой конструктор - нужен для JPA (обязательно!)
     * JPA использует его при создании объектов из БД
     */
    public Server() {}
    
    /**
     * Конструктор для создания нового сервера
     * @param name - название сервера
     * @param url - адрес сервера
     * @param type - тип сервера
     */
    public Server(String name, String url, ServerType type) {
        this.name = name;
        this.url = url;
        this.type = type;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // === LIFECYCLE МЕТОДЫ (вызываются JPA автоматически) ===
    
    /**
     * Вызывается перед сохранением НОВОГО объекта в БД
     * Устанавливаем текущее время создания
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Вызывается перед ОБНОВЛЕНИЕМ существующего объекта в БД
     * Обновляем время последнего изменения
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // === GETTERS И SETTERS ===
    // Это методы для получения (get) и изменения (set) значений полей
    // В Java нельзя напрямую обращаться к полям объекта - только через эти методы
    
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

