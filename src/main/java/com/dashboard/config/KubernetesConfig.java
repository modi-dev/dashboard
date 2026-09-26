package com.dashboard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация для работы с Kubernetes
 * 
 * Этот класс хранит настройки для подключения к Kubernetes кластеру.
 * 
 * Настройки читаются из application.properties (секция kubernetes.*)
 * Например: kubernetes.enabled=true, kubernetes.namespace=dev-tools
 * 
 * @Configuration - класс с настройками
 * @ConfigurationProperties - автоматически заполняет поля из файла настроек
 */
@Configuration
@ConfigurationProperties(prefix = "kubernetes")
public class KubernetesConfig {
    
    private String namespace = "default";
    
    // Путь к команде kubectl
    private String kubectlPath = "kubectl";
    
    // Включена ли интеграция с Kubernetes (если false, функции с подами работать не будут)
    private boolean enabled = false;

    /**
     * Чтение DATABASE_CLUSTER_URL через {@code kubectl get secrets}.
     * По умолчанию выключено: требует RBAC на secrets и даёт N лишних процессов kubectl на каждый sync.
     */
    private boolean secretsDatabaseUrlEnabled = false;
    
    // Getters и Setters - методы для получения и изменения значений полей
    public String getNamespace() {
        return namespace;
    }
    
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
    
    public String getKubectlPath() {
        return kubectlPath;
    }
    
    public void setKubectlPath(String kubectlPath) {
        this.kubectlPath = kubectlPath;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isSecretsDatabaseUrlEnabled() {
        return secretsDatabaseUrlEnabled;
    }

    public void setSecretsDatabaseUrlEnabled(boolean secretsDatabaseUrlEnabled) {
        this.secretsDatabaseUrlEnabled = secretsDatabaseUrlEnabled;
    }
    
    /**
     * Метод для красивого вывода конфигурации в лог
     */
    @Override
    public String toString() {
        return "KubernetesConfig{" +
                "namespace='" + namespace + '\'' +
                ", kubectlPath='" + kubectlPath + '\'' +
                ", enabled=" + enabled +
                ", secretsDatabaseUrlEnabled=" + secretsDatabaseUrlEnabled +
                '}';
    }
}
