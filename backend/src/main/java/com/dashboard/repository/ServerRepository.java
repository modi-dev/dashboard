package com.dashboard.repository;

import com.dashboard.model.Server;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServerRepository extends JpaRepository<Server, Long> {
    
    Optional<Server> findByUrl(String url);
    
    List<Server> findByStatus(com.dashboard.model.ServerStatus status);
    
    @Query("SELECT s FROM Server s ORDER BY s.createdAt DESC")
    List<Server> findAllOrderByCreatedAtDesc();
    
    @Query("SELECT COUNT(s) FROM Server s WHERE s.status = 'ONLINE'")
    long countOnlineServers();
    
    @Query("SELECT COUNT(s) FROM Server s WHERE s.status = 'OFFLINE'")
    long countOfflineServers();
}

