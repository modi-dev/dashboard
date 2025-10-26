package com.dashboard.service;

import com.dashboard.config.KubernetesConfig;
import com.dashboard.model.PodInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Сервис для работы с Kubernetes API
 * Интегрирует функциональность из version.sh скрипта
 */
@Service
public class KubernetesService {
    
    private static final Logger logger = LoggerFactory.getLogger(KubernetesService.class);
    
    @Autowired
    private KubernetesConfig kubernetesConfig;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Получает конфигурацию Kubernetes
     */
    public KubernetesConfig getKubernetesConfig() {
        return kubernetesConfig;
    }
    
    /**
     * Получает информацию о всех запущенных подах в namespace
     * @return список информации о подах
     */
    public List<PodInfo> getRunningPods() {
        List<PodInfo> pods = new ArrayList<>();
        
        try {
            if (!kubernetesConfig.isEnabled()) {
                logger.warn("Kubernetes интеграция отключена. Установите kubernetes.enabled=true для активации");
                return pods;
            }
            
            logger.info("Получение информации о подах в namespace: {}", kubernetesConfig.getNamespace());
            logger.info("Используется kubectl: {}", kubernetesConfig.getKubectlPath());
            
            // Команда kubectl для получения информации о подах
            String command = String.format("%s get pods --field-selector=status.phase==Running -n %s -o json", 
                                         kubernetesConfig.getKubectlPath(), kubernetesConfig.getNamespace());
            
            logger.debug("Выполняем команду: {}", command);
            
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            
            StringBuilder jsonOutput = new StringBuilder();
            StringBuilder errorOutput = new StringBuilder();
            
            String line;
            while ((line = reader.readLine()) != null) {
                jsonOutput.append(line);
            }
            
            while ((line = errorReader.readLine()) != null) {
                errorOutput.append(line).append("\n");
            }
            
            int exitCode = process.waitFor();
            logger.debug("kubectl exit code: {}", exitCode);
            logger.debug("kubectl output: {}", jsonOutput.toString());
            
            if (errorOutput.length() > 0) {
                logger.warn("kubectl stderr: {}", errorOutput.toString());
            }
            
            if (exitCode == 0) {
                pods = parseKubectlOutput(jsonOutput.toString());
                logger.info("Успешно получена информация о {} подах", pods.size());
            } else {
                logger.error("Ошибка выполнения kubectl команды. Код выхода: {}. Ошибка: {}", 
                           exitCode, errorOutput.toString());
            }
            
        } catch (Exception e) {
            logger.error("Ошибка при получении информации о подах: {}", e.getMessage(), e);
        }
        
        return pods;
    }
    
    /**
     * Парсит JSON вывод kubectl и извлекает информацию о подах
     */
    public List<PodInfo> parseKubectlOutput(String jsonOutput) {
        List<PodInfo> pods = new ArrayList<>();
        
        try {
            logger.debug("Парсинг JSON kubectl output, длина: {}", jsonOutput.length());
            
            // Парсим JSON с помощью Jackson
            JsonNode rootNode = objectMapper.readTree(jsonOutput);
            JsonNode itemsNode = rootNode.get("items");
            
            if (itemsNode != null && itemsNode.isArray()) {
                logger.debug("Найден массив items с {} элементами", itemsNode.size());
                
                for (JsonNode itemNode : itemsNode) {
                    if (itemNode.has("kind") && "Pod".equals(itemNode.get("kind").asText())) {
                        PodInfo podInfo = parsePodJson(itemNode);
                        if (podInfo != null) {
                            pods.add(podInfo);
                            logger.debug("Успешно распарсен под: {}", podInfo.getName());
                        }
                    }
                }
            } else {
                logger.warn("Не найден массив items в JSON");
            }
            
            logger.info("Найдено {} подов в JSON, успешно распарсено: {}", 
                       itemsNode != null ? itemsNode.size() : 0, pods.size());
            
        } catch (Exception e) {
            logger.error("Ошибка при парсинге JSON kubectl: {}", e.getMessage(), e);
        }
        
        return pods;
    }
    
    /**
     * Парсит JSON отдельного пода
     */
    private PodInfo parsePodJson(JsonNode podNode) {
        try {
            PodInfo podInfo = new PodInfo();
            
            // Извлекаем имя приложения из labels.app
            JsonNode metadataNode = podNode.get("metadata");
            if (metadataNode != null) {
                JsonNode labelsNode = metadataNode.get("labels");
                if (labelsNode != null && labelsNode.has("app")) {
                    podInfo.setName(labelsNode.get("app").asText());
                }
                
                // Извлекаем дату создания
                if (metadataNode.has("creationTimestamp")) {
                    String creationTimestamp = metadataNode.get("creationTimestamp").asText();
                    try {
                        // Парсим ISO 8601 формат
                        creationTimestamp = creationTimestamp.replace("Z", "");
                        LocalDateTime creationDate = LocalDateTime.parse(creationTimestamp, 
                            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
                        podInfo.setCreationDate(creationDate);
                    } catch (Exception e) {
                        logger.warn("Не удалось распарсить дату создания: {}", creationTimestamp);
                    }
                }
                
                // Извлекаем аннотации
                JsonNode annotationsNode = metadataNode.get("annotations");
                if (annotationsNode != null) {
                    if (annotationsNode.has("ms-branch")) {
                        podInfo.setMsBranch(annotationsNode.get("ms-branch").asText());
                    }
                    if (annotationsNode.has("config-branch")) {
                        podInfo.setConfigBranch(annotationsNode.get("config-branch").asText());
                    }
                }
            }
            
            // Извлекаем информацию из контейнеров
            JsonNode specNode = podNode.get("spec");
            if (specNode != null) {
                JsonNode containersNode = specNode.get("containers");
                if (containersNode != null && containersNode.isArray() && containersNode.size() > 0) {
                    JsonNode firstContainer = containersNode.get(0);
                    
                    // Извлекаем версию образа
                    if (firstContainer.has("image")) {
                        String image = firstContainer.get("image").asText();
                        // Удаляем registry из имени образа (как в оригинальном скрипте)
                        image = image.replaceAll("pcss-prod[^:]*:", "")
                                   .replaceAll("nexus[^:]*:", "")
                                   .replaceAll("docker[^:]*:", "");
                        podInfo.setVersion(image);
                    }
                    
                    // Извлекаем порт
                    if (firstContainer.has("ports")) {
                        JsonNode portsNode = firstContainer.get("ports");
                        if (portsNode.isArray() && portsNode.size() > 0) {
                            JsonNode firstPort = portsNode.get(0);
                            if (firstPort.has("containerPort")) {
                                podInfo.setPort(firstPort.get("containerPort").asInt());
                            }
                        }
                    }
                    
                    // Извлекаем переменные окружения
                    if (firstContainer.has("env")) {
                        JsonNode envNode = firstContainer.get("env");
                        if (envNode.isArray()) {
                            for (JsonNode envVar : envNode) {
                                if (envVar.has("name") && "JAVA_TOOL_OPTIONS".equals(envVar.get("name").asText())) {
                                    if (envVar.has("value")) {
                                        podInfo.setGcOptions(envVar.get("value").asText());
                                    }
                                    break;
                                }
                            }
                        }
                    }
                    
                    // Извлекаем ресурсы
                    if (firstContainer.has("resources")) {
                        JsonNode resourcesNode = firstContainer.get("resources");
                        if (resourcesNode.has("requests")) {
                            JsonNode requestsNode = resourcesNode.get("requests");
                            if (requestsNode.has("cpu")) {
                                podInfo.setCpuRequest(requestsNode.get("cpu").asText());
                            }
                            if (requestsNode.has("memory")) {
                                podInfo.setMemoryRequest(requestsNode.get("memory").asText());
                            }
                        }
                    }
                }
            }
            
            logger.debug("Распарсен под: name={}, version={}, port={}", 
                        podInfo.getName(), podInfo.getVersion(), podInfo.getPort());
            
            return podInfo;
            
        } catch (Exception e) {
            logger.error("Ошибка при парсинге пода: {}", e.getMessage(), e);
            return null;
        }
    }
    
    
    /**
     * Получает текущий namespace
     */
    public String getCurrentNamespace() {
        try {
            String command = String.format("%s config view --minify -o jsonpath='{.contexts[0].context.namespace}'", kubernetesConfig.getKubectlPath());
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            String namespace = reader.readLine();
            if (namespace == null || namespace.trim().isEmpty()) {
                return "default";
            }
            
            return namespace.trim();
            
        } catch (Exception e) {
            logger.warn("Не удалось получить текущий namespace: {}", e.getMessage());
            return kubernetesConfig.getNamespace();
        }
    }
    
    /**
     * Генерирует HTML страницу с информацией о подах (как в оригинальном скрипте)
     */
    public String generateHtmlPage() {
        List<PodInfo> pods = getRunningPods();
        String currentNamespace = getCurrentNamespace();
        String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " UTC";
        
        StringBuilder html = new StringBuilder();
        
        // CSS стили (из оригинального скрипта)
        html.append("<style>");
        html.append("/* Стили таблицы (IKSWEB) */");
        html.append("table.iksweb{text-decoration: none;border-collapse:collapse;width:100%;text-align:center;}");
        html.append("table.iksweb th{font-weight:normal;font-size:14px; color:#ffffff;background-color:#354251;}");
        html.append("table.iksweb td{font-size:14px;color:#354251;}");
        html.append("table.iksweb td,table.iksweb th{white-space:pre-wrap;padding:10px 5px;line-height:13px;vertical-align: middle;border: 1px solid #354251;}");
        html.append("table.iksweb tr:hover{background-color:#f9fafb}");
        html.append("table.iksweb tr:hover td{color:#354251;cursor:default;}");
        html.append("</style>");
        
        // HTML структура
        html.append("<html><body>");
        html.append("<p style=\"font-size:12px\">update time: ").append(currentDate).append("<br>namespace: ").append(currentNamespace).append("</p><br>");
        
        html.append("<table class='iksweb'>");
        html.append("<thead><tr>");
        html.append("<th>NAME</th>");
        html.append("<th>VERSION</th>");
        html.append("<th>MsBranch</th>");
        html.append("<th>ConfigBranch</th>");
        html.append("<th>GC</th>");
        html.append("<th>CREATION DATE</th>");
        html.append("<th>PORT</th>");
        html.append("<th>REQUEST</th>");
        html.append("</tr></thead>");
        
        // Добавляем строки с данными подов
        for (PodInfo pod : pods) {
            html.append("<tr>");
            html.append("<td align=\"left\">").append(pod.getName() != null ? pod.getName() : "").append("</td>");
            html.append("<td align=\"left\">").append(pod.getVersion() != null ? pod.getVersion() : "").append("</td>");
            html.append("<td>").append(pod.getMsBranch() != null ? pod.getMsBranch() : "").append("</td>");
            html.append("<td>").append(pod.getConfigBranch() != null ? pod.getConfigBranch() : "").append("</td>");
            html.append("<td>").append(pod.getGcOptions() != null ? pod.getGcOptions() : "").append("</td>");
            html.append("<td>").append(pod.getCreationDate() != null ? pod.getCreationDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "").append("</td>");
            html.append("<td align=\"left\">").append(pod.getPort() != null ? pod.getPort().toString() : "").append("</td>");
            html.append("<td align=\"left\">");
            html.append("CPU: ").append(pod.getCpuRequest() != null ? pod.getCpuRequest() : "").append("<br><br>");
            html.append("RAM: ").append(pod.getMemoryRequest() != null ? pod.getMemoryRequest() : "");
            html.append("</td>");
            html.append("</tr>");
        }
        
        html.append("</table>");
        html.append("</html></body>");
        
        return html.toString();
    }
}
