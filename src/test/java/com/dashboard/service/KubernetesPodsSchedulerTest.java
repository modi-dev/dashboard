package com.dashboard.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KubernetesPodsSchedulerTest {

    @Mock
    private KubernetesPodsSyncService podsSyncService;

    private KubernetesPodsScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new KubernetesPodsScheduler(podsSyncService, 1.0);
    }

    @Test
    void syncPods_invokesSyncService() {
        scheduler.syncPods();

        verify(podsSyncService).syncPods();
    }

    @Test
    void syncPods_swallowsExceptionsFromSyncService() {
        doThrow(new IllegalStateException("kubectl error"))
                .when(podsSyncService).syncPods();

        assertDoesNotThrow(() -> scheduler.syncPods());
    }
}

