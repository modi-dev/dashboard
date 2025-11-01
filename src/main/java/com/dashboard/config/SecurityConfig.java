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
                    "/css/**",
                    "/js/**",
                    "/webfonts/**",
                    "/favicon.ico",
                    "/favicon-*.png",
                    "/apple-touch-icon.png"
                ).permitAll()
                
                // Публичные страницы - доступны всем
                .requestMatchers(
                    "/",
                    "/dashboard",
                    "/servers",
                    "/pods",
                    "/api/pods/**",
                    "/actuator/**"
                ).permitAll()
                
                // GET запросы к серверам доступны всем
                .requestMatchers(HttpMethod.GET, "/api/servers/**").permitAll()
                
                // Защищенные операции - требуют аутентификации (POST, PUT, DELETE)
                .requestMatchers(HttpMethod.POST, "/api/servers").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/servers/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/servers/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/servers/**/check").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/servers/refresh").authenticated()
                
                // Страница логина доступна всем
                .requestMatchers("/login", "/login-error").permitAll()
                
                // Все остальное требует аутентификации
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login-error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST"))
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .csrf(csrf -> csrf
                // Отключаем CSRF для API endpoints (можно включить позже)
                .ignoringRequestMatchers("/api/**")
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

