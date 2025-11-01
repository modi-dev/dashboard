package com.dashboard.service;

import com.dashboard.model.PodInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для KubernetesPodParser
 */
class KubernetesPodParserTest {
    
    private KubernetesPodParser parser;
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        parser = new KubernetesPodParser();
        objectMapper = new ObjectMapper();
    }
    
    @Test
    void testParseKubectlOutput_WithEmptyItems_ShouldReturnEmptyList() throws Exception {
        // Given
        String json = "{\"items\":[]}";
        
        // When
        List<PodInfo> result = parser.parseKubectlOutput(json);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    void testParseKubectlOutput_WithValidPod_ShouldParseSuccessfully() throws Exception {
        // Given
        String json = "{\n" +
            "  \"items\": [\n" +
            "    {\n" +
            "      \"metadata\": {\n" +
            "        \"name\": \"nginx-pod-123\",\n" +
            "        \"labels\": {\"app\": \"nginx\"},\n" +
            "        \"creationTimestamp\": \"2024-01-15T14:30:00Z\",\n" +
            "        \"annotations\": {\n" +
            "          \"ms-branch\": \"main\",\n" +
            "          \"config-branch\": \"dev\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"spec\": {\n" +
            "        \"containers\": [\n" +
            "          {\n" +
            "            \"name\": \"main\",\n" +
            "            \"image\": \"nexus.example.com/nginx:1.21\",\n" +
            "            \"ports\": [{\"containerPort\": 80}],\n" +
            "            \"env\": [\n" +
            "              {\"name\": \"JAVA_TOOL_OPTIONS\", \"value\": \"-Xmx512m -XX:+UseG1GC\"}\n" +
            "            ],\n" +
            "            \"resources\": {\n" +
            "              \"requests\": {\n" +
            "                \"cpu\": \"100m\",\n" +
            "                \"memory\": \"256Mi\"\n" +
            "              }\n" +
            "            }\n" +
            "          }\n" +
            "        ]\n" +
            "      },\n" +
            "      \"status\": {\n" +
            "        \"containerStatuses\": [\n" +
            "          {\"restartCount\": 2}\n" +
            "        ],\n" +
            "        \"conditions\": [\n" +
            "          {\n" +
            "            \"type\": \"Ready\",\n" +
            "            \"status\": \"True\",\n" +
            "            \"lastTransitionTime\": \"2024-01-15T14:30:30Z\"\n" +
            "          }\n" +
            "        ]\n" +
            "      }\n" +
            "    }\n" +
            "  ]\n" +
            "}";
        
        // When
        List<PodInfo> result = parser.parseKubectlOutput(json);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        
        PodInfo pod = result.get(0);
        assertEquals("nginx", pod.getName());
        assertEquals("nginx-pod-123", pod.getPodName());
        assertEquals("nginx:1.21", pod.getVersion()); // registry должен быть очищен
        assertEquals("main", pod.getMsBranch());
        assertEquals("dev", pod.getConfigBranch());
        assertEquals("80", pod.getPort());
        assertEquals("100m", pod.getCpuRequest());
        assertEquals("256Mi", pod.getMemoryRequest());
        assertEquals(2, pod.getRestarts());
        assertNotNull(pod.getCreationDate());
        assertNotNull(pod.getGcOptions());
        assertTrue(pod.getGcOptions().contains("G1GC"));
    }
    
    @Test
    void testParseKubectlOutput_WithInvalidJson_ShouldReturnEmptyList() {
        // Given
        String invalidJson = "{invalid json}";
        
        // When
        List<PodInfo> result = parser.parseKubectlOutput(invalidJson);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    void testParseKubectlOutput_WithNullInput_ShouldReturnEmptyList() {
        // When
        List<PodInfo> result = parser.parseKubectlOutput(null);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    void testExtractAdditionalPodInfo_ShouldSetPodNameAndReadyTime() throws Exception {
        // Given
        String json = "{\n" +
            "  \"metadata\": {\n" +
            "    \"name\": \"test-pod\",\n" +
            "    \"creationTimestamp\": \"2024-01-15T14:30:00Z\"\n" +
            "  },\n" +
            "  \"status\": {\n" +
            "    \"conditions\": [\n" +
            "      {\n" +
            "        \"type\": \"Ready\",\n" +
            "        \"status\": \"True\",\n" +
            "        \"lastTransitionTime\": \"2024-01-15T14:30:30Z\"\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "}";
        
        JsonNode podNode = objectMapper.readTree(json);
        PodInfo podInfo = new PodInfo();
        
        // When
        parser.extractAdditionalPodInfo(podNode, podInfo);
        
        // Then
        assertEquals("test-pod", podInfo.getPodName());
        assertNotNull(podInfo.getReadyTime());
        assertNotEquals("-", podInfo.getReadyTime());
    }
    
    @Test
    void testExtractAdditionalPodInfo_WhenNoReadyCondition_ShouldSetReadyTimeToDash() throws Exception {
        // Given
        String json = "{\n" +
            "  \"metadata\": {\n" +
            "    \"name\": \"test-pod\",\n" +
            "    \"creationTimestamp\": \"2024-01-15T14:30:00Z\"\n" +
            "  },\n" +
            "  \"status\": {\n" +
            "    \"conditions\": []\n" +
            "  }\n" +
            "}";
        
        JsonNode podNode = objectMapper.readTree(json);
        PodInfo podInfo = new PodInfo();
        
        // When
        parser.extractAdditionalPodInfo(podNode, podInfo);
        
        // Then
        assertEquals("test-pod", podInfo.getPodName());
        assertEquals("-", podInfo.getReadyTime());
    }
}

