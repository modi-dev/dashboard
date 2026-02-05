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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KubernetesClusterInfoSyncServiceAdditionalTest {

    @Mock
    private KubernetesService kubernetesService;

    @Mock
    private KubernetesClusterInfoRepository clusterInfoRepository;

    @Mock
    private KubernetesConfig kubernetesConfig;

    @InjectMocks
    private KubernetesClusterInfoSyncService syncService;

    @BeforeEach
    void setUp() {
        lenient().when(kubernetesConfig.isEnabled()).thenReturn(true);
        lenient().when(kubernetesConfig.getNamespace()).thenReturn("test-ns");
    }

    // getKubernetesVersion
    @Test
    void getKubernetesVersion_ShouldReturnVersion_WhenInfoExists() {
        KubernetesClusterInfo info = new KubernetesClusterInfo();
        info.setKubernetesVersion("v1.28.0");
        when(clusterInfoRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(info));

        assertEquals("v1.28.0", syncService.getKubernetesVersion());
    }

    @Test
    void getKubernetesVersion_ShouldReturnUnknown_WhenNoInfo() {
        when(clusterInfoRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());

        assertEquals("Неизвестно", syncService.getKubernetesVersion());
    }

    @Test
    void getKubernetesVersion_ShouldReturnUnknown_WhenVersionNull() {
        KubernetesClusterInfo info = new KubernetesClusterInfo();
        info.setKubernetesVersion(null);
        when(clusterInfoRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(info));

        assertEquals("Неизвестно", syncService.getKubernetesVersion());
    }

    // getNamespace
    @Test
    void getNamespace_ShouldReturnNamespace_WhenInfoExists() {
        KubernetesClusterInfo info = new KubernetesClusterInfo();
        info.setNamespace("production");
        when(clusterInfoRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(info));

        assertEquals("production", syncService.getNamespace());
    }

    @Test
    void getNamespace_ShouldReturnConfigNamespace_WhenNoInfo() {
        when(clusterInfoRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());

        assertEquals("test-ns", syncService.getNamespace());
    }

    @Test
    void getNamespace_ShouldReturnConfigNamespace_WhenNamespaceNull() {
        KubernetesClusterInfo info = new KubernetesClusterInfo();
        info.setNamespace(null);
        when(clusterInfoRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(info));

        assertEquals("test-ns", syncService.getNamespace());
    }

    // getQuotaCpu
    @Test
    void getQuotaCpu_ShouldReturnFormatted_WhenDataExists() {
        KubernetesClusterInfo info = new KubernetesClusterInfo();
        info.setQuotaCpuUsed("2");
        info.setQuotaCpuHard("10");
        when(clusterInfoRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(info));

        assertEquals("2/10", syncService.getQuotaCpu());
    }

    @Test
    void getQuotaCpu_ShouldReturnNull_WhenNoInfo() {
        when(clusterInfoRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());

        assertNull(syncService.getQuotaCpu());
    }

    @Test
    void getQuotaCpu_ShouldReturnNull_WhenUsedIsNull() {
        KubernetesClusterInfo info = new KubernetesClusterInfo();
        info.setQuotaCpuUsed(null);
        info.setQuotaCpuHard("10");
        when(clusterInfoRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(info));

        assertNull(syncService.getQuotaCpu());
    }

    @Test
    void getQuotaCpu_ShouldReturnNull_WhenHardIsNull() {
        KubernetesClusterInfo info = new KubernetesClusterInfo();
        info.setQuotaCpuUsed("2");
        info.setQuotaCpuHard(null);
        when(clusterInfoRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(info));

        assertNull(syncService.getQuotaCpu());
    }

    // getQuotaMemory
    @Test
    void getQuotaMemory_ShouldReturnFormatted() {
        KubernetesClusterInfo info = new KubernetesClusterInfo();
        info.setQuotaMemoryUsed("4Gi");
        info.setQuotaMemoryHard("20Gi");
        when(clusterInfoRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(info));

        assertEquals("4Gi/20Gi", syncService.getQuotaMemory());
    }

    @Test
    void getQuotaMemory_ShouldReturnNull_WhenPartialData() {
        KubernetesClusterInfo info = new KubernetesClusterInfo();
        info.setQuotaMemoryUsed(null);
        when(clusterInfoRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(info));

        assertNull(syncService.getQuotaMemory());
    }

    // getQuotaPods
    @Test
    void getQuotaPods_ShouldReturnFormatted() {
        KubernetesClusterInfo info = new KubernetesClusterInfo();
        info.setQuotaPodsUsed("5");
        info.setQuotaPodsHard("50");
        when(clusterInfoRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(info));

        assertEquals("5/50", syncService.getQuotaPods());
    }

    @Test
    void getQuotaPods_ShouldReturnNull_WhenPartialData() {
        KubernetesClusterInfo info = new KubernetesClusterInfo();
        info.setQuotaPodsUsed("5");
        info.setQuotaPodsHard(null);
        when(clusterInfoRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(info));

        assertNull(syncService.getQuotaPods());
    }

    // getQuotaConfigmaps
    @Test
    void getQuotaConfigmaps_ShouldReturnFormatted() {
        KubernetesClusterInfo info = new KubernetesClusterInfo();
        info.setQuotaConfigmapsUsed("10");
        info.setQuotaConfigmapsHard("100");
        when(clusterInfoRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(info));

        assertEquals("10/100", syncService.getQuotaConfigmaps());
    }

    @Test
    void getQuotaConfigmaps_ShouldReturnNull_WhenPartialData() {
        KubernetesClusterInfo info = new KubernetesClusterInfo();
        info.setQuotaConfigmapsUsed(null);
        info.setQuotaConfigmapsHard("100");
        when(clusterInfoRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(info));

        assertNull(syncService.getQuotaConfigmaps());
    }

    // getQuotaSecrets
    @Test
    void getQuotaSecrets_ShouldReturnFormatted() {
        KubernetesClusterInfo info = new KubernetesClusterInfo();
        info.setQuotaSecretsUsed("8");
        info.setQuotaSecretsHard("50");
        when(clusterInfoRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(info));

        assertEquals("8/50", syncService.getQuotaSecrets());
    }

    @Test
    void getQuotaSecrets_ShouldReturnNull_WhenPartialData() {
        KubernetesClusterInfo info = new KubernetesClusterInfo();
        info.setQuotaSecretsUsed("8");
        info.setQuotaSecretsHard(null);
        when(clusterInfoRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(info));

        assertNull(syncService.getQuotaSecrets());
    }

    // syncClusterInfo
    @Test
    void syncClusterInfo_ShouldReturnFalse_WhenDisabled() {
        when(kubernetesConfig.isEnabled()).thenReturn(false);

        assertFalse(syncService.syncClusterInfo());
    }

    @Test
    void syncClusterInfo_ShouldCreateNewRecord_WhenNoneExists() {
        when(clusterInfoRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());
        when(kubernetesService.getKubernetesVersion()).thenReturn("v1.28.0");
        when(kubernetesService.getNamespaceQuota()).thenReturn(null);
        when(clusterInfoRepository.save(any(KubernetesClusterInfo.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        assertTrue(syncService.syncClusterInfo());
    }

    @Test
    void syncClusterInfo_ShouldUpdateExistingRecord() {
        KubernetesClusterInfo existing = new KubernetesClusterInfo();
        existing.setId(1L);
        existing.setNamespace("old-ns");
        when(clusterInfoRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(existing));
        when(kubernetesService.getKubernetesVersion()).thenReturn("v1.28.0");
        when(kubernetesService.getNamespaceQuota()).thenReturn(null);
        when(clusterInfoRepository.save(any(KubernetesClusterInfo.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        assertTrue(syncService.syncClusterInfo());
        assertEquals("test-ns", existing.getNamespace());
    }

    @Test
    void syncClusterInfo_ShouldHandleVersionException() {
        when(clusterInfoRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());
        when(kubernetesService.getKubernetesVersion()).thenThrow(new RuntimeException("version error"));
        when(kubernetesService.getNamespaceQuota()).thenReturn(null);
        when(clusterInfoRepository.save(any(KubernetesClusterInfo.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        assertTrue(syncService.syncClusterInfo());
    }

    @Test
    void syncClusterInfo_ShouldHandleQuotaException() {
        when(clusterInfoRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());
        when(kubernetesService.getKubernetesVersion()).thenReturn("v1.28.0");
        when(kubernetesService.getNamespaceQuota()).thenThrow(new RuntimeException("quota error"));
        when(clusterInfoRepository.save(any(KubernetesClusterInfo.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        assertTrue(syncService.syncClusterInfo());
    }

    @Test
    void syncClusterInfo_ShouldReturnFalse_WhenVersionIsUnknown() {
        when(clusterInfoRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());
        when(kubernetesService.getKubernetesVersion()).thenReturn("Неизвестно");
        when(kubernetesService.getNamespaceQuota()).thenReturn(null);
        when(clusterInfoRepository.save(any(KubernetesClusterInfo.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        assertTrue(syncService.syncClusterInfo());
    }

    @Test
    void syncClusterInfo_ShouldReturnFalse_WhenSaveThrows() {
        when(clusterInfoRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());
        when(kubernetesService.getKubernetesVersion()).thenReturn("v1.28.0");
        when(kubernetesService.getNamespaceQuota()).thenReturn(null);
        when(clusterInfoRepository.save(any(KubernetesClusterInfo.class)))
                .thenThrow(new RuntimeException("save error"));

        assertFalse(syncService.syncClusterInfo());
    }

    @Test
    void syncClusterInfo_ShouldUpdateQuota() {
        KubernetesClusterInfo existing = new KubernetesClusterInfo();
        existing.setId(1L);
        when(clusterInfoRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(existing));
        when(kubernetesService.getKubernetesVersion()).thenReturn("v1.28.0");

        KubernetesService.NamespaceQuota quota = new KubernetesService.NamespaceQuota();
        quota.cpuUsed = "2";
        quota.cpuHard = "10";
        quota.memoryUsed = "4Gi";
        quota.memoryHard = "20Gi";
        quota.podsUsed = "5";
        quota.podsHard = "50";
        quota.configmapsUsed = "10";
        quota.configmapsHard = "100";
        quota.secretsUsed = "8";
        quota.secretsHard = "50";
        when(kubernetesService.getNamespaceQuota()).thenReturn(quota);
        when(clusterInfoRepository.save(any(KubernetesClusterInfo.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        assertTrue(syncService.syncClusterInfo());
        assertEquals("2", existing.getQuotaCpuUsed());
        assertEquals("10", existing.getQuotaCpuHard());
    }

    // getClusterInfo
    @Test
    void getClusterInfo_ShouldReturnNull_WhenNoInfo() {
        when(clusterInfoRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());

        assertNull(syncService.getClusterInfo());
    }

    @Test
    void getClusterInfo_ShouldReturnInfo_WhenExists() {
        KubernetesClusterInfo info = new KubernetesClusterInfo();
        info.setNamespace("test-ns");
        when(clusterInfoRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(info));

        assertNotNull(syncService.getClusterInfo());
    }
}
