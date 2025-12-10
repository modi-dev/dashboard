package com.dashboard.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для SecurityConfig
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "security.enabled=true",
    "security.username=testuser",
    "security.password=testpass",
    "spring.task.scheduling.enabled=false",
    "kubernetes.enabled=false"
})
class SecurityConfigTest {
    
    @Autowired
    private SecurityFilterChain securityFilterChain;
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Test
    void testSecurityFilterChainBean() {
        assertNotNull(securityFilterChain);
    }
    
    @Test
    void testUserDetailsServiceBean() {
        assertNotNull(userDetailsService);
        
        var userDetails = userDetailsService.loadUserByUsername("testuser");
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertTrue(userDetails.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }
    
    @Test
    void testPasswordEncoderBean() {
        assertNotNull(passwordEncoder);
        
        String encoded = passwordEncoder.encode("testpass");
        assertNotNull(encoded);
        assertNotEquals("testpass", encoded); // Пароль должен быть закодирован
        assertTrue(passwordEncoder.matches("testpass", encoded));
    }
    
    @Test
    void testPasswordEncoderMatches() {
        String rawPassword = "testpass";
        String encoded = passwordEncoder.encode(rawPassword);
        
        assertTrue(passwordEncoder.matches(rawPassword, encoded));
        assertFalse(passwordEncoder.matches("wrongpassword", encoded));
    }
}

