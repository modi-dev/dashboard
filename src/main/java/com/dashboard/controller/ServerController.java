package com.dashboard.controller;

import com.dashboard.dto.ServerDto;
import com.dashboard.model.Server;
import com.dashboard.model.ServerType;
import com.dashboard.repository.ServerRepository;
import com.dashboard.service.CsvExportService;
import com.dashboard.service.ServerMonitorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/servers")
@CrossOrigin(origins = "*")
public class ServerController {
    
    @Autowired
    private ServerRepository serverRepository;
    
    @Autowired
    private ServerMonitorService serverMonitorService;
    
    @Autowired
    private CsvExportService csvExportService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<ServerDto>>> getAllServers() {
        try {
            List<Server> servers = serverRepository.findAllOrderByCreatedAtDesc();
            List<ServerDto> serverDtos = servers.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(new ApiResponse<>(true, serverDtos, null, "Servers retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, null, "Internal server error", null));
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServerDto>> getServerById(@PathVariable Long id) {
        try {
            Optional<Server> server = serverRepository.findById(id);
            if (server.isPresent()) {
                return ResponseEntity.ok(new ApiResponse<>(true, convertToDto(server.get()), null, "Server retrieved successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, null, "Server not found", null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, null, "Internal server error", null));
        }
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<ServerDto>> createServer(@Valid @RequestBody ServerDto serverDto) {
        try {
            // Check if server with this URL already exists
            if (serverRepository.findByUrl(serverDto.getUrl()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(false, null, "Server with this URL already exists", null));
            }
            
            // Validate healthcheck requirement for "OTHER" type
            if (serverDto.getType() == ServerType.OTHER && 
                (serverDto.getHealthcheck() == null || serverDto.getHealthcheck().trim().isEmpty())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, null, "Healthcheck is required for 'Другое' type", null));
            }
            
            Server server = new Server(serverDto.getName(), serverDto.getUrl(), serverDto.getType());
            server.setHealthcheck(serverDto.getHealthcheck());
            
            Server savedServer = serverRepository.save(server);
            
            // Trigger immediate check
            serverMonitorService.checkServer(savedServer);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, convertToDto(savedServer), null, "Server created successfully"));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, null, "Failed to create server: " + e.getMessage(), null));
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ServerDto>> updateServer(@PathVariable Long id, @Valid @RequestBody ServerDto serverDto) {
        try {
            Optional<Server> serverOpt = serverRepository.findById(id);
            if (!serverOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, null, "Server not found", null));
            }
            
            Server server = serverOpt.get();
            server.setName(serverDto.getName());
            server.setUrl(serverDto.getUrl());
            server.setType(serverDto.getType());
            server.setHealthcheck(serverDto.getHealthcheck());
            
            Server savedServer = serverRepository.save(server);
            
            return ResponseEntity.ok(new ApiResponse<>(true, convertToDto(savedServer), null, "Server updated successfully"));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, null, "Failed to update server: " + e.getMessage(), null));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteServer(@PathVariable Long id) {
        try {
            if (!serverRepository.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, null, "Server not found", null));
            }
            
            serverRepository.deleteById(id);
            return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(true, null, null, "Server deleted successfully"));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, null, "Internal server error", null));
        }
    }
    
    @PostMapping("/{id}/check")
    public ResponseEntity<ApiResponse<Void>> checkServer(@PathVariable Long id) {
        try {
            Optional<Server> serverOpt = serverRepository.findById(id);
            if (!serverOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, null, "Server not found", null));
            }
            
            serverMonitorService.checkServer(serverOpt.get());
            return ResponseEntity.ok(new ApiResponse<>(true, null, null, "Server check triggered"));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, null, "Internal server error", null));
        }
    }
    
    /**
     * Экспортирует список серверов в CSV формат
     * GET /api/servers/export/csv
     */
    @GetMapping("/export/csv")
    public ResponseEntity<String> exportServersToCsv() {
        try {
            List<Server> servers = serverRepository.findAllOrderByCreatedAtDesc();
            String csv = csvExportService.exportServersToCsv(servers);
            
            // Генерируем имя файла с текущей датой
            String filename = "servers_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("no-cache");
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(csv);
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error exporting servers: " + e.getMessage());
        }
    }
    
    private ServerDto convertToDto(Server server) {
        ServerDto dto = new ServerDto();
        dto.setId(server.getId());
        dto.setName(server.getName());
        dto.setUrl(server.getUrl());
        dto.setType(server.getType());
        dto.setHealthcheck(server.getHealthcheck());
        dto.setStatus(server.getStatus());
        dto.setLastChecked(server.getLastChecked());
        dto.setCreatedAt(server.getCreatedAt());
        dto.setUpdatedAt(server.getUpdatedAt());
        return dto;
    }
    
    // Inner class for API response
    public static class ApiResponse<T> {
        private boolean success;
        private T data;
        private String error;
        private String message;
        
        public ApiResponse(boolean success, T data, String error, String message) {
            this.success = success;
            this.data = data;
            this.error = error;
            this.message = message;
        }
        
        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public T getData() { return data; }
        public void setData(T data) { this.data = data; }
        
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}

