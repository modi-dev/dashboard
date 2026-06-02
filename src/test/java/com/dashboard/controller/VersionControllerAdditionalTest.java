package com.dashboard.controller;

import com.dashboard.config.KubernetesConfig;
import com.dashboard.model.PodInfo;
import com.dashboard.repository.PodRepository;
import com.dashboard.service.CsvExportService;
import com.dashboard.service.KubernetesPodsSyncService;
import com.dashboard.service.KubernetesService;
import com.dashboard.service.KubernetesClusterInfoSyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = VersionController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@TestPropertySource(properties = "security.enabled=false")
class VersionControllerAdditionalTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private KubernetesService kubernetesService;

    @MockBean
    private CsvExportService csvExportService;

    @MockBean
    private PodRepository podRepository;

    @MockBean
    private KubernetesPodsSyncService podsSyncService;

    @MockBean
    private KubernetesConfig kubernetesConfig;

    @MockBean
    private KubernetesClusterInfoSyncService clusterInfoSyncService;

    @BeforeEach
    void setUp() {
        when(clusterInfoSyncService.getNamespace()).thenReturn("default");
    }

    @Test
    void testGetPodsLastUpdated_WithData() throws Exception {
        LocalDateTime lastUpdated = LocalDateTime.of(2025, 1, 15, 10, 30, 0);
        when(podRepository.findLastUpdatedAt()).thenReturn(Optional.of(lastUpdated));

        mockMvc.perform(get("/dashboard/api/pods/last-updated"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.lastUpdated").value("2025-01-15T10:30:00"));
    }

    @Test
    void testGetPodsLastUpdated_WithNoData() throws Exception {
        when(podRepository.findLastUpdatedAt()).thenReturn(Optional.empty());

        mockMvc.perform(get("/dashboard/api/pods/last-updated"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.lastUpdated").isEmpty());
    }

    @Test
    void testGetPodsLastUpdated_Exception() throws Exception {
        when(podRepository.findLastUpdatedAt()).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/dashboard/api/pods/last-updated"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testGetPodsSummary_Exception() throws Exception {
        when(kubernetesService.getRunningPods()).thenThrow(new RuntimeException("Error"));

        mockMvc.perform(get("/dashboard/api/pods/summary"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetPodByName_Exception() throws Exception {
        when(kubernetesService.getRunningPods()).thenThrow(new RuntimeException("Error"));

        mockMvc.perform(get("/dashboard/api/pods/pods/test-pod"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetConfig_Exception() throws Exception {
        when(kubernetesService.getKubernetesConfig()).thenThrow(new RuntimeException("Config error"));

        mockMvc.perform(get("/dashboard/api/pods/config"))
                .andExpect(status().isInternalServerError());
    }
}
