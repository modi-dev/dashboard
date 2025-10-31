package com.dashboard.service;

import com.dashboard.model.Server;
import com.dashboard.model.ServerStatus;
import com.dashboard.model.ServerType;
import com.dashboard.repository.ServerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ServerMonitorService {
    
    private static final Logger logger = LoggerFactory.getLogger(ServerMonitorService.class);
    
    @Autowired
    private ServerRepository serverRepository;
    
    @Autowired
    private WebClient.Builder webClientBuilder;
    
    private final long timeoutMs;
    
    public ServerMonitorService(@Value("${monitoring.timeout:0.17}") double timeoutMinutes) {
        // Convert minutes to milliseconds (default: 0.17 minutes = ~10 seconds)
        this.timeoutMs = (long) (timeoutMinutes * 60 * 1000);
        logger.debug("ServerMonitorService initialized with timeout: {} minutes ({} ms)", 
                    timeoutMinutes, timeoutMs);
    }
    
    // Scheduling moved to ServerMonitorScheduler to avoid running in tests
    public void checkAllServers() {
        List<Server> servers = serverRepository.findAll();

        if (servers.isEmpty()) {
            logger.info("No servers to monitor");
            return;
        }

        logger.info("Checking {} server(s)...", servers.size());

        for (Server server : servers) {
            checkServer(server);
        }
    }
    
    public void checkServer(Server server) {
        try {
            long startTime = System.currentTimeMillis();
            ServerStatus status = determineServerStatus(server);
            long responseTime = System.currentTimeMillis() - startTime;
            
            server.setStatus(status);
            server.setLastChecked(LocalDateTime.now());
            serverRepository.save(server);
            
            if (status == ServerStatus.ONLINE) {
                logger.info("✓ Server: {} | Type: {} | Status: {} | Time: {}ms", 
                    server.getName(), server.getType(), status, responseTime);
            } else {
                logger.warn("✗ Server: {} | Type: {} | Status: {} | Time: {}ms", 
                    server.getName(), server.getType(), status, responseTime);
            }
            
        } catch (Exception e) {
            logger.error("Error checking server {}: {}", server.getName(), e.getMessage());
            server.setStatus(ServerStatus.OFFLINE);
            server.setLastChecked(LocalDateTime.now());
            serverRepository.save(server);
        }
    }
    
    private ServerStatus determineServerStatus(Server server) {
        try {
            String host;
            int port;
            
            // Parse URL and extract host/port
            if (server.getUrl().contains("://")) {
                // URL already has protocol
                URI uri = URI.create(server.getUrl());
                host = uri.getHost();
                port = uri.getPort();
            } else {
                // URL without protocol - extract host and port
                String[] parts = server.getUrl().split(":");
                host = parts[0];
                port = parts.length > 1 ? Integer.parseInt(parts[1]) : -1;
            }
            
            // Set default ports based on server type
            if (port == -1) {
                port = getDefaultPort(server.getType());
            }
            
            switch (server.getType()) {
                case POSTGRES:
                case REDIS:
                case KAFKA:
                case ASTRA_LINUX:
                    return checkTcpConnection(host, port) ? ServerStatus.ONLINE : ServerStatus.OFFLINE;
                    
                case OTHER:
                    return checkHttpEndpoint(server) ? ServerStatus.ONLINE : ServerStatus.OFFLINE;
                    
                default:
                    return ServerStatus.UNKNOWN;
            }
            
        } catch (Exception e) {
            logger.error("Error determining status for server {}: {}", server.getName(), e.getMessage());
            return ServerStatus.OFFLINE;
        }
    }
    
    private boolean checkTcpConnection(String host, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), (int) timeoutMs);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    private boolean checkHttpEndpoint(Server server) {
        try {
            String checkUrl = server.getUrl();
            
            // Add protocol if not present
            if (!checkUrl.contains("://")) {
                checkUrl = "http://" + checkUrl;
            }
            
            // If healthcheck is specified, append it to the URL
            if (server.getHealthcheck() != null && !server.getHealthcheck().isEmpty()) {
                String baseUrl = checkUrl.endsWith("/") ? 
                    checkUrl.substring(0, checkUrl.length() - 1) : checkUrl;
                String healthcheckPath = server.getHealthcheck().startsWith("/") ? 
                    server.getHealthcheck() : "/" + server.getHealthcheck();
                checkUrl = baseUrl + healthcheckPath;
            }
            
            logger.debug("Checking HTTP endpoint: {} for server: {}", checkUrl, server.getName());
            
            WebClient.Builder clientBuilder = webClientBuilder
                .baseUrl(checkUrl)
                .defaultHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .defaultHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                .defaultHeader("Accept-Language", "en-US,en;q=0.5")
                .defaultHeader("Accept-Encoding", "gzip, deflate")
                .defaultHeader("Connection", "keep-alive")
                .defaultHeader("Upgrade-Insecure-Requests", "1");
            
            WebClient webClient = clientBuilder.build();
            
            var response = webClient.get()
                .retrieve()
                .toBodilessEntity()
                .timeout(Duration.ofMillis(timeoutMs))
                .block();
            
            boolean isSuccessful = response.getStatusCode().is2xxSuccessful() || 
                                 (response.getStatusCode().value() >= 300 && response.getStatusCode().value() < 400);
            logger.debug("HTTP response for {}: {} - {}", checkUrl, response.getStatusCode(), isSuccessful ? "SUCCESS" : "FAILED");
            
            return isSuccessful;
                
        } catch (Exception e) {
            logger.error("HTTP check failed for server {} (URL: {}): {} - {}", 
                server.getName(), server.getUrl(), e.getClass().getSimpleName(), e.getMessage());
            if (logger.isDebugEnabled()) {
                logger.debug("Full exception details:", e);
            }
            return false;
        }
    }
    
    private int getDefaultPort(ServerType type) {
        switch (type) {
            case POSTGRES: return 5432;
            case REDIS: return 6379;
            case KAFKA: return 9092;
            case ASTRA_LINUX: return 22;
            default: return 80;
        }
    }
}

