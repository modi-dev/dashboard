package com.dashboard.controller;

import com.dashboard.model.PodInfo;
import com.dashboard.service.CsvExportService;
import com.dashboard.service.KubernetesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Контроллер для работы с версиями и информацией о подах
 * Интегрирует функциональность из version.sh скрипта
 */
@Controller
@RequestMapping("/api/pods")
@CrossOrigin(origins = "*")
public class VersionController {
    
    private static final Logger logger = LoggerFactory.getLogger(VersionController.class);
    
    @Autowired
    private KubernetesService kubernetesService;
    
    @Autowired
    private CsvExportService csvExportService;
    
    /**
     * Получает информацию о всех запущенных подах в JSON формате
     * GET /api/pods/pods
     */
    @GetMapping(value = "/api/pods/pods", produces = "application/json")
    @ResponseBody
    public ResponseEntity<List<PodInfo>> getRunningPods() {
        try {
            logger.info("Запрос информации о запущенных подах");
            List<PodInfo> pods = kubernetesService.getRunningPods();
            return ResponseEntity.ok(pods);
        } catch (Exception e) {
            logger.error("Ошибка при получении информации о подах: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Получает HTML страницу с информацией о подах (как в оригинальном скрипте)
     * GET /api/pods/html
     */
    @GetMapping(value = "/html", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public ResponseEntity<String> getHtmlPage() {
        try {
            logger.info("Запрос HTML страницы с информацией о подах");
            String html = kubernetesService.generateHtmlPage();
            return ResponseEntity.ok(html);
        } catch (Exception e) {
            logger.error("Ошибка при генерации HTML страницы: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Получает информацию о текущем namespace
     * GET /api/pods/namespace
     */
    @GetMapping("/namespace")
    @ResponseBody
    public ResponseEntity<String> getCurrentNamespace() {
        try {
            logger.info("Запрос текущего namespace");
            String namespace = kubernetesService.getCurrentNamespace();
            return ResponseEntity.ok(namespace);
        } catch (Exception e) {
            logger.error("Ошибка при получении namespace: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Получает краткую информацию о подах (только имена и версии)
     * GET /api/pods/summary
     */
    @GetMapping("/summary")
    @ResponseBody
    public ResponseEntity<List<PodInfo>> getPodsSummary() {
        try {
            logger.info("Запрос краткой информации о подах");
            List<PodInfo> pods = kubernetesService.getRunningPods();
            
            // Возвращаем только основную информацию
            pods.forEach(pod -> {
                pod.setGcOptions(null);
                pod.setCpuRequest(null);
                pod.setMemoryRequest(null);
                pod.setCreationDate(null);
            });
            
            return ResponseEntity.ok(pods);
        } catch (Exception e) {
            logger.error("Ошибка при получении краткой информации о подах: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Получает информацию о конкретном поде по имени
     * GET /api/pods/pods/{name}
     */
    @GetMapping("/pods/{name}")
    @ResponseBody
    public ResponseEntity<PodInfo> getPodByName(@PathVariable String name) {
        try {
            logger.info("Запрос информации о поде: {}", name);
            List<PodInfo> pods = kubernetesService.getRunningPods();
            
            PodInfo pod = pods.stream()
                    .filter(p -> name.equals(p.getName()))
                    .findFirst()
                    .orElse(null);
            
            if (pod != null) {
                return ResponseEntity.ok(pod);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при получении информации о поде {}: {}", name, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Проверяет доступность Kubernetes API
     * GET /api/pods/health
     */
    @GetMapping("/health")
    @ResponseBody
    public ResponseEntity<String> checkKubernetesHealth() {
        try {
            logger.info("Проверка доступности Kubernetes API");
            String namespace = kubernetesService.getCurrentNamespace();
            return ResponseEntity.ok("Kubernetes API доступен. Namespace: " + namespace);
        } catch (Exception e) {
            logger.error("Ошибка при проверке Kubernetes API: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Kubernetes API недоступен: " + e.getMessage());
        }
    }
    
    /**
     * Тестовый endpoint для проверки работы сервиса
     * GET /api/pods/test
     */
    @GetMapping("/test")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> testKubernetesService() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            logger.info("Тестирование Kubernetes сервиса");
            
            // Проверяем конфигурацию
            result.put("kubernetesEnabled", kubernetesService.getKubernetesConfig().isEnabled());
            result.put("namespace", kubernetesService.getKubernetesConfig().getNamespace());
            result.put("kubectlPath", kubernetesService.getKubernetesConfig().getKubectlPath());
            
            // Пытаемся получить поды
            List<PodInfo> pods = kubernetesService.getRunningPods();
            result.put("podsCount", pods.size());
            result.put("pods", pods);
            
            result.put("status", "success");
            result.put("message", "Kubernetes сервис работает");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Ошибка при тестировании Kubernetes сервиса: {}", e.getMessage(), e);
            result.put("status", "error");
            result.put("message", e.getMessage());
            result.put("error", e.getClass().getSimpleName());
            
            return ResponseEntity.internalServerError().body(result);
        }
    }
    
    /**
     * Получает конфигурацию Kubernetes
     * GET /api/pods/config
     */
    @GetMapping("/config")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getKubernetesConfig() {
        Map<String, Object> config = new HashMap<>();
        
        try {
            config.put("enabled", kubernetesService.getKubernetesConfig().isEnabled());
            config.put("namespace", kubernetesService.getKubernetesConfig().getNamespace());
            config.put("kubectlPath", kubernetesService.getKubernetesConfig().getKubectlPath());
            
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            logger.error("Ошибка при получении конфигурации: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(config);
        }
    }
    
    /**
     * Получает общую информацию о Kubernetes и подах
     * GET /api/pods/info
     */
    @GetMapping("/info")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getInfo() {
        Map<String, Object> info = new HashMap<>();
        
        try {
            logger.info("Запрос общей информации о Kubernetes");
            
            // Информация о конфигурации
            info.put("enabled", kubernetesService.getKubernetesConfig().isEnabled());
            info.put("namespace", kubernetesService.getKubernetesConfig().getNamespace());
            info.put("kubectlPath", kubernetesService.getKubernetesConfig().getKubectlPath());
            
            // Информация о подах
            List<PodInfo> pods = kubernetesService.getRunningPods();
            info.put("totalPods", pods.size());
            info.put("pods", pods);
            
            // Статус
            info.put("status", "ok");
            info.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            logger.error("Ошибка при получении информации: {}", e.getMessage(), e);
            info.put("status", "error");
            info.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(info);
        }
    }
    
    /**
     * Экспортирует список подов в CSV формат
     * GET /api/pods/export/csv
     */
    @GetMapping("/export/csv")
    @ResponseBody
    public ResponseEntity<String> exportPodsToCsv() {
        try {
            logger.info("Экспорт подов в CSV формат");
            List<PodInfo> pods = kubernetesService.getRunningPods();
            String csv = csvExportService.exportPodsToCsv(pods);
            
            // Генерируем имя файла с текущей датой
            String filename = "pods_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("no-cache");
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(csv);
                
        } catch (Exception e) {
            logger.error("Ошибка при экспорте подов: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body("Error exporting pods: " + e.getMessage());
        }
    }
}
