package com.dashboard.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * Модель для хранения информации о поде в Kubernetes
 * Интегрирует функциональность из version.sh скрипта
 */
public class PodInfo {
    
    private String name;           // Имя приложения (из labels.app)
    private String version;        // Версия образа
    private String msBranch;       // Ветка микросервиса
    private String configBranch;   // Ветка конфигурации
    private String gcOptions;      // Опции сборщика мусора
    private LocalDateTime creationDate; // Дата создания
    private String port;           // Порты контейнера (все порты через запятую)
    private String cpuRequest;     // Запрошенные CPU ресурсы
    private String memoryRequest;  // Запрошенные RAM ресурсы
    private Integer restarts;      // Количество рестартов контейнеров в поде (сумма)
    private String podName;         // Полное имя пода (metadata.name)
    private String readyTime;       // Время от создания до статуса Ready
    
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
    
    @Override
    public String toString() {
        return "PodInfo{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", msBranch='" + msBranch + '\'' +
                ", configBranch='" + configBranch + '\'' +
                ", gcOptions='" + gcOptions + '\'' +
                ", creationDate=" + creationDate +
                ", port=" + port +
                ", cpuRequest='" + cpuRequest + '\'' +
                ", memoryRequest='" + memoryRequest + '\'' +
                ", restarts=" + restarts +
                ", podName='" + podName + '\'' +
                ", readyTime='" + readyTime + '\'' +
                '}';
    }
}
