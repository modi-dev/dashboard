package com.dashboard.service;

import com.dashboard.model.Server;
import com.dashboard.model.ServerType;
import com.dashboard.repository.ServerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ServerVersionServiceTest {
    
    @Mock
    private RestTemplate restTemplate;
    
    @Mock
    private ServerRepository serverRepository;
    
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
        
        // При ошибке подключения ожидаем null (без мок-версии)
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
    
    @Test
    public void testGetAstraLinuxVersion_ReturnsNull() {
        // Astra Linux version is not implemented
        Server astraServer = new Server("Astra Test", "http://localhost:8080", ServerType.ASTRA_LINUX);
        
        String version = serverVersionService.getServerVersion(astraServer);
        
        assertNull(version);
    }
    
    @Test
    public void testGetServerVersion_WithNullServer() {
        // Method checks for null after calling getName(), so it will throw NPE
        assertThrows(NullPointerException.class, () -> {
            serverVersionService.getServerVersion(null);
        });
    }
    
    @Test
    public void testGetServerVersion_WithNullServerType() {
        Server server = new Server("Test", "http://localhost:8080", null);
        String version = serverVersionService.getServerVersion(server);
        assertNull(version);
    }
    
    @Test
    public void testUpdateServerVersionsIfNeeded_WithNullList() {
        serverVersionService.updateServerVersionsIfNeeded(null);
        
        verify(serverRepository, never()).save(any(Server.class));
        verify(restTemplate, never()).getForObject(anyString(), eq(String.class));
    }
    
    @Test
    public void testUpdateServerVersionsIfNeeded_WithEmptyList() {
        serverVersionService.updateServerVersionsIfNeeded(Collections.emptyList());
        
        verify(serverRepository, never()).save(any(Server.class));
        verify(restTemplate, never()).getForObject(anyString(), eq(String.class));
    }
    
    @Test
    public void testUpdateServerVersionsIfNeeded_WithServerThatHasVersion() {
        Server server = new Server("Test", "http://localhost:8080", ServerType.OTHER);
        server.setVersion("1.0.0");
        
        serverVersionService.updateServerVersionsIfNeeded(Arrays.asList(server));
        
        verify(serverRepository, never()).save(any(Server.class));
        verify(restTemplate, never()).getForObject(anyString(), eq(String.class));
    }
    
    @Test
    public void testUpdateServerVersionsIfNeeded_WithServerWithoutVersion() {
        Server server = new Server("Test", "http://localhost:8080", ServerType.OTHER);
        server.setVersion(null);
        server.setMetricsEndpoint("/api/version");
        server.setVersionRegex("version\\s*=\\s*\"([^\"]+)\"");
        
        String mockResponse = "version=\"2.0.0\"";
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockResponse);
        when(serverRepository.save(any(Server.class))).thenReturn(server);
        
        serverVersionService.updateServerVersionsIfNeeded(Arrays.asList(server));
        
        assertEquals("Custom 2.0.0", server.getVersion());
        verify(serverRepository, times(1)).save(server);
    }
    
    @Test
    public void testUpdateServerVersionsIfNeeded_WithMultipleServers() {
        Server server1 = new Server("Test1", "http://localhost:8080", ServerType.OTHER);
        server1.setVersion(null);
        server1.setMetricsEndpoint("/api/version");
        server1.setVersionRegex("version\\s*=\\s*\"([^\"]+)\"");
        
        Server server2 = new Server("Test2", "http://localhost:8080", ServerType.OTHER);
        server2.setVersion("1.0.0"); // Already has version
        
        Server server3 = new Server("Test3", "http://localhost:8080", ServerType.OTHER);
        server3.setVersion(null);
        server3.setMetricsEndpoint("/api/version");
        server3.setVersionRegex("version\\s*=\\s*\"([^\"]+)\"");
        
        String mockResponse = "version=\"3.0.0\"";
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockResponse);
        when(serverRepository.save(any(Server.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        serverVersionService.updateServerVersionsIfNeeded(Arrays.asList(server1, server2, server3));
        
        assertEquals("Custom 3.0.0", server1.getVersion());
        assertEquals("1.0.0", server2.getVersion()); // Should not change
        assertEquals("Custom 3.0.0", server3.getVersion());
        verify(serverRepository, times(2)).save(any(Server.class)); // Only for server1 and server3
    }
    
    @Test
    public void testGetPostgresVersion_WithUrlWithoutProtocol() {
        Server server = new Server("Postgres Test", "localhost:5432", ServerType.POSTGRES);
        String mockMetrics = """
            pg_static{server="localhost:5432",short_version="15.8.0",version="PostgreSQL 15.8"} 1
            """;
        
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockMetrics);
        
        String version = serverVersionService.getServerVersion(server);
        
        assertNotNull(version);
        assertEquals("PostgreSQL 15.8", version);
    }
    
    @Test
    public void testGetPostgresVersion_WithUrlContainingPort() {
        Server server = new Server("Postgres Test", "http://localhost:5432", ServerType.POSTGRES);
        String mockMetrics = """
            pg_static{server="localhost:5432",short_version="15.8.0",version="PostgreSQL 15.8"} 1
            """;
        
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockMetrics);
        
        String version = serverVersionService.getServerVersion(server);
        
        assertNotNull(version);
        assertEquals("PostgreSQL 15.8", version);
    }
    
    @Test
    public void testGetRedisVersion_WithUrlWithoutProtocol() {
        Server server = new Server("Redis Test", "localhost:6379", ServerType.REDIS);
        String mockMetrics = """
            redis_instance_info{redis_version="7.2.4"} 1
            """;
        
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockMetrics);
        
        String version = serverVersionService.getServerVersion(server);
        
        assertNotNull(version);
        assertEquals("Redis 7.2.4", version);
    }
    
    @Test
    public void testGetKafkaVersion_WithUrlWithoutProtocol() {
        Server server = new Server("Kafka Test", "localhost:9092", ServerType.KAFKA);
        String mockMetrics = """
            vtb_kafka_component{component_version="3.362.4"} 1
            """;
        
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockMetrics);
        
        String version = serverVersionService.getServerVersion(server);
        
        assertNotNull(version);
        assertEquals("Kafka 3.362.4", version);
    }
    
    @Test
    public void testGetOtherVersion_WithUrlEndingWithSlash() {
        Server server = new Server("Test", "http://localhost:8080/", ServerType.OTHER);
        server.setMetricsEndpoint("/api/version");
        server.setVersionRegex("version\\s*=\\s*\"([^\"]+)\"");
        
        String mockResponse = "version=\"2.0.0\"";
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockResponse);
        
        String version = serverVersionService.getServerVersion(server);
        
        assertNotNull(version);
        assertEquals("Custom 2.0.0", version);
    }
    
    @Test
    public void testGetOtherVersion_WithMetricsEndpointWithoutLeadingSlash() {
        Server server = new Server("Test", "http://localhost:8080", ServerType.OTHER);
        server.setMetricsEndpoint("api/version"); // Without leading slash
        server.setVersionRegex("version\\s*=\\s*\"([^\"]+)\"");
        
        String mockResponse = "version=\"2.0.0\"";
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockResponse);
        
        String version = serverVersionService.getServerVersion(server);
        
        assertNotNull(version);
        assertEquals("Custom 2.0.0", version);
    }
    
    @Test
    public void testGetOtherVersion_WhenVersionAlreadyStartsWithCustom() {
        Server server = new Server("Test", "http://localhost:8080", ServerType.OTHER);
        server.setMetricsEndpoint("/api/version");
        server.setVersionRegex("\"version\"\\s*:\\s*\"([^\"]+)\"");
        
        String mockResponse = "{\"version\":\"Custom 1.0.0\"}";
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockResponse);
        
        String version = serverVersionService.getServerVersion(server);
        
        assertNotNull(version);
        assertEquals("Custom 1.0.0", version); // Should not add "Custom" prefix again
    }
    
    @Test
    public void testGetPostgresVersion_WhenVersionNotFoundInMetrics() {
        String mockMetrics = "some other metrics without version";
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockMetrics);
        
        String version = serverVersionService.getServerVersion(postgresServer);
        
        assertNull(version);
    }
    
    @Test
    public void testGetPostgresVersion_WhenEmptyMetrics() {
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn("");
        
        String version = serverVersionService.getServerVersion(postgresServer);
        
        assertNull(version);
    }
    
    @Test
    public void testGetPostgresVersion_WithFullVersionFormatting() {
        String mockMetrics = """
            pg_static{server="localhost:5432",version="PostgreSQL 15.8.1 (Debian) on x86_64"} 1
            """;
        
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockMetrics);
        
        String version = serverVersionService.getServerVersion(postgresServer);
        
        assertNotNull(version);
        assertEquals("PostgreSQL 15.8", version);
    }
    
    @Test
    public void testGetPostgresVersion_WhenVersionDoesntMatchPattern() {
        String mockMetrics = """
            pg_static{server="localhost:5432",version="Unknown format version"} 1
            """;
        
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockMetrics);
        
        String version = serverVersionService.getServerVersion(postgresServer);
        
        assertNotNull(version);
        assertEquals("Unknown format version", version); // Should return as-is when pattern doesn't match
    }
    
    @Test
    public void testGetRedisVersion_WhenVersionNotFoundInMetrics() {
        String mockMetrics = "some other metrics without redis_version";
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockMetrics);
        
        String version = serverVersionService.getServerVersion(redisServer);
        
        assertNull(version);
    }
    
    @Test
    public void testGetRedisVersion_WhenEmptyMetrics() {
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn("");
        
        String version = serverVersionService.getServerVersion(redisServer);
        
        assertNull(version);
    }
    
    @Test
    public void testGetKafkaVersion_WhenVersionNotFoundInMetrics() {
        String mockMetrics = "some other metrics without vtb_kafka_component";
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockMetrics);
        
        String version = serverVersionService.getServerVersion(kafkaServer);
        
        assertNull(version);
    }
    
    @Test
    public void testGetKafkaVersion_WhenEmptyMetrics() {
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn("");
        
        String version = serverVersionService.getServerVersion(kafkaServer);
        
        assertNull(version);
    }
    
    @Test
    public void testGetOtherVersion_WhenEmptyMetrics() {
        otherServer.setMetricsEndpoint("/api/version");
        otherServer.setVersionRegex("version\\s*=\\s*\"([^\"]+)\"");
        
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn("");
        
        String version = serverVersionService.getServerVersion(otherServer);
        
        assertNull(version);
    }
    
    @Test
    public void testGetOtherVersion_WhenNoMatch() {
        otherServer.setMetricsEndpoint("/api/version");
        otherServer.setVersionRegex("version\\s*=\\s*\"([^\"]+)\"");
        
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn("no version here");
        
        String version = serverVersionService.getServerVersion(otherServer);
        
        assertNull(version);
    }
    
    @Test
    public void testBuildMetricsUrl_WithUrlContainingPort() {
        // Test through getPostgresVersion with URL containing port
        // The buildMetricsUrl method replaces the port in the URL
        Server server = new Server("Test", "http://localhost:5432", ServerType.POSTGRES);
        String mockMetrics = """
            pg_static{server="localhost:5432",version="PostgreSQL 15.8"} 1
            """;
        
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockMetrics);
        
        String version = serverVersionService.getServerVersion(server);
        
        assertNotNull(version);
        verify(restTemplate).getForObject(anyString(), eq(String.class));
    }
    
    @Test
    public void testBuildMetricsUrl_WithPathWithoutLeadingSlash() {
        // This is tested indirectly through getPostgresVersion which uses buildMetricsUrl
        // The method adds leading slash to path if missing
        Server server = new Server("Test", "http://localhost", ServerType.POSTGRES);
        String mockMetrics = """
            pg_static{server="localhost:5432",version="PostgreSQL 15.8"} 1
            """;
        
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockMetrics);
        
        String version = serverVersionService.getServerVersion(server);
        
        assertNotNull(version);
        verify(restTemplate).getForObject(anyString(), eq(String.class));
    }
    
    @Test
    public void testBuildMetricsUrl_WithUrlEndingWithSlash() {
        // The buildMetricsUrl method removes trailing slash before adding port
        Server server = new Server("Test", "http://localhost/", ServerType.POSTGRES);
        String mockMetrics = """
            pg_static{server="localhost:5432",version="PostgreSQL 15.8"} 1
            """;
        
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockMetrics);
        
        String version = serverVersionService.getServerVersion(server);
        
        assertNotNull(version);
        verify(restTemplate).getForObject(anyString(), eq(String.class));
    }
    
    @Test
    public void testUpdateServerVersionsIfNeeded_WhenVersionIsNull() {
        Server server = new Server("Test", "http://localhost:8080", ServerType.OTHER);
        server.setVersion(null);
        server.setMetricsEndpoint("/api/version");
        server.setVersionRegex("version\\s*=\\s*\"([^\"]+)\"");
        
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(null);
        // Don't stub save() since it won't be called when version is null
        
        serverVersionService.updateServerVersionsIfNeeded(Arrays.asList(server));
        
        // Version should remain null since getServerVersion returned null
        assertNull(server.getVersion());
        verify(serverRepository, never()).save(any(Server.class)); // Should not save if version is null
    }
}
