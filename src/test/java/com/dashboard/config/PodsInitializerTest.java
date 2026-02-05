package com.dashboard.config;

import com.dashboard.service.KubernetesPodsSyncService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PodsInitializerTest {

    @Mock
    private KubernetesPodsSyncService podsSyncService;

    @InjectMocks
    private PodsInitializer podsInitializer;

    @Test
    void run_ShouldSyncPods_WhenServiceSucceeds() throws Exception {
        when(podsSyncService.syncPods()).thenReturn(5);

        podsInitializer.run();

        verify(podsSyncService).syncPods();
    }

    @Test
    void run_ShouldHandleException_WhenServiceFails() throws Exception {
        when(podsSyncService.syncPods()).thenThrow(new RuntimeException("Connection refused"));

        // Should not throw exception
        podsInitializer.run();

        verify(podsSyncService).syncPods();
    }

    @Test
    void run_ShouldSyncZeroPods() throws Exception {
        when(podsSyncService.syncPods()).thenReturn(0);

        podsInitializer.run();

        verify(podsSyncService).syncPods();
    }
}
