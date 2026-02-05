package com.dashboard.config;

import org.junit.jupiter.api.Test;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MonitoringAsyncConfig
 */
class MonitoringAsyncConfigTest {

    private MonitoringAsyncConfig config = new MonitoringAsyncConfig();

    @Test
    void testServerMonitorExecutor_WithDefaultPoolSize() {
        TaskExecutor executor = config.serverMonitorExecutor(4);
        
        assertNotNull(executor);
        assertTrue(executor instanceof ThreadPoolTaskExecutor);
        
        ThreadPoolTaskExecutor threadPoolExecutor = (ThreadPoolTaskExecutor) executor;
        assertEquals(4, threadPoolExecutor.getCorePoolSize());
        assertEquals(4, threadPoolExecutor.getMaxPoolSize());
        assertTrue(threadPoolExecutor.getThreadNamePrefix().startsWith("server-monitor-"));
    }

    @Test
    void testServerMonitorExecutor_WithCustomPoolSize() {
        TaskExecutor executor = config.serverMonitorExecutor(8);
        
        assertNotNull(executor);
        assertTrue(executor instanceof ThreadPoolTaskExecutor);
        
        ThreadPoolTaskExecutor threadPoolExecutor = (ThreadPoolTaskExecutor) executor;
        assertEquals(8, threadPoolExecutor.getCorePoolSize());
        assertEquals(8, threadPoolExecutor.getMaxPoolSize());
    }

    @Test
    void testServerMonitorExecutor_WithMinimalPoolSize() {
        TaskExecutor executor = config.serverMonitorExecutor(1);
        
        assertNotNull(executor);
        assertTrue(executor instanceof ThreadPoolTaskExecutor);
        
        ThreadPoolTaskExecutor threadPoolExecutor = (ThreadPoolTaskExecutor) executor;
        assertEquals(1, threadPoolExecutor.getCorePoolSize());
    }

    @Test
    void testServerMonitorExecutor_CanExecuteTask() throws InterruptedException {
        TaskExecutor executor = config.serverMonitorExecutor(2);
        
        final boolean[] executed = {false};
        
        executor.execute(() -> executed[0] = true);
        
        // Wait for task to complete
        Thread.sleep(100);
        
        assertTrue(executed[0]);
    }
}
