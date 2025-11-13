package com.dashboard.service;

import com.dashboard.config.KubernetesConfig;
import com.dashboard.model.PodInfo;
import com.dashboard.repository.PodRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KubernetesPodsSyncServiceTest {

    @Mock
    private KubernetesService kubernetesService;

    @Mock
    private PodRepository podRepository;

    @Mock
    private KubernetesConfig kubernetesConfig;

    @InjectMocks
    private KubernetesPodsSyncService podsSyncService;

    @Captor
    private ArgumentCaptor<PodInfo> podCaptor;

    @Captor
    private ArgumentCaptor<List<PodInfo>> podListCaptor;

    @Test
    void syncPods_returnsZero_whenKubernetesDisabled() {
        when(kubernetesConfig.isEnabled()).thenReturn(false);

        int result = podsSyncService.syncPods();

        assertEquals(0, result);
        verifyNoInteractions(kubernetesService, podRepository);
    }

    @Test
    void syncPods_createsUpdatesAndDeletesPods() {
        when(kubernetesConfig.isEnabled()).thenReturn(true);
        when(kubernetesConfig.getNamespace()).thenReturn("test-ns");

        PodInfo existingPodInDb = new PodInfo();
        existingPodInDb.setPodName("existing-pod");
        existingPodInDb.setNamespace("test-ns");
        existingPodInDb.setVersion("old");
        existingPodInDb.setK8sQueriedAt(LocalDateTime.now().minusHours(1));

        PodInfo stalePod = new PodInfo();
        stalePod.setPodName("stale-pod");
        stalePod.setNamespace("test-ns");

        when(podRepository.findByNamespace("test-ns"))
                .thenReturn(Arrays.asList(existingPodInDb, stalePod));
        when(podRepository.findByPodNameAndNamespace("existing-pod", "test-ns"))
                .thenReturn(Optional.of(existingPodInDb));
        when(podRepository.findByPodNameAndNamespace("new-pod", "test-ns"))
                .thenReturn(Optional.empty());

        PodInfo currentExisting = new PodInfo();
        currentExisting.setPodName("existing-pod");
        currentExisting.setVersion("new-version");
        currentExisting.setReadyTime("10s");

        PodInfo currentNew = new PodInfo();
        currentNew.setPodName("new-pod");
        currentNew.setVersion("1.0.0");

        when(kubernetesService.getRunningPods())
                .thenReturn(Arrays.asList(currentExisting, currentNew));

        int processed = podsSyncService.syncPods();

        assertEquals(2, processed);

        verify(podRepository, times(2)).save(podCaptor.capture());
        List<PodInfo> savedPods = podCaptor.getAllValues();

        PodInfo savedExisting = savedPods.stream()
                .filter(p -> "existing-pod".equals(p.getPodName()))
                .findFirst()
                .orElseThrow();
        assertEquals("new-version", savedExisting.getVersion());
        assertThat(savedExisting.getK8sQueriedAt()).isNotNull();
        assertEquals("test-ns", savedExisting.getNamespace());

        PodInfo savedNew = savedPods.stream()
                .filter(p -> "new-pod".equals(p.getPodName()))
                .findFirst()
                .orElseThrow();
        assertEquals("1.0.0", savedNew.getVersion());
        assertEquals("test-ns", savedNew.getNamespace());
        assertThat(savedNew.getK8sQueriedAt()).isNotNull();

        verify(podRepository).delete(stalePod);
        verify(kubernetesService).getRunningPods();
    }

    @Test
    void syncPods_propagatesException_whenRepositoryFails() {
        when(kubernetesConfig.isEnabled()).thenReturn(true);
        when(kubernetesConfig.getNamespace()).thenReturn("test-ns");
        when(kubernetesService.getRunningPods()).thenThrow(new IllegalStateException("kubectl down"));

        assertThrows(IllegalStateException.class, () -> podsSyncService.syncPods());
    }

    @Test
    void cleanupStalePods_returnsZero_whenDisabled() {
        when(kubernetesConfig.isEnabled()).thenReturn(false);

        int removed = podsSyncService.cleanupStalePods(30);

        assertEquals(0, removed);
        verifyNoInteractions(podRepository);
    }

    @Test
    void cleanupStalePods_removesPodsOlderThanThreshold() {
        when(kubernetesConfig.isEnabled()).thenReturn(true);
        when(kubernetesConfig.getNamespace()).thenReturn("test-ns");

        PodInfo stale = new PodInfo();
        stale.setPodName("stale");
        stale.setK8sQueriedAt(LocalDateTime.now().minusMinutes(60));
        stale.setUpdatedAt(LocalDateTime.now().minusMinutes(60));

        PodInfo fresh = new PodInfo();
        fresh.setPodName("fresh");
        fresh.setK8sQueriedAt(LocalDateTime.now().minusMinutes(5));
        fresh.setUpdatedAt(LocalDateTime.now().minusMinutes(5));

        when(podRepository.findByNamespace("test-ns"))
                .thenReturn(Arrays.asList(stale, fresh));

        int removed = podsSyncService.cleanupStalePods(30);

        assertEquals(1, removed);
        verify(podRepository).deleteAll(podListCaptor.capture());
        assertThat(podListCaptor.getValue())
                .containsExactly(stale);
    }
}

