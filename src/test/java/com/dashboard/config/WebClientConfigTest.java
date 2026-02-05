package com.dashboard.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for WebClientConfig
 */
class WebClientConfigTest {

    private WebClientConfig config = new WebClientConfig();

    @Test
    void testWebClientBuilderBean() {
        WebClient.Builder builder = config.webClientBuilder();
        
        assertNotNull(builder);
    }

    @Test
    void testWebClientBuilderCanBuildWebClient() {
        WebClient.Builder builder = config.webClientBuilder();
        WebClient webClient = builder.build();
        
        assertNotNull(webClient);
    }

    @Test
    void testWebClientBuilderCanSetBaseUrl() {
        WebClient.Builder builder = config.webClientBuilder();
        WebClient webClient = builder
            .baseUrl("http://example.com")
            .build();
        
        assertNotNull(webClient);
    }

    @Test
    void testWebClientBuilderIsNewInstanceEachCall() {
        WebClient.Builder builder1 = config.webClientBuilder();
        WebClient.Builder builder2 = config.webClientBuilder();
        
        // Each call should return a new builder instance
        assertNotSame(builder1, builder2);
    }

    @Test
    void testWebClientBuilderCanAddHeaders() {
        WebClient.Builder builder = config.webClientBuilder();
        WebClient webClient = builder
            .defaultHeader("X-Custom-Header", "test-value")
            .build();
        
        assertNotNull(webClient);
    }
}
