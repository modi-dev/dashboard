package com.dashboard.service;

import com.dashboard.config.KubernetesConfig;
import com.dashboard.model.PodInfo;
import com.dashboard.repository.PodRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Сервис для синхронизации информации о подах из Kubernetes в БД
 * 
 * Выполняет полную синхронизацию:
 * - Получает актуальные поды через KubernetesService
 * - Обновляет существующие поды в БД
 * - Добавляет новые поды
 * - Удаляет поды, которые больше не существуют в кластере
 */
@Service
public class KubernetesPodsSyncService {
    
    private static final Logger logger = LoggerFactory.getLogger(KubernetesPodsSyncService.class);
    
    @Autowired
    private KubernetesService kubernetesService;
    
    @Autowired
    private PodRepository podRepository;
    
    @Autowired
    private KubernetesConfig kubernetesConfig;
    
    /**
     * Синхронизирует поды из Kubernetes в БД
     * 
     * Алгоритм:
     * 1. Получает актуальные поды через kubectl
     * 2. Для каждого пода устанавливает namespace
     * 3. Обновляет существующие поды или создает новые
     * 4. Удаляет поды, которых больше нет в кластере
     * 
     * @return количество синхронизированных подов
     */
    @Transactional
    public int syncPods() {
        if (!kubernetesConfig.isEnabled()) {
            logger.debug("Kubernetes интеграция отключена, пропускаем синхронизацию");
            return 0;
        }
        
        String namespace = kubernetesConfig.getNamespace();
        logger.info("Начало синхронизации подов для namespace: {}", namespace);
        
        try {
            // Получаем актуальные поды из Kubernetes
            List<PodInfo> currentPods = kubernetesService.getRunningPods();
            LocalDateTime queryTime = LocalDateTime.now();
            
            // Устанавливаем namespace и время запроса для всех подов
            currentPods.forEach(pod -> {
                pod.setNamespace(namespace);
                pod.setK8sQueriedAt(queryTime);
            });
            
            logger.info("Получено {} подов из Kubernetes", currentPods.size());
            
            // Получаем существующие поды из БД
            List<PodInfo> existingPods = podRepository.findByNamespace(namespace);
            Map<String, PodInfo> existingPodsMap = existingPods.stream()
                    .collect(Collectors.toMap(
                            p -> p.getPodName(),
                            p -> p,
                            (p1, p2) -> p1 // В случае дубликатов берем первый
                    ));
            
            int updatedCount = 0;
            int createdCount = 0;
            
            // Обновляем или создаем поды
            for (PodInfo currentPod : currentPods) {
                if (currentPod.getPodName() == null || currentPod.getPodName().isEmpty()) {
                    logger.warn("Пропускаем под без имени: {}", currentPod);
                    continue;
                }
                
                Optional<PodInfo> existingOpt = podRepository.findByPodNameAndNamespace(
                        currentPod.getPodName(), namespace);
                
                if (existingOpt.isPresent()) {
                    // Обновляем существующий под
                    PodInfo existing = existingOpt.get();
                    updatePodInfo(existing, currentPod, queryTime);
                    podRepository.save(existing);
                    updatedCount++;
                } else {
                    // Создаем новый под
                    currentPod.setK8sQueriedAt(queryTime);
                    podRepository.save(currentPod);
                    createdCount++;
                }
                
                // Удаляем из карты, чтобы потом удалить те, которых нет в текущем списке
                existingPodsMap.remove(currentPod.getPodName());
            }
            
            // Удаляем поды, которых больше нет в кластере
            int deletedCount = 0;
            for (PodInfo podToDelete : existingPodsMap.values()) {
                podRepository.delete(podToDelete);
                deletedCount++;
            }
            
            logger.info("Синхронизация завершена: создано {}, обновлено {}, удалено {} подов", 
                       createdCount, updatedCount, deletedCount);
            
            return currentPods.size();
            
        } catch (Exception e) {
            logger.error("Ошибка при синхронизации подов: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Обновляет поля существующего пода данными из текущего пода
     * Сохраняет ID и временные метки создания
     */
    private void updatePodInfo(PodInfo existing, PodInfo current, LocalDateTime queryTime) {
        existing.setName(current.getName());
        existing.setVersion(current.getVersion());
        existing.setMsBranch(current.getMsBranch());
        existing.setConfigBranch(current.getConfigBranch());
        existing.setGcOptions(current.getGcOptions());
        existing.setCreationDate(current.getCreationDate());
        existing.setPort(current.getPort());
        existing.setCpuRequest(current.getCpuRequest());
        existing.setMemoryRequest(current.getMemoryRequest());
        existing.setRestarts(current.getRestarts());
        existing.setReadyTime(current.getReadyTime());
        existing.setNamespace(current.getNamespace());
        existing.setK8sQueriedAt(queryTime);
        // updatedAt будет установлен автоматически через @PreUpdate
    }
    
    /**
     * Очищает устаревшие поды (которые не обновлялись более указанного времени)
     * 
     * @param olderThanMinutes удалить поды, которые не обновлялись более N минут
     * @return количество удаленных подов
     */
    @Transactional
    public int cleanupStalePods(int olderThanMinutes) {
        if (!kubernetesConfig.isEnabled()) {
            return 0;
        }
        
        String namespace = kubernetesConfig.getNamespace();
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(olderThanMinutes);
        
        List<PodInfo> stalePods = podRepository.findByNamespace(namespace).stream()
                .filter(pod -> {
                    LocalDateTime referenceTime = pod.getK8sQueriedAt() != null ? pod.getK8sQueriedAt() : pod.getUpdatedAt();
                    return referenceTime != null && referenceTime.isBefore(cutoffTime);
                })
                .collect(Collectors.toList());
        
        if (!stalePods.isEmpty()) {
            podRepository.deleteAll(stalePods);
            logger.info("Удалено {} устаревших подов (не обновлялись более {} минут)", 
                       stalePods.size(), olderThanMinutes);
        }
        
        return stalePods.size();
    }
}

