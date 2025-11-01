package com.dashboard.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Тесты для TestController
 */
@WebMvcTest(value = TestController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@TestPropertySource(properties = "security.enabled=false")
class TestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testEndpoint() throws Exception {
        String response = mockMvc.perform(get("/test"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/plain;charset=UTF-8"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Проверяем, что ответ содержит ожидаемый текст
        assertTrue(response.contains("Application is working!"));
        assertTrue(response.contains("Time:"));
    }

    @Test
    void testEndpointContainsCurrentTime() throws Exception {
        String response = mockMvc.perform(get("/test"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Проверяем, что ответ содержит время
        assertTrue(response.startsWith("Application is working! Time: "));
        
        // Извлекаем время из ответа
        String timeString = response.substring("Application is working! Time: ".length());
        
        // Проверяем, что это валидная дата/время (попытка распарсить)
        try {
            LocalDateTime.parse(timeString.trim());
        } catch (Exception e) {
            // Если не удалось распарсить, проверяем хотя бы формат
            assertTrue(timeString.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*"));
        }
    }

    @Test
    void testEndpointResponseBody() throws Exception {
        mockMvc.perform(get("/test"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Application is working!")));
    }
}

