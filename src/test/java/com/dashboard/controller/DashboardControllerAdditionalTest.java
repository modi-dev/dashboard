package com.dashboard.controller;

import com.dashboard.config.KubernetesConfig;
import com.dashboard.model.PodInfo;
import com.dashboard.model.Server;
import com.dashboard.model.ServerStatus;
import com.dashboard.model.ServerType;
import com.dashboard.repository.PodRepository;
import com.dashboard.repository.ServerRepository;
import com.dashboard.service.KubernetesService;
import com.dashboard.service.KubernetesClusterInfoSyncService;
import com.dashboard.service.ServerVersionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = DashboardController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@TestPropertySource(properties = "security.enabled=false")
class DashboardControllerAdditionalTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ServerRepository serverRepository;

    @MockBean
    private PodRepository podRepository;

    @MockBean
    private KubernetesService kubernetesService;

    @MockBean
    private ServerVersionService serverVersionService;

    @MockBean
    private KubernetesConfig kubernetesConfig;

    @MockBean
    private KubernetesClusterInfoSyncService clusterInfoSyncService;

    @BeforeEach
    void setUp() {
        when(clusterInfoSyncService.getNamespace()).thenReturn("default");
        when(clusterInfoSyncService.getKubernetesVersion()).thenReturn("v1.28.0");
        when(clusterInfoSyncService.getQuotaCpu()).thenReturn(null);
        when(clusterInfoSyncService.getQuotaMemory()).thenReturn(null);
        when(clusterInfoSyncService.getQuotaPods()).thenReturn(null);
        when(clusterInfoSyncService.getQuotaConfigmaps()).thenReturn(null);
        when(clusterInfoSyncService.getQuotaSecrets()).thenReturn(null);
    }

    @Test
    void testIndex_WithAllUnknownServers() throws Exception {
        Server server = new Server("Server 1", "https://server1.com", ServerType.POSTGRES);
        server.setId(1L);
        server.setStatus(ServerStatus.UNKNOWN);

        when(serverRepository.findAllOrderByCreatedAtDesc()).thenReturn(List.of(server));
        when(podRepository.findByNamespaceOrderByName("default")).thenReturn(Collections.emptyList());
        when(kubernetesService.countUniqueServices(Collections.emptyList())).thenReturn(0L);

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("totalServers", 1))
                .andExpect(model().attribute("onlineServers", 0))
                .andExpect(model().attribute("offlineServers", 0));
    }

    @Test
    void testServers_WithAllOnlineServers() throws Exception {
        Server server1 = new Server("Server 1", "https://server1.com", ServerType.POSTGRES);
        server1.setId(1L);
        server1.setStatus(ServerStatus.ONLINE);

        Server server2 = new Server("Server 2", "https://server2.com", ServerType.REDIS);
        server2.setId(2L);
        server2.setStatus(ServerStatus.ONLINE);

        when(serverRepository.findAllOrderByCreatedAtDesc()).thenReturn(List.of(server1, server2));

        mockMvc.perform(get("/servers"))
                .andExpect(status().isOk())
                .andExpect(view().name("servers"))
                .andExpect(model().attribute("totalServers", 2))
                .andExpect(model().attribute("onlineServers", 2))
                .andExpect(model().attribute("offlineServers", 0));
    }

    @Test
    void testServers_WithNullQuotas() throws Exception {
        when(serverRepository.findAllOrderByCreatedAtDesc()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/servers"))
                .andExpect(status().isOk())
                .andExpect(view().name("servers"))
                .andExpect(model().attribute("totalServers", 0));
    }

    @Test
    void testIndex_WithNullQuotas() throws Exception {
        when(serverRepository.findAllOrderByCreatedAtDesc()).thenReturn(Collections.emptyList());
        when(podRepository.findByNamespaceOrderByName("default")).thenReturn(Collections.emptyList());
        when(kubernetesService.countUniqueServices(Collections.emptyList())).thenReturn(0L);

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }
}
