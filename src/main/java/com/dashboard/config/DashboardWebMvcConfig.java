package com.dashboard.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Статика (css, js, favicon) доступна по префиксу {@link DashboardPaths#BASE}.
 */
@Configuration
public class DashboardWebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(DashboardPaths.BASE + "/**")
            .addResourceLocations("classpath:/static/");
    }
}
