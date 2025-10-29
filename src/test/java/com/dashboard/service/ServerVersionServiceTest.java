package com.dashboard.service;

import com.dashboard.model.Server;
import com.dashboard.model.ServerType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ServerVersionServiceTest {
    
    @Mock
    private RestTemplate restTemplate;
    
    @InjectMocks
    private ServerVersionService serverVersionService;
    
    private Server postgresServer;
    private Server redisServer;
    private Server kafkaServer;
    private Server otherServer;
    
    @BeforeEach
    void setUp() {
        postgresServer = new Server("Postgres Test", "http://localhost:5432", ServerType.POSTGRES);
        redisServer = new Server("Redis Test", "http://localhost:6379", ServerType.REDIS);
        kafkaServer = new Server("Kafka Test", "http://localhost:9092", ServerType.KAFKA);
        otherServer = new Server("Other Test", "http://localhost:8080", ServerType.OTHER);
    }
    
    @Test
    public void testGetPostgresVersion() {
        // Mock response from postgres_exporter
        String mockMetrics = """
            # HELP pg_static PostgreSQL static information
            # TYPE pg_static gauge
            pg_static{server="localhost:5432",short_version="15.8.0",version="PostgreSQL 15.8 (Debian 15.8-1.pgdg100+1) on x86_64-pc-linux-gnu, compiled by gcc (Debian 8.3.0-6) 8.3.0, 64-bit"} 1
            """;
        
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockMetrics);
        
        String version = serverVersionService.getServerVersion(postgresServer);
        
        assertNotNull(version);
        assertEquals("PostgreSQL 15.8", version);
        verify(restTemplate).getForObject("http://localhost:9134/metrics", String.class);
    }
    
    @Test
    public void testGetRedisVersion() {
        // Mock response from redis_exporter
        String mockMetrics = """
            # HELP redis_instance_info Redis instance information
            # TYPE redis_instance_info gauge
            redis_instance_info{maxmemory_policy="allkeys-lru",os="Linux 5.15.0-127-lowlatency x86_64",process_id="1141",redis_build_id="44edf16bebe6fed2",redis_mode="standalone",redis_version="7.2.4",role="master",run_id="5c6c49410d31de1f658ac548ed30e76797a6ff03",tcp_port="6379"} 1
            """;
        
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockMetrics);
        
        String version = serverVersionService.getServerVersion(redisServer);
        
        assertNotNull(version);
        assertEquals("Redis 7.2.4", version);
        verify(restTemplate).getForObject("http://localhost:9121/metrics", String.class);
    }
    
    @Test
    public void testGetKafkaVersion() {
        // Mock response from vtb_jmx_exporter
        String mockMetrics = """
            # HELP vtb_kafka_component VTB Kafka component information
            # TYPE vtb_kafka_component gauge
            vtb_kafka_component{app_type="kafka",ci_host="localhost",cluster_name="test-kafka-cluster",component_name="VTB-Kafka",component_version="3.362.4",exp_name="vtb_jmx_exporter",ris_name="test",ris_sub="test"} 1
            """;
        
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockMetrics);
        
        String version = serverVersionService.getServerVersion(kafkaServer);
        
        assertNotNull(version);
        assertEquals("Kafka 3.362.4", version);
        verify(restTemplate).getForObject("http://localhost:7171/metrics", String.class);
    }
    
    @Test
    public void testGetOtherVersionWithCustomMetrics() {
        // Setup custom metrics parameters
        otherServer.setMetricsEndpoint("/api/version");
        otherServer.setVersionRegex("version\\s*=\\s*\"([^\"]+)\"");
        
        // Mock response from custom endpoint
        String mockResponse = "app_name=myapp,version=\"2.1.0\",build=123";
        
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockResponse);
        
        String version = serverVersionService.getServerVersion(otherServer);
        
        assertNotNull(version);
        assertEquals("Custom 2.1.0", version);
        verify(restTemplate).getForObject("http://localhost:8080/api/version", String.class);
    }
    
    @Test
    public void testGetOtherVersionWithJsonResponse() {
        // Setup custom metrics parameters for JSON response
        otherServer.setMetricsEndpoint("/metrics");
        otherServer.setVersionRegex("\"version\"\\s*:\\s*\"([^\"]+)\"");
        
        // Mock JSON response
        String mockResponse = """
            {
                "service": "myapp",
                "version": "3.5.2",
                "status": "ok",
                "build": "456"
            }
            """;
        
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockResponse);
        
        String version = serverVersionService.getServerVersion(otherServer);
        
        assertNotNull(version);
        assertEquals("Custom 3.5.2", version);
        verify(restTemplate).getForObject("http://localhost:8080/metrics", String.class);
    }
    
    @Test
    public void testGetOtherVersionWithoutMetricsEndpoint() {
        // Test with missing metrics endpoint
        otherServer.setVersionRegex("version\\s*=\\s*\"([^\"]+)\"");
        
        String version = serverVersionService.getServerVersion(otherServer);
        
        assertNull(version);
        verify(restTemplate, never()).getForObject(anyString(), eq(String.class));
    }
    
    @Test
    public void testGetOtherVersionWithoutVersionRegex() {
        // Test with missing version regex
        otherServer.setMetricsEndpoint("/metrics");
        
        String version = serverVersionService.getServerVersion(otherServer);
        
        assertNull(version);
        verify(restTemplate, never()).getForObject(anyString(), eq(String.class));
    }
    
    @Test
    public void testGetOtherVersionWithInvalidRegex() {
        // Test with invalid regex
        otherServer.setMetricsEndpoint("/metrics");
        otherServer.setVersionRegex("[invalid regex");
        
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn("version=1.0.0");
        
        String version = serverVersionService.getServerVersion(otherServer);
        
        assertNull(version);
    }
    
    @Test
    public void testGetOtherVersionWithNoMatch() {
        // Test when regex doesn't match
        otherServer.setMetricsEndpoint("/metrics");
        otherServer.setVersionRegex("version\\s*=\\s*\"([^\"]+)\"");
        
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn("no version here");
        
        String version = serverVersionService.getServerVersion(otherServer);
        
        assertNull(version);
    }
    
    @Test
    public void testGetVersionWithNetworkError() {
        // Test network error handling
        when(restTemplate.getForObject(anyString(), eq(String.class)))
            .thenThrow(new org.springframework.web.client.ResourceAccessException("Connection refused"));
        
        String version = serverVersionService.getServerVersion(postgresServer);
        
        assertNull(version);
    }
    
    @Test
    public void testGetVersionWithHttpError() {
        // Test HTTP error handling
        when(restTemplate.getForObject(anyString(), eq(String.class)))
            .thenThrow(new org.springframework.web.client.HttpClientErrorException(org.springframework.http.HttpStatus.NOT_FOUND, "Not Found", null, null, null));
        
        String version = serverVersionService.getServerVersion(redisServer);
        
        assertNull(version);
    }
    
    @Test
    public void testGetVersionWithEmptyResponse() {
        // Test empty response handling
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn("");
        
        String version = serverVersionService.getServerVersion(kafkaServer);
        
        assertNull(version);
    }
    
    @Test
    public void testGetVersionWithNullResponse() {
        // Test null response handling
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(null);
        
        String version = serverVersionService.getServerVersion(postgresServer);
        
        assertNull(version);
    }
}
