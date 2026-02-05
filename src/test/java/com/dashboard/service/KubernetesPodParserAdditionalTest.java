package com.dashboard.service;

import com.dashboard.model.PodInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KubernetesPodParserAdditionalTest {

    private KubernetesPodParser parser;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        parser = new KubernetesPodParser();
        objectMapper = new ObjectMapper();
    }

    @Test
    void parseKubectlOutput_ShouldReturnEmpty_WhenNoItems() {
        List<PodInfo> pods = parser.parseKubectlOutput("{}");
        assertTrue(pods.isEmpty());
    }

    @Test
    void parseKubectlOutput_ShouldReturnEmpty_WhenItemsNotArray() {
        List<PodInfo> pods = parser.parseKubectlOutput("{\"items\":\"not-array\"}");
        assertTrue(pods.isEmpty());
    }

    @Test
    void parseKubectlOutput_ShouldReturnEmpty_WhenInvalidJson() {
        List<PodInfo> pods = parser.parseKubectlOutput("not json");
        assertTrue(pods.isEmpty());
    }

    @Test
    void parseKubectlOutput_ShouldReturnEmpty_WhenEmptyItems() {
        List<PodInfo> pods = parser.parseKubectlOutput("{\"items\":[]}");
        assertTrue(pods.isEmpty());
    }

    @Test
    void parsePodJson_ShouldHandleFullPod() throws Exception {
        String json = """
        {
            "metadata": {
                "name": "my-pod-abc123",
                "labels": {"app": "my-app"},
                "creationTimestamp": "2025-01-15T10:30:00Z",
                "annotations": {
                    "ms-branch": "main",
                    "config-branch": "develop"
                }
            },
            "spec": {
                "containers": [{
                    "name": "main",
                    "image": "registry.example.com/my-app:1.0.0",
                    "ports": [
                        {"containerPort": 8080},
                        {"containerPort": 9090}
                    ],
                    "env": [
                        {"name": "JAVA_TOOL_OPTIONS", "value": "-XX:+UseG1GC -Xmx512m -XX:MaxGCPauseMillis=200"}
                    ],
                    "resources": {
                        "requests": {
                            "cpu": "100m",
                            "memory": "256Mi"
                        }
                    }
                }]
            },
            "status": {
                "containerStatuses": [
                    {"restartCount": 2},
                    {"restartCount": 1}
                ],
                "conditions": [
                    {"type": "Ready", "status": "True", "lastTransitionTime": "2025-01-15T10:30:45Z"}
                ]
            }
        }
        """;
        JsonNode node = objectMapper.readTree(json);
        PodInfo pod = parser.parsePodJson(node);

        assertNotNull(pod);
        assertEquals("my-app", pod.getName());
        assertEquals("1.0.0", pod.getVersion());
        assertEquals("main", pod.getMsBranch());
        assertEquals("develop", pod.getConfigBranch());
        assertEquals("8080, 9090", pod.getPort());
        assertEquals("100m", pod.getCpuRequest());
        assertEquals("256Mi", pod.getMemoryRequest());
        assertTrue(pod.getGcOptions().contains("UseG1GC"));
        assertEquals(3, pod.getRestarts()); // 2 + 1
    }

    @Test
    void parsePodJson_ShouldHandleMissingMetadata() throws Exception {
        String json = """
        {
            "spec": {
                "containers": [{
                    "name": "test",
                    "image": "nginx:latest"
                }]
            }
        }
        """;
        JsonNode node = objectMapper.readTree(json);
        PodInfo pod = parser.parsePodJson(node);

        assertNotNull(pod);
        assertNull(pod.getName());
        assertEquals("latest", pod.getVersion());
    }

    @Test
    void parsePodJson_ShouldHandleMissingLabels() throws Exception {
        String json = """
        {
            "metadata": {
                "name": "test-pod"
            },
            "spec": {
                "containers": [{
                    "name": "test",
                    "image": "nginx:latest"
                }]
            }
        }
        """;
        JsonNode node = objectMapper.readTree(json);
        PodInfo pod = parser.parsePodJson(node);

        assertNotNull(pod);
        assertNull(pod.getName());
    }

    @Test
    void parsePodJson_ShouldHandleMissingAnnotations() throws Exception {
        String json = """
        {
            "metadata": {
                "name": "test-pod",
                "labels": {"app": "my-app"}
            },
            "spec": {
                "containers": [{
                    "name": "test",
                    "image": "nginx:latest"
                }]
            }
        }
        """;
        JsonNode node = objectMapper.readTree(json);
        PodInfo pod = parser.parsePodJson(node);

        assertNotNull(pod);
        assertEquals("my-app", pod.getName());
        assertNull(pod.getMsBranch());
        assertNull(pod.getConfigBranch());
    }

    @Test
    void parsePodJson_ShouldHandleMissingSpec() throws Exception {
        String json = """
        {
            "metadata": {
                "labels": {"app": "test"}
            }
        }
        """;
        JsonNode node = objectMapper.readTree(json);
        PodInfo pod = parser.parsePodJson(node);

        assertNotNull(pod);
        assertEquals("test", pod.getName());
    }

    @Test
    void parsePodJson_ShouldHandleMissingContainers() throws Exception {
        String json = """
        {
            "metadata": {"labels": {"app": "test"}},
            "spec": {}
        }
        """;
        JsonNode node = objectMapper.readTree(json);
        PodInfo pod = parser.parsePodJson(node);
        assertNotNull(pod);
    }

    @Test
    void parsePodJson_ShouldHandleEmptyContainers() throws Exception {
        String json = """
        {
            "metadata": {"labels": {"app": "test"}},
            "spec": {"containers": []}
        }
        """;
        JsonNode node = objectMapper.readTree(json);
        PodInfo pod = parser.parsePodJson(node);
        assertNotNull(pod);
    }

    @Test
    void parsePodJson_ShouldHandleNoMainContainer() throws Exception {
        // Should fallback to first container when no "main" named container
        String json = """
        {
            "metadata": {"labels": {"app": "test"}},
            "spec": {
                "containers": [{
                    "name": "sidecar",
                    "image": "proxy:2.0"
                }]
            }
        }
        """;
        JsonNode node = objectMapper.readTree(json);
        PodInfo pod = parser.parsePodJson(node);

        assertNotNull(pod);
        assertEquals("2.0", pod.getVersion());
    }

    @Test
    void parsePodJson_ShouldHandleNoPorts() throws Exception {
        String json = """
        {
            "metadata": {"labels": {"app": "test"}},
            "spec": {
                "containers": [{
                    "name": "main",
                    "image": "app:1.0"
                }]
            }
        }
        """;
        JsonNode node = objectMapper.readTree(json);
        PodInfo pod = parser.parsePodJson(node);

        assertNotNull(pod);
        assertNull(pod.getPort());
    }

    @Test
    void parsePodJson_ShouldHandleEmptyPorts() throws Exception {
        String json = """
        {
            "metadata": {"labels": {"app": "test"}},
            "spec": {
                "containers": [{
                    "name": "main",
                    "image": "app:1.0",
                    "ports": []
                }]
            }
        }
        """;
        JsonNode node = objectMapper.readTree(json);
        PodInfo pod = parser.parsePodJson(node);
        assertNotNull(pod);
    }

    @Test
    void parsePodJson_ShouldHandleNoEnv() throws Exception {
        String json = """
        {
            "metadata": {"labels": {"app": "test"}},
            "spec": {
                "containers": [{
                    "name": "main",
                    "image": "app:1.0"
                }]
            }
        }
        """;
        JsonNode node = objectMapper.readTree(json);
        PodInfo pod = parser.parsePodJson(node);

        assertNotNull(pod);
        assertNull(pod.getGcOptions());
    }

    @Test
    void parsePodJson_ShouldHandleEnvWithoutJavaToolOptions() throws Exception {
        String json = """
        {
            "metadata": {"labels": {"app": "test"}},
            "spec": {
                "containers": [{
                    "name": "main",
                    "image": "app:1.0",
                    "env": [{"name": "OTHER_VAR", "value": "test"}]
                }]
            }
        }
        """;
        JsonNode node = objectMapper.readTree(json);
        PodInfo pod = parser.parsePodJson(node);

        assertNotNull(pod);
        assertNull(pod.getGcOptions());
    }

    @Test
    void parsePodJson_ShouldHandleEnvWithJavaToolOptionsNoValue() throws Exception {
        String json = """
        {
            "metadata": {"labels": {"app": "test"}},
            "spec": {
                "containers": [{
                    "name": "main",
                    "image": "app:1.0",
                    "env": [{"name": "JAVA_TOOL_OPTIONS"}]
                }]
            }
        }
        """;
        JsonNode node = objectMapper.readTree(json);
        PodInfo pod = parser.parsePodJson(node);
        assertNotNull(pod);
    }

    @Test
    void parsePodJson_ShouldHandleNoResources() throws Exception {
        String json = """
        {
            "metadata": {"labels": {"app": "test"}},
            "spec": {
                "containers": [{
                    "name": "main",
                    "image": "app:1.0"
                }]
            }
        }
        """;
        JsonNode node = objectMapper.readTree(json);
        PodInfo pod = parser.parsePodJson(node);

        assertNotNull(pod);
        assertNull(pod.getCpuRequest());
        assertNull(pod.getMemoryRequest());
    }

    @Test
    void parsePodJson_ShouldHandleResourcesWithNoRequests() throws Exception {
        String json = """
        {
            "metadata": {"labels": {"app": "test"}},
            "spec": {
                "containers": [{
                    "name": "main",
                    "image": "app:1.0",
                    "resources": {"limits": {"cpu": "200m"}}
                }]
            }
        }
        """;
        JsonNode node = objectMapper.readTree(json);
        PodInfo pod = parser.parsePodJson(node);
        assertNotNull(pod);
    }

    @Test
    void parsePodJson_ShouldHandleNoStatus() throws Exception {
        String json = """
        {
            "metadata": {"labels": {"app": "test"}},
            "spec": {
                "containers": [{"name": "main", "image": "app:1.0"}]
            }
        }
        """;
        JsonNode node = objectMapper.readTree(json);
        PodInfo pod = parser.parsePodJson(node);
        assertNotNull(pod);
        assertNull(pod.getRestarts());
    }

    @Test
    void parsePodJson_ShouldHandleStatusWithNoContainerStatuses() throws Exception {
        String json = """
        {
            "metadata": {"labels": {"app": "test"}},
            "spec": {
                "containers": [{"name": "main", "image": "app:1.0"}]
            },
            "status": {}
        }
        """;
        JsonNode node = objectMapper.readTree(json);
        PodInfo pod = parser.parsePodJson(node);
        assertNotNull(pod);
    }

    @Test
    void extractAdditionalPodInfo_ShouldSetReadyTime() throws Exception {
        String json = """
        {
            "metadata": {
                "name": "my-pod-123",
                "creationTimestamp": "2025-01-15T10:30:00Z"
            },
            "status": {
                "conditions": [
                    {"type": "Ready", "status": "True", "lastTransitionTime": "2025-01-15T10:31:30Z"}
                ]
            }
        }
        """;
        JsonNode node = objectMapper.readTree(json);
        PodInfo pod = new PodInfo();
        parser.extractAdditionalPodInfo(node, pod);

        assertEquals("my-pod-123", pod.getPodName());
        assertEquals("1m 30s", pod.getReadyTime());
    }

    @Test
    void extractAdditionalPodInfo_ShouldSetDash_WhenNoReadyCondition() throws Exception {
        String json = """
        {
            "metadata": {
                "name": "my-pod-123",
                "creationTimestamp": "2025-01-15T10:30:00Z"
            },
            "status": {
                "conditions": [
                    {"type": "Initialized", "status": "True"}
                ]
            }
        }
        """;
        JsonNode node = objectMapper.readTree(json);
        PodInfo pod = new PodInfo();
        parser.extractAdditionalPodInfo(node, pod);

        assertEquals("-", pod.getReadyTime());
    }

    @Test
    void extractAdditionalPodInfo_ShouldSetDash_WhenReadyNotTrue() throws Exception {
        String json = """
        {
            "metadata": {
                "name": "my-pod-123",
                "creationTimestamp": "2025-01-15T10:30:00Z"
            },
            "status": {
                "conditions": [
                    {"type": "Ready", "status": "False", "lastTransitionTime": "2025-01-15T10:31:30Z"}
                ]
            }
        }
        """;
        JsonNode node = objectMapper.readTree(json);
        PodInfo pod = new PodInfo();
        parser.extractAdditionalPodInfo(node, pod);

        assertEquals("-", pod.getReadyTime());
    }

    @Test
    void extractAdditionalPodInfo_ShouldSetDash_WhenNoConditions() throws Exception {
        String json = """
        {
            "metadata": {
                "name": "my-pod-123",
                "creationTimestamp": "2025-01-15T10:30:00Z"
            },
            "status": {}
        }
        """;
        JsonNode node = objectMapper.readTree(json);
        PodInfo pod = new PodInfo();
        parser.extractAdditionalPodInfo(node, pod);

        assertEquals("-", pod.getReadyTime());
    }

    @Test
    void extractAdditionalPodInfo_ShouldSetDash_WhenNoMetadata() throws Exception {
        String json = """
        {
            "status": {
                "conditions": []
            }
        }
        """;
        JsonNode node = objectMapper.readTree(json);
        PodInfo pod = new PodInfo();
        parser.extractAdditionalPodInfo(node, pod);

        assertEquals("-", pod.getReadyTime());
    }

    @Test
    void parseKubectlOutput_ShouldHandleMultiplePods() {
        String json = """
        {
            "items": [
                {
                    "metadata": {"name": "pod-1", "labels": {"app": "app1"}},
                    "spec": {"containers": [{"name": "main", "image": "app1:1.0"}]},
                    "status": {"conditions": []}
                },
                {
                    "metadata": {"name": "pod-2", "labels": {"app": "app2"}},
                    "spec": {"containers": [{"name": "main", "image": "app2:2.0"}]},
                    "status": {"conditions": []}
                }
            ]
        }
        """;
        List<PodInfo> pods = parser.parseKubectlOutput(json);
        assertEquals(2, pods.size());
    }

    @Test
    void parsePodJson_ShouldHandleSinglePort() throws Exception {
        String json = """
        {
            "metadata": {"labels": {"app": "test"}},
            "spec": {
                "containers": [{
                    "name": "main",
                    "image": "app:1.0",
                    "ports": [{"containerPort": 8080}]
                }]
            }
        }
        """;
        JsonNode node = objectMapper.readTree(json);
        PodInfo pod = parser.parsePodJson(node);

        assertNotNull(pod);
        assertEquals("8080", pod.getPort());
    }

    @Test
    void parsePodJson_ShouldHandleRequestsWithOnlyCpu() throws Exception {
        String json = """
        {
            "metadata": {"labels": {"app": "test"}},
            "spec": {
                "containers": [{
                    "name": "main",
                    "image": "app:1.0",
                    "resources": {
                        "requests": {"cpu": "100m"}
                    }
                }]
            }
        }
        """;
        JsonNode node = objectMapper.readTree(json);
        PodInfo pod = parser.parsePodJson(node);

        assertNotNull(pod);
        assertEquals("100m", pod.getCpuRequest());
        assertNull(pod.getMemoryRequest());
    }

    @Test
    void parsePodJson_ShouldHandleRequestsWithOnlyMemory() throws Exception {
        String json = """
        {
            "metadata": {"labels": {"app": "test"}},
            "spec": {
                "containers": [{
                    "name": "main",
                    "image": "app:1.0",
                    "resources": {
                        "requests": {"memory": "256Mi"}
                    }
                }]
            }
        }
        """;
        JsonNode node = objectMapper.readTree(json);
        PodInfo pod = parser.parsePodJson(node);

        assertNotNull(pod);
        assertNull(pod.getCpuRequest());
        assertEquals("256Mi", pod.getMemoryRequest());
    }

    @Test
    void parsePodJson_ShouldHandlePortWithoutContainerPort() throws Exception {
        String json = """
        {
            "metadata": {"labels": {"app": "test"}},
            "spec": {
                "containers": [{
                    "name": "main",
                    "image": "app:1.0",
                    "ports": [{"protocol": "TCP"}]
                }]
            }
        }
        """;
        JsonNode node = objectMapper.readTree(json);
        PodInfo pod = parser.parsePodJson(node);
        assertNotNull(pod);
    }
}
