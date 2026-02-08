package com.dashboard.config;

import org.junit.jupiter.api.Test;
import org.springframework.core.task.TaskExecutor;

import static org.junit.jupiter.api.Assertions.*;

class MonitoringAsyncConfigTest {

    @Test
    void serverMonitorExecutor_ShouldCreateExecutor() {
        MonitoringAsyncConfig config = new MonitoringAsyncConfig();
        TaskExecutor executor = config.serverMonitorExecutor(4);
        assertNotNull(executor);
    }

    @Test
    void serverMonitorExecutor_ShouldCreateWithCustomPoolSize() {
        MonitoringAsyncConfig config = new MonitoringAsyncConfig();
        TaskExecutor executor = config.serverMonitorExecutor(8);
        assertNotNull(executor);
    }
}
