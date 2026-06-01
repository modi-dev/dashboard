package com.dashboard.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class KubernetesClusterInfoTest {

    @Test
    void defaultConstructor_ShouldCreateEmptyInstance() {
        KubernetesClusterInfo info = new KubernetesClusterInfo();
        assertNull(info.getId());
        assertNull(info.getKubernetesVersion());
        assertNull(info.getNamespace());
    }

    @Test
    void settersAndGetters_ShouldWork() {
        KubernetesClusterInfo info = new KubernetesClusterInfo();
        LocalDateTime now = LocalDateTime.now();

        info.setId(1L);
        info.setKubernetesVersion("v1.28.0");
        info.setNamespace("production");
        info.setCreatedAt(now);
        info.setUpdatedAt(now);
        info.setK8sQueriedAt(now);
        info.setQuotaCpuUsed("2");
        info.setQuotaCpuHard("10");
        info.setQuotaMemoryUsed("4Gi");
        info.setQuotaMemoryHard("20Gi");
        info.setQuotaPodsUsed("5");
        info.setQuotaPodsHard("50");
        info.setQuotaConfigmapsUsed("10");
        info.setQuotaConfigmapsHard("100");
        info.setQuotaSecretsUsed("8");
        info.setQuotaSecretsHard("50");

        assertEquals(1L, info.getId());
        assertEquals("v1.28.0", info.getKubernetesVersion());
        assertEquals("production", info.getNamespace());
        assertEquals(now, info.getCreatedAt());
        assertEquals(now, info.getUpdatedAt());
        assertEquals(now, info.getK8sQueriedAt());
        assertEquals("2", info.getQuotaCpuUsed());
        assertEquals("10", info.getQuotaCpuHard());
        assertEquals("4Gi", info.getQuotaMemoryUsed());
        assertEquals("20Gi", info.getQuotaMemoryHard());
        assertEquals("5", info.getQuotaPodsUsed());
        assertEquals("50", info.getQuotaPodsHard());
        assertEquals("10", info.getQuotaConfigmapsUsed());
        assertEquals("100", info.getQuotaConfigmapsHard());
        assertEquals("8", info.getQuotaSecretsUsed());
        assertEquals("50", info.getQuotaSecretsHard());
    }

    @Test
    void onCreate_ShouldSetTimestamps() {
        KubernetesClusterInfo info = new KubernetesClusterInfo();
        info.onCreate();
        assertNotNull(info.getCreatedAt());
        assertNotNull(info.getUpdatedAt());
    }

    @Test
    void onUpdate_ShouldUpdateTimestamp() {
        KubernetesClusterInfo info = new KubernetesClusterInfo();
        info.onCreate();

        info.onUpdate();

        assertNotNull(info.getUpdatedAt());
    }
}
