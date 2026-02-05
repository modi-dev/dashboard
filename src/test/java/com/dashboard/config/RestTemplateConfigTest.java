package com.dashboard.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RestTemplateConfig
 */
class RestTemplateConfigTest {

    private RestTemplateConfig config = new RestTemplateConfig();

    @Test
    void testRestTemplateBean() {
        RestTemplate restTemplate = config.restTemplate();
        
        assertNotNull(restTemplate);
    }

    @Test
    void testRestTemplateIsNewInstanceEachCall() {
        RestTemplate restTemplate1 = config.restTemplate();
        RestTemplate restTemplate2 = config.restTemplate();
        
        // Each call should return a new instance (not a singleton from the config class itself)
        assertNotSame(restTemplate1, restTemplate2);
    }

    @Test
    void testRestTemplateHasDefaultMessageConverters() {
        RestTemplate restTemplate = config.restTemplate();
        
        assertNotNull(restTemplate.getMessageConverters());
        assertFalse(restTemplate.getMessageConverters().isEmpty());
    }
}
