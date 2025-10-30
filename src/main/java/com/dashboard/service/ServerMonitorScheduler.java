package com.dashboard.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Schedules periodic server checks. Enabled only when scheduling is on.
 */
@Service
@ConditionalOnProperty(value = "spring.task.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class ServerMonitorScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ServerMonitorScheduler.class);

    private final ServerMonitorService serverMonitorService;

    public ServerMonitorScheduler(ServerMonitorService serverMonitorService) {
        this.serverMonitorService = serverMonitorService;
    }

    @Scheduled(fixedRate = 30000) // Every 30 seconds
    public void checkAllServers() {
        try {
            serverMonitorService.checkAllServers();
        } catch (Exception e) {
            logger.warn("Scheduled checkAllServers failed: {}", e.getMessage());
        }
    }
}


