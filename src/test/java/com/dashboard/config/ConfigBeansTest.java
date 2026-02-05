package com.dashboard.config;

import com.dashboard.service.KubernetesPodsSyncService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigBeansTest {

    @Mock
    private KubernetesPodsSyncService podsSyncService;

    @Test
    void serverMonitorExecutorUsesPoolSettings() {
        MonitoringAsyncConfig config = new MonitoringAsyncConfig();

        TaskExecutor executor = config.serverMonitorExecutor(3);
        assertNotNull(executor);
        assertTrue(executor instanceof ThreadPoolTaskExecutor);

        ThreadPoolTaskExecutor pool = (ThreadPoolTaskExecutor) executor;
        assertEquals(3, pool.getCorePoolSize());
        assertEquals(3, pool.getMaxPoolSize());
        assertEquals("server-monitor-", pool.getThreadNamePrefix());
        assertNotNull(pool.getThreadPoolExecutor());

        BlockingQueue<Runnable> queue = pool.getThreadPoolExecutor().getQueue();
        assertTrue(queue instanceof LinkedBlockingQueue);
        assertEquals(100, ((LinkedBlockingQueue<?>) queue).remainingCapacity());
    }

    @Test
    void restTemplateBeanCreatesTemplate() {
        RestTemplateConfig config = new RestTemplateConfig();

        RestTemplate template = config.restTemplate();
        assertNotNull(template);
        assertNotNull(template.getRequestFactory());
    }

    @Test
    void webClientBuilderBuildsClient() {
        WebClientConfig config = new WebClientConfig();

        WebClient.Builder builder = config.webClientBuilder();
        assertNotNull(builder);

        WebClient client = builder.build();
        assertNotNull(client);

        WebClient.Builder mutated = client.mutate();
        assertNotNull(mutated);
        assertNotNull(mutated.build());
    }

    @Test
    void podsInitializerRunsSync() {
        PodsInitializer initializer = new PodsInitializer(podsSyncService);
        when(podsSyncService.syncPods()).thenReturn(5);

        initializer.run();

        verify(podsSyncService).syncPods();
    }

    @Test
    void podsInitializerHandlesException() {
        PodsInitializer initializer = new PodsInitializer(podsSyncService);
        when(podsSyncService.syncPods()).thenThrow(new RuntimeException("boom"));

        assertDoesNotThrow(initializer::run);
        verify(podsSyncService).syncPods();
    }
}
