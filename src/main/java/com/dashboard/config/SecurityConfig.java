package com.dashboard.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Конфигурация безопасности для защиты операций с серверами
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Value("${security.username:admin}")
    private String username;
    
    @Value("${security.password:admin}")
    private String password;
    
    @Value("${security.enabled:true}")
    private boolean securityEnabled;
    
    /**
     * Настройка цепочки фильтров безопасности
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        if (!securityEnabled) {
            // Если безопасность отключена, разрешаем все запросы
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable());
            return http.build();
        }
        
        http
            .authorizeHttpRequests(auth -> auth
                // Статические ресурсы доступны всем
                .requestMatchers(
                    "/dashboard/css/**",
                    "/dashboard/js/**",
                    "/dashboard/webfonts/**",
                    "/dashboard/favicon.ico",
                    "/dashboard/favicon-*.png",
                    "/dashboard/apple-touch-icon.png"
                ).permitAll()
                
                // Публичные страницы - доступны всем
                .requestMatchers(
                    "/dashboard",
                    "/dashboard/",
                    "/dashboard/servers",
                    "/dashboard/pods",
                    "/dashboard/api/pods/**",
                    "/actuator/**"
                ).permitAll()
                
                // GET запросы к серверам доступны всем
                .requestMatchers(HttpMethod.GET, "/dashboard/api/servers/**").permitAll()
                
                // Обновление статуса серверов доступно всем (только чтение данных)
                .requestMatchers(HttpMethod.POST, "/dashboard/api/servers/refresh").permitAll()
                
                // Защищенные операции - требуют аутентификации (POST, PUT, DELETE)
                // ВАЖНО: Специфичные паттерны должны быть ДО общих паттернов с **
                .requestMatchers(HttpMethod.POST, "/dashboard/api/servers").authenticated()
                .requestMatchers(HttpMethod.POST, "/dashboard/api/servers/*/check").authenticated()
                .requestMatchers(HttpMethod.PUT, "/dashboard/api/servers/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/dashboard/api/servers/**").authenticated()
                
                // Страница логина доступна всем
                .requestMatchers("/dashboard/login", "/dashboard/login-error").permitAll()
                
                // Все остальное требует аутентификации
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/dashboard/login")
                .loginProcessingUrl("/dashboard/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/dashboard/login-error")
                .permitAll()
            )
            .exceptionHandling(exceptions -> exceptions
                // Для API запросов возвращаем 401 вместо редиректа на /login
                .authenticationEntryPoint((request, response, authException) -> {
                    String requestPath = request.getRequestURI();
                    if (requestPath.startsWith("/dashboard/api/")) {
                        response.setStatus(401);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"success\":false,\"error\":\"Unauthorized\"}");
                    } else {
                        response.sendRedirect("/dashboard/login");
                    }
                })
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/dashboard/logout", "POST"))
                .logoutSuccessUrl("/dashboard")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .csrf(csrf -> csrf
                // Отключаем CSRF для API endpoints (можно включить позже)
                .ignoringRequestMatchers("/dashboard/api/**")
            );
        
        return http.build();
    }
    
    /**
     * Создание пользователя в памяти
     */
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails user = User.builder()
            .username(username)
            .password(passwordEncoder.encode(password))
            .roles("ADMIN")
            .build();
        
        return new InMemoryUserDetailsManager(user);
    }
    
    /**
     * Кодировщик паролей
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

