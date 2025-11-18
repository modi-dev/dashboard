package com.dashboard.config;

import com.dashboard.service.KubernetesClusterInfoSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Инициализатор для загрузки информации о кластере при старте приложения
 * 
 * Выполняет первоначальную синхронизацию информации о Kubernetes кластере в БД,
 * чтобы при первом открытии страницы данные уже были доступны.
 * 
 * Запускается только если Kubernetes интеграция включена.
 */
@Component
@Order(2) // Запускается после PodsInitializer
@ConditionalOnProperty(value = "kubernetes.enabled", havingValue = "true", matchIfMissing = true)
public class ClusterInfoInitializer implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(ClusterInfoInitializer.class);
    
    private final KubernetesClusterInfoSyncService clusterInfoSyncService;
    
    public ClusterInfoInitializer(KubernetesClusterInfoSyncService clusterInfoSyncService) {
        this.clusterInfoSyncService = clusterInfoSyncService;
    }
    
    @Override
    public void run(String... args) {
        try {
            logger.info("Инициализация: загрузка информации о Kubernetes кластере в БД...");
            boolean success = clusterInfoSyncService.syncClusterInfo();
            if (success) {
                logger.info("Инициализация завершена: информация о кластере загружена");
            } else {
                logger.warn("Инициализация информации о кластере пропущена (Kubernetes отключен или ошибка)");
            }
        } catch (Exception e) {
            logger.warn("Ошибка при инициализации информации о кластере: {}", e.getMessage());
            if (logger.isDebugEnabled()) {
                logger.debug("Полные детали ошибки:", e);
            }
            // Не прерываем запуск приложения, если инициализация не удалась
            // Scheduler продолжит попытки синхронизации
        }
    }
}

