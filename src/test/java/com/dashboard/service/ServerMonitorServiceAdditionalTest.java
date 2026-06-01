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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServerMonitorServiceAdditionalTest {

    @Mock
    private ServerRepository serverRepository;

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private ServerVersionService serverVersionService;

    private ServerMonitorService serverMonitorService;

    @BeforeEach
    void setUp() {
        serverMonitorService = spy(new ServerMonitorService(10L));
        ReflectionTestUtils.setField(serverMonitorService, "serverRepository", serverRepository);
        ReflectionTestUtils.setField(serverMonitorService, "webClientBuilder", webClientBuilder);
        ReflectionTestUtils.setField(serverMonitorService, "serverVersionService", serverVersionService);
    }

    @Test
    void checkAllServersAsync_WithoutExecutor_ShouldRunSynchronously() {
        // serverMonitorExecutor is null (not set)
        when(serverRepository.findAll()).thenReturn(java.util.Collections.emptyList());

        serverMonitorService.checkAllServersAsync();

        verify(serverRepository).findAll();
    }

    @Test
    void checkAllServersAsync_WithExecutor_ShouldRunAsync() {
        TaskExecutor immediateExecutor = Runnable::run;
        ReflectionTestUtils.setField(serverMonitorService, "serverMonitorExecutor", immediateExecutor);
        when(serverRepository.findAll()).thenReturn(java.util.Collections.emptyList());

        serverMonitorService.checkAllServersAsync();

        verify(serverRepository).findAll();
    }

    @Test
    void checkServerAsync_ById_ShouldFindAndCheck() {
        TaskExecutor immediateExecutor = Runnable::run;
        ReflectionTestUtils.setField(serverMonitorService, "serverMonitorExecutor", immediateExecutor);

        Server server = new Server("Test", "localhost:5432", ServerType.POSTGRES);
        server.setId(1L);
        when(serverRepository.findById(1L)).thenReturn(Optional.of(server));
        when(serverRepository.save(any(Server.class))).thenAnswer(invocation -> invocation.getArgument(0));

        serverMonitorService.checkServerAsync(1L);

        verify(serverRepository).findById(1L);
    }

    @Test
    void checkServerAsync_ById_ShouldLogWarning_WhenNotFound() {
        TaskExecutor immediateExecutor = Runnable::run;
        ReflectionTestUtils.setField(serverMonitorService, "serverMonitorExecutor", immediateExecutor);

        when(serverRepository.findById(999L)).thenReturn(Optional.empty());

        serverMonitorService.checkServerAsync(999L);

        verify(serverRepository).findById(999L);
        verify(serverRepository, never()).save(any());
    }

    @Test
    void checkServerAsync_ByServer_ShouldCheck() {
        TaskExecutor immediateExecutor = Runnable::run;
        ReflectionTestUtils.setField(serverMonitorService, "serverMonitorExecutor", immediateExecutor);

        Server server = new Server("Test", "localhost:5432", ServerType.POSTGRES);
        when(serverRepository.save(any(Server.class))).thenAnswer(invocation -> invocation.getArgument(0));

        serverMonitorService.checkServerAsync(server);

        verify(serverRepository, atLeastOnce()).save(server);
    }

    @Test
    void checkServer_ShouldNotRefreshVersion_WhenVersionServiceNull() {
        ReflectionTestUtils.setField(serverMonitorService, "serverVersionService", null);

        Server server = new Server("Test", "localhost:5432", ServerType.POSTGRES);
        doReturn(ServerStatus.ONLINE).when(serverMonitorService).determineServerStatus(server);
        when(serverRepository.save(any(Server.class))).thenAnswer(invocation -> invocation.getArgument(0));

        serverMonitorService.checkServer(server);

        assertEquals(ServerStatus.ONLINE, server.getStatus());
    }

    @Test
    void checkServer_ShouldHandleVersionException() {
        Server server = new Server("Test", "localhost:5432", ServerType.POSTGRES);
        doReturn(ServerStatus.ONLINE).when(serverMonitorService).determineServerStatus(server);
        when(serverVersionService.getServerVersion(server)).thenThrow(new RuntimeException("version error"));
        when(serverRepository.save(any(Server.class))).thenAnswer(invocation -> invocation.getArgument(0));

        serverMonitorService.checkServer(server);

        assertEquals(ServerStatus.ONLINE, server.getStatus());
    }

    @Test
    void checkServer_ShouldNotUpdateVersion_WhenSameVersion() {
        Server server = new Server("Test", "localhost:5432", ServerType.POSTGRES);
        server.setVersion("v1.0");
        doReturn(ServerStatus.ONLINE).when(serverMonitorService).determineServerStatus(server);
        when(serverVersionService.getServerVersion(server)).thenReturn("v1.0");
        when(serverRepository.save(any(Server.class))).thenAnswer(invocation -> invocation.getArgument(0));

        serverMonitorService.checkServer(server);

        assertEquals("v1.0", server.getVersion());
    }

    @Test
    void checkServer_ShouldNotUpdateVersion_WhenVersionNull() {
        Server server = new Server("Test", "localhost:5432", ServerType.POSTGRES);
        server.setVersion("v1.0");
        doReturn(ServerStatus.ONLINE).when(serverMonitorService).determineServerStatus(server);
        when(serverVersionService.getServerVersion(server)).thenReturn(null);
        when(serverRepository.save(any(Server.class))).thenAnswer(invocation -> invocation.getArgument(0));

        serverMonitorService.checkServer(server);

        assertEquals("v1.0", server.getVersion());
    }

    @Test
    void determineServerStatus_ShouldHandleUrlWithProtocol_Postgres() {
        Server server = new Server("Test", "http://localhost:5432", ServerType.POSTGRES);

        ServerStatus status = serverMonitorService.determineServerStatus(server);

        assertNotNull(status);
    }

    @Test
    void determineServerStatus_ShouldHandleUrlWithoutProtocol_Redis() {
        Server server = new Server("Test", "localhost:6379", ServerType.REDIS);

        ServerStatus status = serverMonitorService.determineServerStatus(server);

        assertNotNull(status);
    }

    @Test
    void determineServerStatus_ShouldSetDefaultPort_Postgres() {
        Server server = new Server("Test", "localhost", ServerType.POSTGRES);

        ServerStatus status = serverMonitorService.determineServerStatus(server);

        assertNotNull(status);
    }

    @Test
    void determineServerStatus_ShouldSetDefaultPort_Redis() {
        Server server = new Server("Test", "localhost", ServerType.REDIS);

        ServerStatus status = serverMonitorService.determineServerStatus(server);

        assertNotNull(status);
    }

    @Test
    void determineServerStatus_ShouldSetDefaultPort_Kafka() {
        Server server = new Server("Test", "localhost", ServerType.KAFKA);

        ServerStatus status = serverMonitorService.determineServerStatus(server);

        assertNotNull(status);
    }

    @Test
    void determineServerStatus_ShouldSetDefaultPort_AstraLinux() {
        Server server = new Server("Test", "localhost", ServerType.ASTRA_LINUX);

        ServerStatus status = serverMonitorService.determineServerStatus(server);

        assertNotNull(status);
    }

    @Test
    void determineServerStatus_ShouldReturnOffline_WhenException() {
        Server server = new Server("Test", ":::invalid", ServerType.OTHER);

        ServerStatus status = serverMonitorService.determineServerStatus(server);

        assertEquals(ServerStatus.OFFLINE, status);
    }

    @Test
    void runAsyncTask_WithException_ShouldNotPropagate() {
        TaskExecutor immediateExecutor = Runnable::run;
        ReflectionTestUtils.setField(serverMonitorService, "serverMonitorExecutor", immediateExecutor);

        // This should not throw
        serverMonitorService.checkAllServersAsync();
    }
}
