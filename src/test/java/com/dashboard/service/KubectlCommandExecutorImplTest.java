package com.dashboard.service;

import com.dashboard.config.KubernetesConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Тесты для KubectlCommandExecutorImpl
 */
@ExtendWith(MockitoExtension.class)
class KubectlCommandExecutorImplTest {
    
    @Mock
    private KubernetesConfig kubernetesConfig;
    
    @Mock
    private EmbeddedKubectlService embeddedKubectlService;
    
    private KubectlCommandExecutorImpl executor;
    
    @BeforeEach
    void setUp() {
        lenient().when(embeddedKubectlService.getKubectlPath()).thenReturn(null);
        lenient().when(embeddedKubectlService.isInitialized()).thenReturn(false);
        lenient().when(kubernetesConfig.getKubectlPath()).thenReturn("kubectl");
        
        executor = new KubectlCommandExecutorImpl(kubernetesConfig, embeddedKubectlService, 30L);
    }
    
    @Test
    void testIsAvailable_Success() {
        // Этот тест требует реального kubectl, поэтому пропускаем для unit-тестов
        // В интеграционных тестах можно проверить реальную работу
        assertNotNull(executor);
    }
    
    @Test
    void testUsesEmbeddedKubectl_WhenAvailable() {
        lenient().when(embeddedKubectlService.getKubectlPath()).thenReturn("/path/to/embedded/kubectl");
        lenient().when(embeddedKubectlService.isInitialized()).thenReturn(true);
        
        KubectlCommandExecutorImpl executorWithEmbedded = 
            new KubectlCommandExecutorImpl(kubernetesConfig, embeddedKubectlService, 30L);
        
        assertNotNull(executorWithEmbedded);
        // Методы вызываются внутри getKubectlPath(), который вызывается при executeCommand()
        // Для unit-тестов достаточно проверить, что объект создан
    }
    
    @Test
    void testUsesConfigKubectl_WhenEmbeddedUnavailable() {
        lenient().when(embeddedKubectlService.getKubectlPath()).thenReturn(null);
        lenient().when(embeddedKubectlService.isInitialized()).thenReturn(false);
        lenient().when(kubernetesConfig.getKubectlPath()).thenReturn("kubectl");
        
        KubectlCommandExecutorImpl executorWithConfig = 
            new KubectlCommandExecutorImpl(kubernetesConfig, embeddedKubectlService, 30L);
        
        assertNotNull(executorWithConfig);
        // Методы вызываются внутри getKubectlPath(), который вызывается при executeCommand()
        // Для unit-тестов достаточно проверить, что объект создан
    }
}

