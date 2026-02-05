package com.dashboard.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PodInfoTest {

    @Test
    void defaultConstructor_ShouldCreateEmptyPod() {
        PodInfo pod = new PodInfo();
        assertNull(pod.getName());
        assertNull(pod.getVersion());
        assertNull(pod.getId());
    }

    @Test
    void parameterizedConstructor_ShouldSetFields() {
        LocalDateTime now = LocalDateTime.now();
        PodInfo pod = new PodInfo("app", "1.0", "main", "config-main",
                "-XX:+UseG1GC", now, "8080", "100m", "256Mi");

        assertEquals("app", pod.getName());
        assertEquals("1.0", pod.getVersion());
        assertEquals("main", pod.getMsBranch());
        assertEquals("config-main", pod.getConfigBranch());
        assertEquals("-XX:+UseG1GC", pod.getGcOptions());
        assertEquals(now, pod.getCreationDate());
        assertEquals("8080", pod.getPort());
        assertEquals("100m", pod.getCpuRequest());
        assertEquals("256Mi", pod.getMemoryRequest());
    }

    @Test
    void settersAndGetters_ShouldWork() {
        PodInfo pod = new PodInfo();
        LocalDateTime now = LocalDateTime.now();

        pod.setId(1L);
        pod.setPodName("my-pod-123");
        pod.setNamespace("default");
        pod.setName("my-app");
        pod.setVersion("2.0.0");
        pod.setMsBranch("feature/test");
        pod.setConfigBranch("main");
        pod.setGcOptions("-XX:+UseZGC");
        pod.setCreationDate(now);
        pod.setPort("8080, 9090");
        pod.setCpuRequest("200m");
        pod.setMemoryRequest("512Mi");
        pod.setRestarts(3);
        pod.setReadyTime("45s");
        pod.setDatabaseClusterUrl("jdbc:postgresql://host:5432/db");
        pod.setCreatedAt(now);
        pod.setUpdatedAt(now);
        pod.setK8sQueriedAt(now);

        assertEquals(1L, pod.getId());
        assertEquals("my-pod-123", pod.getPodName());
        assertEquals("default", pod.getNamespace());
        assertEquals("my-app", pod.getName());
        assertEquals("2.0.0", pod.getVersion());
        assertEquals("feature/test", pod.getMsBranch());
        assertEquals("main", pod.getConfigBranch());
        assertEquals("-XX:+UseZGC", pod.getGcOptions());
        assertEquals(now, pod.getCreationDate());
        assertEquals("8080, 9090", pod.getPort());
        assertEquals("200m", pod.getCpuRequest());
        assertEquals("512Mi", pod.getMemoryRequest());
        assertEquals(3, pod.getRestarts());
        assertEquals("45s", pod.getReadyTime());
        assertEquals("jdbc:postgresql://host:5432/db", pod.getDatabaseClusterUrl());
        assertEquals(now, pod.getCreatedAt());
        assertEquals(now, pod.getUpdatedAt());
        assertEquals(now, pod.getK8sQueriedAt());
    }

    @Test
    void toString_ShouldContainFields() {
        PodInfo pod = new PodInfo();
        pod.setName("test-app");
        pod.setPodName("test-pod-123");
        pod.setNamespace("default");

        String str = pod.toString();
        assertNotNull(str);
        assertTrue(str.contains("test-app"));
        assertTrue(str.contains("test-pod-123"));
        assertTrue(str.contains("default"));
    }

    @Test
    void onCreate_ShouldSetTimestamps() {
        PodInfo pod = new PodInfo();
        pod.onCreate();
        assertNotNull(pod.getCreatedAt());
        assertNotNull(pod.getUpdatedAt());
    }

    @Test
    void onUpdate_ShouldUpdateTimestamp() {
        PodInfo pod = new PodInfo();
        pod.onCreate();
        LocalDateTime firstUpdate = pod.getUpdatedAt();

        pod.onUpdate();

        assertNotNull(pod.getUpdatedAt());
    }
}
