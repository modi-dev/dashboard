package com.dashboard.controller;

import com.dashboard.dto.ServerDto;
import com.dashboard.model.Server;
import com.dashboard.model.ServerType;
import com.dashboard.repository.ServerRepository;
import com.dashboard.service.ServerMonitorService;
import com.dashboard.service.ServerVersionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
public class ServerControllerTest {
    
    @Autowired
    private WebApplicationContext webApplicationContext;
    
    private MockMvc mockMvc;
    
    @MockBean
    private ServerRepository serverRepository;
    
    @MockBean
    private ServerMonitorService serverMonitorService;
    
    @MockBean
    private ServerVersionService serverVersionService;
    
    @MockBean
    private com.dashboard.service.CsvExportService csvExportService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }
    
    @Test
    public void testGetAllServers() throws Exception {
        // Create test servers
        Server server1 = new Server("Test Server 1", "https://example1.com", ServerType.POSTGRES);
        server1.setId(1L);
        server1.setVersion("PostgreSQL 15.8");
        
        Server server2 = new Server("Test Server 2", "https://example2.com", ServerType.REDIS);
        server2.setId(2L);
        server2.setVersion("Redis 7.2.4");
        
        when(serverRepository.findAllOrderByCreatedAtDesc()).thenReturn(Arrays.asList(server1, server2));
        when(serverVersionService.getServerVersion(any(Server.class))).thenReturn(null);
        
        mockMvc.perform(get("/api/servers"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("Test Server 1"))
                .andExpect(jsonPath("$.data[0].type").value("Postgres"))
                .andExpect(jsonPath("$.data[1].name").value("Test Server 2"))
                .andExpect(jsonPath("$.data[1].type").value("Redis"));
    }
    
    @Test
    public void testGetServerById() throws Exception {
        Server server = new Server("Test Server", "https://example.com", ServerType.POSTGRES);
        server.setId(1L);
        server.setVersion("PostgreSQL 15.8");
        
        when(serverRepository.findById(1L)).thenReturn(Optional.of(server));
        
        mockMvc.perform(get("/api/servers/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Test Server"))
                .andExpect(jsonPath("$.data.type").value("Postgres"));
    }
    
    @Test
    public void testGetServerByIdNotFound() throws Exception {
        when(serverRepository.findById(999L)).thenReturn(Optional.empty());
        
        mockMvc.perform(get("/api/servers/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Server not found"));
    }
    
    @Test
    public void testCreateServer() throws Exception {
        ServerDto serverDto = new ServerDto();
        serverDto.setName("New Server");
        serverDto.setUrl("https://newserver.com");
        serverDto.setType(ServerType.POSTGRES);
        
        Server savedServer = new Server("New Server", "https://newserver.com", ServerType.POSTGRES);
        savedServer.setId(1L);
        
        when(serverRepository.findByUrl("https://newserver.com")).thenReturn(Optional.empty());
        when(serverRepository.save(any(Server.class))).thenReturn(savedServer);
        doNothing().when(serverMonitorService).checkServer(any(Server.class));
        
        mockMvc.perform(post("/api/servers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(serverDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("New Server"))
                .andExpect(jsonPath("$.data.type").value("Postgres"));
    }
    
    @Test
    public void testCreateServerWithDuplicateUrl() throws Exception {
        ServerDto serverDto = new ServerDto();
        serverDto.setName("New Server");
        serverDto.setUrl("https://existing.com");
        serverDto.setType(ServerType.POSTGRES);
        
        Server existingServer = new Server("Existing Server", "https://existing.com", ServerType.POSTGRES);
        when(serverRepository.findByUrl("https://existing.com")).thenReturn(Optional.of(existingServer));
        
        mockMvc.perform(post("/api/servers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(serverDto)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Server with this URL already exists"));
    }
    
    @Test
    public void testCreateOtherServerWithoutHealthcheck() throws Exception {
        ServerDto serverDto = new ServerDto();
        serverDto.setName("Other Server");
        serverDto.setUrl("https://other.com");
        serverDto.setType(ServerType.OTHER);
        // No healthcheck provided
        
        mockMvc.perform(post("/api/servers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(serverDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Healthcheck is required for 'Другое' type"));
    }
    
    @Test
    public void testCreateOtherServerWithCustomMetrics() throws Exception {
        ServerDto serverDto = new ServerDto();
        serverDto.setName("Custom Server");
        serverDto.setUrl("https://custom.com");
        serverDto.setType(ServerType.OTHER);
        serverDto.setHealthcheck("/health");
        serverDto.setMetricsEndpoint("/metrics");
        serverDto.setVersionRegex("version\\s*=\\s*\"([^\"]+)\"");
        
        Server savedServer = new Server("Custom Server", "https://custom.com", ServerType.OTHER);
        savedServer.setId(1L);
        savedServer.setHealthcheck("/health");
        savedServer.setMetricsEndpoint("/metrics");
        savedServer.setVersionRegex("version\\s*=\\s*\"([^\"]+)\"");
        
        when(serverRepository.findByUrl("https://custom.com")).thenReturn(Optional.empty());
        when(serverRepository.save(any(Server.class))).thenReturn(savedServer);
        doNothing().when(serverMonitorService).checkServer(any(Server.class));
        
        mockMvc.perform(post("/api/servers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(serverDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Custom Server"))
                .andExpect(jsonPath("$.data.type").value("Другое"))
                .andExpect(jsonPath("$.data.healthcheck").value("/health"))
                .andExpect(jsonPath("$.data.metricsEndpoint").value("/metrics"))
                .andExpect(jsonPath("$.data.versionRegex").value("version\\s*=\\s*\"([^\"]+)\""));
    }
    
    @Test
    public void testUpdateServer() throws Exception {
        ServerDto serverDto = new ServerDto();
        serverDto.setName("Updated Server");
        serverDto.setUrl("https://updated.com");
        serverDto.setType(ServerType.REDIS);
        
        Server existingServer = new Server("Old Server", "https://old.com", ServerType.POSTGRES);
        existingServer.setId(1L);
        
        Server updatedServer = new Server("Updated Server", "https://updated.com", ServerType.REDIS);
        updatedServer.setId(1L);
        
        when(serverRepository.findById(1L)).thenReturn(Optional.of(existingServer));
        when(serverRepository.save(any(Server.class))).thenReturn(updatedServer);
        
        mockMvc.perform(put("/api/servers/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(serverDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Updated Server"))
                .andExpect(jsonPath("$.data.type").value("Redis"));
    }
    
    @Test
    public void testDeleteServer() throws Exception {
        when(serverRepository.existsById(1L)).thenReturn(true);
        doNothing().when(serverRepository).deleteById(1L);
        
        mockMvc.perform(delete("/api/servers/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Server deleted successfully"));
    }
    
    @Test
    public void testDeleteServerNotFound() throws Exception {
        when(serverRepository.existsById(999L)).thenReturn(false);
        
        mockMvc.perform(delete("/api/servers/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Server not found"));
    }
    
    @Test
    public void testCheckServer() throws Exception {
        Server server = new Server("Test Server", "https://example.com", ServerType.POSTGRES);
        server.setId(1L);
        
        when(serverRepository.findById(1L)).thenReturn(Optional.of(server));
        doNothing().when(serverMonitorService).checkServer(any(Server.class));
        
        mockMvc.perform(post("/api/servers/1/check"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Server check triggered"));
    }
    
    @Test
    public void testExportServersToCsv() throws Exception {
        Server server = new Server("Test Server", "https://example.com", ServerType.POSTGRES);
        server.setId(1L);
        server.setVersion("PostgreSQL 15.8");
        
        String csvContent = "ID;Name;URL;Type;Version;Status;Healthcheck;Last Checked;Created At;Updated At\n1;Test Server;https://example.com;Postgres;PostgreSQL 15.8;;;";
        
        when(serverRepository.findAllOrderByCreatedAtDesc()).thenReturn(Arrays.asList(server));
        when(csvExportService.exportServersToCsv(any())).thenReturn(csvContent);
        
        mockMvc.perform(get("/api/servers/export/csv"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv; charset=UTF-8"))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("filename=\"servers_")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("ID;Name;URL;Type;Version;Status;Healthcheck;Last Checked;Created At;Updated At")));
    }
    
    @Test
    public void testGetAllServersException() throws Exception {
        when(serverRepository.findAllOrderByCreatedAtDesc()).thenThrow(new RuntimeException("Database error"));
        
        mockMvc.perform(get("/api/servers"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Internal server error"));
    }
    
    @Test
    public void testGetServerByIdException() throws Exception {
        when(serverRepository.findById(1L)).thenThrow(new RuntimeException("Database error"));
        
        mockMvc.perform(get("/api/servers/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Internal server error"));
    }
    
    @Test
    public void testUpdateServerNotFound() throws Exception {
        ServerDto serverDto = new ServerDto();
        serverDto.setName("Updated Server");
        serverDto.setUrl("https://updated.com");
        serverDto.setType(ServerType.REDIS);
        
        when(serverRepository.findById(999L)).thenReturn(Optional.empty());
        
        mockMvc.perform(put("/api/servers/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(serverDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Server not found"));
    }
    
    @Test
    public void testUpdateServerException() throws Exception {
        ServerDto serverDto = new ServerDto();
        serverDto.setName("Updated Server");
        serverDto.setUrl("https://updated.com");
        serverDto.setType(ServerType.REDIS);
        
        Server existingServer = new Server("Old Server", "https://old.com", ServerType.POSTGRES);
        existingServer.setId(1L);
        
        when(serverRepository.findById(1L)).thenReturn(Optional.of(existingServer));
        when(serverRepository.save(any(Server.class))).thenThrow(new RuntimeException("Save error"));
        
        mockMvc.perform(put("/api/servers/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(serverDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Failed to update server: Save error"));
    }
    
    @Test
    public void testCheckServerNotFound() throws Exception {
        when(serverRepository.findById(999L)).thenReturn(Optional.empty());
        
        mockMvc.perform(post("/api/servers/999/check"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Server not found"));
    }
    
    @Test
    public void testCheckServerException() throws Exception {
        Server server = new Server("Test Server", "https://example.com", ServerType.POSTGRES);
        server.setId(1L);
        
        when(serverRepository.findById(1L)).thenReturn(Optional.of(server));
        doThrow(new RuntimeException("Check error")).when(serverMonitorService).checkServer(any(Server.class));
        
        mockMvc.perform(post("/api/servers/1/check"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Internal server error"));
    }
    
    @Test
    public void testDeleteServerException() throws Exception {
        when(serverRepository.existsById(1L)).thenReturn(true);
        doThrow(new RuntimeException("Delete error")).when(serverRepository).deleteById(1L);
        
        mockMvc.perform(delete("/api/servers/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Internal server error"));
    }
    
    @Test
    public void testCreateServerException() throws Exception {
        ServerDto serverDto = new ServerDto();
        serverDto.setName("New Server");
        serverDto.setUrl("https://newserver.com");
        serverDto.setType(ServerType.POSTGRES);
        
        when(serverRepository.findByUrl("https://newserver.com")).thenReturn(Optional.empty());
        when(serverRepository.save(any(Server.class))).thenThrow(new RuntimeException("Save error"));
        
        mockMvc.perform(post("/api/servers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(serverDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Failed to create server: Save error"));
    }
    
    @Test
    public void testRefreshServers() throws Exception {
        doNothing().when(serverMonitorService).checkAllServers();
        
        mockMvc.perform(post("/api/servers/refresh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("Статус серверов успешно обновлен"));
        
        verify(serverMonitorService, times(1)).checkAllServers();
    }
    
    @Test
    public void testRefreshServersException() throws Exception {
        doThrow(new RuntimeException("Refresh error")).when(serverMonitorService).checkAllServers();
        
        mockMvc.perform(post("/api/servers/refresh"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Ошибка при обновлении статуса серверов: Refresh error"));
    }
    
    @Test
    public void testExportServersToCsvException() throws Exception {
        when(serverRepository.findAllOrderByCreatedAtDesc()).thenThrow(new RuntimeException("Export error"));
        
        mockMvc.perform(get("/api/servers/export/csv"))
                .andExpect(status().isInternalServerError())
                // При ошибке Spring использует application/octet-stream по умолчанию для byte[]
                .andExpect(content().contentTypeCompatibleWith("application/octet-stream"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error exporting servers")));
    }
}
