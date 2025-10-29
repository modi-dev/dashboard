package com.dashboard.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Конфигурация для RestTemplate
 * 
 * RestTemplate используется для HTTP запросов к внешним сервисам,
 * например, для получения метрик PostgreSQL
 */
@Configuration
public class RestTemplateConfig {
    
    /**
     * Создает бин RestTemplate для HTTP запросов
     * 
     * @return настроенный RestTemplate
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
