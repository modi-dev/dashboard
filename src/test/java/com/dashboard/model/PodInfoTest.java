package com.dashboard.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PodInfo model
 */
class PodInfoTest {

    @Test
    void testDefaultConstructor() {
        PodInfo podInfo = new PodInfo();
        
        assertNull(podInfo.getId());
        assertNull(podInfo.getPodName());
        assertNull(podInfo.getNamespace());
        assertNull(podInfo.getName());
        assertNull(podInfo.getVersion());
        assertNull(podInfo.getMsBranch());
        assertNull(podInfo.getConfigBranch());
        assertNull(podInfo.getGcOptions());
        assertNull(podInfo.getCreationDate());
        assertNull(podInfo.getPort());
        assertNull(podInfo.getCpuRequest());
        assertNull(podInfo.getMemoryRequest());
        assertNull(podInfo.getRestarts());
        assertNull(podInfo.getReadyTime());
        assertNull(podInfo.getDatabaseClusterUrl());
        assertNull(podInfo.getCreatedAt());
        assertNull(podInfo.getUpdatedAt());
        assertNull(podInfo.getK8sQueriedAt());
    }

    @Test
    void testParameterizedConstructor() {
        LocalDateTime creationDate = LocalDateTime.of(2024, 1, 15, 10, 30);
        PodInfo podInfo = new PodInfo(
            "my-app", 
            "1.0.0", 
            "main", 
            "config-main", 
            "-XX:+UseG1GC", 
            creationDate, 
            "8080", 
            "500m", 
            "512Mi"
        );
        
        assertEquals("my-app", podInfo.getName());
        assertEquals("1.0.0", podInfo.getVersion());
        assertEquals("main", podInfo.getMsBranch());
        assertEquals("config-main", podInfo.getConfigBranch());
        assertEquals("-XX:+UseG1GC", podInfo.getGcOptions());
        assertEquals(creationDate, podInfo.getCreationDate());
        assertEquals("8080", podInfo.getPort());
        assertEquals("500m", podInfo.getCpuRequest());
        assertEquals("512Mi", podInfo.getMemoryRequest());
    }

    @Test
    void testSettersAndGetters() {
        PodInfo podInfo = new PodInfo();
        LocalDateTime now = LocalDateTime.now();
        
        podInfo.setId(1L);
        podInfo.setPodName("my-app-12345");
        podInfo.setNamespace("default");
        podInfo.setName("my-app");
        podInfo.setVersion("1.0.0");
        podInfo.setMsBranch("main");
        podInfo.setConfigBranch("config-main");
        podInfo.setGcOptions("-XX:+UseG1GC");
        podInfo.setCreationDate(now);
        podInfo.setPort("8080,8443");
        podInfo.setCpuRequest("500m");
        podInfo.setMemoryRequest("512Mi");
        podInfo.setRestarts(3);
        podInfo.setReadyTime("45s");
        podInfo.setDatabaseClusterUrl("postgresql://db.example.com:5432/mydb");
        podInfo.setCreatedAt(now);
        podInfo.setUpdatedAt(now);
        podInfo.setK8sQueriedAt(now);
        
        assertEquals(1L, podInfo.getId());
        assertEquals("my-app-12345", podInfo.getPodName());
        assertEquals("default", podInfo.getNamespace());
        assertEquals("my-app", podInfo.getName());
        assertEquals("1.0.0", podInfo.getVersion());
        assertEquals("main", podInfo.getMsBranch());
        assertEquals("config-main", podInfo.getConfigBranch());
        assertEquals("-XX:+UseG1GC", podInfo.getGcOptions());
        assertEquals(now, podInfo.getCreationDate());
        assertEquals("8080,8443", podInfo.getPort());
        assertEquals("500m", podInfo.getCpuRequest());
        assertEquals("512Mi", podInfo.getMemoryRequest());
        assertEquals(3, podInfo.getRestarts());
        assertEquals("45s", podInfo.getReadyTime());
        assertEquals("postgresql://db.example.com:5432/mydb", podInfo.getDatabaseClusterUrl());
        assertEquals(now, podInfo.getCreatedAt());
        assertEquals(now, podInfo.getUpdatedAt());
        assertEquals(now, podInfo.getK8sQueriedAt());
    }

    @Test
    void testOnCreate() {
        PodInfo podInfo = new PodInfo();
        LocalDateTime before = LocalDateTime.now();
        
        // Simulate JPA PrePersist
        podInfo.onCreate();
        
        LocalDateTime after = LocalDateTime.now();
        
        assertNotNull(podInfo.getCreatedAt());
        assertNotNull(podInfo.getUpdatedAt());
        // Both should be set to approximately the same time
        assertTrue(!podInfo.getCreatedAt().isBefore(before));
        assertTrue(!podInfo.getCreatedAt().isAfter(after));
        assertTrue(!podInfo.getUpdatedAt().isBefore(before));
        assertTrue(!podInfo.getUpdatedAt().isAfter(after));
    }

    @Test
    void testOnUpdate() {
        PodInfo podInfo = new PodInfo();
        LocalDateTime oldTime = LocalDateTime.of(2024, 1, 1, 0, 0);
        podInfo.setUpdatedAt(oldTime);
        
        // Simulate JPA PreUpdate
        podInfo.onUpdate();
        
        assertNotNull(podInfo.getUpdatedAt());
        assertTrue(podInfo.getUpdatedAt().isAfter(oldTime));
    }

    @Test
    void testToString() {
        PodInfo podInfo = new PodInfo();
        podInfo.setId(1L);
        podInfo.setPodName("my-app-12345");
        podInfo.setNamespace("default");
        podInfo.setName("my-app");
        podInfo.setVersion("1.0.0");
        
        String str = podInfo.toString();
        
        assertTrue(str.contains("PodInfo"));
        assertTrue(str.contains("id=1"));
        assertTrue(str.contains("podName='my-app-12345'"));
        assertTrue(str.contains("namespace='default'"));
        assertTrue(str.contains("name='my-app'"));
        assertTrue(str.contains("version='1.0.0'"));
    }

    @Test
    void testToStringWithNullValues() {
        PodInfo podInfo = new PodInfo();
        
        String str = podInfo.toString();
        
        assertNotNull(str);
        assertTrue(str.contains("PodInfo"));
        assertTrue(str.contains("id=null"));
    }

    @Test
    void testNullValues() {
        PodInfo podInfo = new PodInfo("app", "1.0", "main", "config", "gc", LocalDateTime.now(), "80", "100m", "256Mi");
        
        podInfo.setName(null);
        podInfo.setVersion(null);
        podInfo.setMsBranch(null);
        podInfo.setConfigBranch(null);
        podInfo.setGcOptions(null);
        podInfo.setCreationDate(null);
        podInfo.setPort(null);
        podInfo.setCpuRequest(null);
        podInfo.setMemoryRequest(null);
        
        assertNull(podInfo.getName());
        assertNull(podInfo.getVersion());
        assertNull(podInfo.getMsBranch());
        assertNull(podInfo.getConfigBranch());
        assertNull(podInfo.getGcOptions());
        assertNull(podInfo.getCreationDate());
        assertNull(podInfo.getPort());
        assertNull(podInfo.getCpuRequest());
        assertNull(podInfo.getMemoryRequest());
    }
    
    @Test
    void testRestartsWithZero() {
        PodInfo podInfo = new PodInfo();
        podInfo.setRestarts(0);
        
        assertEquals(0, podInfo.getRestarts());
    }
    
    @Test
    void testRestartsWithHighValue() {
        PodInfo podInfo = new PodInfo();
        podInfo.setRestarts(999);
        
        assertEquals(999, podInfo.getRestarts());
    }
}
