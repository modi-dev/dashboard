package com.dashboard.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KubernetesClusterInfoSchedulerTest {

    @Mock
    private KubernetesClusterInfoSyncService clusterInfoSyncService;

    private KubernetesClusterInfoScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new KubernetesClusterInfoScheduler(clusterInfoSyncService, 60.0);
    }

    @Test
    void testSyncClusterInfo_Success() {
        // Arrange
        when(clusterInfoSyncService.syncClusterInfo()).thenReturn(true);

        // Act
        scheduler.syncClusterInfo();

        // Assert
        verify(clusterInfoSyncService).syncClusterInfo();
    }

    @Test
    void testSyncClusterInfo_Failure() {
        // Arrange
        when(clusterInfoSyncService.syncClusterInfo()).thenReturn(false);

        // Act
        scheduler.syncClusterInfo();

        // Assert
        verify(clusterInfoSyncService).syncClusterInfo();
    }

    @Test
    void testSyncClusterInfo_Exception() {
        // Arrange
        when(clusterInfoSyncService.syncClusterInfo()).thenThrow(new RuntimeException("Error"));

        // Act
        scheduler.syncClusterInfo();

        // Assert
        verify(clusterInfoSyncService).syncClusterInfo();
        // Не должно прервать выполнение
    }
}

