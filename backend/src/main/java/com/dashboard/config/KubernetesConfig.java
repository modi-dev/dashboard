package com.dashboard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация для работы с Kubernetes
 * Интегрирует настройки из version.sh скрипта
 */
@Configuration
@ConfigurationProperties(prefix = "kubernetes")
public class KubernetesConfig {
    
    private String namespace = "default";
    private String kubectlPath = "kubectl";
    private boolean enabled = false;
    
    // Getters и Setters
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
    
    @Override
    public String toString() {
        return "KubernetesConfig{" +
                "namespace='" + namespace + '\'' +
                ", kubectlPath='" + kubectlPath + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}
