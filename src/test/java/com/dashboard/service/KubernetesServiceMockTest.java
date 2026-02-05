package com.dashboard.service;

import com.dashboard.config.KubernetesConfig;
import com.dashboard.model.PodInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KubernetesServiceMockTest {

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
        lenient().when(kubernetesConfig.getNamespace()).thenReturn("test-namespace");
        lenient().when(kubernetesConfig.isEnabled()).thenReturn(true);
        lenient().when(kubernetesConfig.getKubectlPath()).thenReturn("/usr/bin/kubectl");
    }

    // ===== getKubernetesConfig =====
    @Test
    void getKubernetesConfig_ShouldReturnConfig() {
        assertNotNull(kubernetesService.getKubernetesConfig());
        assertEquals(kubernetesConfig, kubernetesService.getKubernetesConfig());
    }

    // ===== getRunningPods =====
    @Test
    void getRunningPods_ShouldReturnPods_WhenEnabled() throws KubectlException {
        String jsonOutput = "{\"items\":[]}";
        PodInfo pod = new PodInfo();
        pod.setName("test-pod");

        when(kubectlExecutor.executeCommand(any(String[].class))).thenReturn(jsonOutput);
        when(podParser.parseKubectlOutput(jsonOutput)).thenReturn(List.of(pod));

        List<PodInfo> pods = kubernetesService.getRunningPods();

        assertEquals(1, pods.size());
        assertEquals("test-pod", pods.get(0).getName());
    }

    @Test
    void getRunningPods_ShouldReturnEmpty_WhenDisabled() {
        when(kubernetesConfig.isEnabled()).thenReturn(false);

        List<PodInfo> pods = kubernetesService.getRunningPods();

        assertTrue(pods.isEmpty());
    }

    @Test
    void getRunningPods_ShouldReturnEmpty_WhenKubectlException() throws KubectlException {
        when(kubectlExecutor.executeCommand(any(String[].class)))
                .thenThrow(new KubectlException("kubectl not found"));

        List<PodInfo> pods = kubernetesService.getRunningPods();

        assertTrue(pods.isEmpty());
    }

    @Test
    void getRunningPods_ShouldReturnEmpty_WhenGenericException() throws KubectlException {
        when(kubectlExecutor.executeCommand(any(String[].class)))
                .thenThrow(new RuntimeException("unexpected error"));

        List<PodInfo> pods = kubernetesService.getRunningPods();

        assertTrue(pods.isEmpty());
    }

    // ===== parseKubectlOutput =====
    @Test
    void parseKubectlOutput_ShouldDelegateToParser() {
        String json = "{\"items\":[]}";
        when(podParser.parseKubectlOutput(json)).thenReturn(List.of());

        List<PodInfo> result = kubernetesService.parseKubectlOutput(json);

        assertNotNull(result);
        verify(podParser).parseKubectlOutput(json);
    }

    // ===== getCurrentNamespace =====
    @Test
    void getCurrentNamespace_ShouldReturnNamespace() throws KubectlException {
        when(kubectlExecutor.executeCommand(any(String[].class))).thenReturn("my-namespace");

        String namespace = kubernetesService.getCurrentNamespace();

        assertEquals("my-namespace", namespace);
    }

    @Test
    void getCurrentNamespace_ShouldReturnDefault_WhenEmpty() throws KubectlException {
        when(kubectlExecutor.executeCommand(any(String[].class))).thenReturn("");

        String namespace = kubernetesService.getCurrentNamespace();

        assertEquals("default", namespace);
    }

    @Test
    void getCurrentNamespace_ShouldReturnDefault_WhenNull() throws KubectlException {
        when(kubectlExecutor.executeCommand(any(String[].class))).thenReturn(null);

        String namespace = kubernetesService.getCurrentNamespace();

        assertEquals("default", namespace);
    }

    @Test
    void getCurrentNamespace_ShouldReturnConfigNamespace_WhenKubectlException() throws KubectlException {
        when(kubectlExecutor.executeCommand(any(String[].class)))
                .thenThrow(new KubectlException("kubectl error"));

        String namespace = kubernetesService.getCurrentNamespace();

        assertEquals("test-namespace", namespace);
    }

    @Test
    void getCurrentNamespace_ShouldReturnConfigNamespace_WhenGenericException() throws KubectlException {
        when(kubectlExecutor.executeCommand(any(String[].class)))
                .thenThrow(new RuntimeException("unexpected error"));

        String namespace = kubernetesService.getCurrentNamespace();

        assertEquals("test-namespace", namespace);
    }

    // ===== countUniqueServices =====
    @Test
    void countUniqueServices_ShouldCountDistinctNames() {
        PodInfo pod1 = new PodInfo();
        pod1.setName("service-a");
        PodInfo pod2 = new PodInfo();
        pod2.setName("service-b");
        PodInfo pod3 = new PodInfo();
        pod3.setName("service-a"); // duplicate

        long count = kubernetesService.countUniqueServices(List.of(pod1, pod2, pod3));

        assertEquals(2, count);
    }

    @Test
    void countUniqueServices_ShouldIgnoreNullNames() {
        PodInfo pod1 = new PodInfo();
        pod1.setName(null);
        PodInfo pod2 = new PodInfo();
        pod2.setName("service-a");
        PodInfo pod3 = new PodInfo();
        pod3.setName("");

        long count = kubernetesService.countUniqueServices(List.of(pod1, pod2, pod3));

        assertEquals(1, count);
    }

    @Test
    void countUniqueServices_ShouldReturnZero_WhenEmpty() {
        long count = kubernetesService.countUniqueServices(List.of());
        assertEquals(0, count);
    }

    // ===== getKubernetesVersion =====
    @Test
    void getKubernetesVersion_ShouldReturnServerVersion() throws KubectlException {
        String json = "{\"serverVersion\":{\"gitVersion\":\"v1.28.0\"},\"clientVersion\":{\"gitVersion\":\"v1.28.0\"}}";
        when(kubectlExecutor.executeCommand("version", "-o", "json")).thenReturn(json);

        String version = kubernetesService.getKubernetesVersion();

        assertEquals("v1.28.0", version);
    }

    @Test
    void getKubernetesVersion_ShouldReturnClientVersion_WhenNoServerVersion() throws KubectlException {
        String json = "{\"clientVersion\":{\"gitVersion\":\"v1.28.0\"}}";
        when(kubectlExecutor.executeCommand("version", "-o", "json")).thenReturn(json);

        String version = kubernetesService.getKubernetesVersion();

        assertEquals("v1.28.0", version);
    }

    @Test
    void getKubernetesVersion_ShouldReturnUnknown_WhenNoVersions() throws KubectlException {
        String json = "{}";
        when(kubectlExecutor.executeCommand("version", "-o", "json")).thenReturn(json);

        String version = kubernetesService.getKubernetesVersion();

        assertEquals("Неизвестно", version);
    }

    @Test
    void getKubernetesVersion_ShouldReturnUnknown_WhenEmptyJson() throws KubectlException {
        when(kubectlExecutor.executeCommand("version", "-o", "json")).thenReturn("");

        String version = kubernetesService.getKubernetesVersion();

        assertEquals("Неизвестно", version);
    }

    @Test
    void getKubernetesVersion_ShouldReturnUnknown_WhenNullJson() throws KubectlException {
        when(kubectlExecutor.executeCommand("version", "-o", "json")).thenReturn(null);

        String version = kubernetesService.getKubernetesVersion();

        assertEquals("Неизвестно", version);
    }

    @Test
    void getKubernetesVersion_ShouldReturnUnknown_WhenKubectlException() throws KubectlException {
        when(kubectlExecutor.executeCommand("version", "-o", "json"))
                .thenThrow(new KubectlException("error"));

        String version = kubernetesService.getKubernetesVersion();

        assertEquals("Неизвестно", version);
    }

    @Test
    void getKubernetesVersion_ShouldReturnUnknown_WhenGenericException() throws KubectlException {
        when(kubectlExecutor.executeCommand("version", "-o", "json"))
                .thenThrow(new RuntimeException("unexpected"));

        String version = kubernetesService.getKubernetesVersion();

        assertEquals("Неизвестно", version);
    }

    @Test
    void getKubernetesVersion_ShouldReturnUnknown_WhenServerVersionBlank() throws KubectlException {
        String json = "{\"serverVersion\":{\"gitVersion\":\" \"}}";
        when(kubectlExecutor.executeCommand("version", "-o", "json")).thenReturn(json);

        String version = kubernetesService.getKubernetesVersion();

        assertEquals("Неизвестно", version);
    }

    // ===== getNamespaceQuota =====
    @Test
    void getNamespaceQuota_ShouldReturnQuota_WhenDataAvailable() throws KubectlException {
        String quotaJson = """
            {
                "items": [{
                    "status": {
                        "used": {
                            "requests.cpu": "2",
                            "requests.memory": "4Gi",
                            "pods": "5",
                            "configmaps": "10",
                            "secrets": "8"
                        },
                        "hard": {
                            "requests.cpu": "10",
                            "requests.memory": "20Gi",
                            "pods": "50",
                            "configmaps": "100",
                            "secrets": "50"
                        }
                    }
                }]
            }
        """;
        when(kubectlExecutor.executeCommand("get", "quota", "-n", "test-namespace", "-o", "json"))
                .thenReturn(quotaJson);

        KubernetesService.NamespaceQuota quota = kubernetesService.getNamespaceQuota();

        assertNotNull(quota);
        assertEquals("2", quota.cpuUsed);
        assertEquals("10", quota.cpuHard);
        assertEquals("4Gi", quota.memoryUsed);
        assertEquals("20Gi", quota.memoryHard);
        assertEquals("5", quota.podsUsed);
        assertEquals("50", quota.podsHard);
        assertEquals("10", quota.configmapsUsed);
        assertEquals("100", quota.configmapsHard);
        assertEquals("8", quota.secretsUsed);
        assertEquals("50", quota.secretsHard);
    }

    @Test
    void getNamespaceQuota_ShouldReturnNull_WhenEmptyJson() throws KubectlException {
        when(kubectlExecutor.executeCommand("get", "quota", "-n", "test-namespace", "-o", "json"))
                .thenReturn("");

        KubernetesService.NamespaceQuota quota = kubernetesService.getNamespaceQuota();

        assertNull(quota);
    }

    @Test
    void getNamespaceQuota_ShouldReturnNull_WhenNullJson() throws KubectlException {
        when(kubectlExecutor.executeCommand("get", "quota", "-n", "test-namespace", "-o", "json"))
                .thenReturn(null);

        KubernetesService.NamespaceQuota quota = kubernetesService.getNamespaceQuota();

        assertNull(quota);
    }

    @Test
    void getNamespaceQuota_ShouldReturnNull_WhenKubectlException() throws KubectlException {
        when(kubectlExecutor.executeCommand("get", "quota", "-n", "test-namespace", "-o", "json"))
                .thenThrow(new KubectlException("error"));

        KubernetesService.NamespaceQuota quota = kubernetesService.getNamespaceQuota();

        assertNull(quota);
    }

    @Test
    void getNamespaceQuota_ShouldReturnNull_WhenGenericException() throws KubectlException {
        when(kubectlExecutor.executeCommand("get", "quota", "-n", "test-namespace", "-o", "json"))
                .thenThrow(new RuntimeException("unexpected"));

        KubernetesService.NamespaceQuota quota = kubernetesService.getNamespaceQuota();

        assertNull(quota);
    }

    @Test
    void getNamespaceQuota_ShouldReturnEmptyQuota_WhenNoItems() throws KubectlException {
        String quotaJson = "{\"items\":[]}";
        when(kubectlExecutor.executeCommand("get", "quota", "-n", "test-namespace", "-o", "json"))
                .thenReturn(quotaJson);

        KubernetesService.NamespaceQuota quota = kubernetesService.getNamespaceQuota();

        assertNotNull(quota);
        assertNull(quota.cpuUsed);
    }

    @Test
    void getNamespaceQuota_ShouldHandlePartialData() throws KubectlException {
        String quotaJson = """
            {
                "items": [{
                    "status": {
                        "used": {
                            "requests.cpu": "2"
                        },
                        "hard": {
                            "requests.cpu": "10"
                        }
                    }
                }]
            }
        """;
        when(kubectlExecutor.executeCommand("get", "quota", "-n", "test-namespace", "-o", "json"))
                .thenReturn(quotaJson);

        KubernetesService.NamespaceQuota quota = kubernetesService.getNamespaceQuota();

        assertNotNull(quota);
        assertEquals("2", quota.cpuUsed);
        assertEquals("10", quota.cpuHard);
        assertNull(quota.memoryUsed);
    }

    // ===== getDatabaseClusterUrlFromSecrets =====
    @Test
    void getDatabaseClusterUrlFromSecrets_ShouldReturnNull_WhenServiceNameNull() {
        assertNull(kubernetesService.getDatabaseClusterUrlFromSecrets(null));
    }

    @Test
    void getDatabaseClusterUrlFromSecrets_ShouldReturnNull_WhenServiceNameEmpty() {
        assertNull(kubernetesService.getDatabaseClusterUrlFromSecrets(""));
    }

    @Test
    void getDatabaseClusterUrlFromSecrets_ShouldReturnNull_WhenEmptyJson() throws KubectlException {
        when(kubectlExecutor.executeCommand("get", "secrets", "-n", "test-namespace", "-o", "json"))
                .thenReturn("");

        assertNull(kubernetesService.getDatabaseClusterUrlFromSecrets("my-service"));
    }

    @Test
    void getDatabaseClusterUrlFromSecrets_ShouldReturnNull_WhenNullJson() throws KubectlException {
        when(kubectlExecutor.executeCommand("get", "secrets", "-n", "test-namespace", "-o", "json"))
                .thenReturn(null);

        assertNull(kubernetesService.getDatabaseClusterUrlFromSecrets("my-service"));
    }

    @Test
    void getDatabaseClusterUrlFromSecrets_ShouldReturnNull_WhenKubectlException() throws KubectlException {
        when(kubectlExecutor.executeCommand("get", "secrets", "-n", "test-namespace", "-o", "json"))
                .thenThrow(new KubectlException("error"));

        assertNull(kubernetesService.getDatabaseClusterUrlFromSecrets("my-service"));
    }

    @Test
    void getDatabaseClusterUrlFromSecrets_ShouldReturnNull_WhenGenericException() throws KubectlException {
        when(kubectlExecutor.executeCommand("get", "secrets", "-n", "test-namespace", "-o", "json"))
                .thenThrow(new RuntimeException("unexpected"));

        assertNull(kubernetesService.getDatabaseClusterUrlFromSecrets("my-service"));
    }

    @Test
    void getDatabaseClusterUrlFromSecrets_ShouldReturnNull_WhenNoItems() throws KubectlException {
        String secretsJson = "{\"items\":[]}";
        when(kubectlExecutor.executeCommand("get", "secrets", "-n", "test-namespace", "-o", "json"))
                .thenReturn(secretsJson);

        assertNull(kubernetesService.getDatabaseClusterUrlFromSecrets("my-service"));
    }

    @Test
    void getDatabaseClusterUrlFromSecrets_ShouldReturnUrl_WhenMatchingSecretFound() throws KubectlException {
        // "jdbc:postgresql://db-cluster:5432/mydb" encoded in base64
        String base64Value = java.util.Base64.getEncoder()
                .encodeToString("jdbc:postgresql://db-cluster:5432/mydb".getBytes());
        String secretsJson = """
            {
                "items": [{
                    "metadata": {"name": "my-service"},
                    "data": {
                        "DATABASE_CLUSTER_URL": "%s"
                    }
                }]
            }
        """.formatted(base64Value);
        when(kubectlExecutor.executeCommand("get", "secrets", "-n", "test-namespace", "-o", "json"))
                .thenReturn(secretsJson);

        String result = kubernetesService.getDatabaseClusterUrlFromSecrets("my-service");

        assertNotNull(result);
        assertEquals("jdbc:postgresql://db-cluster:5432/mydb", result);
    }

    @Test
    void getDatabaseClusterUrlFromSecrets_ShouldReturnNull_WhenNoMatchingSecret() throws KubectlException {
        String secretsJson = """
            {
                "items": [{
                    "metadata": {"name": "other-service"},
                    "data": {
                        "DATABASE_CLUSTER_URL": "dGVzdA=="
                    }
                }]
            }
        """;
        when(kubectlExecutor.executeCommand("get", "secrets", "-n", "test-namespace", "-o", "json"))
                .thenReturn(secretsJson);

        assertNull(kubernetesService.getDatabaseClusterUrlFromSecrets("my-service"));
    }

    @Test
    void getDatabaseClusterUrlFromSecrets_ShouldReturnNull_WhenNoItemsArray() throws KubectlException {
        String secretsJson = "{\"kind\":\"SecretList\"}";
        when(kubectlExecutor.executeCommand("get", "secrets", "-n", "test-namespace", "-o", "json"))
                .thenReturn(secretsJson);

        assertNull(kubernetesService.getDatabaseClusterUrlFromSecrets("my-service"));
    }

    @Test
    void getDatabaseClusterUrlFromSecrets_ShouldHandleSecretSuffix() throws KubectlException {
        String base64Value = java.util.Base64.getEncoder()
                .encodeToString("jdbc:postgresql://host:5432/db".getBytes());
        String secretsJson = """
            {
                "items": [{
                    "metadata": {"name": "my-service-secret"},
                    "data": {
                        "DATABASE_CLUSTER_URL": "%s"
                    }
                }]
            }
        """.formatted(base64Value);
        when(kubectlExecutor.executeCommand("get", "secrets", "-n", "test-namespace", "-o", "json"))
                .thenReturn(secretsJson);

        String result = kubernetesService.getDatabaseClusterUrlFromSecrets("my-service");

        assertNotNull(result);
        assertEquals("jdbc:postgresql://host:5432/db", result);
    }

    // ===== generateHtmlPage =====
    @Test
    void generateHtmlPage_ShouldGenerateHtml_WhenPodsExist() throws KubectlException {
        String jsonOutput = "{\"items\":[]}";
        PodInfo pod = new PodInfo();
        pod.setName("test-pod");
        pod.setVersion("1.0.0");
        pod.setPort("8080");
        pod.setCpuRequest("100m");
        pod.setMemoryRequest("256Mi");

        when(kubectlExecutor.executeCommand(any(String[].class))).thenReturn(jsonOutput);
        when(podParser.parseKubectlOutput(jsonOutput)).thenReturn(List.of(pod));

        String html = kubernetesService.generateHtmlPage();

        assertNotNull(html);
        assertTrue(html.contains("test-pod"));
        assertTrue(html.contains("1.0.0"));
        assertTrue(html.contains("8080"));
        assertTrue(html.contains("100m"));
        assertTrue(html.contains("256Mi"));
        assertTrue(html.contains("<table"));
        assertTrue(html.contains("iksweb"));
    }

    @Test
    void generateHtmlPage_ShouldGenerateHtml_WhenNoPods() throws KubectlException {
        when(kubernetesConfig.isEnabled()).thenReturn(false);

        String html = kubernetesService.generateHtmlPage();

        assertNotNull(html);
        assertTrue(html.contains("<table"));
    }

    @Test
    void generateHtmlPage_ShouldHandleNullFields() throws KubectlException {
        String jsonOutput = "{\"items\":[]}";
        PodInfo pod = new PodInfo();
        // All fields null

        when(kubectlExecutor.executeCommand(any(String[].class))).thenReturn(jsonOutput);
        when(podParser.parseKubectlOutput(jsonOutput)).thenReturn(List.of(pod));

        String html = kubernetesService.generateHtmlPage();

        assertNotNull(html);
        assertTrue(html.contains("<table"));
    }
}
