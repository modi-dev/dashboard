package com.dashboard.controller;

import com.dashboard.model.PodInfo;
import com.dashboard.model.Server;
import com.dashboard.model.ServerStatus;
import com.dashboard.model.ServerType;
import com.dashboard.repository.ServerRepository;
import com.dashboard.service.KubernetesService;
import com.dashboard.service.ServerVersionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ServerRepository serverRepository;

    @MockBean
    private KubernetesService kubernetesService;

    @MockBean
    private ServerVersionService serverVersionService;

    private List<Server> testServers;
    private List<PodInfo> testPods;

    @BeforeEach
    void setUp() {
        Server server1 = new Server("Server 1", "https://server1.com", ServerType.POSTGRES);
        server1.setId(1L);
        server1.setStatus(ServerStatus.ONLINE);
        
        Server server2 = new Server("Server 2", "https://server2.com", ServerType.REDIS);
        server2.setId(2L);
        server2.setStatus(ServerStatus.OFFLINE);
        
        testServers = Arrays.asList(server1, server2);
        
        PodInfo pod1 = new PodInfo();
        pod1.setName("nginx");
        pod1.setPodName("nginx-123");
        
        PodInfo pod2 = new PodInfo();
        pod2.setName("redis");
        pod2.setPodName("redis-456");
        
        testPods = Arrays.asList(pod1, pod2);
    }

    @Test
    void testIndex_Success() throws Exception {
        when(serverRepository.findAllOrderByCreatedAtDesc()).thenReturn(testServers);
        when(kubernetesService.getRunningPods()).thenReturn(testPods);
        when(kubernetesService.countUniqueServices(testPods)).thenReturn(2L);
        when(kubernetesService.getKubernetesVersion()).thenReturn("v1.34.0");
        doNothing().when(serverVersionService).updateServerVersionsIfNeeded(anyList());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("servers", testServers))
                .andExpect(model().attribute("pods", testPods))
                .andExpect(model().attribute("totalServers", 2))
                .andExpect(model().attribute("onlineServers", 1))
                .andExpect(model().attribute("offlineServers", 1))
                .andExpect(model().attribute("totalPods", 2))
                .andExpect(model().attribute("uniqueServices", 2L))
                .andExpect(model().attribute("kubernetesVersion", "v1.34.0"));

        verify(serverRepository).findAllOrderByCreatedAtDesc();
        verify(kubernetesService).getRunningPods();
        verify(serverVersionService).updateServerVersionsIfNeeded(anyList());
    }

    @Test
    void testIndex_WithEmptyData() throws Exception {
        when(serverRepository.findAllOrderByCreatedAtDesc()).thenReturn(Collections.emptyList());
        when(kubernetesService.getRunningPods()).thenReturn(Collections.emptyList());
        when(kubernetesService.countUniqueServices(Collections.emptyList())).thenReturn(0L);
        when(kubernetesService.getKubernetesVersion()).thenReturn("v1.34.0");
        doNothing().when(serverVersionService).updateServerVersionsIfNeeded(anyList());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("totalServers", 0))
                .andExpect(model().attribute("onlineServers", 0))
                .andExpect(model().attribute("offlineServers", 0))
                .andExpect(model().attribute("totalPods", 0))
                .andExpect(model().attribute("uniqueServices", 0L));
    }

    @Test
    void testIndex_ExceptionHandling() throws Exception {
        when(serverRepository.findAllOrderByCreatedAtDesc()).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("servers", Collections.emptyList()))
                .andExpect(model().attribute("pods", Collections.emptyList()))
                .andExpect(model().attribute("totalServers", 0))
                .andExpect(model().attribute("totalPods", 0))
                .andExpect(model().attribute("uniqueServices", 0))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    void testDashboard_RedirectsToIndex() throws Exception {
        when(serverRepository.findAllOrderByCreatedAtDesc()).thenReturn(testServers);
        when(kubernetesService.getRunningPods()).thenReturn(testPods);
        when(kubernetesService.countUniqueServices(testPods)).thenReturn(2L);
        when(kubernetesService.getKubernetesVersion()).thenReturn("v1.34.0");
        doNothing().when(serverVersionService).updateServerVersionsIfNeeded(anyList());

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void testServers_Success() throws Exception {
        when(serverRepository.findAllOrderByCreatedAtDesc()).thenReturn(testServers);
        doNothing().when(serverVersionService).updateServerVersionsIfNeeded(anyList());

        mockMvc.perform(get("/servers"))
                .andExpect(status().isOk())
                .andExpect(view().name("servers"))
                .andExpect(model().attribute("servers", testServers))
                .andExpect(model().attribute("totalServers", 2))
                .andExpect(model().attribute("onlineServers", 1))
                .andExpect(model().attribute("offlineServers", 1));

        verify(serverRepository).findAllOrderByCreatedAtDesc();
        verify(serverVersionService).updateServerVersionsIfNeeded(anyList());
    }

    @Test
    void testServers_ExceptionHandling() throws Exception {
        when(serverRepository.findAllOrderByCreatedAtDesc()).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/servers"))
                .andExpect(status().isOk())
                .andExpect(view().name("servers"))
                .andExpect(model().attribute("servers", Collections.emptyList()))
                .andExpect(model().attribute("totalServers", 0))
                .andExpect(model().attributeExists("error"));
    }
}

