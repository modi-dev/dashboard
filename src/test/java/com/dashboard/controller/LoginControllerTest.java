package com.dashboard.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Тесты для LoginController
 */
@WebMvcTest(value = LoginController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@TestPropertySource(properties = "security.enabled=false")
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testLoginPage() throws Exception {
        mockMvc.perform(get("/dashboard/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void testLoginPageWithSkipAttributesWhenSecurityDisabled() throws Exception {
        mockMvc.perform(get("/dashboard/login"))
                .andExpect(model().attribute("securityEnabled", false))
                .andExpect(model().attribute("skipUrl", "/dashboard"));
    }

    @Test
    void testLoginPageRespectsSafeRedirectParam() throws Exception {
        mockMvc.perform(get("/dashboard/login").param("redirect", "/dashboard/servers"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("skipUrl", "/dashboard/servers"));
    }

    @Test
    void testLoginPageIgnoresUnsafeRedirectParam() throws Exception {
        mockMvc.perform(get("/dashboard/login").param("redirect", "https://evil.example"))
                .andExpect(model().attribute("skipUrl", "/dashboard"));
    }

    @Test
    void testLoginErrorPage() throws Exception {
        mockMvc.perform(get("/dashboard/login-error"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", true))
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(model().attribute("errorMessage", "Неверный логин или пароль"));
    }

    @Test
    void testLoginErrorPageWithAttributes() throws Exception {
        mockMvc.perform(get("/dashboard/login-error"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("error", true))
                .andExpect(model().attribute("errorMessage", "Неверный логин или пароль"));
    }
}

