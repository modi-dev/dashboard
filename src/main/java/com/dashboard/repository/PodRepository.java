package com.dashboard.repository;

import com.dashboard.model.PodInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PodRepository extends JpaRepository<PodInfo, Long> {
    
    /**
     * Находит под по имени и namespace
     */
    Optional<PodInfo> findByPodNameAndNamespace(String podName, String namespace);
    
    /**
     * Находит все поды в указанном namespace
     */
    List<PodInfo> findByNamespace(String namespace);
    
    /**
     * Удаляет все поды в указанном namespace
     */
    @Modifying
    @Query("DELETE FROM PodInfo p WHERE p.namespace = :namespace")
    void deleteByNamespace(@Param("namespace") String namespace);
    
    /**
     * Удаляет поды, которые не были обновлены после указанного времени
     * (используется для очистки устаревших подов)
     */
    @Modifying
    @Query("DELETE FROM PodInfo p WHERE p.namespace = :namespace AND p.updatedAt < :beforeTime")
    void deleteStalePods(@Param("namespace") String namespace, @Param("beforeTime") LocalDateTime beforeTime);
    
    /**
     * Подсчитывает количество подов в указанном namespace
     */
    long countByNamespace(String namespace);
    
    /**
     * Находит все поды, отсортированные по имени
     */
    @Query("SELECT p FROM PodInfo p WHERE p.namespace = :namespace ORDER BY p.name, p.podName")
    List<PodInfo> findByNamespaceOrderByName(@Param("namespace") String namespace);
}

