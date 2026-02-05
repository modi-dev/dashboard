package com.dashboard.config;

import com.dashboard.service.KubernetesPodsSyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

/**
 * Unit tests for PodsInitializer
 */
@ExtendWith(MockitoExtension.class)
class PodsInitializerTest {

    @Mock
    private KubernetesPodsSyncService podsSyncService;

    private PodsInitializer podsInitializer;

    @BeforeEach
    void setUp() {
        podsInitializer = new PodsInitializer(podsSyncService);
    }

    @Test
    void testRun_SuccessfulSync() throws Exception {
        when(podsSyncService.syncPods()).thenReturn(10);

        podsInitializer.run();

        verify(podsSyncService).syncPods();
    }

    @Test
    void testRun_ZeroPodsSync() throws Exception {
        when(podsSyncService.syncPods()).thenReturn(0);

        podsInitializer.run();

        verify(podsSyncService).syncPods();
    }

    @Test
    void testRun_WithArgs() throws Exception {
        when(podsSyncService.syncPods()).thenReturn(5);

        podsInitializer.run("arg1", "arg2");

        verify(podsSyncService).syncPods();
    }

    @Test
    void testRun_WhenSyncThrowsException_ShouldNotThrow() throws Exception {
        when(podsSyncService.syncPods()).thenThrow(new RuntimeException("Kubernetes unavailable"));

        // Should not throw - exception is handled internally
        podsInitializer.run();

        verify(podsSyncService).syncPods();
    }

    @Test
    void testRun_WhenSyncThrowsKubectlException_ShouldNotThrow() throws Exception {
        when(podsSyncService.syncPods()).thenThrow(new RuntimeException("kubectl command failed"));

        // Should not throw - exception is handled internally
        podsInitializer.run();

        verify(podsSyncService).syncPods();
    }

    @Test
    void testRun_MultipleRuns() throws Exception {
        when(podsSyncService.syncPods()).thenReturn(10, 15, 20);

        podsInitializer.run();
        podsInitializer.run();
        podsInitializer.run();

        verify(podsSyncService, times(3)).syncPods();
    }
}
