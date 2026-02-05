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
    
    // ===== Tests for getNamespaceQuota() =====
    
    @Test
    void testGetNamespaceQuota_WhenValidQuota_ShouldReturnQuota() throws KubectlException {
        // Given
        String quotaJson = """
            {
                "items": [{
                    "status": {
                        "used": {
                            "requests.cpu": "2",
                            "requests.memory": "4Gi",
                            "pods": "10",
                            "configmaps": "5",
                            "secrets": "3"
                        },
                        "hard": {
                            "requests.cpu": "4",
                            "requests.memory": "8Gi",
                            "pods": "20",
                            "configmaps": "10",
                            "secrets": "10"
                        }
                    }
                }]
            }
            """;
        when(kubernetesConfig.getNamespace()).thenReturn("test-namespace");
        when(kubectlExecutor.executeCommand("get", "quota", "-n", "test-namespace", "-o", "json"))
            .thenReturn(quotaJson);
        
        // When
        KubernetesService.NamespaceQuota quota = kubernetesService.getNamespaceQuota();
        
        // Then
        assertNotNull(quota);
        assertEquals("2", quota.cpuUsed);
        assertEquals("4", quota.cpuHard);
        assertEquals("4Gi", quota.memoryUsed);
        assertEquals("8Gi", quota.memoryHard);
        assertEquals("10", quota.podsUsed);
        assertEquals("20", quota.podsHard);
        assertEquals("5", quota.configmapsUsed);
        assertEquals("10", quota.configmapsHard);
        assertEquals("3", quota.secretsUsed);
        assertEquals("10", quota.secretsHard);
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
    void testGetNamespaceQuota_WhenEmptyResponse_ShouldReturnNull() throws KubectlException {
        // Given
        when(kubernetesConfig.getNamespace()).thenReturn("test-namespace");
        when(kubectlExecutor.executeCommand("get", "quota", "-n", "test-namespace", "-o", "json"))
            .thenReturn("   ");
        
        // When
        KubernetesService.NamespaceQuota quota = kubernetesService.getNamespaceQuota();
        
        // Then
        assertNull(quota);
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
    void testGetNamespaceQuota_WhenEmptyItems_ShouldReturnEmptyQuota() throws KubectlException {
        // Given
        String quotaJson = "{\"items\": []}";
        when(kubernetesConfig.getNamespace()).thenReturn("test-namespace");
        when(kubectlExecutor.executeCommand("get", "quota", "-n", "test-namespace", "-o", "json"))
            .thenReturn(quotaJson);
        
        // When
        KubernetesService.NamespaceQuota quota = kubernetesService.getNamespaceQuota();
        
        // Then
        assertNotNull(quota);
        // Все поля должны быть null, так как items пустой
        assertNull(quota.cpuUsed);
    }
    
    @Test
    void testGetNamespaceQuota_WhenNoStatus_ShouldReturnEmptyQuota() throws KubectlException {
        // Given
        String quotaJson = "{\"items\": [{}]}";
        when(kubernetesConfig.getNamespace()).thenReturn("test-namespace");
        when(kubectlExecutor.executeCommand("get", "quota", "-n", "test-namespace", "-o", "json"))
            .thenReturn(quotaJson);
        
        // When
        KubernetesService.NamespaceQuota quota = kubernetesService.getNamespaceQuota();
        
        // Then
        assertNotNull(quota);
        assertNull(quota.cpuUsed);
    }
    
    @Test
    void testGetNamespaceQuota_WhenPartialQuotaData_ShouldReturnPartialQuota() throws KubectlException {
        // Given
        String quotaJson = """
            {
                "items": [{
                    "status": {
                        "used": {
                            "requests.cpu": "2"
                        },
                        "hard": {
                            "requests.memory": "8Gi"
                        }
                    }
                }]
            }
            """;
        when(kubernetesConfig.getNamespace()).thenReturn("test-namespace");
        when(kubectlExecutor.executeCommand("get", "quota", "-n", "test-namespace", "-o", "json"))
            .thenReturn(quotaJson);
        
        // When
        KubernetesService.NamespaceQuota quota = kubernetesService.getNamespaceQuota();
        
        // Then
        assertNotNull(quota);
        assertEquals("2", quota.cpuUsed);
        assertNull(quota.cpuHard);
        assertNull(quota.memoryUsed);
        assertEquals("8Gi", quota.memoryHard);
    }
    
    // ===== Tests for getDatabaseClusterUrlFromSecrets() =====
    
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
    void testGetDatabaseClusterUrlFromSecrets_WhenNullResponse_ShouldReturnNull() throws KubectlException {
        // Given
        when(kubernetesConfig.getNamespace()).thenReturn("test-namespace");
        when(kubectlExecutor.executeCommand("get", "secrets", "-n", "test-namespace", "-o", "json"))
            .thenReturn(null);
        
        // When
        String result = kubernetesService.getDatabaseClusterUrlFromSecrets("my-service");
        
        // Then
        assertNull(result);
    }
    
    @Test
    void testGetDatabaseClusterUrlFromSecrets_WhenEmptyResponse_ShouldReturnNull() throws KubectlException {
        // Given
        when(kubernetesConfig.getNamespace()).thenReturn("test-namespace");
        when(kubectlExecutor.executeCommand("get", "secrets", "-n", "test-namespace", "-o", "json"))
            .thenReturn("   ");
        
        // When
        String result = kubernetesService.getDatabaseClusterUrlFromSecrets("my-service");
        
        // Then
        assertNull(result);
    }
    
    @Test
    void testGetDatabaseClusterUrlFromSecrets_WhenNoItems_ShouldReturnNull() throws KubectlException {
        // Given
        String secretsJson = "{\"kind\": \"SecretList\"}";
        when(kubernetesConfig.getNamespace()).thenReturn("test-namespace");
        when(kubectlExecutor.executeCommand("get", "secrets", "-n", "test-namespace", "-o", "json"))
            .thenReturn(secretsJson);
        
        // When
        String result = kubernetesService.getDatabaseClusterUrlFromSecrets("my-service");
        
        // Then
        assertNull(result);
    }
    
    @Test
    void testGetDatabaseClusterUrlFromSecrets_WhenNoMatchingSecrets_ShouldReturnNull() throws KubectlException {
        // Given
        String secretsJson = """
            {
                "items": [{
                    "metadata": {"name": "other-service"},
                    "data": {"DATABASE_CLUSTER_URL": "dGVzdC11cmw="}
                }]
            }
            """;
        when(kubernetesConfig.getNamespace()).thenReturn("test-namespace");
        when(kubectlExecutor.executeCommand("get", "secrets", "-n", "test-namespace", "-o", "json"))
            .thenReturn(secretsJson);
        
        // When
        String result = kubernetesService.getDatabaseClusterUrlFromSecrets("my-service");
        
        // Then
        assertNull(result);
    }
    
    @Test
    void testGetDatabaseClusterUrlFromSecrets_WhenMatchingSecret_ShouldReturnDecodedUrl() throws KubectlException {
        // Given - "test-url" encoded in base64 is "dGVzdC11cmw="
        String secretsJson = """
            {
                "items": [{
                    "metadata": {"name": "my-service"},
                    "data": {"DATABASE_CLUSTER_URL": "dGVzdC11cmw="}
                }]
            }
            """;
        when(kubernetesConfig.getNamespace()).thenReturn("test-namespace");
        when(kubectlExecutor.executeCommand("get", "secrets", "-n", "test-namespace", "-o", "json"))
            .thenReturn(secretsJson);
        
        // When
        String result = kubernetesService.getDatabaseClusterUrlFromSecrets("my-service");
        
        // Then
        assertEquals("test-url", result);
    }
    
    @Test
    void testGetDatabaseClusterUrlFromSecrets_WhenSecretHasSuffix_ShouldReturnDecodedUrl() throws KubectlException {
        // Given - "test-url" encoded in base64 is "dGVzdC11cmw="
        String secretsJson = """
            {
                "items": [{
                    "metadata": {"name": "my-service-secret"},
                    "data": {"DATABASE_CLUSTER_URL": "dGVzdC11cmw="}
                }]
            }
            """;
        when(kubernetesConfig.getNamespace()).thenReturn("test-namespace");
        when(kubectlExecutor.executeCommand("get", "secrets", "-n", "test-namespace", "-o", "json"))
            .thenReturn(secretsJson);
        
        // When
        String result = kubernetesService.getDatabaseClusterUrlFromSecrets("my-service");
        
        // Then
        assertEquals("test-url", result);
    }
    
    @Test
    void testGetDatabaseClusterUrlFromSecrets_WhenKubectlException_ShouldReturnNull() throws KubectlException {
        // Given
        when(kubernetesConfig.getNamespace()).thenReturn("test-namespace");
        when(kubectlExecutor.executeCommand("get", "secrets", "-n", "test-namespace", "-o", "json"))
            .thenThrow(new KubectlException("kubectl command failed"));
        
        // When
        String result = kubernetesService.getDatabaseClusterUrlFromSecrets("my-service");
        
        // Then
        assertNull(result);
    }
    
    @Test
    void testGetDatabaseClusterUrlFromSecrets_WhenGenericException_ShouldReturnNull() throws KubectlException {
        // Given
        when(kubernetesConfig.getNamespace()).thenReturn("test-namespace");
        when(kubectlExecutor.executeCommand("get", "secrets", "-n", "test-namespace", "-o", "json"))
            .thenThrow(new RuntimeException("Unexpected error"));
        
        // When
        String result = kubernetesService.getDatabaseClusterUrlFromSecrets("my-service");
        
        // Then
        assertNull(result);
    }
    
    @Test
    void testGetDatabaseClusterUrlFromSecrets_WhenNoDataInSecret_ShouldReturnNull() throws KubectlException {
        // Given
        String secretsJson = """
            {
                "items": [{
                    "metadata": {"name": "my-service"}
                }]
            }
            """;
        when(kubernetesConfig.getNamespace()).thenReturn("test-namespace");
        when(kubectlExecutor.executeCommand("get", "secrets", "-n", "test-namespace", "-o", "json"))
            .thenReturn(secretsJson);
        
        // When
        String result = kubernetesService.getDatabaseClusterUrlFromSecrets("my-service");
        
        // Then
        assertNull(result);
    }
    
    @Test
    void testGetDatabaseClusterUrlFromSecrets_WhenNoDatabaseUrlInSecret_ShouldReturnNull() throws KubectlException {
        // Given
        String secretsJson = """
            {
                "items": [{
                    "metadata": {"name": "my-service"},
                    "data": {"OTHER_KEY": "dGVzdA=="}
                }]
            }
            """;
        when(kubernetesConfig.getNamespace()).thenReturn("test-namespace");
        when(kubectlExecutor.executeCommand("get", "secrets", "-n", "test-namespace", "-o", "json"))
            .thenReturn(secretsJson);
        
        // When
        String result = kubernetesService.getDatabaseClusterUrlFromSecrets("my-service");
        
        // Then
        assertNull(result);
    }
    
    @Test
    void testGetDatabaseClusterUrlFromSecrets_WhenMultipleMatchingSecrets_ShouldReturnAllUrls() throws KubectlException {
        // Given - "url1" in base64 is "dXJsMQ==", "url2" in base64 is "dXJsMg=="
        String secretsJson = """
            {
                "items": [
                    {
                        "metadata": {"name": "my-service"},
                        "data": {"DATABASE_CLUSTER_URL": "dXJsMQ=="}
                    },
                    {
                        "metadata": {"name": "my-service-secret"},
                        "data": {"DATABASE_CLUSTER_URL": "dXJsMg=="}
                    }
                ]
            }
            """;
        when(kubernetesConfig.getNamespace()).thenReturn("test-namespace");
        when(kubectlExecutor.executeCommand("get", "secrets", "-n", "test-namespace", "-o", "json"))
            .thenReturn(secretsJson);
        
        // When
        String result = kubernetesService.getDatabaseClusterUrlFromSecrets("my-service");
        
        // Then
        assertNotNull(result);
        assertTrue(result.contains("url1"));
        assertTrue(result.contains("url2"));
    }
    
    @Test
    void testGetDatabaseClusterUrlFromSecrets_WhenSecretContainsServiceName_ShouldMatch() throws KubectlException {
        // Given - "test-url" encoded in base64 is "dGVzdC11cmw="
        String secretsJson = """
            {
                "items": [{
                    "metadata": {"name": "prefix-my-service-suffix"},
                    "data": {"DATABASE_CLUSTER_URL": "dGVzdC11cmw="}
                }]
            }
            """;
        when(kubernetesConfig.getNamespace()).thenReturn("test-namespace");
        when(kubectlExecutor.executeCommand("get", "secrets", "-n", "test-namespace", "-o", "json"))
            .thenReturn(secretsJson);
        
        // When
        String result = kubernetesService.getDatabaseClusterUrlFromSecrets("my-service");
        
        // Then
        assertEquals("test-url", result);
    }
    
    @Test
    void testGetDatabaseClusterUrlFromSecrets_WhenServiceNameContainsSecretName_ShouldMatch() throws KubectlException {
        // Given - "test-url" encoded in base64 is "dGVzdC11cmw="
        String secretsJson = """
            {
                "items": [{
                    "metadata": {"name": "service"},
                    "data": {"DATABASE_CLUSTER_URL": "dGVzdC11cmw="}
                }]
            }
            """;
        when(kubernetesConfig.getNamespace()).thenReturn("test-namespace");
        when(kubectlExecutor.executeCommand("get", "secrets", "-n", "test-namespace", "-o", "json"))
            .thenReturn(secretsJson);
        
        // When
        String result = kubernetesService.getDatabaseClusterUrlFromSecrets("my-service-name");
        
        // Then
        assertEquals("test-url", result);
    }
    
    @Test
    void testGetDatabaseClusterUrlFromSecrets_WhenKeyContainsDatabaseClusterUrl_ShouldMatch() throws KubectlException {
        // Given - "test-url" encoded in base64 is "dGVzdC11cmw="
        String secretsJson = """
            {
                "items": [{
                    "metadata": {"name": "my-service"},
                    "data": {"APP_DATABASE_CLUSTER_URL_PRIMARY": "dGVzdC11cmw="}
                }]
            }
            """;
        when(kubernetesConfig.getNamespace()).thenReturn("test-namespace");
        when(kubectlExecutor.executeCommand("get", "secrets", "-n", "test-namespace", "-o", "json"))
            .thenReturn(secretsJson);
        
        // When
        String result = kubernetesService.getDatabaseClusterUrlFromSecrets("my-service");
        
        // Then
        assertEquals("test-url", result);
    }
    
    @Test
    void testGetDatabaseClusterUrlFromSecrets_WhenKeyHasDatabaseAndClusterAndUrl_ShouldMatch() throws KubectlException {
        // Given - "test-url" encoded in base64 is "dGVzdC11cmw="
        String secretsJson = """
            {
                "items": [{
                    "metadata": {"name": "my-service"},
                    "data": {"DATABASE_CONNECTION_CLUSTER_SERVICE_URL": "dGVzdC11cmw="}
                }]
            }
            """;
        when(kubernetesConfig.getNamespace()).thenReturn("test-namespace");
        when(kubectlExecutor.executeCommand("get", "secrets", "-n", "test-namespace", "-o", "json"))
            .thenReturn(secretsJson);
        
        // When
        String result = kubernetesService.getDatabaseClusterUrlFromSecrets("my-service");
        
        // Then
        assertEquals("test-url", result);
    }
    
    // ===== Tests for generateHtmlPage() with actual pod data =====
    
    @Test
    void testGenerateHtmlPage_WithPods_ShouldContainPodData() throws KubectlException {
        // Given
        when(kubernetesConfig.isEnabled()).thenReturn(true);
        when(kubernetesConfig.getNamespace()).thenReturn("test-namespace");
        
        PodInfo pod1 = new PodInfo();
        pod1.setName("my-app");
        pod1.setVersion("1.0.0");
        pod1.setMsBranch("main");
        pod1.setConfigBranch("config-main");
        pod1.setGcOptions("-XX:+UseG1GC");
        pod1.setPort("8080");
        pod1.setCpuRequest("500m");
        pod1.setMemoryRequest("512Mi");
        pod1.setCreationDate(java.time.LocalDateTime.of(2024, 1, 15, 10, 30, 0));
        
        List<PodInfo> pods = Arrays.asList(pod1);
        
        // Mock for getRunningPods() call - exact arguments
        when(kubectlExecutor.executeCommand(
            "get", "pods",
            "--field-selector=status.phase==Running",
            "-n", "test-namespace",
            "-o", "json"
        )).thenReturn("{\"items\":[]}");
        when(podParser.parseKubectlOutput("{\"items\":[]}")).thenReturn(pods);
        
        // Mock for getCurrentNamespace() call - exact arguments
        when(kubectlExecutor.executeCommand(
            "config", "view",
            "--minify",
            "-o", "jsonpath={.contexts[0].context.namespace}"
        )).thenReturn("test-namespace");
        
        // When
        String html = kubernetesService.generateHtmlPage();
        
        // Then
        assertNotNull(html);
        assertTrue(html.contains("my-app"), "HTML should contain pod name 'my-app'");
        assertTrue(html.contains("1.0.0"), "HTML should contain version '1.0.0'");
        assertTrue(html.contains("main"), "HTML should contain msBranch 'main'");
        assertTrue(html.contains("config-main"), "HTML should contain configBranch 'config-main'");
        assertTrue(html.contains("-XX:+UseG1GC"), "HTML should contain gcOptions");
        assertTrue(html.contains("8080"), "HTML should contain port '8080'");
        assertTrue(html.contains("500m"), "HTML should contain cpuRequest '500m'");
        assertTrue(html.contains("512Mi"), "HTML should contain memoryRequest '512Mi'");
        // Check date format - the HTML generation uses formatter "yyyy-MM-dd HH:mm:ss"
        assertTrue(html.contains("2024-01-15") && html.contains("10:30"), "HTML should contain creation date");
    }
    
    @Test
    void testGenerateHtmlPage_WithNullPodFields_ShouldHandleGracefully() throws KubectlException {
        // Given
        when(kubernetesConfig.isEnabled()).thenReturn(true);
        when(kubernetesConfig.getNamespace()).thenReturn("test-namespace");
        
        PodInfo pod = new PodInfo();
        // All fields are null
        
        List<PodInfo> pods = Arrays.asList(pod);
        
        when(kubectlExecutor.executeCommand(anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn("{\"items\":[]}");
        when(podParser.parseKubectlOutput(anyString())).thenReturn(pods);
        
        when(kubectlExecutor.executeCommand(anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn("test-namespace");
        
        // When
        String html = kubernetesService.generateHtmlPage();
        
        // Then
        assertNotNull(html);
        assertTrue(html.contains("<tr>"));
        assertTrue(html.contains("</tr>"));
        // Should not throw NPE
    }
    
    @Test
    void testGenerateHtmlPage_ShouldContainUpdateTime() throws KubectlException {
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
        assertTrue(html.contains("update time:"));
        assertTrue(html.contains("namespace:"));
        assertTrue(html.contains("UTC"));
    }
}
