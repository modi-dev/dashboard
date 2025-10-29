package com.dashboard.service;

import com.dashboard.model.Server;
import com.dashboard.model.ServerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.HttpClientErrorException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Сервис для получения версий серверов через метрики
 * 
 * Поддерживаемые типы серверов:
 * - PostgreSQL: получает версию из метрик postgres_exporter (порт 9134)
 * - Redis: планируется добавить
 * - Kafka: планируется добавить
 * - Astra Linux: планируется добавить
 */
@Service
public class ServerVersionService {
    
    private static final Logger logger = LoggerFactory.getLogger(ServerVersionService.class);
    
    @Autowired
    private RestTemplate restTemplate;
    
    // Регулярное выражение для извлечения версии PostgreSQL из метрик
    // Ищем строку вида: pg_static{server="hostname:6432",short_version="15.8.0",version="PostgreSQL 15.8 (Debian 15.8-1.pgdg100+1) on x86_64-pc-linux-gnu, compiled by gcc (Debian 8.3.0-6) 8.3.0, 64-bit"}
    private static final Pattern POSTGRES_VERSION_PATTERN = Pattern.compile(
        "pg_static\\{[^}]*version=\"([^\"]+)\"", 
        Pattern.CASE_INSENSITIVE
    );
    
    // Регулярное выражение для извлечения версии Redis из метрик
    // Ищем строку вида: redis_instance_info{maxmemory_policy="allkeys-lru",os="Linux 5.15.0-127-lowlatency x86_64",process_id="1141",redis_build_id="44edf16bebe6fed2",redis_mode="standalone",redis_version="7.2.4",role="master",run_id="5c6c49410d31de1f658ac548ed30e76797a6ff03",tcp_port="6379"}
    private static final Pattern REDIS_VERSION_PATTERN = Pattern.compile(
        "redis_instance_info\\{[^}]*redis_version=\"([^\"]+)\"", 
        Pattern.CASE_INSENSITIVE
    );
    
    // Регулярное выражение для извлечения версии Kafka из метрик
    // Ищем строку вида: vtb_kafka_component{app_type="kafka",ci_host="hostname",cluster_name="1357-kafka-if-cluster-kafka-astra-7144",component_name="VTB-Kafka",component_version="3.362.4",exp_name="vtb_jmx_exporter",ris_name="1357",ris_sub="test"}
    private static final Pattern KAFKA_VERSION_PATTERN = Pattern.compile(
        "vtb_kafka_component\\{[^}]*component_version=\"([^\"]+)\"", 
        Pattern.CASE_INSENSITIVE
    );
    
    /**
     * Получает версию сервера в зависимости от его типа
     * 
     * @param server сервер для получения версии
     * @return версия сервера или null если не удалось получить
     */
    public String getServerVersion(Server server) {
        if (server == null || server.getType() == null) {
            return null;
        }
        
        try {
            switch (server.getType()) {
                case POSTGRES:
                    return getPostgresVersion(server);
                case REDIS:
                    return getRedisVersion(server);
                case KAFKA:
                    return getKafkaVersion(server);
                case ASTRA_LINUX:
                    return getAstraLinuxVersion(server);
                case OTHER:
                    return getOtherVersion(server);
                default:
                    logger.warn("Неподдерживаемый тип сервера для получения версии: {}", server.getType());
                    return null;
            }
        } catch (Exception e) {
            logger.error("Ошибка при получении версии сервера {} ({}): {}", 
                        server.getName(), server.getType(), e.getMessage());
            return null;
        }
    }
    
    /**
     * Получает версию PostgreSQL через метрики postgres_exporter
     * 
     * @param server сервер PostgreSQL
     * @return версия PostgreSQL или null
     */
    private String getPostgresVersion(Server server) {
        try {
            // Формируем URL для метрик PostgreSQL
            String metricsUrl = buildMetricsUrl(server, 9134, "/metrics");
            logger.debug("Запрашиваем метрики PostgreSQL: {}", metricsUrl);
            
            // Получаем метрики
            String metrics = restTemplate.getForObject(metricsUrl, String.class);
            if (metrics == null || metrics.trim().isEmpty()) {
                logger.warn("Пустой ответ от метрик PostgreSQL: {}", server.getName());
                return null;
            }
            
            // Ищем версию в метриках
            Matcher matcher = POSTGRES_VERSION_PATTERN.matcher(metrics);
            if (matcher.find()) {
                String fullVersion = matcher.group(1);
                // Извлекаем краткую версию (например, "PostgreSQL 15.8" из "PostgreSQL 15.8 (Debian 15.8-1.pgdg100+1) on x86_64-pc-linux-gnu, compiled by gcc (Debian 8.3.0-6) 8.3.0, 64-bit")
                String shortVersion = extractPostgresShortVersion(fullVersion);
                logger.info("Получена версия PostgreSQL для {}: {}", server.getName(), shortVersion);
                return shortVersion;
            } else {
                logger.warn("Не найдена версия PostgreSQL в метриках: {}", server.getName());
                return null;
            }
            
        } catch (ResourceAccessException e) {
            logger.warn("Не удалось подключиться к метрикам PostgreSQL {}: {}", server.getName(), e.getMessage());
            return null;
        } catch (HttpClientErrorException e) {
            logger.warn("HTTP ошибка при получении метрик PostgreSQL {}: {}", server.getName(), e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("Неожиданная ошибка при получении версии PostgreSQL {}: {}", server.getName(), e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Извлекает краткую версию PostgreSQL из полной строки версии
     * 
     * @param fullVersion полная строка версии
     * @return краткая версия (например, "PostgreSQL 15.8")
     */
    private String extractPostgresShortVersion(String fullVersion) {
        if (fullVersion == null) {
            return null;
        }
        
        // Ищем паттерн "PostgreSQL X.Y" в начале строки
        Pattern shortVersionPattern = Pattern.compile("^(PostgreSQL \\d+\\.\\d+)");
        Matcher matcher = shortVersionPattern.matcher(fullVersion);
        
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        // Если не нашли, возвращаем как есть
        return fullVersion;
    }
    
    /**
     * Формирует URL для получения метрик
     * 
     * @param server сервер
     * @param port порт для метрик
     * @param path путь к метрикам
     * @return URL для метрик
     */
    private String buildMetricsUrl(Server server, int port, String path) {
        String baseUrl = server.getUrl();
        
        // Если URL уже содержит порт, заменяем его
        if (baseUrl.matches(".*:\\d+.*")) {
            baseUrl = baseUrl.replaceAll(":\\d+", ":" + port);
        } else {
            // Если порта нет, добавляем его
            if (baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
            }
            baseUrl += ":" + port;
        }
        
        // Добавляем путь к метрикам
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        baseUrl += path;
        
        return baseUrl;
    }
    
    /**
     * Получает версию Redis через метрики redis_exporter
     * 
     * @param server сервер Redis
     * @return версия Redis или null
     */
    private String getRedisVersion(Server server) {
        try {
            // Формируем URL для метрик Redis
            String metricsUrl = buildMetricsUrl(server, 9121, "/metrics");
            logger.debug("Запрашиваем метрики Redis: {}", metricsUrl);
            
            // Получаем метрики
            String metrics = restTemplate.getForObject(metricsUrl, String.class);
            if (metrics == null || metrics.trim().isEmpty()) {
                logger.warn("Пустой ответ от метрик Redis: {}", server.getName());
                return null;
            }
            
            // Ищем версию в метриках
            Matcher matcher = REDIS_VERSION_PATTERN.matcher(metrics);
            if (matcher.find()) {
                String version = matcher.group(1);
                // Форматируем версию как "Redis X.Y.Z"
                String formattedVersion = "Redis " + version;
                logger.info("Получена версия Redis для {}: {}", server.getName(), formattedVersion);
                return formattedVersion;
            } else {
                logger.warn("Не найдена версия Redis в метриках: {}", server.getName());
                return null;
            }
            
        } catch (ResourceAccessException e) {
            logger.warn("Не удалось подключиться к метрикам Redis {}: {}", server.getName(), e.getMessage());
            return null;
        } catch (HttpClientErrorException e) {
            logger.warn("HTTP ошибка при получении метрик Redis {}: {}", server.getName(), e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("Неожиданная ошибка при получении версии Redis {}: {}", server.getName(), e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Получает версию Kafka через метрики vtb_jmx_exporter
     * 
     * @param server сервер Kafka
     * @return версия Kafka или null
     */
    private String getKafkaVersion(Server server) {
        try {
            // Формируем URL для метрик Kafka
            String metricsUrl = buildMetricsUrl(server, 7171, "/metrics");
            logger.debug("Запрашиваем метрики Kafka: {}", metricsUrl);
            
            // Получаем метрики
            String metrics = restTemplate.getForObject(metricsUrl, String.class);
            if (metrics == null || metrics.trim().isEmpty()) {
                logger.warn("Пустой ответ от метрик Kafka: {}", server.getName());
                return null;
            }
            
            // Ищем версию в метриках
            Matcher matcher = KAFKA_VERSION_PATTERN.matcher(metrics);
            if (matcher.find()) {
                String version = matcher.group(1);
                // Форматируем версию как "Kafka X.Y.Z"
                String formattedVersion = "Kafka " + version;
                logger.info("Получена версия Kafka для {}: {}", server.getName(), formattedVersion);
                return formattedVersion;
            } else {
                logger.warn("Не найдена версия Kafka в метриках: {}", server.getName());
                return null;
            }
            
        } catch (ResourceAccessException e) {
            logger.warn("Не удалось подключиться к метрикам Kafka {}: {}", server.getName(), e.getMessage());
            return null;
        } catch (HttpClientErrorException e) {
            logger.warn("HTTP ошибка при получении метрик Kafka {}: {}", server.getName(), e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("Неожиданная ошибка при получении версии Kafka {}: {}", server.getName(), e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Получает версию Astra Linux (заглушка)
     */
    private String getAstraLinuxVersion(Server server) {
        logger.debug("Получение версии Astra Linux не реализовано для: {}", server.getName());
        return null;
    }
    
    /**
     * Получает версию для серверов типа OTHER через кастомные метрики
     * 
     * @param server сервер OTHER
     * @return версия сервера или null
     */
    private String getOtherVersion(Server server) {
        try {
            // Проверяем, что у сервера настроены кастомные параметры
            if (server.getMetricsEndpoint() == null || server.getMetricsEndpoint().trim().isEmpty()) {
                logger.debug("У сервера {} не настроен endpoint для метрик", server.getName());
                return null;
            }
            
            if (server.getVersionRegex() == null || server.getVersionRegex().trim().isEmpty()) {
                logger.debug("У сервера {} не настроено регулярное выражение для извлечения версии", server.getName());
                return null;
            }
            
            // Формируем URL для кастомных метрик
            String baseUrl = server.getUrl();
            String metricsEndpoint = server.getMetricsEndpoint();
            
            // Убеждаемся, что endpoint начинается с /
            if (!metricsEndpoint.startsWith("/")) {
                metricsEndpoint = "/" + metricsEndpoint;
            }
            
            // Убираем / в конце baseUrl если есть
            if (baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
            }
            
            String metricsUrl = baseUrl + metricsEndpoint;
            logger.debug("Запрашиваем кастомные метрики: {}", metricsUrl);
            
            // Получаем метрики
            String metrics = restTemplate.getForObject(metricsUrl, String.class);
            if (metrics == null || metrics.trim().isEmpty()) {
                logger.warn("Пустой ответ от кастомных метрик: {}", server.getName());
                return null;
            }
            
            // Компилируем кастомное регулярное выражение
            Pattern customPattern;
            try {
                customPattern = Pattern.compile(server.getVersionRegex(), Pattern.CASE_INSENSITIVE);
            } catch (Exception e) {
                logger.error("Ошибка в регулярном выражении для сервера {}: {}", server.getName(), e.getMessage());
                return null;
            }
            
            // Ищем версию в метриках
            Matcher matcher = customPattern.matcher(metrics);
            if (matcher.find()) {
                String version = matcher.group(1);
                // Форматируем версию как "Custom X.Y.Z" или просто версию
                String formattedVersion = version.startsWith("Custom") ? version : "Custom " + version;
                logger.info("Получена кастомная версия для {}: {}", server.getName(), formattedVersion);
                return formattedVersion;
            } else {
                logger.warn("Не найдена версия в кастомных метриках для {} с regex: {}", server.getName(), server.getVersionRegex());
                return null;
            }
            
        } catch (ResourceAccessException e) {
            logger.warn("Не удалось подключиться к кастомным метрикам {}: {}", server.getName(), e.getMessage());
            return null;
        } catch (HttpClientErrorException e) {
            logger.warn("HTTP ошибка при получении кастомных метрик {}: {}", server.getName(), e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("Неожиданная ошибка при получении кастомной версии {}: {}", server.getName(), e.getMessage(), e);
            return null;
        }
    }
}
