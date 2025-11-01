package com.dashboard.service;

import com.dashboard.config.KubernetesConfig;
import com.dashboard.model.PodInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Интеграционные тесты для KubernetesService
 * Тестирует парсинг реального JSON от kubectl
 */
@ExtendWith(MockitoExtension.class)
class KubernetesServiceIntegrationTest {
    
    @Mock
    private KubernetesConfig kubernetesConfig;
    
    @Mock
    private KubectlCommandExecutor kubectlExecutor;
    
    private KubernetesPodParser podParser;
    
    private KubernetesService kubernetesService;
    
    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        // Используем реальный парсер для интеграционных тестов
        podParser = new KubernetesPodParser();
        kubernetesService = new KubernetesService();
        
        // Установим зависимости через рефлексию
        try {
            java.lang.reflect.Field configField = KubernetesService.class.getDeclaredField("kubernetesConfig");
            configField.setAccessible(true);
            configField.set(kubernetesService, kubernetesConfig);
            
            java.lang.reflect.Field executorField = KubernetesService.class.getDeclaredField("kubectlExecutor");
            executorField.setAccessible(true);
            executorField.set(kubernetesService, kubectlExecutor);
            
            java.lang.reflect.Field parserField = KubernetesService.class.getDeclaredField("podParser");
            parserField.setAccessible(true);
            parserField.set(kubernetesService, podParser);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set up KubernetesService for testing", e);
        }
    }
    
    @Test
    void testParseKubectlOutput_WithRealJson() {
        // Given - реальный JSON от kubectl
        String realJson = """
            {
                "apiVersion": "v1",
                "items": [
                    {
                        "apiVersion": "v1",
                        "kind": "Pod",
                        "metadata": {
                            "creationTimestamp": "2025-10-26T13:56:45Z",
                            "labels": {
                                "app": "grafana"
                            },
                            "name": "grafana-554d785dcc-5jjh6",
                            "namespace": "dev-tools"
                        },
                        "spec": {
                            "containers": [
                                {
                                    "image": "grafana/grafana:latest",
                                    "name": "grafana",
                                    "ports": [
                                        {
                                            "containerPort": 3000,
                                            "protocol": "TCP"
                                        }
                                    ],
                                    "resources": {
                                        "requests": {
                                            "cpu": "250m",
                                            "memory": "256Mi"
                                        }
                                    }
                                }
                            ]
                        },
                        "status": {
                            "phase": "Running"
                        }
                    }
                ],
                "kind": "List"
            }
            """;
        
        // When
        List<PodInfo> pods = kubernetesService.parseKubectlOutput(realJson);
        
        // Then
        assertNotNull(pods);
        assertEquals(1, pods.size());
        
        PodInfo pod = pods.get(0);
        assertEquals("grafana", pod.getName());
        assertEquals("grafana/grafana:latest", pod.getVersion());
        assertEquals("3000", pod.getPort());
        assertEquals("250m", pod.getCpuRequest());
        assertEquals("256Mi", pod.getMemoryRequest());
    }
    
    @Test
    void testParseKubectlOutput_WithMultiplePods() {
        // Given - JSON с несколькими подами
        String jsonWithMultiplePods = """
            {
                "apiVersion": "v1",
                "items": [
                    {
                        "apiVersion": "v1",
                        "kind": "Pod",
                        "metadata": {
                            "creationTimestamp": "2025-10-26T13:56:45Z",
                            "labels": {
                                "app": "redis-dev"
                            },
                            "name": "redis-dev-5689758666-flwnl",
                            "namespace": "dev-tools"
                        },
                        "spec": {
                            "containers": [
                                {
                                    "image": "redis:7-alpine",
                                    "name": "redis",
                                    "ports": [
                                        {
                                            "containerPort": 6379,
                                            "protocol": "TCP"
                                        }
                                    ],
                                    "resources": {
                                        "requests": {
                                            "cpu": "100m",
                                            "memory": "128Mi"
                                        }
                                    }
                                }
                            ]
                        },
                        "status": {
                            "phase": "Running"
                        }
                    },
                    {
                        "apiVersion": "v1",
                        "kind": "Pod",
                        "metadata": {
                            "creationTimestamp": "2025-10-26T13:56:46Z",
                            "labels": {
                                "app": "postgres-dev"
                            },
                            "name": "postgres-dev-5689758666-abc123",
                            "namespace": "dev-tools"
                        },
                        "spec": {
                            "containers": [
                                {
                                    "image": "postgres:15-alpine",
                                    "name": "postgres",
                                    "ports": [
                                        {
                                            "containerPort": 5432,
                                            "protocol": "TCP"
                                        }
                                    ],
                                    "resources": {
                                        "requests": {
                                            "cpu": "250m",
                                            "memory": "256Mi"
                                        }
                                    }
                                }
                            ]
                        },
                        "status": {
                            "phase": "Running"
                        }
                    }
                ],
                "kind": "List"
            }
            """;
        
        // When
        List<PodInfo> pods = kubernetesService.parseKubectlOutput(jsonWithMultiplePods);
        
        // Then
        assertNotNull(pods);
        assertEquals(2, pods.size());
        
        // Проверяем первый под (redis-dev - первый в JSON)
        PodInfo redisPod = pods.get(0);
        assertEquals("redis-dev", redisPod.getName());
        assertEquals("redis:7-alpine", redisPod.getVersion());
        assertEquals("6379", redisPod.getPort());
        assertEquals("100m", redisPod.getCpuRequest());
        assertEquals("128Mi", redisPod.getMemoryRequest());
        
        // Проверяем второй под (postgres-dev - второй в JSON)
        PodInfo postgresPod = pods.get(1);
        assertEquals("postgres-dev", postgresPod.getName());
        assertEquals("postgres:15-alpine", postgresPod.getVersion());
        assertEquals("5432", postgresPod.getPort());
        assertEquals("250m", postgresPod.getCpuRequest());
        assertEquals("256Mi", postgresPod.getMemoryRequest());
    }
    
    @Test
    void testParseKubectlOutput_EmptyJson() {
        // Given
        String emptyJson = "{\"apiVersion\":\"v1\",\"items\":[],\"kind\":\"List\"}";
        
        // When
        List<PodInfo> pods = kubernetesService.parseKubectlOutput(emptyJson);
        
        // Then
        assertNotNull(pods);
        assertTrue(pods.isEmpty());
    }
    
    @Test
    void testParseKubectlOutput_InvalidJson() {
        // Given
        String invalidJson = "invalid json";
        
        // When
        List<PodInfo> pods = kubernetesService.parseKubectlOutput(invalidJson);
        
        // Then
        assertNotNull(pods);
        assertTrue(pods.isEmpty());
    }
}
