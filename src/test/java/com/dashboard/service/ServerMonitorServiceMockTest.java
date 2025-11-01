package com.dashboard.service;

import com.dashboard.model.Server;
import com.dashboard.model.ServerStatus;
import com.dashboard.model.ServerType;
import com.dashboard.repository.ServerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ServerMonitorService using mocks
 */
@ExtendWith(MockitoExtension.class)
class ServerMonitorServiceMockTest {
    
    @Mock
    private ServerRepository serverRepository;
    
    @Mock
    private WebClient.Builder webClientBuilder;
    
    private ServerMonitorService serverMonitorService;
    
    private Server postgresServer;
    private Server redisServer;
    private Server otherServer;
    
    @BeforeEach
    void setUp() {
        serverMonitorService = new ServerMonitorService(0.17);
        ReflectionTestUtils.setField(serverMonitorService, "serverRepository", serverRepository);
        ReflectionTestUtils.setField(serverMonitorService, "webClientBuilder", webClientBuilder);
        
        postgresServer = new Server("Postgres Test", "localhost:5432", ServerType.POSTGRES);
        redisServer = new Server("Redis Test", "http://localhost:6379", ServerType.REDIS);
        otherServer = new Server("Other Test", "http://example.com", ServerType.OTHER);
    }
    
    @Test
    void testCheckAllServers_WithEmptyList() {
        when(serverRepository.findAll()).thenReturn(Collections.emptyList());
        
        serverMonitorService.checkAllServers();
        
        verify(serverRepository).findAll();
        verify(serverRepository, never()).save(any(Server.class));
    }
    
    @Test
    void testCheckAllServers_WithMultipleServers() {
        List<Server> servers = Arrays.asList(postgresServer, redisServer);
        when(serverRepository.findAll()).thenReturn(servers);
        when(serverRepository.save(any(Server.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        serverMonitorService.checkAllServers();
        
        verify(serverRepository).findAll();
        verify(serverRepository, atLeast(2)).save(any(Server.class));
    }
    
    @Test
    void testCheckServer_WithException() {
        Server server = new Server("Test", "invalid-url", ServerType.OTHER);
        when(serverRepository.save(any(Server.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        serverMonitorService.checkServer(server);
        
        assertEquals(ServerStatus.OFFLINE, server.getStatus());
        assertNotNull(server.getLastChecked());
        verify(serverRepository, atLeastOnce()).save(server);
    }
    
    @Test
    void testCheckServer_WithNullUrl() {
        Server server = new Server("Test", null, ServerType.OTHER);
        when(serverRepository.save(any(Server.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        serverMonitorService.checkServer(server);
        
        assertEquals(ServerStatus.OFFLINE, server.getStatus());
        verify(serverRepository).save(server);
    }
    
    @Test
    void testCheckServer_OtherTypeWithHttpError() {
        // WebClient mocking is complex, so we just verify exception handling
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.defaultHeader(anyString(), anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenThrow(new RuntimeException("WebClient error"));
        when(serverRepository.save(any(Server.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        serverMonitorService.checkServer(otherServer);
        
        assertEquals(ServerStatus.OFFLINE, otherServer.getStatus());
        verify(serverRepository).save(otherServer);
    }
    
    @Test
    void testCheckServer_OtherTypeWithHealthcheck() {
        otherServer.setHealthcheck("/health");
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.defaultHeader(anyString(), anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenThrow(new RuntimeException("WebClient error"));
        when(serverRepository.save(any(Server.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        serverMonitorService.checkServer(otherServer);
        
        assertEquals(ServerStatus.OFFLINE, otherServer.getStatus());
        verify(serverRepository).save(otherServer);
    }
    
    @Test
    void testCheckServer_OtherTypeWithHealthcheckStartingWithSlash() {
        otherServer.setHealthcheck("health"); // Without leading slash
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.defaultHeader(anyString(), anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenThrow(new RuntimeException("WebClient error"));
        when(serverRepository.save(any(Server.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        serverMonitorService.checkServer(otherServer);
        
        assertEquals(ServerStatus.OFFLINE, otherServer.getStatus());
        verify(serverRepository).save(otherServer);
    }
    
    @Test
    void testCheckServer_OtherTypeWithUrlEndingWithSlash() {
        otherServer.setUrl("http://example.com/");
        otherServer.setHealthcheck("/health");
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.defaultHeader(anyString(), anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenThrow(new RuntimeException("WebClient error"));
        when(serverRepository.save(any(Server.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        serverMonitorService.checkServer(otherServer);
        
        assertEquals(ServerStatus.OFFLINE, otherServer.getStatus());
        verify(serverRepository).save(otherServer);
    }
    
    @Test
    void testCheckServer_WithUnknownServerType() {
        Server server = new Server("Test", "localhost", null);
        when(serverRepository.save(any(Server.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        serverMonitorService.checkServer(server);
        
        assertEquals(ServerStatus.OFFLINE, server.getStatus());
        verify(serverRepository).save(server);
    }
    
    @Test
    void testCheckServer_WithUrlWithoutPort() {
        Server server = new Server("Test", "localhost", ServerType.POSTGRES);
        when(serverRepository.save(any(Server.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        serverMonitorService.checkServer(server);
        
        assertNotNull(server.getStatus());
        assertNotNull(server.getLastChecked());
        verify(serverRepository).save(server);
    }
    
    @Test
    void testCheckServer_WithUrlWithProtocolAndPort() {
        Server server = new Server("Test", "http://localhost:5432", ServerType.POSTGRES);
        when(serverRepository.save(any(Server.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        serverMonitorService.checkServer(server);
        
        assertNotNull(server.getStatus());
        verify(serverRepository).save(server);
    }
    
    @Test
    void testCheckServer_WithKafkaType() {
        Server server = new Server("Kafka Test", "localhost:9092", ServerType.KAFKA);
        when(serverRepository.save(any(Server.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        serverMonitorService.checkServer(server);
        
        assertNotNull(server.getStatus());
        verify(serverRepository).save(server);
    }
    
    @Test
    void testCheckServer_WithAstraLinuxType() {
        Server server = new Server("Astra Test", "localhost", ServerType.ASTRA_LINUX);
        when(serverRepository.save(any(Server.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        serverMonitorService.checkServer(server);
        
        assertNotNull(server.getStatus());
        verify(serverRepository).save(server);
    }
    
}

