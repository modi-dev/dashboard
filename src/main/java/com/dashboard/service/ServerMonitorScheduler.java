package com.dashboard.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Schedules periodic server checks. Enabled only when scheduling is on.
 * Monitoring interval is configured in MINUTES and converted to milliseconds for scheduling.
 */
@Service
@ConditionalOnProperty(value = "spring.task.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class ServerMonitorScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ServerMonitorScheduler.class);

    private final ServerMonitorService serverMonitorService;
    private final long intervalMs;

    public ServerMonitorScheduler(ServerMonitorService serverMonitorService,
                                  @Value("${monitoring.interval:5}") double intervalMinutes) {
        this.serverMonitorService = serverMonitorService;
        // Convert minutes to milliseconds
        this.intervalMs = (long) (intervalMinutes * 60 * 1000);
        logger.info("Server monitoring scheduler initialized with interval: {} minutes ({} ms)", 
                   intervalMinutes, intervalMs);
    }

    @Scheduled(fixedRateString = "#{${monitoring.interval:5} * 60 * 1000}")
    public void checkAllServers() {
        try {
            serverMonitorService.checkAllServers();
        } catch (Exception e) {
            logger.warn("Scheduled checkAllServers failed: {}", e.getMessage());
        }
    }
}


