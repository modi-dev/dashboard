package com.dashboard.service;

import com.dashboard.config.KubernetesConfig;
import com.dashboard.model.KubernetesClusterInfo;
import com.dashboard.repository.KubernetesClusterInfoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class KubernetesClusterInfoSyncServiceTest {

    @Mock
    private KubernetesService kubernetesService;

    @Mock
    private KubernetesClusterInfoRepository clusterInfoRepository;

    @Mock
    private KubernetesConfig kubernetesConfig;

    @InjectMocks
    private KubernetesClusterInfoSyncService clusterInfoSyncService;

    private KubernetesClusterInfo existingClusterInfo;

    @BeforeEach
    void setUp() {
        existingClusterInfo = new KubernetesClusterInfo();
        existingClusterInfo.setId(1L);
        existingClusterInfo.setKubernetesVersion("v1.28.0");
        existingClusterInfo.setNamespace("default");
        existingClusterInfo.setK8sQueriedAt(LocalDateTime.now());
    }

    @Test
    void testSyncClusterInfo_Success_WithExistingInfo() {
        // Arrange
        when(kubernetesConfig.isEnabled()).thenReturn(true);
        when(kubernetesConfig.getNamespace()).thenReturn("dev-tools");
        when(clusterInfoRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(existingClusterInfo));
        when(kubernetesService.getKubernetesVersion()).thenReturn("v1.29.0");
        when(clusterInfoRepository.save(any(KubernetesClusterInfo.class))).thenReturn(existingClusterInfo);

        // Act
        boolean result = clusterInfoSyncService.syncClusterInfo();

        // Assert
        assertTrue(result);
        verify(kubernetesConfig).isEnabled();
        verify(kubernetesConfig).getNamespace();
        verify(clusterInfoRepository).findFirstByOrderByIdAsc();
        verify(kubernetesService).getKubernetesVersion();
        verify(clusterInfoRepository).save(any(KubernetesClusterInfo.class));
        assertEquals("v1.29.0", existingClusterInfo.getKubernetesVersion());
        assertEquals("dev-tools", existingClusterInfo.getNamespace());
    }

    @Test
    void testSyncClusterInfo_Success_WithNewInfo() {
        // Arrange
        when(kubernetesConfig.isEnabled()).thenReturn(true);
        when(kubernetesConfig.getNamespace()).thenReturn("dev-tools");
        when(clusterInfoRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());
        when(kubernetesService.getKubernetesVersion()).thenReturn("v1.29.0");
        when(clusterInfoRepository.save(any(KubernetesClusterInfo.class))).thenAnswer(invocation -> {
            KubernetesClusterInfo info = invocation.getArgument(0);
            info.setId(1L);
            return info;
        });

        // Act
        boolean result = clusterInfoSyncService.syncClusterInfo();

        // Assert
        assertTrue(result);
        verify(clusterInfoRepository).findFirstByOrderByIdAsc();
        verify(kubernetesService).getKubernetesVersion();
        verify(clusterInfoRepository).save(any(KubernetesClusterInfo.class));
    }

    @Test
    void testSyncClusterInfo_KubernetesDisabled() {
        // Arrange
        when(kubernetesConfig.isEnabled()).thenReturn(false);

        // Act
        boolean result = clusterInfoSyncService.syncClusterInfo();

        // Assert
        assertFalse(result);
        verify(kubernetesConfig).isEnabled();
        verify(kubernetesConfig, never()).getNamespace();
        verify(clusterInfoRepository, never()).findFirstByOrderByIdAsc();
    }

    @Test
    void testSyncClusterInfo_VersionUnknown() {
        // Arrange
        when(kubernetesConfig.isEnabled()).thenReturn(true);
        when(kubernetesConfig.getNamespace()).thenReturn("dev-tools");
        when(clusterInfoRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(existingClusterInfo));
        when(kubernetesService.getKubernetesVersion()).thenReturn("Неизвестно");
        when(clusterInfoRepository.save(any(KubernetesClusterInfo.class))).thenReturn(existingClusterInfo);

        // Act
        boolean result = clusterInfoSyncService.syncClusterInfo();

        // Assert
        assertTrue(result);
        verify(kubernetesService).getKubernetesVersion();
        // Версия не должна обновиться, если она "Неизвестно"
        verify(clusterInfoRepository).save(any(KubernetesClusterInfo.class));
    }

    @Test
    void testSyncClusterInfo_VersionException() {
        // Arrange
        when(kubernetesConfig.isEnabled()).thenReturn(true);
        when(kubernetesConfig.getNamespace()).thenReturn("dev-tools");
        when(clusterInfoRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(existingClusterInfo));
        when(kubernetesService.getKubernetesVersion()).thenThrow(new RuntimeException("Kubectl error"));
        when(clusterInfoRepository.save(any(KubernetesClusterInfo.class))).thenReturn(existingClusterInfo);

        // Act
        boolean result = clusterInfoSyncService.syncClusterInfo();

        // Assert
        assertTrue(result);
        verify(kubernetesService).getKubernetesVersion();
        verify(clusterInfoRepository).save(any(KubernetesClusterInfo.class));
    }

    @Test
    void testSyncClusterInfo_RepositoryException() {
        // Arrange
        when(kubernetesConfig.isEnabled()).thenReturn(true);
        lenient().when(kubernetesConfig.getNamespace()).thenReturn("dev-tools");
        when(clusterInfoRepository.findFirstByOrderByIdAsc()).thenThrow(new RuntimeException("DB error"));

        // Act
        boolean result = clusterInfoSyncService.syncClusterInfo();

        // Assert
        assertFalse(result);
        verify(clusterInfoRepository).findFirstByOrderByIdAsc();
    }

    @Test
    void testGetClusterInfo_WithExistingInfo() {
        // Arrange
        when(clusterInfoRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(existingClusterInfo));

        // Act
        KubernetesClusterInfo result = clusterInfoSyncService.getClusterInfo();

        // Assert
        assertNotNull(result);
        assertEquals(existingClusterInfo, result);
        verify(clusterInfoRepository).findFirstByOrderByIdAsc();
    }

    @Test
    void testGetClusterInfo_NoInfo() {
        // Arrange
        when(clusterInfoRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());

        // Act
        KubernetesClusterInfo result = clusterInfoSyncService.getClusterInfo();

        // Assert
        assertNull(result);
        verify(clusterInfoRepository).findFirstByOrderByIdAsc();
    }

    @Test
    void testGetKubernetesVersion_WithInfo() {
        // Arrange
        when(clusterInfoRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(existingClusterInfo));

        // Act
        String result = clusterInfoSyncService.getKubernetesVersion();

        // Assert
        assertEquals("v1.28.0", result);
        verify(clusterInfoRepository).findFirstByOrderByIdAsc();
    }

    @Test
    void testGetKubernetesVersion_NoInfo() {
        // Arrange
        when(clusterInfoRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());

        // Act
        String result = clusterInfoSyncService.getKubernetesVersion();

        // Assert
        assertEquals("Неизвестно", result);
        verify(clusterInfoRepository).findFirstByOrderByIdAsc();
    }

    @Test
    void testGetKubernetesVersion_NullVersion() {
        // Arrange
        existingClusterInfo.setKubernetesVersion(null);
        when(clusterInfoRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(existingClusterInfo));

        // Act
        String result = clusterInfoSyncService.getKubernetesVersion();

        // Assert
        assertEquals("Неизвестно", result);
    }

    @Test
    void testGetNamespace_WithInfo() {
        // Arrange
        when(clusterInfoRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(existingClusterInfo));

        // Act
        String result = clusterInfoSyncService.getNamespace();

        // Assert
        assertEquals("default", result);
        verify(clusterInfoRepository).findFirstByOrderByIdAsc();
        verify(kubernetesConfig, never()).getNamespace();
    }

    @Test
    void testGetNamespace_NoInfo() {
        // Arrange
        when(clusterInfoRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());
        when(kubernetesConfig.getNamespace()).thenReturn("dev-tools");

        // Act
        String result = clusterInfoSyncService.getNamespace();

        // Assert
        assertEquals("dev-tools", result);
        verify(clusterInfoRepository).findFirstByOrderByIdAsc();
        verify(kubernetesConfig).getNamespace();
    }

    @Test
    void testGetNamespace_NullNamespace() {
        // Arrange
        existingClusterInfo.setNamespace(null);
        when(clusterInfoRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(existingClusterInfo));
        when(kubernetesConfig.getNamespace()).thenReturn("dev-tools");

        // Act
        String result = clusterInfoSyncService.getNamespace();

        // Assert
        assertEquals("dev-tools", result);
        verify(kubernetesConfig).getNamespace();
    }
}

