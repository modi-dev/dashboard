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
    
    @Test
    void testGetNamespaceQuota_Success() throws KubectlException {
        // Given
        String quotaJson = "{\"items\":[{\"status\":{\"used\":{\"requests.cpu\":\"2\",\"requests.memory\":\"4Gi\",\"pods\":\"5\",\"configmaps\":\"10\",\"secrets\":\"15\"},\"hard\":{\"requests.cpu\":\"10\",\"requests.memory\":\"20Gi\",\"pods\":\"50\",\"configmaps\":\"100\",\"secrets\":\"150\"}}}]}";
        when(kubernetesConfig.getNamespace()).thenReturn("test-namespace");
        when(kubectlExecutor.executeCommand("get", "quota", "-n", "test-namespace", "-o", "json"))
            .thenReturn(quotaJson);
        
        // When
        KubernetesService.NamespaceQuota quota = kubernetesService.getNamespaceQuota();
        
        // Then
        assertNotNull(quota);
        assertEquals("2", quota.cpuUsed);
        assertEquals("10", quota.cpuHard);
        assertEquals("4Gi", quota.memoryUsed);
        assertEquals("20Gi", quota.memoryHard);
        assertEquals("5", quota.podsUsed);
        assertEquals("50", quota.podsHard);
        assertEquals("10", quota.configmapsUsed);
        assertEquals("100", quota.configmapsHard);
        assertEquals("15", quota.secretsUsed);
        assertEquals("150", quota.secretsHard);
    }
    
    @Test
    void testGetNamespaceQuota_WhenEmptyResponse_ShouldReturnNull() throws KubectlException {
        // Given
        when(kubernetesConfig.getNamespace()).thenReturn("test-namespace");
        when(kubectlExecutor.executeCommand("get", "quota", "-n", "test-namespace", "-o", "json"))
            .thenReturn("");
        
        // When
        KubernetesService.NamespaceQuota quota = kubernetesService.getNamespaceQuota();
        
        // Then
        assertNull(quota);
    }
    
    @Test
    void testGetNamespaceQuota_WhenNullResponse_ShouldReturnNull() throws KubectlException {
        // Given
        when(kubernetesConfig.getNamespace()).thenReturn("test-namespace");
        when(kubectlExecutor.executeCommand("get", "quota", "-n", "test-namespace", "-o", "json"))
            .thenReturn(null);
        
        // When
        KubernetesService.NamespaceQuota quota = kubernetesService.getNamespaceQuota();
        
        // Then
        assertNull(quota);
    }
    
    @Test
    void testGetNamespaceQuota_WhenNoItems_ShouldReturnEmptyQuota() throws KubectlException {
        // Given
        String quotaJson = "{\"items\":[]}";
        when(kubernetesConfig.getNamespace()).thenReturn("test-namespace");
        when(kubectlExecutor.executeCommand("get", "quota", "-n", "test-namespace", "-o", "json"))
            .thenReturn(quotaJson);
        
        // When
        KubernetesService.NamespaceQuota quota = kubernetesService.getNamespaceQuota();
        
        // Then
        assertNotNull(quota);
        assertNull(quota.cpuUsed);
        assertNull(quota.cpuHard);
    }
    
    @Test
    void testGetNamespaceQuota_WhenKubectlException_ShouldReturnNull() throws KubectlException {
        // Given
        when(kubernetesConfig.getNamespace()).thenReturn("test-namespace");
        when(kubectlExecutor.executeCommand("get", "quota", "-n", "test-namespace", "-o", "json"))
            .thenThrow(new KubectlException("kubectl command failed"));
        
        // When
        KubernetesService.NamespaceQuota quota = kubernetesService.getNamespaceQuota();
        
        // Then
        assertNull(quota);
    }
    
    @Test
    void testGetNamespaceQuota_WhenGenericException_ShouldReturnNull() throws KubectlException {
        // Given
        when(kubernetesConfig.getNamespace()).thenReturn("test-namespace");
        when(kubectlExecutor.executeCommand("get", "quota", "-n", "test-namespace", "-o", "json"))
            .thenThrow(new RuntimeException("Unexpected error"));
        
        // When
        KubernetesService.NamespaceQuota quota = kubernetesService.getNamespaceQuota();
        
        // Then
        assertNull(quota);
    }
    
    @Test
    void testGetNamespaceQuota_WhenInvalidJson_ShouldReturnNull() throws KubectlException {
        // Given
        when(kubernetesConfig.getNamespace()).thenReturn("test-namespace");
        when(kubectlExecutor.executeCommand("get", "quota", "-n", "test-namespace", "-o", "json"))
            .thenReturn("invalid json");
        
        // When
        KubernetesService.NamespaceQuota quota = kubernetesService.getNamespaceQuota();
        
        // Then
        assertNull(quota);
    }
    
    @Test
    void testGetDatabaseClusterUrlFromSecrets_Success() throws KubectlException {
        // Given
        String serviceName = "test-service";
        String secretsJson = "{\"items\":[{\"metadata\":{\"name\":\"test-service-secret\"},\"data\":{\"DATABASE_CLUSTER_URL\":\"" + 
            java.util.Base64.getEncoder().encodeToString("jdbc:postgresql://localhost:5432/test".getBytes()) + "\"}}]}";
        when(kubernetesConfig.getNamespace()).thenReturn("test-namespace");
        when(kubectlExecutor.executeCommand("get", "secrets", "-n", "test-namespace", "-o", "json"))
            .thenReturn(secretsJson);
        
        // When
        String result = kubernetesService.getDatabaseClusterUrlFromSecrets(serviceName);
        
        // Then
        assertNotNull(result);
        assertTrue(result.contains("jdbc:postgresql://localhost:5432/test"));
    }
    
    @Test
    void testGetDatabaseClusterUrlFromSecrets_WhenNullServiceName_ShouldReturnNull() {
        // When
        String result = kubernetesService.getDatabaseClusterUrlFromSecrets(null);
        
        // Then
        assertNull(result);
    }
    
    @Test
    void testGetDatabaseClusterUrlFromSecrets_WhenEmptyServiceName_ShouldReturnNull() {
        // When
        String result = kubernetesService.getDatabaseClusterUrlFromSecrets("");
        
        // Then
        assertNull(result);
    }
    
    @Test
    void testGetDatabaseClusterUrlFromSecrets_WhenNoSecrets_ShouldReturnNull() throws KubectlException {
        // Given
        String serviceName = "test-service";
        String secretsJson = "{\"items\":[]}";
        when(kubernetesConfig.getNamespace()).thenReturn("test-namespace");
        when(kubectlExecutor.executeCommand("get", "secrets", "-n", "test-namespace", "-o", "json"))
            .thenReturn(secretsJson);
        
        // When
        String result = kubernetesService.getDatabaseClusterUrlFromSecrets(serviceName);
        
        // Then
        assertNull(result);
    }
    
    @Test
    void testGetDatabaseClusterUrlFromSecrets_WhenEmptyResponse_ShouldReturnNull() throws KubectlException {
        // Given
        String serviceName = "test-service";
        when(kubernetesConfig.getNamespace()).thenReturn("test-namespace");
        when(kubectlExecutor.executeCommand("get", "secrets", "-n", "test-namespace", "-o", "json"))
            .thenReturn("");
        
        // When
        String result = kubernetesService.getDatabaseClusterUrlFromSecrets(serviceName);
        
        // Then
        assertNull(result);
    }
    
    @Test
    void testGetDatabaseClusterUrlFromSecrets_WhenNullResponse_ShouldReturnNull() throws KubectlException {
        // Given
        String serviceName = "test-service";
        when(kubernetesConfig.getNamespace()).thenReturn("test-namespace");
        when(kubectlExecutor.executeCommand("get", "secrets", "-n", "test-namespace", "-o", "json"))
            .thenReturn(null);
        
        // When
        String result = kubernetesService.getDatabaseClusterUrlFromSecrets(serviceName);
        
        // Then
        assertNull(result);
    }
    
    @Test
    void testGetDatabaseClusterUrlFromSecrets_WhenKubectlException_ShouldReturnNull() throws KubectlException {
        // Given
        String serviceName = "test-service";
        when(kubernetesConfig.getNamespace()).thenReturn("test-namespace");
        when(kubectlExecutor.executeCommand("get", "secrets", "-n", "test-namespace", "-o", "json"))
            .thenThrow(new KubectlException("kubectl command failed"));
        
        // When
        String result = kubernetesService.getDatabaseClusterUrlFromSecrets(serviceName);
        
        // Then
        assertNull(result);
    }
    
    @Test
    void testGetDatabaseClusterUrlFromSecrets_WhenGenericException_ShouldReturnNull() throws KubectlException {
        // Given
        String serviceName = "test-service";
        when(kubernetesConfig.getNamespace()).thenReturn("test-namespace");
        when(kubectlExecutor.executeCommand("get", "secrets", "-n", "test-namespace", "-o", "json"))
            .thenThrow(new RuntimeException("Unexpected error"));
        
        // When
        String result = kubernetesService.getDatabaseClusterUrlFromSecrets(serviceName);
        
        // Then
        assertNull(result);
    }
    
    @Test
    void testGetDatabaseClusterUrlFromSecrets_WithMultipleMatchingSecrets() throws KubectlException {
        // Given
        String serviceName = "test-service";
        String url1 = "jdbc:postgresql://host1:5432/db1";
        String url2 = "jdbc:postgresql://host2:5432/db2";
        String secretsJson = "{\"items\":[" +
            "{\"metadata\":{\"name\":\"test-service-secret\"},\"data\":{\"DATABASE_CLUSTER_URL\":\"" + 
            java.util.Base64.getEncoder().encodeToString(url1.getBytes()) + "\"}}," +
            "{\"metadata\":{\"name\":\"test-service-secrets\"},\"data\":{\"DATABASE_CLUSTER_URL\":\"" + 
            java.util.Base64.getEncoder().encodeToString(url2.getBytes()) + "\"}}]}";
        when(kubernetesConfig.getNamespace()).thenReturn("test-namespace");
        when(kubectlExecutor.executeCommand("get", "secrets", "-n", "test-namespace", "-o", "json"))
            .thenReturn(secretsJson);
        
        // When
        String result = kubernetesService.getDatabaseClusterUrlFromSecrets(serviceName);
        
        // Then
        assertNotNull(result);
        assertTrue(result.contains(url1) || result.contains(url2));
    }
    
    @Test
    void testGetDatabaseClusterUrlFromSecrets_WithDifferentKeyFormats() throws KubectlException {
        // Given
        String serviceName = "test-service";
        String url = "jdbc:postgresql://localhost:5432/test";
        String secretsJson = "{\"items\":[{\"metadata\":{\"name\":\"test-service\"},\"data\":{\"database_cluster_url\":\"" + 
            java.util.Base64.getEncoder().encodeToString(url.getBytes()) + "\"}}]}";
        when(kubernetesConfig.getNamespace()).thenReturn("test-namespace");
        when(kubectlExecutor.executeCommand("get", "secrets", "-n", "test-namespace", "-o", "json"))
            .thenReturn(secretsJson);
        
        // When
        String result = kubernetesService.getDatabaseClusterUrlFromSecrets(serviceName);
        
        // Then
        assertNotNull(result);
        assertTrue(result.contains(url));
    }
    
    @Test
    void testGetDatabaseClusterUrlFromSecrets_WhenInvalidJson_ShouldReturnNull() throws KubectlException {
        // Given
        String serviceName = "test-service";
        when(kubernetesConfig.getNamespace()).thenReturn("test-namespace");
        when(kubectlExecutor.executeCommand("get", "secrets", "-n", "test-namespace", "-o", "json"))
            .thenReturn("invalid json");
        
        // When
        String result = kubernetesService.getDatabaseClusterUrlFromSecrets(serviceName);
        
        // Then
        assertNull(result);
    }
    
    @Test
    void testGetDatabaseClusterUrlFromSecrets_WhenNoItemsArray_ShouldReturnNull() throws KubectlException {
        // Given
        String serviceName = "test-service";
        String secretsJson = "{\"kind\":\"SecretList\"}";
        when(kubernetesConfig.getNamespace()).thenReturn("test-namespace");
        when(kubectlExecutor.executeCommand("get", "secrets", "-n", "test-namespace", "-o", "json"))
            .thenReturn(secretsJson);
        
        // When
        String result = kubernetesService.getDatabaseClusterUrlFromSecrets(serviceName);
        
        // Then
        assertNull(result);
    }
}
