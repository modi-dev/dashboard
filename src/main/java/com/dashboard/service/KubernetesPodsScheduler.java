package com.dashboard.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Планировщик для периодической синхронизации подов из Kubernetes в БД
 * 
 * Включен только когда scheduling включен и Kubernetes интеграция активна.
 * Интервал синхронизации настраивается в application.yml (kubernetes.pods.sync-interval).
 */
@Service
@ConditionalOnProperty(value = "spring.task.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class KubernetesPodsScheduler {

    private static final Logger logger = LoggerFactory.getLogger(KubernetesPodsScheduler.class);

    private final KubernetesPodsSyncService podsSyncService;
    private final long intervalMs;

    public KubernetesPodsScheduler(KubernetesPodsSyncService podsSyncService,
                                  @Value("${kubernetes.pods.sync-interval:1}") double intervalMinutes) {
        this.podsSyncService = podsSyncService;
        // Convert minutes to milliseconds
        this.intervalMs = (long) (intervalMinutes * 60 * 1000);
        logger.info("Kubernetes pods sync scheduler initialized with interval: {} minutes ({} ms)", 
                   intervalMinutes, intervalMs);
    }

    /**
     * Периодически синхронизирует поды из Kubernetes в БД
     * 
     * Использует SpEL выражение для вычисления интервала из конфигурации
     */
    @Scheduled(fixedRateString = "#{${kubernetes.pods.sync-interval:1} * 60 * 1000}")
    public void syncPods() {
        try {
            int syncedCount = podsSyncService.syncPods();
            logger.debug("Scheduled pods sync completed: {} pods synchronized", syncedCount);
        } catch (Exception e) {
            logger.warn("Scheduled pods sync failed: {}", e.getMessage());
            if (logger.isDebugEnabled()) {
                logger.debug("Full exception details:", e);
            }
        }
    }
}

