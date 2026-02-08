package com.dashboard.service;

import com.dashboard.model.PodInfo;
import com.dashboard.model.Server;
import com.dashboard.model.ServerStatus;
import com.dashboard.model.ServerType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvExportServiceAdditionalTest {

    private CsvExportService csvExportService;

    @BeforeEach
    void setUp() {
        csvExportService = new CsvExportService();
    }

    @Test
    void exportServersToCsv_ShouldReturnHeaderOnly_WhenEmptyList() {
        String csv = csvExportService.exportServersToCsv(Collections.emptyList());
        assertNotNull(csv);
        assertTrue(csv.contains("ID"));
        assertTrue(csv.contains("Name"));
        assertTrue(csv.startsWith("\uFEFF")); // BOM
    }

    @Test
    void exportServersToCsv_ShouldHandleNullFields() {
        Server server = new Server("Test", "http://test.com", ServerType.POSTGRES);
        server.setId(1L);
        server.setVersion(null);
        server.setHealthcheck(null);
        server.setLastChecked(null);
        // createdAt and updatedAt are set in constructor

        String csv = csvExportService.exportServersToCsv(List.of(server));
        assertNotNull(csv);
        assertTrue(csv.contains("Test"));
    }

    @Test
    void exportServersToCsv_ShouldHandleAllFields() {
        Server server = new Server("Test;Server", "http://test.com", ServerType.OTHER);
        server.setId(1L);
        server.setVersion("1.0.0");
        server.setHealthcheck("/health");
        server.setLastChecked(LocalDateTime.of(2025, 1, 15, 10, 0, 0));
        server.setCreatedAt(LocalDateTime.of(2025, 1, 1, 0, 0, 0));
        server.setUpdatedAt(LocalDateTime.of(2025, 1, 15, 10, 0, 0));

        String csv = csvExportService.exportServersToCsv(List.of(server));
        assertNotNull(csv);
        // Semicolons in name should be escaped with quotes
        assertTrue(csv.contains("\"Test;Server\""));
    }

    @Test
    void exportServersToCsv_ShouldEscapeQuotes() {
        Server server = new Server("Test \"Server\"", "http://test.com", ServerType.POSTGRES);
        server.setId(1L);

        String csv = csvExportService.exportServersToCsv(List.of(server));
        assertNotNull(csv);
        // Quotes should be doubled
        assertTrue(csv.contains("\"\"Server\"\""));
    }

    @Test
    void exportPodsToCsv_ShouldReturnHeaderOnly_WhenEmptyList() {
        String csv = csvExportService.exportPodsToCsv(Collections.emptyList());
        assertNotNull(csv);
        assertTrue(csv.contains("Name"));
        assertTrue(csv.contains("POD_NAME"));
        assertTrue(csv.startsWith("\uFEFF")); // BOM
    }

    @Test
    void exportPodsToCsv_ShouldHandleNullFields() {
        PodInfo pod = new PodInfo();
        pod.setName(null);
        pod.setPodName(null);
        pod.setVersion(null);
        pod.setMsBranch(null);
        pod.setConfigBranch(null);
        pod.setGcOptions(null);
        pod.setPort(null);
        pod.setRestarts(null);
        pod.setReadyTime(null);
        pod.setCpuRequest(null);
        pod.setMemoryRequest(null);
        pod.setDatabaseClusterUrl(null);
        pod.setCreationDate(null);

        String csv = csvExportService.exportPodsToCsv(List.of(pod));
        assertNotNull(csv);
    }

    @Test
    void exportPodsToCsv_ShouldHandleAllFields() {
        PodInfo pod = new PodInfo();
        pod.setName("my-app");
        pod.setPodName("my-app-abc123");
        pod.setVersion("1.0.0");
        pod.setMsBranch("main");
        pod.setConfigBranch("develop");
        pod.setGcOptions("-XX:+UseG1GC");
        pod.setPort("8080");
        pod.setRestarts(3);
        pod.setReadyTime("45s");
        pod.setCpuRequest("100m");
        pod.setMemoryRequest("256Mi");
        pod.setDatabaseClusterUrl("jdbc:postgresql://host:5432/db");
        pod.setCreationDate(LocalDateTime.of(2025, 6, 15, 12, 0, 0));

        String csv = csvExportService.exportPodsToCsv(List.of(pod));
        assertNotNull(csv);
        assertTrue(csv.contains("my-app"));
        assertTrue(csv.contains("my-app-abc123"));
        assertTrue(csv.contains("main"));
    }

    @Test
    void exportPodsToCsv_ShouldProtectVersionForExcel() {
        PodInfo pod = new PodInfo();
        pod.setName("app");
        pod.setPodName("pod-1");
        pod.setVersion("1.0.0"); // This looks like a version number, should be protected
        pod.setReadyTime("45s");
        pod.setRestarts(0);

        String csv = csvExportService.exportPodsToCsv(List.of(pod));
        assertNotNull(csv);
        // Version 1.0.0 matches the pattern and should be tab-protected
        assertTrue(csv.contains("\t1.0.0"));
    }

    @Test
    void exportServersToCsv_ShouldHandleNewlinesInValues() {
        Server server = new Server("Test\nServer", "http://test.com", ServerType.POSTGRES);
        server.setId(1L);

        String csv = csvExportService.exportServersToCsv(List.of(server));
        assertNotNull(csv);
        // Newlines should be replaced with spaces
        assertTrue(csv.contains("Test Server"));
    }

    @Test
    void exportServersToCsv_ShouldHandleCarriageReturns() {
        Server server = new Server("Test\r\nServer", "http://test.com", ServerType.POSTGRES);
        server.setId(1L);

        String csv = csvExportService.exportServersToCsv(List.of(server));
        assertNotNull(csv);
        assertTrue(csv.contains("Test Server"));
    }

    @Test
    void exportServersToCsv_ShouldProtectDateLikeValues() {
        Server server = new Server("Test", "http://test.com", ServerType.POSTGRES);
        server.setId(1L);
        server.setVersion("12.01.2024"); // Looks like a date

        String csv = csvExportService.exportServersToCsv(List.of(server));
        assertNotNull(csv);
        // Should be tab-protected
        assertTrue(csv.contains("\t12.01.2024"));
    }

    @Test
    void exportServersToCsv_ShouldProtectLongNumericStrings() {
        Server server = new Server("Test", "http://test.com", ServerType.POSTGRES);
        server.setId(1L);
        server.setVersion("123456"); // Long numeric string

        String csv = csvExportService.exportServersToCsv(List.of(server));
        assertNotNull(csv);
        assertTrue(csv.contains("\t123456"));
    }
}
