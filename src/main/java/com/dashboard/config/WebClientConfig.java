package com.dashboard.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

/**
 * Конфигурация HTTP клиента для выполнения запросов к внешним серверам
 * 
 * WebClient - это современный HTTP клиент от Spring для отправки запросов.
 * Он нужен для проверки доступности серверов (делает запросы к их URL).
 * 
 * @Configuration - говорит Spring, что это класс с настройками
 */
@Configuration
public class WebClientConfig {
    
    /**
     * Создаем и настраиваем WebClient для использования в приложении
     * 
     * @Bean - говорит Spring создать этот объект и сделать его доступным
     *         для других компонентов приложения (через @Autowired)
     * 
     * @return WebClient.Builder - строитель (builder) для создания HTTP клиента
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
            // Настраиваем максимальный размер ответа в памяти - 1 МБ
            // Это защита от слишком больших ответов, которые могут привести к переполнению памяти
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
            .build()
            .mutate(); // Возвращаем изменяемую копию для дальнейшей настройки
    }
}

