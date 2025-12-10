package com.dashboard.repository;

import com.dashboard.model.KubernetesClusterInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для работы с информацией о Kubernetes кластере
 */
@Repository
public interface KubernetesClusterInfoRepository extends JpaRepository<KubernetesClusterInfo, Long> {
    
    /**
     * Находит единственную запись о кластере (если есть)
     * В системе должна быть только одна запись
     */
    Optional<KubernetesClusterInfo> findFirstByOrderByIdAsc();
}

