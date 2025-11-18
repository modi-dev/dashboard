package com.dashboard.service;

import com.dashboard.config.KubernetesConfig;
import com.dashboard.model.KubernetesClusterInfo;
import com.dashboard.repository.KubernetesClusterInfoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Сервис для синхронизации информации о Kubernetes кластере в БД
 * 
 * Обновляет версию Kubernetes и namespace в фоне
 */
@Service
public class KubernetesClusterInfoSyncService {
    
    private static final Logger logger = LoggerFactory.getLogger(KubernetesClusterInfoSyncService.class);
    
    @Autowired
    private KubernetesService kubernetesService;
    
    @Autowired
    private KubernetesClusterInfoRepository clusterInfoRepository;
    
    @Autowired
    private KubernetesConfig kubernetesConfig;
    
    /**
     * Синхронизирует информацию о кластере из Kubernetes в БД
     * 
     * Получает версию Kubernetes через kubectl и сохраняет в БД
     * Namespace берется из конфигурации
     * 
     * @return true если синхронизация прошла успешно
     */
    @Transactional
    public boolean syncClusterInfo() {
        try {
            if (!kubernetesConfig.isEnabled()) {
                logger.debug("Kubernetes интеграция отключена, пропускаем синхронизацию информации о кластере");
                return false;
            }
            
            logger.debug("Начало синхронизации информации о Kubernetes кластере");
            
            // Получаем или создаем запись о кластере
            KubernetesClusterInfo clusterInfo = clusterInfoRepository.findFirstByOrderByIdAsc()
                .orElse(new KubernetesClusterInfo());
            
            // Обновляем namespace из конфигурации
            String namespace = kubernetesConfig.getNamespace();
            clusterInfo.setNamespace(namespace);
            
            // Получаем версию Kubernetes напрямую через kubectl (только для синхронизации в БД)
            try {
                String version = kubernetesService.getKubernetesVersion();
                if (version != null && !version.equals("Неизвестно")) {
                    clusterInfo.setKubernetesVersion(version);
                    logger.debug("Версия Kubernetes обновлена: {}", version);
                } else {
                    logger.debug("Не удалось получить версию Kubernetes, оставляем текущее значение");
                }
            } catch (Exception e) {
                logger.warn("Ошибка при получении версии Kubernetes: {}", e.getMessage());
                // Не прерываем синхронизацию, просто не обновляем версию
            }
            
            // Устанавливаем время последнего запроса к kubectl
            clusterInfo.setK8sQueriedAt(LocalDateTime.now());
            
            // Сохраняем в БД
            clusterInfoRepository.save(clusterInfo);
            
            logger.info("Информация о Kubernetes кластере синхронизирована: namespace={}, version={}", 
                       namespace, clusterInfo.getKubernetesVersion());
            
            return true;
            
        } catch (Exception e) {
            logger.error("Ошибка при синхронизации информации о кластере: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Получает информацию о кластере из БД
     * 
     * @return информация о кластере или null если не найдена
     */
    public KubernetesClusterInfo getClusterInfo() {
        return clusterInfoRepository.findFirstByOrderByIdAsc().orElse(null);
    }
    
    /**
     * Получает версию Kubernetes из БД
     * 
     * @return версия Kubernetes или "Неизвестно" если не найдена
     */
    public String getKubernetesVersion() {
        KubernetesClusterInfo info = getClusterInfo();
        if (info != null && info.getKubernetesVersion() != null) {
            return info.getKubernetesVersion();
        }
        return "Неизвестно";
    }
    
    /**
     * Получает namespace из БД
     * 
     * @return namespace или значение из конфигурации если не найдено в БД
     */
    public String getNamespace() {
        KubernetesClusterInfo info = getClusterInfo();
        if (info != null && info.getNamespace() != null) {
            return info.getNamespace();
        }
        // Fallback на конфигурацию
        return kubernetesConfig.getNamespace();
    }
}

