package com.dashboard.controller;

import com.dashboard.model.PodInfo;
import com.dashboard.service.CsvExportService;
import com.dashboard.service.KubernetesService;
import com.dashboard.config.KubernetesConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VersionController.class)
class VersionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private KubernetesService kubernetesService;

    @MockBean
    private CsvExportService csvExportService;

    private List<PodInfo> testPods;
    private KubernetesConfig mockConfig;

    @BeforeEach
    void setUp() {
        PodInfo pod1 = new PodInfo();
        pod1.setName("nginx");
        pod1.setPodName("nginx-123");
        pod1.setVersion("nginx:alpine");
        
        PodInfo pod2 = new PodInfo();
        pod2.setName("redis");
        pod2.setPodName("redis-456");
        pod2.setVersion("redis:7.2");
        
        testPods = Arrays.asList(pod1, pod2);
        
        mockConfig = mock(KubernetesConfig.class);
        when(mockConfig.isEnabled()).thenReturn(true);
        when(mockConfig.getNamespace()).thenReturn("default");
        when(mockConfig.getKubectlPath()).thenReturn("/usr/bin/kubectl");
    }

    @Test
    void testGetRunningPods_Success() throws Exception {
        when(kubernetesService.getRunningPods()).thenReturn(testPods);

        mockMvc.perform(get("/api/pods/api/pods/pods"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("nginx"))
                .andExpect(jsonPath("$[1].name").value("redis"));

        verify(kubernetesService, atLeastOnce()).getRunningPods();
    }

    @Test
    void testGetRunningPods_Exception() throws Exception {
        when(kubernetesService.getRunningPods()).thenThrow(new RuntimeException("Kubernetes error"));

        mockMvc.perform(get("/api/pods/api/pods/pods"))
                .andExpect(status().isInternalServerError());

        verify(kubernetesService).getRunningPods();
    }

    @Test
    void testGetHtmlPage_Success() throws Exception {
        String htmlContent = "<html><body>Test HTML</body></html>";
        when(kubernetesService.generateHtmlPage()).thenReturn(htmlContent);

        mockMvc.perform(get("/api/pods/html"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(htmlContent));

        verify(kubernetesService).generateHtmlPage();
    }

    @Test
    void testGetHtmlPage_Exception() throws Exception {
        when(kubernetesService.generateHtmlPage()).thenThrow(new RuntimeException("Generation error"));

        mockMvc.perform(get("/api/pods/html"))
                .andExpect(status().isInternalServerError());

        verify(kubernetesService).generateHtmlPage();
    }

    @Test
    void testGetCurrentNamespace_Success() throws Exception {
        when(kubernetesService.getCurrentNamespace()).thenReturn("default");

        mockMvc.perform(get("/api/pods/namespace"))
                .andExpect(status().isOk())
                .andExpect(content().string("default"));

        verify(kubernetesService).getCurrentNamespace();
    }

    @Test
    void testGetCurrentNamespace_Exception() throws Exception {
        when(kubernetesService.getCurrentNamespace()).thenThrow(new RuntimeException("Namespace error"));

        mockMvc.perform(get("/api/pods/namespace"))
                .andExpect(status().isInternalServerError());

        verify(kubernetesService).getCurrentNamespace();
    }

    @Test
    void testGetPodsSummary_Success() throws Exception {
        when(kubernetesService.getRunningPods()).thenReturn(testPods);

        mockMvc.perform(get("/api/pods/summary"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("nginx"));

        verify(kubernetesService, atLeastOnce()).getRunningPods();
    }

    @Test
    void testGetPodByName_Success() throws Exception {
        PodInfo pod = testPods.get(0);
        when(kubernetesService.getRunningPods()).thenReturn(testPods);

        mockMvc.perform(get("/api/pods/pods/nginx"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("nginx"));

        verify(kubernetesService, atLeastOnce()).getRunningPods();
    }

    @Test
    void testGetPodByName_NotFound() throws Exception {
        when(kubernetesService.getRunningPods()).thenReturn(testPods);

        mockMvc.perform(get("/api/pods/pods/nonexistent"))
                .andExpect(status().isNotFound());

        verify(kubernetesService).getRunningPods();
    }

    @Test
    void testCheckKubernetesHealth_Success() throws Exception {
        when(kubernetesService.getCurrentNamespace()).thenReturn("default");

        mockMvc.perform(get("/api/pods/health"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Kubernetes API доступен")));

        verify(kubernetesService).getCurrentNamespace();
    }

    @Test
    void testCheckKubernetesHealth_Exception() throws Exception {
        when(kubernetesService.getCurrentNamespace()).thenThrow(new RuntimeException("Health check error"));

        mockMvc.perform(get("/api/pods/health"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Kubernetes API недоступен")));

        verify(kubernetesService).getCurrentNamespace();
    }

    @Test
    void testTestKubernetesService_Success() throws Exception {
        when(kubernetesService.getKubernetesConfig()).thenReturn(mockConfig);
        when(kubernetesService.getRunningPods()).thenReturn(testPods);

        mockMvc.perform(get("/api/pods/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.kubernetesEnabled").value(true))
                .andExpect(jsonPath("$.podsCount").value(2));

        verify(kubernetesService, atLeastOnce()).getKubernetesConfig();
        verify(kubernetesService, atLeastOnce()).getRunningPods();
    }

    @Test
    void testTestKubernetesService_Exception() throws Exception {
        when(kubernetesService.getKubernetesConfig()).thenReturn(mockConfig);
        when(kubernetesService.getRunningPods()).thenThrow(new RuntimeException("Test error"));

        mockMvc.perform(get("/api/pods/test"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("error"));

        verify(kubernetesService, atLeastOnce()).getKubernetesConfig();
        verify(kubernetesService).getRunningPods();
    }

    @Test
    void testGetKubernetesConfig_Success() throws Exception {
        when(kubernetesService.getKubernetesConfig()).thenReturn(mockConfig);

        mockMvc.perform(get("/api/pods/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.namespace").value("default"))
                .andExpect(jsonPath("$.kubectlPath").value("/usr/bin/kubectl"));

        verify(kubernetesService, atLeastOnce()).getKubernetesConfig();
    }

    @Test
    void testGetInfo_Success() throws Exception {
        when(kubernetesService.getKubernetesConfig()).thenReturn(mockConfig);
        when(kubernetesService.getRunningPods()).thenReturn(testPods);

        mockMvc.perform(get("/api/pods/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"))
                .andExpect(jsonPath("$.totalPods").value(2))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(kubernetesService, atLeastOnce()).getKubernetesConfig();
        verify(kubernetesService, atLeastOnce()).getRunningPods();
    }

    @Test
    void testGetInfo_Exception() throws Exception {
        when(kubernetesService.getKubernetesConfig()).thenReturn(mockConfig);
        when(kubernetesService.getRunningPods()).thenThrow(new RuntimeException("Info error"));

        mockMvc.perform(get("/api/pods/info"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("error"));

        verify(kubernetesService, atLeastOnce()).getKubernetesConfig();
        verify(kubernetesService).getRunningPods();
    }

    @Test
    void testRefreshPods_Success() throws Exception {
        when(kubernetesService.getRunningPods()).thenReturn(testPods);

        mockMvc.perform(post("/api/pods/refresh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("Информация о подах успешно обновлена"))
                .andExpect(jsonPath("$.error").isEmpty())
                .andExpect(jsonPath("$.message").isEmpty());

        verify(kubernetesService, atLeastOnce()).getRunningPods();
    }

    @Test
    void testRefreshPods_Exception() throws Exception {
        when(kubernetesService.getRunningPods()).thenThrow(new RuntimeException("Refresh error"));

        mockMvc.perform(post("/api/pods/refresh"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").exists());

        verify(kubernetesService).getRunningPods();
    }

    @Test
    void testExportPodsToCsv_Success() throws Exception {
        String csvContent = "Name,Version\nnginx,alpine\nredis,7.2";
        when(kubernetesService.getRunningPods()).thenReturn(testPods);
        when(csvExportService.exportPodsToCsv(testPods)).thenReturn(csvContent);

        mockMvc.perform(get("/api/pods/export/csv"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv; charset=UTF-8"))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("filename=\"pods_")));

        verify(kubernetesService).getRunningPods();
        verify(csvExportService).exportPodsToCsv(testPods);
    }

    @Test
    void testExportPodsToCsv_Exception() throws Exception {
        when(kubernetesService.getRunningPods()).thenThrow(new RuntimeException("Export error"));

        mockMvc.perform(get("/api/pods/export/csv"))
                .andExpect(status().isInternalServerError());

        verify(kubernetesService).getRunningPods();
    }
}

