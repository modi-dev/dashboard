package com.dashboard.config;

import com.dashboard.service.KubernetesClusterInfoSyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClusterInfoInitializerTest {

    @Mock
    private KubernetesClusterInfoSyncService clusterInfoSyncService;

    private ClusterInfoInitializer clusterInfoInitializer;

    @BeforeEach
    void setUp() {
        clusterInfoInitializer = new ClusterInfoInitializer(clusterInfoSyncService);
    }

    @Test
    void testRun_Success() {
        // Arrange
        when(clusterInfoSyncService.syncClusterInfo()).thenReturn(true);

        // Act
        clusterInfoInitializer.run();

        // Assert
        verify(clusterInfoSyncService).syncClusterInfo();
    }

    @Test
    void testRun_Failure() {
        // Arrange
        when(clusterInfoSyncService.syncClusterInfo()).thenReturn(false);

        // Act
        clusterInfoInitializer.run();

        // Assert
        verify(clusterInfoSyncService).syncClusterInfo();
    }

    @Test
    void testRun_Exception() {
        // Arrange
        when(clusterInfoSyncService.syncClusterInfo()).thenThrow(new RuntimeException("Error"));

        // Act
        clusterInfoInitializer.run();

        // Assert
        verify(clusterInfoSyncService).syncClusterInfo();
        // Не должно прервать выполнение
    }
}

