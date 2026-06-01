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
class PodsControllerTest {

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

    private List<PodInfo> testPods;

    @BeforeEach
    void setUp() {
        PodInfo pod1 = new PodInfo();
        pod1.setName("nginx");
        pod1.setPodName("nginx-123");
        
        PodInfo pod2 = new PodInfo();
        pod2.setName("redis");
        pod2.setPodName("redis-456");
        
        testPods = Arrays.asList(pod1, pod2);

        when(kubernetesConfig.getNamespace()).thenReturn("default");
        when(clusterInfoSyncService.getNamespace()).thenReturn("default");
        when(clusterInfoSyncService.getKubernetesVersion()).thenReturn("v1.28.0");
        when(clusterInfoSyncService.getQuotaCpu()).thenReturn("2/10");
        when(clusterInfoSyncService.getQuotaMemory()).thenReturn("4Gi/20Gi");
        when(clusterInfoSyncService.getQuotaPods()).thenReturn("5/50");
        when(clusterInfoSyncService.getQuotaConfigmaps()).thenReturn("10/100");
        when(clusterInfoSyncService.getQuotaSecrets()).thenReturn("5/50");
    }

    @Test
    void testPods_Success() throws Exception {
        when(podRepository.findByNamespaceOrderByName("default")).thenReturn(testPods);
        when(kubernetesService.countUniqueServices(testPods)).thenReturn(2L);

        mockMvc.perform(get("/pods"))
                .andExpect(status().isOk())
                .andExpect(view().name("pods"))
                .andExpect(model().attribute("pods", testPods))
                .andExpect(model().attribute("totalPods", 2))
                .andExpect(model().attribute("uniqueServices", 2L))
                .andExpect(model().attribute("totalReplicas", 2))
                .andExpect(model().attribute("withReplicas", 0))
                .andExpect(model().attribute("kubernetesVersion", "v1.28.0"))
                .andExpect(model().attribute("quotaCpu", "2/10"))
                .andExpect(model().attribute("quotaMemory", "4Gi/20Gi"))
                .andExpect(model().attribute("quotaPods", "5/50"));

        verify(podRepository).findByNamespaceOrderByName("default");
        verify(kubernetesService).countUniqueServices(testPods);
        verify(clusterInfoSyncService).getKubernetesVersion();
        verify(clusterInfoSyncService, atLeastOnce()).getNamespace();
        verify(clusterInfoSyncService).getQuotaCpu();
        verify(clusterInfoSyncService).getQuotaMemory();
        verify(clusterInfoSyncService).getQuotaPods();
    }

    @Test
    void testPods_WithEmptyPods() throws Exception {
        when(podRepository.findByNamespaceOrderByName("default")).thenReturn(Collections.emptyList());
        when(kubernetesService.countUniqueServices(Collections.emptyList())).thenReturn(0L);

        mockMvc.perform(get("/pods"))
                .andExpect(status().isOk())
                .andExpect(view().name("pods"))
                .andExpect(model().attribute("totalPods", 0))
                .andExpect(model().attribute("uniqueServices", 0L));
    }

    @Test
    void testPods_ExceptionHandling() throws Exception {
        when(podRepository.findByNamespaceOrderByName("default")).thenThrow(new RuntimeException("Kubernetes error"));

        mockMvc.perform(get("/pods"))
                .andExpect(status().isOk())
                .andExpect(view().name("pods"))
                .andExpect(model().attributeExists("error"));
    }
}

