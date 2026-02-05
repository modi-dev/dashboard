package com.dashboard.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ModelCoverageTest {

    @Test
    void serverConstructorSetsDefaultsAndFields() {
        Server server = new Server("API", "example.com", ServerType.POSTGRES);

        assertEquals("API", server.getName());
        assertEquals("example.com", server.getUrl());
        assertEquals(ServerType.POSTGRES, server.getType());
        assertEquals(ServerStatus.UNKNOWN, server.getStatus());
        assertNotNull(server.getCreatedAt());
        assertNotNull(server.getUpdatedAt());
    }

    @Test
    void serverLifecycleMethodsUpdateTimestamps() {
        Server server = new Server();

        LocalDateTime beforeCreate = LocalDateTime.now().minusSeconds(1);
        server.onCreate();

        assertNotNull(server.getCreatedAt());
        assertNotNull(server.getUpdatedAt());
        assertFalse(server.getCreatedAt().isBefore(beforeCreate));

        LocalDateTime oldUpdate = LocalDateTime.now().minusHours(1);
        server.setUpdatedAt(oldUpdate);
        server.onUpdate();
        assertTrue(server.getUpdatedAt().isAfter(oldUpdate));

        server.setHealthcheck("/health");
        server.setVersion("1.2.3");
        server.setMetricsEndpoint("/metrics");
        server.setVersionRegex("version=(.*)");
        server.setLastChecked(LocalDateTime.now());
        server.setStatus(ServerStatus.ONLINE);

        assertEquals("/health", server.getHealthcheck());
        assertEquals("1.2.3", server.getVersion());
        assertEquals("/metrics", server.getMetricsEndpoint());
        assertEquals("version=(.*)", server.getVersionRegex());
        assertEquals(ServerStatus.ONLINE, server.getStatus());
    }

    @Test
    void podInfoLifecycleAndToString() {
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        PodInfo pod = new PodInfo(
            "app",
            "1.0",
            "main",
            "config",
            "-XX:+UseG1GC",
            createdAt,
            "8080",
            "100m",
            "256Mi"
        );

        pod.setPodName("app-123");
        pod.setNamespace("default");
        pod.setReadyTime("15s");
        pod.setRestarts(2);
        pod.setDatabaseClusterUrl("jdbc:postgresql://db");
        pod.setK8sQueriedAt(LocalDateTime.now());

        pod.onCreate();
        assertNotNull(pod.getCreatedAt());
        assertNotNull(pod.getUpdatedAt());

        LocalDateTime oldUpdate = LocalDateTime.now().minusHours(2);
        pod.setUpdatedAt(oldUpdate);
        pod.onUpdate();
        assertTrue(pod.getUpdatedAt().isAfter(oldUpdate));

        String description = pod.toString();
        assertTrue(description.contains("podName='app-123'"));
        assertTrue(description.contains("namespace='default'"));
        assertTrue(description.contains("version='1.0'"));
    }

    @Test
    void kubernetesClusterInfoLifecycleAndGetters() {
        KubernetesClusterInfo info = new KubernetesClusterInfo();
        info.setNamespace("default");
        info.setKubernetesVersion("1.27");
        info.setQuotaCpuUsed("100m");
        info.setQuotaCpuHard("1");
        info.setQuotaMemoryUsed("512Mi");
        info.setQuotaMemoryHard("1Gi");
        info.setQuotaPodsUsed("10");
        info.setQuotaPodsHard("20");
        info.setQuotaConfigmapsUsed("5");
        info.setQuotaConfigmapsHard("10");
        info.setQuotaSecretsUsed("1");
        info.setQuotaSecretsHard("5");

        info.onCreate();
        assertNotNull(info.getCreatedAt());
        assertNotNull(info.getUpdatedAt());

        LocalDateTime oldUpdate = LocalDateTime.now().minusHours(3);
        info.setUpdatedAt(oldUpdate);
        info.onUpdate();
        assertTrue(info.getUpdatedAt().isAfter(oldUpdate));

        assertEquals("default", info.getNamespace());
        assertEquals("1.27", info.getKubernetesVersion());
        assertEquals("100m", info.getQuotaCpuUsed());
        assertEquals("1", info.getQuotaCpuHard());
        assertEquals("512Mi", info.getQuotaMemoryUsed());
        assertEquals("1Gi", info.getQuotaMemoryHard());
        assertEquals("10", info.getQuotaPodsUsed());
        assertEquals("20", info.getQuotaPodsHard());
        assertEquals("5", info.getQuotaConfigmapsUsed());
        assertEquals("10", info.getQuotaConfigmapsHard());
        assertEquals("1", info.getQuotaSecretsUsed());
        assertEquals("5", info.getQuotaSecretsHard());
    }

    @Test
    void serverStatusExposesValue() {
        assertEquals("online", ServerStatus.ONLINE.getValue());
        assertEquals("online", ServerStatus.ONLINE.toString());
        assertEquals("offline", ServerStatus.OFFLINE.getValue());
        assertEquals("unknown", ServerStatus.UNKNOWN.getValue());
    }
}
