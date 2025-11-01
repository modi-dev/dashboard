package com.dashboard.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для KubernetesConfig
 */
class KubernetesConfigTest {
    
    private KubernetesConfig config;
    
    @BeforeEach
    void setUp() {
        config = new KubernetesConfig();
    }
    
    @Test
    void testDefaultValues() {
        assertEquals("default", config.getNamespace());
        assertEquals("kubectl", config.getKubectlPath());
        assertFalse(config.isEnabled());
    }
    
    @Test
    void testSetNamespace() {
        config.setNamespace("test-namespace");
        assertEquals("test-namespace", config.getNamespace());
    }
    
    @Test
    void testSetKubectlPath() {
        config.setKubectlPath("/usr/local/bin/kubectl");
        assertEquals("/usr/local/bin/kubectl", config.getKubectlPath());
    }
    
    @Test
    void testSetEnabled() {
        config.setEnabled(true);
        assertTrue(config.isEnabled());
        
        config.setEnabled(false);
        assertFalse(config.isEnabled());
    }
    
    @Test
    void testToString() {
        config.setNamespace("dev-tools");
        config.setKubectlPath("kubectl");
        config.setEnabled(true);
        
        String result = config.toString();
        
        assertNotNull(result);
        assertTrue(result.contains("KubernetesConfig"));
        assertTrue(result.contains("namespace='dev-tools'"));
        assertTrue(result.contains("kubectlPath='kubectl'"));
        assertTrue(result.contains("enabled=true"));
    }
    
    @Test
    void testGetNamespace() {
        config.setNamespace("production");
        assertEquals("production", config.getNamespace());
    }
    
    @Test
    void testGetKubectlPath() {
        config.setKubectlPath("/custom/path/kubectl");
        assertEquals("/custom/path/kubectl", config.getKubectlPath());
    }
    
    @Test
    void testIsEnabled() {
        config.setEnabled(true);
        assertTrue(config.isEnabled());
        
        config.setEnabled(false);
        assertFalse(config.isEnabled());
    }
}

