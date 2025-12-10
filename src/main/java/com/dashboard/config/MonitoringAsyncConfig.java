package com.dashboard.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class MonitoringAsyncConfig {

    @Bean(name = "serverMonitorExecutor")
    public TaskExecutor serverMonitorExecutor(@Value("${monitoring.async-pool-size:4}") int poolSize) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(poolSize);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("server-monitor-");
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}

