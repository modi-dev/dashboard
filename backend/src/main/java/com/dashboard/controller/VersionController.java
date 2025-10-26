package com.dashboard.controller;

import com.dashboard.model.PodInfo;
import com.dashboard.service.KubernetesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Контроллер для работы с версиями и информацией о подах
 * Интегрирует функциональность из version.sh скрипта
 */
@RestController
@RequestMapping("/api/version")
@CrossOrigin(origins = "*")
public class VersionController {
    
    private static final Logger logger = LoggerFactory.getLogger(VersionController.class);
    
    @Autowired
    private KubernetesService kubernetesService;
    
    /**
     * Получает информацию о всех запущенных подах в JSON формате
     * GET /api/version/pods
     */
    @GetMapping("/pods")
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
     * GET /api/version/html
     */
    @GetMapping(value = "/html", produces = MediaType.TEXT_HTML_VALUE)
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
     * GET /api/version/namespace
     */
    @GetMapping("/namespace")
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
     * GET /api/version/summary
     */
    @GetMapping("/summary")
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
     * GET /api/version/pods/{name}
     */
    @GetMapping("/pods/{name}")
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
     * GET /api/version/health
     */
    @GetMapping("/health")
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
     * GET /api/version/test
     */
    @GetMapping("/test")
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
     * GET /api/version/config
     */
    @GetMapping("/config")
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
}
