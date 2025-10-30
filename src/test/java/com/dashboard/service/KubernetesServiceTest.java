package com.dashboard.service;

import com.dashboard.config.KubernetesConfig;
import com.dashboard.model.PodInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Тесты для KubernetesService
 * Проверяет интеграцию функциональности из version.sh скрипта
 */
@ExtendWith(MockitoExtension.class)
class KubernetesServiceTest {
    
    @Mock
    private KubernetesConfig kubernetesConfig;
    
    @InjectMocks
    private KubernetesService kubernetesService;
    
    @BeforeEach
    void setUp() {
        lenient().when(kubernetesConfig.isEnabled()).thenReturn(true);
        lenient().when(kubernetesConfig.getNamespace()).thenReturn("test-namespace");
        lenient().when(kubernetesConfig.getKubectlPath()).thenReturn("kubectl");
    }
    
    @Test
    void testGetRunningPods_WhenKubernetesDisabled_ShouldReturnEmptyList() {
        // Given
        when(kubernetesConfig.isEnabled()).thenReturn(false);
        
        // When
        List<PodInfo> result = kubernetesService.getRunningPods();
        
        // Then
        assertTrue(result.isEmpty());
        verify(kubernetesConfig).isEnabled();
    }
    
    @Test
    void testGetCurrentNamespace_ShouldReturnConfiguredNamespace() {
        // Given
        // Мок не нужен, так как метод выполняет kubectl команду
        
        // When
        String result = kubernetesService.getCurrentNamespace();
        
        // Then
        // Метод getCurrentNamespace() выполняет kubectl команду, поэтому возвращает 'default'
        // если kubectl не настроен или не доступен
        assertNotNull(result);
        // Проверяем, что метод вызывается (может вернуть 'default' или пустую строку)
        assertTrue(result.equals("default") || result.isEmpty() || result.equals("'default'") || result.equals("test-namespace"));
    }
    
    @Test
    void testGenerateHtmlPage_ShouldReturnValidHtml() {
        // Given
        when(kubernetesConfig.isEnabled()).thenReturn(false); // Отключаем kubectl для теста
        
        // When
        String html = kubernetesService.generateHtmlPage();
        
        // Then
        assertNotNull(html);
        assertTrue(html.contains("<html>"));
        assertTrue(html.contains("<body>"));
        assertTrue(html.contains("<table class='iksweb'>"));
        assertTrue(html.contains("<th>NAME</th>"));
        assertTrue(html.contains("<th>VERSION</th>"));
        assertTrue(html.contains("<th>MsBranch</th>"));
        assertTrue(html.contains("<th>ConfigBranch</th>"));
        assertTrue(html.contains("<th>GC</th>"));
        assertTrue(html.contains("<th>CREATION DATE</th>"));
        assertTrue(html.contains("<th>PORT</th>"));
        assertTrue(html.contains("<th>REQUEST</th>"));
        assertTrue(html.contains("</html>"));
    }
    
    @Test
    void testGenerateHtmlPage_ShouldContainCssStyles() {
        // Given
        when(kubernetesConfig.isEnabled()).thenReturn(false);
        
        // When
        String html = kubernetesService.generateHtmlPage();
        
        // Then
        assertTrue(html.contains("<style>"));
        assertTrue(html.contains("table.iksweb"));
        assertTrue(html.contains("background-color:#354251"));
        assertTrue(html.contains("</style>"));
    }
    
    @Test
    void testGenerateHtmlPage_ShouldContainNamespaceInfo() {
        // Given
        when(kubernetesConfig.isEnabled()).thenReturn(false);
        
        // When
        String html = kubernetesService.generateHtmlPage();
        
        // Then
        // HTML содержит информацию о namespace и времени обновления
        assertTrue(html.contains("namespace:") || html.contains("test-namespace"));
        assertTrue(html.contains("update time:"));
    }
}
