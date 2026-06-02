package com.dashboard.controller;

import com.dashboard.model.Server;
import com.dashboard.model.ServerStatus;
import com.dashboard.model.ServerType;
import com.dashboard.repository.ServerRepository;
import com.dashboard.service.CsvExportService;
import com.dashboard.service.ServerMonitorService;
import com.dashboard.service.ServerVersionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = ServerController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@TestPropertySource(properties = "security.enabled=false")
class ServerControllerAdditionalTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ServerRepository serverRepository;

    @MockBean
    private ServerMonitorService serverMonitorService;

    @MockBean
    private CsvExportService csvExportService;

    @MockBean
    private ServerVersionService serverVersionService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
    }

    @Test
    void getAllServers_Exception() throws Exception {
        when(serverRepository.findAllOrderByCreatedAtDesc()).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/dashboard/api/servers"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getServerById_Found() throws Exception {
        Server server = new Server("Test", "http://test.com", ServerType.POSTGRES);
        server.setId(1L);
        server.setStatus(ServerStatus.ONLINE);
        when(serverRepository.findById(1L)).thenReturn(Optional.of(server));

        mockMvc.perform(get("/dashboard/api/servers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Test"));
    }

    @Test
    void getServerById_NotFound() throws Exception {
        when(serverRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/dashboard/api/servers/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getServerById_Exception() throws Exception {
        when(serverRepository.findById(1L)).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/dashboard/api/servers/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void createServer_Success() throws Exception {
        Server savedServer = new Server("New Server", "http://new.com", ServerType.OTHER);
        savedServer.setId(1L);
        savedServer.setHealthcheck("/health");
        savedServer.setStatus(ServerStatus.UNKNOWN);

        when(serverRepository.findByUrl("http://new.com")).thenReturn(Optional.empty());
        when(serverRepository.save(any(Server.class))).thenReturn(savedServer);

        String json = """
            {"name":"New Server","url":"http://new.com","type":"OTHER","healthcheck":"/health"}
        """;

        mockMvc.perform(post("/dashboard/api/servers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void createServer_DuplicateUrl() throws Exception {
        Server existing = new Server("Existing", "http://new.com", ServerType.OTHER);
        when(serverRepository.findByUrl("http://new.com")).thenReturn(Optional.of(existing));

        String json = """
            {"name":"New Server","url":"http://new.com","type":"OTHER","healthcheck":"/health"}
        """;

        mockMvc.perform(post("/dashboard/api/servers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void createServer_OtherTypeWithoutHealthcheck() throws Exception {
        when(serverRepository.findByUrl("http://new.com")).thenReturn(Optional.empty());

        String json = """
            {"name":"New Server","url":"http://new.com","type":"OTHER"}
        """;

        mockMvc.perform(post("/dashboard/api/servers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void createServer_Exception() throws Exception {
        when(serverRepository.findByUrl(anyString())).thenThrow(new RuntimeException("DB error"));

        String json = """
            {"name":"New Server","url":"http://new.com","type":"OTHER","healthcheck":"/health"}
        """;

        mockMvc.perform(post("/dashboard/api/servers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void updateServer_Success() throws Exception {
        Server existing = new Server("Old", "http://old.com", ServerType.POSTGRES);
        existing.setId(1L);
        when(serverRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(serverRepository.save(any(Server.class))).thenAnswer(inv -> inv.getArgument(0));

        String json = """
            {"name":"Updated","url":"http://updated.com","type":"REDIS"}
        """;

        mockMvc.perform(put("/dashboard/api/servers/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void updateServer_NotFound() throws Exception {
        when(serverRepository.findById(999L)).thenReturn(Optional.empty());

        String json = """
            {"name":"Updated","url":"http://updated.com","type":"REDIS"}
        """;

        mockMvc.perform(put("/dashboard/api/servers/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void updateServer_Exception() throws Exception {
        when(serverRepository.findById(1L)).thenThrow(new RuntimeException("DB error"));

        String json = """
            {"name":"Updated","url":"http://updated.com","type":"REDIS"}
        """;

        mockMvc.perform(put("/dashboard/api/servers/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void deleteServer_Success() throws Exception {
        when(serverRepository.existsById(1L)).thenReturn(true);

        mockMvc.perform(delete("/dashboard/api/servers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(serverRepository).deleteById(1L);
    }

    @Test
    void deleteServer_NotFound() throws Exception {
        when(serverRepository.existsById(999L)).thenReturn(false);

        mockMvc.perform(delete("/dashboard/api/servers/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void deleteServer_Exception() throws Exception {
        when(serverRepository.existsById(1L)).thenReturn(true);
        doThrow(new RuntimeException("DB error")).when(serverRepository).deleteById(1L);

        mockMvc.perform(delete("/dashboard/api/servers/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void checkServer_Success() throws Exception {
        Server server = new Server("Test", "http://test.com", ServerType.POSTGRES);
        server.setId(1L);
        when(serverRepository.findById(1L)).thenReturn(Optional.of(server));

        mockMvc.perform(post("/dashboard/api/servers/1/check"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.success").value(true));

        verify(serverMonitorService).checkServerAsync(1L);
    }

    @Test
    void checkServer_NotFound() throws Exception {
        when(serverRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/dashboard/api/servers/999/check"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void checkServer_Exception() throws Exception {
        when(serverRepository.findById(1L)).thenThrow(new RuntimeException("error"));

        mockMvc.perform(post("/dashboard/api/servers/1/check"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void refreshServers_Success() throws Exception {
        mockMvc.perform(post("/dashboard/api/servers/refresh"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.success").value(true));

        verify(serverMonitorService).checkAllServersAsync();
    }

    @Test
    void refreshServers_Exception() throws Exception {
        doThrow(new RuntimeException("error")).when(serverMonitorService).checkAllServersAsync();

        mockMvc.perform(post("/dashboard/api/servers/refresh"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getServersLastUpdated_WithData() throws Exception {
        LocalDateTime now = LocalDateTime.of(2025, 6, 15, 12, 0, 0);
        when(serverRepository.findLastUpdatedAt()).thenReturn(Optional.of(now));

        mockMvc.perform(get("/dashboard/api/servers/last-updated"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.lastUpdated").value("2025-06-15T12:00:00"));
    }

    @Test
    void getServersLastUpdated_WithNoData() throws Exception {
        when(serverRepository.findLastUpdatedAt()).thenReturn(Optional.empty());

        mockMvc.perform(get("/dashboard/api/servers/last-updated"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getServersLastUpdated_Exception() throws Exception {
        when(serverRepository.findLastUpdatedAt()).thenThrow(new RuntimeException("error"));

        mockMvc.perform(get("/dashboard/api/servers/last-updated"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void exportServersToCsv_Success() throws Exception {
        List<Server> servers = List.of(new Server("Test", "http://test.com", ServerType.POSTGRES));
        when(serverRepository.findAllOrderByCreatedAtDesc()).thenReturn(servers);
        when(csvExportService.exportServersToCsv(servers)).thenReturn("ID;Name\n1;Test");

        mockMvc.perform(get("/dashboard/api/servers/export/csv"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv; charset=UTF-8"));
    }

    @Test
    void exportServersToCsv_Exception() throws Exception {
        when(serverRepository.findAllOrderByCreatedAtDesc()).thenThrow(new RuntimeException("error"));

        mockMvc.perform(get("/dashboard/api/servers/export/csv"))
                .andExpect(status().isInternalServerError());
    }
}
