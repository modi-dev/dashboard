package com.dashboard.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Планировщик для периодической синхронизации информации о Kubernetes кластере в БД
 * 
 * Обновляет версию Kubernetes и namespace в фоне.
 * Интервал синхронизации настраивается в application.yml (kubernetes.cluster-info.sync-interval).
 * По умолчанию обновляется раз в час, так как версия Kubernetes меняется редко.
 */
@Service
@ConditionalOnProperty(value = "spring.task.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class KubernetesClusterInfoScheduler {

    private static final Logger logger = LoggerFactory.getLogger(KubernetesClusterInfoScheduler.class);

    private final KubernetesClusterInfoSyncService clusterInfoSyncService;
    private final long intervalMs;

    public KubernetesClusterInfoScheduler(KubernetesClusterInfoSyncService clusterInfoSyncService,
                                        @Value("${kubernetes.cluster-info.sync-interval:60}") double intervalMinutes) {
        this.clusterInfoSyncService = clusterInfoSyncService;
        // Convert minutes to milliseconds
        this.intervalMs = (long) (intervalMinutes * 60 * 1000);
        logger.info("Kubernetes cluster info sync scheduler initialized with interval: {} minutes ({} ms)", 
                   intervalMinutes, intervalMs);
    }

    /**
     * Периодически синхронизирует информацию о кластере из Kubernetes в БД
     * 
     * Использует SpEL выражение для вычисления интервала из конфигурации
     * По умолчанию: 60 минут (1 час)
     */
    @Scheduled(fixedRateString = "#{${kubernetes.cluster-info.sync-interval:60} * 60 * 1000}")
    public void syncClusterInfo() {
        try {
            boolean success = clusterInfoSyncService.syncClusterInfo();
            if (success) {
                logger.debug("Scheduled cluster info sync completed successfully");
            } else {
                logger.debug("Scheduled cluster info sync skipped (Kubernetes disabled or error)");
            }
        } catch (Exception e) {
            logger.warn("Scheduled cluster info sync failed: {}", e.getMessage());
            if (logger.isDebugEnabled()) {
                logger.debug("Full exception details:", e);
            }
        }
    }
}

