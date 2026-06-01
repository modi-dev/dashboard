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
     * Обновляет версию Kubernetes, namespace и quota в фоне
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
     * Получает версию Kubernetes и quota через kubectl и сохраняет в БД
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
            
            // Получаем информацию о quota для namespace (одновременно с версией)
            try {
                KubernetesService.NamespaceQuota quota = kubernetesService.getNamespaceQuota();
                if (quota != null) {
                    clusterInfo.setQuotaCpuUsed(quota.cpuUsed);
                    clusterInfo.setQuotaCpuHard(quota.cpuHard);
                    clusterInfo.setQuotaMemoryUsed(quota.memoryUsed);
                    clusterInfo.setQuotaMemoryHard(quota.memoryHard);
                    clusterInfo.setQuotaPodsUsed(quota.podsUsed);
                    clusterInfo.setQuotaPodsHard(quota.podsHard);
                    clusterInfo.setQuotaConfigmapsUsed(quota.configmapsUsed);
                    clusterInfo.setQuotaConfigmapsHard(quota.configmapsHard);
                    clusterInfo.setQuotaSecretsUsed(quota.secretsUsed);
                    clusterInfo.setQuotaSecretsHard(quota.secretsHard);
                    logger.debug("Quota обновлены: CPU={}/{}, Memory={}/{}, Pods={}/{}, ConfigMaps={}/{}, Secrets={}/{}",
                               quota.cpuUsed, quota.cpuHard, quota.memoryUsed, quota.memoryHard,
                               quota.podsUsed, quota.podsHard, quota.configmapsUsed, quota.configmapsHard,
                               quota.secretsUsed, quota.secretsHard);
                } else {
                    logger.debug("Не удалось получить quota, оставляем текущие значения");
                }
            } catch (Exception e) {
                logger.warn("Ошибка при получении quota: {}", e.getMessage());
                // Не прерываем синхронизацию, просто не обновляем quota
            }
            
            // Устанавливаем время последнего запроса к kubectl
            clusterInfo.setK8sQueriedAt(LocalDateTime.now());
            
            // Сохраняем в БД
            clusterInfoRepository.save(clusterInfo);
            
            logger.info("Информация о Kubernetes кластере синхронизирована: namespace={}, version={}, quota={}/{}/{}/{}/{}/{}", 
                       namespace, clusterInfo.getKubernetesVersion(),
                       clusterInfo.getQuotaCpuUsed(), clusterInfo.getQuotaCpuHard(),
                       clusterInfo.getQuotaMemoryUsed(), clusterInfo.getQuotaMemoryHard(),
                       clusterInfo.getQuotaPodsUsed(), clusterInfo.getQuotaPodsHard());
            
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
    
    /**
     * Получает информацию о quota CPU из БД
     * 
     * @return строка в формате "used/hard" или null если не найдено
     */
    public String getQuotaCpu() {
        KubernetesClusterInfo info = getClusterInfo();
        if (info != null && info.getQuotaCpuUsed() != null && info.getQuotaCpuHard() != null) {
            return info.getQuotaCpuUsed() + "/" + info.getQuotaCpuHard();
        }
        return null;
    }
    
    /**
     * Получает информацию о quota Memory из БД
     * 
     * @return строка в формате "used/hard" или null если не найдено
     */
    public String getQuotaMemory() {
        KubernetesClusterInfo info = getClusterInfo();
        if (info != null && info.getQuotaMemoryUsed() != null && info.getQuotaMemoryHard() != null) {
            return info.getQuotaMemoryUsed() + "/" + info.getQuotaMemoryHard();
        }
        return null;
    }
    
    /**
     * Получает информацию о quota Pods из БД
     * 
     * @return строка в формате "used/hard" или null если не найдено
     */
    public String getQuotaPods() {
        KubernetesClusterInfo info = getClusterInfo();
        if (info != null && info.getQuotaPodsUsed() != null && info.getQuotaPodsHard() != null) {
            return info.getQuotaPodsUsed() + "/" + info.getQuotaPodsHard();
        }
        return null;
    }
    
    /**
     * Получает информацию о quota ConfigMaps из БД
     * 
     * @return строка в формате "used/hard" или null если не найдено
     */
    public String getQuotaConfigmaps() {
        KubernetesClusterInfo info = getClusterInfo();
        if (info != null && info.getQuotaConfigmapsUsed() != null && info.getQuotaConfigmapsHard() != null) {
            return info.getQuotaConfigmapsUsed() + "/" + info.getQuotaConfigmapsHard();
        }
        return null;
    }
    
    /**
     * Получает информацию о quota Secrets из БД
     * 
     * @return строка в формате "used/hard" или null если не найдено
     */
    public String getQuotaSecrets() {
        KubernetesClusterInfo info = getClusterInfo();
        if (info != null && info.getQuotaSecretsUsed() != null && info.getQuotaSecretsHard() != null) {
            return info.getQuotaSecretsUsed() + "/" + info.getQuotaSecretsHard();
        }
        return null;
    }
}

