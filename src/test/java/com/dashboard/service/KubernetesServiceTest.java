package com.dashboard.service;

import com.dashboard.config.KubernetesConfig;
import com.dashboard.model.PodInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Тесты для KubernetesService
 * Проверяет оркестрацию работы с KubectlCommandExecutor и KubernetesPodParser
 */
@ExtendWith(MockitoExtension.class)
class KubernetesServiceTest {
    
    @Mock
    private KubernetesConfig kubernetesConfig;
    
    @Mock
    private KubectlCommandExecutor kubectlExecutor;
    
    @Mock
    private KubernetesPodParser podParser;
    
    @InjectMocks
    private KubernetesService kubernetesService;
    
    @BeforeEach
    void setUp() {
        lenient().when(kubernetesConfig.isEnabled()).thenReturn(true);
        lenient().when(kubernetesConfig.getNamespace()).thenReturn("test-namespace");
        lenient().when(kubernetesConfig.getKubectlPath()).thenReturn("kubectl");
    }
    
    @Test
    void testGetRunningPods_WhenKubernetesDisabled_ShouldReturnEmptyList() {
        // Given
        when(kubernetesConfig.isEnabled()).thenReturn(false);
        
        // When
        List<PodInfo> result = kubernetesService.getRunningPods();
        
        // Then
        assertTrue(result.isEmpty());
        verify(kubernetesConfig).isEnabled();
        verifyNoInteractions(kubectlExecutor);
        verifyNoInteractions(podParser);
    }
    
    @Test
    void testGetRunningPods_WhenKubernetesEnabled_ShouldReturnPods() throws KubectlException {
        // Given
        when(kubernetesConfig.isEnabled()).thenReturn(true);
        when(kubernetesConfig.getNamespace()).thenReturn("test-namespace");
        
        String jsonOutput = "{\"items\":[]}";
        List<PodInfo> expectedPods = Arrays.asList(new PodInfo(), new PodInfo());
        
        when(kubectlExecutor.executeCommand(
            "get", "pods",
            "--field-selector=status.phase==Running",
            "-n", "test-namespace",
            "-o", "json"
        )).thenReturn(jsonOutput);
        
        when(podParser.parseKubectlOutput(jsonOutput)).thenReturn(expectedPods);
        
        // When
        List<PodInfo> result = kubernetesService.getRunningPods();
        
        // Then
        assertEquals(expectedPods, result);
        verify(kubectlExecutor).executeCommand(
            "get", "pods",
            "--field-selector=status.phase==Running",
            "-n", "test-namespace",
            "-o", "json"
        );
        verify(podParser).parseKubectlOutput(jsonOutput);
    }
    
    @Test
    void testGetRunningPods_WhenKubectlException_ShouldReturnEmptyList() throws KubectlException {
        // Given
        when(kubernetesConfig.isEnabled()).thenReturn(true);
        when(kubernetesConfig.getNamespace()).thenReturn("test-namespace");
        
        when(kubectlExecutor.executeCommand(anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenThrow(new KubectlException("kubectl command failed"));
        
        // When
        List<PodInfo> result = kubernetesService.getRunningPods();
        
        // Then
        assertTrue(result.isEmpty());
        verifyNoInteractions(podParser);
    }
    
    @Test
    void testGetCurrentNamespace_ShouldReturnNamespace() throws KubectlException {
        // Given
        when(kubectlExecutor.executeCommand(
            "config", "view",
            "--minify",
            "-o", "jsonpath={.contexts[0].context.namespace}"
        )).thenReturn("test-namespace");
        
        // When
        String result = kubernetesService.getCurrentNamespace();
        
        // Then
        assertEquals("test-namespace", result);
        verify(kubectlExecutor).executeCommand(
            "config", "view",
            "--minify",
            "-o", "jsonpath={.contexts[0].context.namespace}"
        );
    }
    
    @Test
    void testGetCurrentNamespace_WhenKubectlException_ShouldReturnConfigNamespace() throws KubectlException {
        // Given
        when(kubectlExecutor.executeCommand(anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenThrow(new KubectlException("kubectl command failed"));
        when(kubernetesConfig.getNamespace()).thenReturn("default-namespace");
        
        // When
        String result = kubernetesService.getCurrentNamespace();
        
        // Then
        assertEquals("default-namespace", result);
    }
    
    @Test
    void testGetCurrentNamespace_WhenEmptyResult_ShouldReturnDefault() throws KubectlException {
        // Given
        when(kubectlExecutor.executeCommand(anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn("");
        
        // When
        String result = kubernetesService.getCurrentNamespace();
        
        // Then
        assertEquals("default", result);
    }
    
    @Test
    void testGenerateHtmlPage_ShouldReturnValidHtml() throws KubectlException {
        // Given
        when(kubernetesConfig.isEnabled()).thenReturn(true);
        when(kubernetesConfig.getNamespace()).thenReturn("test-namespace");
        
        when(kubectlExecutor.executeCommand(anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn("{\"items\":[]}");
        when(podParser.parseKubectlOutput(anyString())).thenReturn(new ArrayList<>());
        
        when(kubectlExecutor.executeCommand(anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn("test-namespace");
        
        // When
        String html = kubernetesService.generateHtmlPage();
        
        // Then
        assertNotNull(html);
        assertTrue(html.contains("<html>"));
        assertTrue(html.contains("<body>"));
        assertTrue(html.contains("<table class='iksweb'>"));
        assertTrue(html.contains("<th>NAME</th>"));
        assertTrue(html.contains("<th>VERSION</th>"));
        assertTrue(html.contains("</html>"));
    }
    
    @Test
    void testGenerateHtmlPage_ShouldContainCssStyles() throws KubectlException {
        // Given
        when(kubernetesConfig.isEnabled()).thenReturn(true);
        when(kubernetesConfig.getNamespace()).thenReturn("test-namespace");
        
        when(kubectlExecutor.executeCommand(anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn("{\"items\":[]}");
        when(podParser.parseKubectlOutput(anyString())).thenReturn(new ArrayList<>());
        
        when(kubectlExecutor.executeCommand(anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn("test-namespace");
        
        // When
        String html = kubernetesService.generateHtmlPage();
        
        // Then
        assertTrue(html.contains("<style>"));
        assertTrue(html.contains("table.iksweb"));
        assertTrue(html.contains("background-color:#354251"));
        assertTrue(html.contains("</style>"));
    }
    
    @Test
    void testGetKubernetesConfig_ShouldReturnConfig() {
        // When
        KubernetesConfig config = kubernetesService.getKubernetesConfig();
        
        // Then
        assertNotNull(config);
        assertEquals(kubernetesConfig, config);
    }
    
    @Test
    void testParseKubectlOutput_ShouldDelegateToParser() {
        // Given
        String jsonOutput = "{\"items\":[]}";
        List<PodInfo> expectedPods = Arrays.asList(new PodInfo());
        when(podParser.parseKubectlOutput(jsonOutput)).thenReturn(expectedPods);
        
        // When
        List<PodInfo> result = kubernetesService.parseKubectlOutput(jsonOutput);
        
        // Then
        assertEquals(expectedPods, result);
        verify(podParser).parseKubectlOutput(jsonOutput);
    }
    
    @Test
    void testCountUniqueServices_WithEmptyList() {
        // Given
        List<PodInfo> emptyList = new ArrayList<>();
        
        // When
        long count = kubernetesService.countUniqueServices(emptyList);
        
        // Then
        assertEquals(0L, count);
    }
    
    @Test
    void testCountUniqueServices_WithUniquePods() {
        // Given
        PodInfo pod1 = new PodInfo();
        pod1.setName("nginx");
        
        PodInfo pod2 = new PodInfo();
        pod2.setName("redis");
        
        PodInfo pod3 = new PodInfo();
        pod3.setName("postgres");
        
        List<PodInfo> pods = Arrays.asList(pod1, pod2, pod3);
        
        // When
        long count = kubernetesService.countUniqueServices(pods);
        
        // Then
        assertEquals(3L, count);
    }
    
    @Test
    void testCountUniqueServices_WithDuplicateNames() {
        // Given
        PodInfo pod1 = new PodInfo();
        pod1.setName("nginx");
        
        PodInfo pod2 = new PodInfo();
        pod2.setName("nginx");
        
        PodInfo pod3 = new PodInfo();
        pod3.setName("redis");
        
        List<PodInfo> pods = Arrays.asList(pod1, pod2, pod3);
        
        // When
        long count = kubernetesService.countUniqueServices(pods);
        
        // Then
        assertEquals(2L, count);
    }
    
    @Test
    void testCountUniqueServices_WithNullNames() {
        // Given
        PodInfo pod1 = new PodInfo();
        pod1.setName("nginx");
        
        PodInfo pod2 = new PodInfo();
        pod2.setName(null);
        
        PodInfo pod3 = new PodInfo();
        pod3.setName("");
        
        List<PodInfo> pods = Arrays.asList(pod1, pod2, pod3);
        
        // When
        long count = kubernetesService.countUniqueServices(pods);
        
        // Then
        assertEquals(1L, count);
    }
    
    @Test
    void testGetKubernetesVersion_ShouldReturnVersion() throws KubectlException {
        // Given
        String json = "{\"serverVersion\":{\"gitVersion\":\"v1.28.0\"}}";
        when(kubectlExecutor.executeCommand("version", "-o", "json")).thenReturn(json);
        
        // When
        String version = kubernetesService.getKubernetesVersion();
        
        // Then
        assertEquals("v1.28.0", version);
        verify(kubectlExecutor).executeCommand("version", "-o", "json");
    }
    
    @Test
    void testGetKubernetesVersion_WhenException_ShouldReturnUnknown() throws KubectlException {
        // Given
        when(kubectlExecutor.executeCommand("version", "-o", "json"))
            .thenThrow(new KubectlException("kubectl command failed"));
        
        // When
        String version = kubernetesService.getKubernetesVersion();
        
        // Then
        assertEquals("Неизвестно", version);
    }
    
    @Test
    void testGetKubernetesVersion_WhenNullResponse_ShouldReturnUnknown() throws KubectlException {
        // Given
        when(kubectlExecutor.executeCommand("version", "-o", "json")).thenReturn(null);
        
        // When
        String version = kubernetesService.getKubernetesVersion();
        
        // Then
        assertEquals("Неизвестно", version);
    }
    
    @Test
    void testGetKubernetesVersion_WhenEmptyResponse_ShouldReturnUnknown() throws KubectlException {
        // Given
        when(kubectlExecutor.executeCommand("version", "-o", "json")).thenReturn("");
        
        // When
        String version = kubernetesService.getKubernetesVersion();
        
        // Then
        assertEquals("Неизвестно", version);
    }
    
    @Test
    void testGetKubernetesVersion_WhenOnlyClientVersion_ShouldReturnClientVersion() throws KubectlException {
        // Given
        String json = "{\"clientVersion\":{\"gitVersion\":\"v1.28.0\"}}";
        when(kubectlExecutor.executeCommand("version", "-o", "json")).thenReturn(json);
        
        // When
        String version = kubernetesService.getKubernetesVersion();
        
        // Then
        assertEquals("v1.28.0", version);
    }
    
    @Test
    void testGetKubernetesVersion_WhenServerVersionBlank_ShouldUseClientVersion() throws KubectlException {
        // Given
        String json = "{\"serverVersion\":{\"gitVersion\":\"\"},\"clientVersion\":{\"gitVersion\":\"v1.28.0\"}}";
        when(kubectlExecutor.executeCommand("version", "-o", "json")).thenReturn(json);
        
        // When
        String version = kubernetesService.getKubernetesVersion();
        
        // Then
        assertEquals("v1.28.0", version);
    }
    
    @Test
    void testGetKubernetesVersion_WhenNoGitVersion_ShouldReturnUnknown() throws KubectlException {
        // Given
        String json = "{\"serverVersion\":{},\"clientVersion\":{}}";
        when(kubectlExecutor.executeCommand("version", "-o", "json")).thenReturn(json);
        
        // When
        String version = kubernetesService.getKubernetesVersion();
        
        // Then
        assertEquals("Неизвестно", version);
    }
    
    @Test
    void testGetKubernetesVersion_WhenJsonParseException_ShouldReturnUnknown() throws KubectlException {
        // Given
        String invalidJson = "invalid json";
        when(kubectlExecutor.executeCommand("version", "-o", "json")).thenReturn(invalidJson);
        
        // When
        String version = kubernetesService.getKubernetesVersion();
        
        // Then
        assertEquals("Неизвестно", version);
    }
    
    @Test
    void testGetRunningPods_WhenGenericException_ShouldReturnEmptyList() throws KubectlException {
        // Given
        when(kubernetesConfig.isEnabled()).thenReturn(true);
        when(kubernetesConfig.getNamespace()).thenReturn("test-namespace");
        
        when(kubectlExecutor.executeCommand(anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenThrow(new RuntimeException("Unexpected error"));
        
        // When
        List<PodInfo> result = kubernetesService.getRunningPods();
        
        // Then
        assertTrue(result.isEmpty());
        verifyNoInteractions(podParser);
    }
    
    @Test
    void testGetCurrentNamespace_WhenGenericException_ShouldReturnConfigNamespace() throws KubectlException {
        // Given - simulate exception from kubectl
        when(kubectlExecutor.executeCommand(anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenThrow(new RuntimeException("Unexpected error"));
        lenient().when(kubernetesConfig.getNamespace()).thenReturn("default-namespace");
        
        // When
        String result = kubernetesService.getCurrentNamespace();
        
        // Then - should fall back to config namespace
        assertEquals("default-namespace", result);
    }
    
    @Test
    void testGetCurrentNamespace_WhenWhitespaceResult_ShouldReturnDefault() throws KubectlException {
        // Given
        when(kubectlExecutor.executeCommand(anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn("   ");
        
        // When
        String result = kubernetesService.getCurrentNamespace();
        
        // Then
        assertEquals("default", result);
    }
}
