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
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = VersionController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@TestPropertySource(properties = "security.enabled=false")
class VersionControllerTest {

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
        when(kubernetesConfig.getNamespace()).thenReturn("default");
        when(clusterInfoSyncService.getNamespace()).thenReturn("default");
    }

    @Test
    void testGetRunningPods_Success() throws Exception {
        when(podRepository.findByNamespaceOrderByName("default")).thenReturn(testPods);

        mockMvc.perform(get("/dashboard/api/pods/pods"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("nginx"))
                .andExpect(jsonPath("$[1].name").value("redis"));

        verify(podRepository, atLeastOnce()).findByNamespaceOrderByName("default");
    }

    @Test
    void testGetRunningPods_Exception() throws Exception {
        when(podRepository.findByNamespaceOrderByName("default")).thenThrow(new RuntimeException("Kubernetes error"));

        mockMvc.perform(get("/dashboard/api/pods/pods"))
                .andExpect(status().isInternalServerError());

        verify(podRepository).findByNamespaceOrderByName("default");
    }

    @Test
    void testGetHtmlPage_Success() throws Exception {
        String htmlContent = "<html><body>Test HTML</body></html>";
        when(kubernetesService.generateHtmlPage()).thenReturn(htmlContent);

        mockMvc.perform(get("/dashboard/api/pods/html"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(htmlContent));

        verify(kubernetesService).generateHtmlPage();
    }

    @Test
    void testGetHtmlPage_Exception() throws Exception {
        when(kubernetesService.generateHtmlPage()).thenThrow(new RuntimeException("Generation error"));

        mockMvc.perform(get("/dashboard/api/pods/html"))
                .andExpect(status().isInternalServerError());

        verify(kubernetesService).generateHtmlPage();
    }

    @Test
    void testGetCurrentNamespace_Success() throws Exception {
        when(kubernetesService.getCurrentNamespace()).thenReturn("default");

        mockMvc.perform(get("/dashboard/api/pods/namespace"))
                .andExpect(status().isOk())
                .andExpect(content().string("default"));

        verify(kubernetesService).getCurrentNamespace();
    }

    @Test
    void testGetCurrentNamespace_Exception() throws Exception {
        when(kubernetesService.getCurrentNamespace()).thenThrow(new RuntimeException("Namespace error"));

        mockMvc.perform(get("/dashboard/api/pods/namespace"))
                .andExpect(status().isInternalServerError());

        verify(kubernetesService).getCurrentNamespace();
    }

    @Test
    void testGetPodsSummary_Success() throws Exception {
        when(kubernetesService.getRunningPods()).thenReturn(testPods);

        mockMvc.perform(get("/dashboard/api/pods/summary"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("nginx"));

        verify(kubernetesService, atLeastOnce()).getRunningPods();
    }

    @Test
    void testGetPodByName_Success() throws Exception {
        when(kubernetesService.getRunningPods()).thenReturn(testPods);

        mockMvc.perform(get("/dashboard/api/pods/pods/nginx"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("nginx"));

        verify(kubernetesService, atLeastOnce()).getRunningPods();
    }

    @Test
    void testGetPodByName_NotFound() throws Exception {
        when(kubernetesService.getRunningPods()).thenReturn(testPods);

        mockMvc.perform(get("/dashboard/api/pods/pods/nonexistent"))
                .andExpect(status().isNotFound());

        verify(kubernetesService).getRunningPods();
    }

    @Test
    void testCheckKubernetesHealth_Success() throws Exception {
        when(kubernetesService.getCurrentNamespace()).thenReturn("default");

        mockMvc.perform(get("/dashboard/api/pods/health"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Kubernetes API доступен")));

        verify(kubernetesService).getCurrentNamespace();
    }

    @Test
    void testCheckKubernetesHealth_Exception() throws Exception {
        when(kubernetesService.getCurrentNamespace()).thenThrow(new RuntimeException("Health check error"));

        mockMvc.perform(get("/dashboard/api/pods/health"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Kubernetes API недоступен")));

        verify(kubernetesService).getCurrentNamespace();
    }

    @Test
    void testTestKubernetesService_Success() throws Exception {
        when(kubernetesService.getKubernetesConfig()).thenReturn(mockConfig);
        when(kubernetesService.getRunningPods()).thenReturn(testPods);

        mockMvc.perform(get("/dashboard/api/pods/test"))
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

        mockMvc.perform(get("/dashboard/api/pods/test"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("error"));

        verify(kubernetesService, atLeastOnce()).getKubernetesConfig();
        verify(kubernetesService).getRunningPods();
    }

    @Test
    void testGetKubernetesConfig_Success() throws Exception {
        when(kubernetesService.getKubernetesConfig()).thenReturn(mockConfig);

        mockMvc.perform(get("/dashboard/api/pods/config"))
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

        mockMvc.perform(get("/dashboard/api/pods/info"))
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

        mockMvc.perform(get("/dashboard/api/pods/info"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("error"));

        verify(kubernetesService, atLeastOnce()).getKubernetesConfig();
        verify(kubernetesService).getRunningPods();
    }

    @Test
    void testRefreshPods_Success() throws Exception {
        when(podsSyncService.syncPods()).thenReturn(testPods.size());

        mockMvc.perform(post("/dashboard/api/pods/refresh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.containsString("Информация о подах успешно обновлена")))
                .andExpect(jsonPath("$.error").isEmpty())
                .andExpect(jsonPath("$.message").isEmpty());

        verify(podsSyncService, atLeastOnce()).syncPods();
    }

    @Test
    void testRefreshPods_Exception() throws Exception {
        when(podsSyncService.syncPods()).thenThrow(new RuntimeException("Refresh error"));

        mockMvc.perform(post("/dashboard/api/pods/refresh"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").exists());

        verify(podsSyncService).syncPods();
    }

    @Test
    void testExportPodsToCsv_Success() throws Exception {
        String csvContent = "Name,Version\nnginx,alpine\nredis,7.2";
        when(podRepository.findByNamespaceOrderByName("default")).thenReturn(testPods);
        when(csvExportService.exportPodsToCsv(testPods)).thenReturn(csvContent);

        mockMvc.perform(get("/dashboard/api/pods/export/csv"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv; charset=UTF-8"))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("filename=\"pods_")));

        verify(podRepository).findByNamespaceOrderByName("default");
        verify(csvExportService).exportPodsToCsv(testPods);
    }

    @Test
    void testExportPodsToCsv_Exception() throws Exception {
        when(podRepository.findByNamespaceOrderByName("default")).thenThrow(new RuntimeException("Export error"));

        mockMvc.perform(get("/dashboard/api/pods/export/csv"))
                .andExpect(status().isInternalServerError());

        verify(podRepository).findByNamespaceOrderByName("default");
    }
}

