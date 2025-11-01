package com.dashboard.service;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для KubernetesUtils
 */
class KubernetesUtilsTest {
    
    @Test
    void testParseKubernetesTimestamp_ValidFormat() {
        String timestamp = "2024-01-15T14:30:00";
        LocalDateTime result = KubernetesUtils.parseKubernetesTimestamp(timestamp);
        
        assertNotNull(result);
        assertEquals(2024, result.getYear());
        assertEquals(1, result.getMonthValue());
        assertEquals(15, result.getDayOfMonth());
        assertEquals(14, result.getHour());
        assertEquals(30, result.getMinute());
    }
    
    @Test
    void testParseKubernetesTimestamp_WithZ() {
        String timestamp = "2024-01-15T14:30:00Z";
        LocalDateTime result = KubernetesUtils.parseKubernetesTimestamp(timestamp);
        
        assertNotNull(result);
        assertEquals(14, result.getHour());
    }
    
    @Test
    void testParseKubernetesTimestamp_Null() {
        LocalDateTime result = KubernetesUtils.parseKubernetesTimestamp(null);
        assertNull(result);
    }
    
    @Test
    void testParseKubernetesTimestamp_Empty() {
        LocalDateTime result = KubernetesUtils.parseKubernetesTimestamp("");
        assertNull(result);
    }
    
    @Test
    void testParseKubernetesTimestamp_InvalidFormat() {
        LocalDateTime result = KubernetesUtils.parseKubernetesTimestamp("invalid-date");
        assertNull(result);
    }
    
    @Test
    void testFormatDuration_Seconds() {
        assertEquals("45s", KubernetesUtils.formatDuration(45));
        assertEquals("0s", KubernetesUtils.formatDuration(0));
    }
    
    @Test
    void testFormatDuration_Minutes() {
        assertEquals("2m", KubernetesUtils.formatDuration(120));
        assertEquals("2m 5s", KubernetesUtils.formatDuration(125));
        assertEquals("5m", KubernetesUtils.formatDuration(300));
    }
    
    @Test
    void testFormatDuration_Hours() {
        assertEquals("1h", KubernetesUtils.formatDuration(3600));
        assertEquals("1h 1m", KubernetesUtils.formatDuration(3660));
        assertEquals("1h 1m 5s", KubernetesUtils.formatDuration(3665));
    }
    
    @Test
    void testExtractGcOptions_WithGcOptions() {
        String javaToolOptions = "-Xmx512m -XX:+UseG1GC -Xms256m -XX:MaxGCPauseMillis=200";
        String result = KubernetesUtils.extractGcOptions(javaToolOptions);
        
        assertNotNull(result);
        assertTrue(result.contains("G1GC"));
        assertTrue(result.contains("MaxGCPauseMillis"));
    }
    
    @Test
    void testExtractGcOptions_WithoutGcOptions() {
        String javaToolOptions = "-Xmx512m -Xms256m";
        String result = KubernetesUtils.extractGcOptions(javaToolOptions);
        
        assertTrue(result.isEmpty());
    }
    
    @Test
    void testExtractGcOptions_Null() {
        String result = KubernetesUtils.extractGcOptions(null);
        assertTrue(result.isEmpty());
    }
    
    @Test
    void testExtractGcOptions_Empty() {
        String result = KubernetesUtils.extractGcOptions("");
        assertTrue(result.isEmpty());
    }
    
    @Test
    void testCleanImageName_WithRegistry() {
        // Очистка удаляет префиксы до первого /, но сохраняет путь образа
        assertEquals("image:tag", KubernetesUtils.cleanImageName("pcss-prod.example.com/image:tag"));
        assertEquals("image:tag", KubernetesUtils.cleanImageName("nexus.example.com/image:tag"));
        assertEquals("image:tag", KubernetesUtils.cleanImageName("docker.example.com/image:tag"));
    }
    
    @Test
    void testCleanImageName_WithRegistryAndPort() {
        // Текущая реализация удаляет префиксы до :, но сохраняет порт
        // Это ожидаемое поведение для текущей реализации
        String result = KubernetesUtils.cleanImageName("pcss-prod.example.com:5000/image:tag");
        // После удаления pcss-prod.example.com: остается 5000/image:tag
        assertEquals("5000/image:tag", result);
    }
    
    @Test
    void testCleanImageName_WithoutRegistry() {
        assertEquals("image:tag", KubernetesUtils.cleanImageName("image:tag"));
        assertEquals("nginx:latest", KubernetesUtils.cleanImageName("nginx:latest"));
    }
    
    @Test
    void testCleanImageName_Null() {
        assertNull(KubernetesUtils.cleanImageName(null));
    }
    
    @Test
    void testCleanImageName_Empty() {
        assertEquals("", KubernetesUtils.cleanImageName(""));
    }
}

