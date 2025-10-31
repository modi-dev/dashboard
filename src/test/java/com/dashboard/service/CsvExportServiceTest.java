package com.dashboard.service;

import com.dashboard.model.PodInfo;
import com.dashboard.model.Server;
import com.dashboard.model.ServerStatus;
import com.dashboard.model.ServerType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CsvExportServiceTest {
    
    private CsvExportService csvExportService;
    
    @BeforeEach
    void setUp() {
        csvExportService = new CsvExportService();
    }
    
    @Test
    public void testExportServersToCsv() {
        // Create test servers
        Server server1 = new Server("Test Server 1", "https://example1.com", ServerType.POSTGRES);
        server1.setId(1L);
        server1.setStatus(ServerStatus.ONLINE);
        server1.setVersion("PostgreSQL 15.8");
        server1.setHealthcheck("/health");
        server1.setLastChecked(LocalDateTime.of(2024, 1, 15, 10, 30, 0));
        server1.setCreatedAt(LocalDateTime.of(2024, 1, 1, 9, 0, 0));
        server1.setUpdatedAt(LocalDateTime.of(2024, 1, 15, 10, 30, 0));
        
        Server server2 = new Server("Test Server 2", "https://example2.com", ServerType.REDIS);
        server2.setId(2L);
        server2.setStatus(ServerStatus.OFFLINE);
        server2.setVersion("Redis 7.2.4");
        server2.setHealthcheck(null);
        server2.setLastChecked(LocalDateTime.of(2024, 1, 15, 10, 25, 0));
        server2.setCreatedAt(LocalDateTime.of(2024, 1, 2, 14, 0, 0));
        server2.setUpdatedAt(LocalDateTime.of(2024, 1, 15, 10, 25, 0));
        
        List<Server> servers = Arrays.asList(server1, server2);
        
        String csv = csvExportService.exportServersToCsv(servers);
        
        assertNotNull(csv);
        assertTrue(csv.contains("ID;Name;URL;Type;Version;Status;Healthcheck;Last Checked;Created At;Updated At"));
        assertTrue(csv.contains("1;Test Server 1;https://example1.com;Postgres;PostgreSQL 15.8;ONLINE;/health;2024-01-15 10:30:00;2024-01-01 09:00:00;2024-01-15 10:30:00"));
        assertTrue(csv.contains("2;Test Server 2;https://example2.com;Redis;Redis 7.2.4;OFFLINE;;2024-01-15 10:25:00;2024-01-02 14:00:00;2024-01-15 10:25:00"));
    }
    
    @Test
    public void testExportPodsToCsv() {
        // Create test pods
        PodInfo pod1 = new PodInfo("app1", "1.0.0", "main", "config-branch", "-XX:+UseG1GC", 
                                  LocalDateTime.of(2024, 1, 15, 9, 0, 0), "8080", "100m", "256Mi");
        
        PodInfo pod2 = new PodInfo("app2", "2.1.0", "develop", "dev-config", "-XX:+UseParallelGC", 
                                  LocalDateTime.of(2024, 1, 15, 10, 0, 0), "9090", "200m", "512Mi");
        
        List<PodInfo> pods = Arrays.asList(pod1, pod2);
        
        String csv = csvExportService.exportPodsToCsv(pods);
        
        assertNotNull(csv);
        assertTrue(csv.contains("Name;POD_NAME;Version;MS Branch;Config Branch;GC Options;Port;Restarts;Ready Time;CPU Request;Memory Request;Creation Date"));
        assertTrue(csv.contains("app1;;1.0.0;main;config-branch;-XX:+UseG1GC;8080;0;;100m;256Mi;2024-01-15 09:00:00"));
        assertTrue(csv.contains("app2;;2.1.0;develop;dev-config;-XX:+UseParallelGC;9090;0;;200m;512Mi;2024-01-15 10:00:00"));
    }
    
    @Test
    public void testExportServersToCsvWithSpecialCharacters() {
        // Test CSV escaping with special characters
        Server server = new Server("Test \"Server\" with; special, characters", "https://example.com", ServerType.OTHER);
        server.setId(1L);
        server.setStatus(ServerStatus.ONLINE);
        server.setVersion("Custom 1.0.0");
        server.setHealthcheck("/health");
        server.setLastChecked(LocalDateTime.of(2024, 1, 15, 10, 30, 0));
        server.setCreatedAt(LocalDateTime.of(2024, 1, 1, 9, 0, 0));
        server.setUpdatedAt(LocalDateTime.of(2024, 1, 15, 10, 30, 0));
        
        List<Server> servers = Arrays.asList(server);
        
        String csv = csvExportService.exportServersToCsv(servers);
        
        assertNotNull(csv);
        // Should be properly escaped
        assertTrue(csv.contains("\"Test \"\"Server\"\" with; special, characters\""));
    }
    
    @Test
    public void testExportPodsToCsvWithNullValues() {
        // Test with null values
        PodInfo pod = new PodInfo("app", "1.0.0", null, null, null, null, null, null, null);
        
        List<PodInfo> pods = Arrays.asList(pod);
        
        String csv = csvExportService.exportPodsToCsv(pods);
        
        assertNotNull(csv);
        // Проверяем наличие основных данных (podName и readyTime могут быть null)
        assertTrue(csv.contains("app"));
        assertTrue(csv.contains("1.0.0"));
        assertTrue(csv.contains("0")); // restarts default to 0
    }
    
    @Test
    public void testExportEmptyServersList() {
        List<Server> servers = Arrays.asList();
        
        String csv = csvExportService.exportServersToCsv(servers);
        
        assertNotNull(csv);
        assertTrue(csv.contains("ID;Name;URL;Type;Version;Status;Healthcheck;Last Checked;Created At;Updated At"));
        // Should only contain header, no data rows
        String[] lines = csv.split("\n");
        assertEquals(1, lines.length);
    }
    
    @Test
    public void testExportEmptyPodsList() {
        List<PodInfo> pods = Arrays.asList();
        
        String csv = csvExportService.exportPodsToCsv(pods);
        
        assertNotNull(csv);
        assertTrue(csv.contains("Name;POD_NAME;Version;MS Branch;Config Branch;GC Options;Port;Restarts;Ready Time;CPU Request;Memory Request;Creation Date"));
        // Should only contain header, no data rows
        String[] lines = csv.split("\n");
        assertEquals(1, lines.length);
    }
}
