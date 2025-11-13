package com.dashboard.config;

import com.dashboard.service.KubernetesPodsSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Инициализатор для загрузки подов при старте приложения
 * 
 * Выполняет первоначальную синхронизацию подов из Kubernetes в БД,
 * чтобы при первом открытии страницы данные уже были доступны.
 * 
 * Запускается только если Kubernetes интеграция включена.
 */
@Component
@Order(1) // Запускается раньше других CommandLineRunner
@ConditionalOnProperty(value = "kubernetes.enabled", havingValue = "true", matchIfMissing = true)
public class PodsInitializer implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(PodsInitializer.class);
    
    private final KubernetesPodsSyncService podsSyncService;
    
    public PodsInitializer(KubernetesPodsSyncService podsSyncService) {
        this.podsSyncService = podsSyncService;
    }
    
    @Override
    public void run(String... args) {
        try {
            logger.info("Инициализация: загрузка подов из Kubernetes в БД...");
            int syncedCount = podsSyncService.syncPods();
            logger.info("Инициализация завершена: загружено {} подов", syncedCount);
        } catch (Exception e) {
            logger.warn("Ошибка при инициализации подов: {}", e.getMessage());
            if (logger.isDebugEnabled()) {
                logger.debug("Полные детали ошибки:", e);
            }
            // Не прерываем запуск приложения, если инициализация не удалась
            // Scheduler продолжит попытки синхронизации
        }
    }
}

