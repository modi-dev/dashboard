package com.dashboard.controller;

import com.dashboard.config.KubernetesConfig;
import com.dashboard.model.PodInfo;
import com.dashboard.repository.PodRepository;
import com.dashboard.service.KubernetesService;
import com.dashboard.service.KubernetesClusterInfoSyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = PodsController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@TestPropertySource(properties = "security.enabled=false")
class PodsControllerAdditionalTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private KubernetesService kubernetesService;

    @MockBean
    private PodRepository podRepository;

    @MockBean
    private KubernetesConfig kubernetesConfig;

    @MockBean
    private KubernetesClusterInfoSyncService clusterInfoSyncService;

    @BeforeEach
    void setUp() {
        when(clusterInfoSyncService.getNamespace()).thenReturn("default");
        when(clusterInfoSyncService.getKubernetesVersion()).thenReturn("v1.28.0");
        when(clusterInfoSyncService.getQuotaCpu()).thenReturn(null);
        when(clusterInfoSyncService.getQuotaMemory()).thenReturn(null);
        when(clusterInfoSyncService.getQuotaPods()).thenReturn(null);
        when(clusterInfoSyncService.getQuotaConfigmaps()).thenReturn(null);
        when(clusterInfoSyncService.getQuotaSecrets()).thenReturn(null);
    }

    @Test
    void testPods_WithNullQuotas() throws Exception {
        when(podRepository.findByNamespaceOrderByName("default")).thenReturn(Collections.emptyList());
        when(kubernetesService.countUniqueServices(Collections.emptyList())).thenReturn(0L);

        mockMvc.perform(get("/pods"))
                .andExpect(status().isOk())
                .andExpect(view().name("pods"))
                .andExpect(model().attribute("totalPods", 0));
    }

    @Test
    void testPods_ExceptionInRepositoryCall() throws Exception {
        when(clusterInfoSyncService.getNamespace()).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/pods"))
                .andExpect(status().isOk())
                .andExpect(view().name("pods"))
                .andExpect(model().attributeExists("error"));
    }
}
