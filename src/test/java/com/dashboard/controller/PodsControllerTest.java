package com.dashboard.controller;

import com.dashboard.model.PodInfo;
import com.dashboard.service.KubernetesService;
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
    }

    @Test
    void testPods_Success() throws Exception {
        when(kubernetesService.getRunningPods()).thenReturn(testPods);
        when(kubernetesService.countUniqueServices(testPods)).thenReturn(2L);
        when(kubernetesService.getKubernetesVersion()).thenReturn("v1.34.0");

        mockMvc.perform(get("/pods"))
                .andExpect(status().isOk())
                .andExpect(view().name("pods"))
                .andExpect(model().attribute("pods", testPods))
                .andExpect(model().attribute("totalPods", 2))
                .andExpect(model().attribute("uniqueServices", 2L))
                .andExpect(model().attribute("totalReplicas", 2))
                .andExpect(model().attribute("withReplicas", 0))
                .andExpect(model().attribute("kubernetesVersion", "v1.34.0"));

        verify(kubernetesService).getRunningPods();
        verify(kubernetesService).countUniqueServices(testPods);
        verify(kubernetesService).getKubernetesVersion();
    }

    @Test
    void testPods_WithEmptyPods() throws Exception {
        when(kubernetesService.getRunningPods()).thenReturn(Collections.emptyList());
        when(kubernetesService.countUniqueServices(Collections.emptyList())).thenReturn(0L);
        when(kubernetesService.getKubernetesVersion()).thenReturn("v1.34.0");

        mockMvc.perform(get("/pods"))
                .andExpect(status().isOk())
                .andExpect(view().name("pods"))
                .andExpect(model().attribute("totalPods", 0))
                .andExpect(model().attribute("uniqueServices", 0L));
    }

    @Test
    void testPods_ExceptionHandling() throws Exception {
        when(kubernetesService.getRunningPods()).thenThrow(new RuntimeException("Kubernetes error"));

        mockMvc.perform(get("/pods"))
                .andExpect(status().isOk())
                .andExpect(view().name("pods"))
                .andExpect(model().attributeExists("error"));
    }
}

