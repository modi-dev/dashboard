package com.dashboard.service;

import com.dashboard.config.KubernetesConfig;
import com.dashboard.model.PodInfo;
import com.dashboard.repository.PodRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KubernetesPodsSyncServiceAdditionalTest {

    @Mock
    private KubernetesService kubernetesService;

    @Mock
    private PodRepository podRepository;

    @Mock
    private KubernetesConfig kubernetesConfig;

    @Mock
    private KubernetesClusterInfoSyncService clusterInfoSyncService;

    @InjectMocks
    private KubernetesPodsSyncService podsSyncService;

    @BeforeEach
    void setUp() {
        lenient().when(kubernetesConfig.isEnabled()).thenReturn(true);
        lenient().when(kubernetesConfig.getNamespace()).thenReturn("test-ns");
    }

    @Test
    void syncPods_ShouldReturnZero_WhenDisabled() {
        when(kubernetesConfig.isEnabled()).thenReturn(false);

        int result = podsSyncService.syncPods();

        assertEquals(0, result);
        verify(kubernetesService, never()).getRunningPods();
    }

    @Test
    void syncPods_ShouldCreateNewPods() {
        PodInfo pod = new PodInfo();
        pod.setName("app");
        pod.setPodName("app-123");

        when(kubernetesService.getRunningPods()).thenReturn(List.of(pod));
        when(podRepository.findByNamespace("test-ns")).thenReturn(Collections.emptyList());
        when(podRepository.findByPodNameAndNamespace("app-123", "test-ns")).thenReturn(Optional.empty());
        when(podRepository.save(any(PodInfo.class))).thenAnswer(inv -> inv.getArgument(0));
        when(clusterInfoSyncService.syncClusterInfo()).thenReturn(true);

        int result = podsSyncService.syncPods();

        assertEquals(1, result);
        verify(podRepository).save(any(PodInfo.class));
    }

    @Test
    void syncPods_ShouldUpdateExistingPods() {
        PodInfo currentPod = new PodInfo();
        currentPod.setName("app");
        currentPod.setPodName("app-123");
        currentPod.setVersion("2.0");

        PodInfo existingPod = new PodInfo();
        existingPod.setId(1L);
        existingPod.setName("app");
        existingPod.setPodName("app-123");
        existingPod.setVersion("1.0");
        existingPod.setNamespace("test-ns");

        when(kubernetesService.getRunningPods()).thenReturn(List.of(currentPod));
        when(podRepository.findByNamespace("test-ns")).thenReturn(List.of(existingPod));
        when(podRepository.findByPodNameAndNamespace("app-123", "test-ns")).thenReturn(Optional.of(existingPod));
        when(podRepository.save(any(PodInfo.class))).thenAnswer(inv -> inv.getArgument(0));
        when(clusterInfoSyncService.syncClusterInfo()).thenReturn(true);

        int result = podsSyncService.syncPods();

        assertEquals(1, result);
        assertEquals("2.0", existingPod.getVersion());
    }

    @Test
    void syncPods_ShouldDeleteRemovedPods() {
        PodInfo existingPod = new PodInfo();
        existingPod.setId(1L);
        existingPod.setPodName("old-pod-123");
        existingPod.setNamespace("test-ns");

        when(kubernetesService.getRunningPods()).thenReturn(Collections.emptyList());
        when(podRepository.findByNamespace("test-ns")).thenReturn(List.of(existingPod));
        when(clusterInfoSyncService.syncClusterInfo()).thenReturn(true);

        int result = podsSyncService.syncPods();

        assertEquals(0, result);
        verify(podRepository).delete(existingPod);
    }

    @Test
    void syncPods_ShouldSkipPodsWithoutPodName() {
        PodInfo pod = new PodInfo();
        pod.setName("app");
        pod.setPodName(null);

        when(kubernetesService.getRunningPods()).thenReturn(List.of(pod));
        when(podRepository.findByNamespace("test-ns")).thenReturn(Collections.emptyList());
        when(clusterInfoSyncService.syncClusterInfo()).thenReturn(true);

        int result = podsSyncService.syncPods();

        assertEquals(1, result);
        verify(podRepository, never()).save(any(PodInfo.class));
    }

    @Test
    void syncPods_ShouldSkipPodsWithEmptyPodName() {
        PodInfo pod = new PodInfo();
        pod.setName("app");
        pod.setPodName("");

        when(kubernetesService.getRunningPods()).thenReturn(List.of(pod));
        when(podRepository.findByNamespace("test-ns")).thenReturn(Collections.emptyList());
        when(clusterInfoSyncService.syncClusterInfo()).thenReturn(true);

        int result = podsSyncService.syncPods();

        assertEquals(1, result);
    }

    @Test
    void syncPods_ShouldHandleClusterInfoSyncFailure() {
        when(kubernetesService.getRunningPods()).thenReturn(Collections.emptyList());
        when(podRepository.findByNamespace("test-ns")).thenReturn(Collections.emptyList());
        when(clusterInfoSyncService.syncClusterInfo()).thenThrow(new RuntimeException("sync error"));

        int result = podsSyncService.syncPods();

        assertEquals(0, result);
    }

    @Test
    void syncPods_ShouldThrow_WhenGetRunningPodsThrows() {
        when(kubernetesService.getRunningPods()).thenThrow(new RuntimeException("kubectl error"));

        assertThrows(RuntimeException.class, () -> podsSyncService.syncPods());
    }

    @Test
    void syncPods_ShouldSetDatabaseClusterUrl() {
        PodInfo pod = new PodInfo();
        pod.setName("my-service");
        pod.setPodName("my-service-abc");

        when(kubernetesService.getRunningPods()).thenReturn(List.of(pod));
        when(kubernetesService.getDatabaseClusterUrlFromSecrets("my-service"))
                .thenReturn("jdbc:postgresql://host:5432/db");
        when(podRepository.findByNamespace("test-ns")).thenReturn(Collections.emptyList());
        when(podRepository.findByPodNameAndNamespace("my-service-abc", "test-ns")).thenReturn(Optional.empty());
        when(podRepository.save(any(PodInfo.class))).thenAnswer(inv -> inv.getArgument(0));
        when(clusterInfoSyncService.syncClusterInfo()).thenReturn(true);

        int result = podsSyncService.syncPods();

        assertEquals(1, result);
        assertEquals("jdbc:postgresql://host:5432/db", pod.getDatabaseClusterUrl());
    }

    @Test
    void syncPods_ShouldHandleDatabaseClusterUrlException() {
        PodInfo pod = new PodInfo();
        pod.setName("my-service");
        pod.setPodName("my-service-abc");

        when(kubernetesService.getRunningPods()).thenReturn(List.of(pod));
        when(kubernetesService.getDatabaseClusterUrlFromSecrets("my-service"))
                .thenThrow(new RuntimeException("secrets error"));
        when(podRepository.findByNamespace("test-ns")).thenReturn(Collections.emptyList());
        when(podRepository.findByPodNameAndNamespace("my-service-abc", "test-ns")).thenReturn(Optional.empty());
        when(podRepository.save(any(PodInfo.class))).thenAnswer(inv -> inv.getArgument(0));
        when(clusterInfoSyncService.syncClusterInfo()).thenReturn(true);

        int result = podsSyncService.syncPods();

        assertEquals(1, result);
        assertNull(pod.getDatabaseClusterUrl());
    }

    @Test
    void cleanupStalePods_ShouldReturnZero_WhenDisabled() {
        when(kubernetesConfig.isEnabled()).thenReturn(false);

        int result = podsSyncService.cleanupStalePods(60);

        assertEquals(0, result);
    }

    @Test
    void cleanupStalePods_ShouldDeleteStalePods() {
        PodInfo stalePod = new PodInfo();
        stalePod.setPodName("stale-pod");
        stalePod.setNamespace("test-ns");
        stalePod.setUpdatedAt(LocalDateTime.now().minusHours(2));
        stalePod.setK8sQueriedAt(LocalDateTime.now().minusHours(2));

        when(podRepository.findByNamespace("test-ns")).thenReturn(List.of(stalePod));

        int result = podsSyncService.cleanupStalePods(60);

        assertEquals(1, result);
        verify(podRepository).deleteAll(anyList());
    }

    @Test
    void cleanupStalePods_ShouldNotDeleteFreshPods() {
        PodInfo freshPod = new PodInfo();
        freshPod.setPodName("fresh-pod");
        freshPod.setNamespace("test-ns");
        freshPod.setUpdatedAt(LocalDateTime.now());
        freshPod.setK8sQueriedAt(LocalDateTime.now());

        when(podRepository.findByNamespace("test-ns")).thenReturn(List.of(freshPod));

        int result = podsSyncService.cleanupStalePods(60);

        assertEquals(0, result);
        verify(podRepository, never()).deleteAll(anyList());
    }

    @Test
    void cleanupStalePods_ShouldUseUpdatedAt_WhenK8sQueriedAtNull() {
        PodInfo pod = new PodInfo();
        pod.setPodName("pod-1");
        pod.setNamespace("test-ns");
        pod.setUpdatedAt(LocalDateTime.now().minusHours(2));
        pod.setK8sQueriedAt(null);

        when(podRepository.findByNamespace("test-ns")).thenReturn(List.of(pod));

        int result = podsSyncService.cleanupStalePods(60);

        assertEquals(1, result);
    }
}
