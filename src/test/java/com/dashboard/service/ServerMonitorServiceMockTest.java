package com.dashboard.service;

import com.dashboard.model.Server;
import com.dashboard.model.ServerStatus;
import com.dashboard.model.ServerType;
import com.dashboard.repository.ServerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskExecutor;
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

    @Mock
    private ServerVersionService serverVersionService;
    
    private ServerMonitorService serverMonitorService;
    
    private Server postgresServer;
    private Server redisServer;
    private Server otherServer;
    
    @BeforeEach
    void setUp() {
        serverMonitorService = spy(new ServerMonitorService(10L)); // 10 seconds timeout
        ReflectionTestUtils.setField(serverMonitorService, "serverRepository", serverRepository);
        ReflectionTestUtils.setField(serverMonitorService, "webClientBuilder", webClientBuilder);
        TaskExecutor immediateExecutor = Runnable::run;
        ReflectionTestUtils.setField(serverMonitorService, "serverMonitorExecutor", immediateExecutor);
        ReflectionTestUtils.setField(serverMonitorService, "serverVersionService", serverVersionService);
        
        postgresServer = new Server("Postgres Test", "localhost:5432", ServerType.POSTGRES);
        redisServer = new Server("Redis Test", "http://localhost:6379", ServerType.REDIS);
        otherServer = new Server("Other Test", "http://example.com", ServerType.OTHER);
    }

    @Test
    void testCheckServer_UpdatesVersionWhenOnline() {
        Server server = new Server("Versioned", "localhost:5432", ServerType.POSTGRES);
        server.setVersion("Old");
        when(serverRepository.save(any(Server.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doReturn(ServerStatus.ONLINE).when(serverMonitorService).determineServerStatus(server);
        when(serverVersionService.getServerVersion(server)).thenReturn("New");
        
        serverMonitorService.checkServer(server);
        
        assertEquals("New", server.getVersion());
        verify(serverVersionService).getServerVersion(server);
    }

    @Test
    void testCheckServer_SkipsVersionRefreshWhenOffline() {
        Server server = new Server("Offline", "localhost:5432", ServerType.POSTGRES);
        server.setVersion("Old");
        when(serverRepository.save(any(Server.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doReturn(ServerStatus.OFFLINE).when(serverMonitorService).determineServerStatus(server);
        
        serverMonitorService.checkServer(server);
        
        assertEquals("Old", server.getVersion());
        verify(serverVersionService, never()).getServerVersion(server);
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
    
    @Test
    void testCheckServer_WithRedisType() {
        Server server = new Server("Redis Test", "localhost:6379", ServerType.REDIS);
        when(serverRepository.save(any(Server.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        serverMonitorService.checkServer(server);
        
        assertNotNull(server.getStatus());
        verify(serverRepository).save(server);
    }
    
    @Test
    void testCheckServer_VersionRefreshExceptionHandled() {
        Server server = new Server("Versioned", "localhost:5432", ServerType.POSTGRES);
        server.setVersion("Old");
        when(serverRepository.save(any(Server.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doReturn(ServerStatus.ONLINE).when(serverMonitorService).determineServerStatus(server);
        when(serverVersionService.getServerVersion(server)).thenThrow(new RuntimeException("Version fetch failed"));
        
        serverMonitorService.checkServer(server);
        
        // Version should remain unchanged when exception occurs
        assertEquals("Old", server.getVersion());
        verify(serverVersionService).getServerVersion(server);
    }
    
    @Test
    void testCheckServer_VersionNotUpdatedWhenSame() {
        Server server = new Server("Versioned", "localhost:5432", ServerType.POSTGRES);
        server.setVersion("Same");
        when(serverRepository.save(any(Server.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doReturn(ServerStatus.ONLINE).when(serverMonitorService).determineServerStatus(server);
        when(serverVersionService.getServerVersion(server)).thenReturn("Same");
        
        serverMonitorService.checkServer(server);
        
        assertEquals("Same", server.getVersion());
    }
    
    @Test
    void testCheckServer_VersionNotUpdatedWhenNull() {
        Server server = new Server("Versioned", "localhost:5432", ServerType.POSTGRES);
        server.setVersion("Old");
        when(serverRepository.save(any(Server.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doReturn(ServerStatus.ONLINE).when(serverMonitorService).determineServerStatus(server);
        when(serverVersionService.getServerVersion(server)).thenReturn(null);
        
        serverMonitorService.checkServer(server);
        
        assertEquals("Old", server.getVersion());
    }
    
    @Test
    void testCheckServer_WithNullServerVersionService() {
        ReflectionTestUtils.setField(serverMonitorService, "serverVersionService", null);
        Server server = new Server("Test", "localhost:5432", ServerType.POSTGRES);
        server.setVersion("Old");
        when(serverRepository.save(any(Server.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doReturn(ServerStatus.ONLINE).when(serverMonitorService).determineServerStatus(server);
        
        serverMonitorService.checkServer(server);
        
        assertEquals("Old", server.getVersion());
    }
    
    @Test
    void testCheckAllServersAsync() {
        List<Server> servers = Arrays.asList(postgresServer);
        when(serverRepository.findAll()).thenReturn(servers);
        when(serverRepository.save(any(Server.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        serverMonitorService.checkAllServersAsync();
        
        verify(serverRepository).findAll();
    }
    
    @Test
    void testCheckServerAsync_ById() {
        Long serverId = 1L;
        when(serverRepository.findById(serverId)).thenReturn(java.util.Optional.of(postgresServer));
        when(serverRepository.save(any(Server.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        serverMonitorService.checkServerAsync(serverId);
        
        verify(serverRepository).findById(serverId);
    }
    
    @Test
    void testCheckServerAsync_ByIdNotFound() {
        Long serverId = 999L;
        when(serverRepository.findById(serverId)).thenReturn(java.util.Optional.empty());
        
        serverMonitorService.checkServerAsync(serverId);
        
        verify(serverRepository).findById(serverId);
        verify(serverRepository, never()).save(any(Server.class));
    }
    
    @Test
    void testCheckServerAsync_ByServer() {
        when(serverRepository.save(any(Server.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        serverMonitorService.checkServerAsync(postgresServer);
        
        verify(serverRepository).save(postgresServer);
    }
    
    @Test
    void testCheckAllServersAsync_WithNullExecutor() {
        ReflectionTestUtils.setField(serverMonitorService, "serverMonitorExecutor", null);
        List<Server> servers = Arrays.asList(postgresServer);
        when(serverRepository.findAll()).thenReturn(servers);
        when(serverRepository.save(any(Server.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Should run synchronously when executor is null
        serverMonitorService.checkAllServersAsync();
        
        verify(serverRepository).findAll();
    }
    
    @Test
    void testCheckServerAsync_ExceptionInTask() {
        // Use a real executor that catches exceptions
        TaskExecutor realExecutor = Runnable::run;
        ReflectionTestUtils.setField(serverMonitorService, "serverMonitorExecutor", realExecutor);
        
        doThrow(new RuntimeException("Simulated error")).when(serverMonitorService).determineServerStatus(any());
        when(serverRepository.save(any(Server.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Should not throw exception
        serverMonitorService.checkServerAsync(postgresServer);
        
        // Server should be marked offline due to exception
        assertEquals(ServerStatus.OFFLINE, postgresServer.getStatus());
    }
    
    @Test
    void testDetermineServerStatus_WithInvalidUrl() {
        Server server = new Server("Test", "not a valid url", ServerType.OTHER);
        
        ServerStatus status = serverMonitorService.determineServerStatus(server);
        
        assertEquals(ServerStatus.OFFLINE, status);
    }
    
    @Test
    void testDetermineServerStatus_OtherWithoutProtocol() {
        Server server = new Server("Test", "example.com", ServerType.OTHER);
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.defaultHeader(anyString(), anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenThrow(new RuntimeException("WebClient error"));
        
        ServerStatus status = serverMonitorService.determineServerStatus(server);
        
        assertEquals(ServerStatus.OFFLINE, status);
    }
    
    @Test
    void testCheckServer_StatusLoggingOnline() {
        Server server = new Server("Test", "localhost:5432", ServerType.POSTGRES);
        when(serverRepository.save(any(Server.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doReturn(ServerStatus.ONLINE).when(serverMonitorService).determineServerStatus(server);
        
        serverMonitorService.checkServer(server);
        
        assertEquals(ServerStatus.ONLINE, server.getStatus());
        verify(serverRepository).save(server);
    }
    
    @Test
    void testCheckServer_StatusLoggingOffline() {
        Server server = new Server("Test", "localhost:5432", ServerType.POSTGRES);
        when(serverRepository.save(any(Server.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doReturn(ServerStatus.OFFLINE).when(serverMonitorService).determineServerStatus(server);
        
        serverMonitorService.checkServer(server);
        
        assertEquals(ServerStatus.OFFLINE, server.getStatus());
        verify(serverRepository).save(server);
    }
    
}

